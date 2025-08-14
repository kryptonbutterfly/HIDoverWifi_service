package kryptonbutterfly.hid;

import static kryptonbutterfly.hid.Constants.*;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.Objects;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import kryptonbutterfly.hid.misc.ClientConnection;
import kryptonbutterfly.hid.misc.EventHandler;
import kryptonbutterfly.hid.prefs.Persister;
import kryptonbutterfly.hid.prefs.Prefs;

public final class HIDService
{
	public static Prefs								prefs;
	private static final HashSet<ClientConnection>	connections		= new HashSet<>();
	private static volatile boolean					keepListening	= true;
	private static ServerSocket						socket			= null;
	
	private static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	
	private static FlavorListener clipboardListener = new FlavorListener()
	{
		@Override
		public void flavorsChanged(FlavorEvent _e)
		{
			if (!prefs.sendClipboardEvents)
				return;
			
			try
			{
				if (!clipboard.getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor))
					return;
				
				final var s = (String) clipboard.getContents(null).getTransferData(DataFlavor.stringFlavor);
				synchronized (connections)
				{
					connections.parallelStream().forEach(c -> {
						try
						{
							c.oStream.writeUTF("CLIPBOARD");
							c.oStream.writeUTF(s);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					});
				}
			}
			catch (UnsupportedFlavorException | IOException e)
			{
				e.printStackTrace();
			}
		}
	};
	
	public static void startService()
	{
		prefs = Persister.load(CONFIG_FILE, Prefs.class);
		
		Thread.startVirtualThread(HIDService::startTimeoutConnections);
		
		clipboard.addFlavorListener(clipboardListener);
		
		startSslServer();
	}
	
	private static void startTimeoutConnections()
	{
		while (keepListening)
			try
			{
				synchronized (connections)
				{
					connections.stream().filter(c -> c.isTimedOut(prefs.connectionIdleTimeout)).forEach(c -> {
						System.out.printf("Connection to %s timed out!\n", c.getAddress());
						connections.remove(c);
						try
						{
							c.close();
						}
						catch (IOException e)
						{}
					});
				}
				Thread.sleep(prefs.connectionIdleTimeout / 6);
			}
			catch (InterruptedException | IndexOutOfBoundsException e)
			{}
		
	}
	
	private static void stopServer()
	{
		keepListening = false;
		try
		{
			if (socket != null)
				socket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			synchronized (connections)
			{
				while (!connections.isEmpty())
					try
					{
						for (final var c : connections)
						{
							if (!c.isClosed())
								c.close();
						}
						connections.clear();
					}
					catch (IOException e)
					{}
			}
			EventHandler.releaseAll();
		}
	}
	
	private static void startSslServer()
	{
		if (socket != null && !socket.isClosed())
		{
			System.err.println("already running");
			return;
		}
		
		if (prefs.cryptoData.keystoreFile == null)
		{
			System.err.println("No keystore specified.");
			System.err.printf("To generate a self signed certificate run `%s`\n", LINUX_COMMAND);
			return;
		}
		
		try
		{
			final var socketFactory = factory();
			if (socketFactory == null)
			{
				System.err.println("Keystore not found!");
				return;
			}
			if (prefs.binding != null)
			{
				socket = socketFactory.createServerSocket(prefs.port, 0, InetAddress.getByName(prefs.binding));
				System.out.printf("listening on %s:%d\n", prefs.binding, prefs.port);
			}
			else
			{
				socket = socketFactory.createServerSocket(prefs.port);
				System.out.printf("listening on: %d\n", prefs.port);
			}
			
			keepListening = true;
			
			Thread.ofPlatform().start(() -> {
				Runtime.getRuntime().addShutdownHook(Thread.ofPlatform().unstarted(HIDService::stopServer));
				while (keepListening)
					try
					{
						final var client = socket.accept();
						Thread.startVirtualThread(() -> handleInput(client));
					}
					catch (SocketException e)
					{}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				EventHandler.releaseAll();
			});
		}
		catch (
			IOException
			| KeyStoreException
			| NoSuchAlgorithmException
			| CertificateException
			| UnrecoverableKeyException
			| KeyManagementException e)
		{
			e.printStackTrace();
		}
	}
	
	private static SSLServerSocketFactory factory()
		throws FileNotFoundException,
		IOException,
		KeyStoreException,
		NoSuchAlgorithmException,
		CertificateException,
		UnrecoverableKeyException,
		KeyManagementException
	{
		final var file = new File(prefs.cryptoData.keystoreFile);
		if (!file.exists())
			return null;
		try (final var iStream = new FileInputStream(file))
		{
			final var keyStore = KeyStore.getInstance("JKS");
			keyStore.load(iStream, prefs.cryptoData.keystorePassword.toCharArray());
			final var kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, prefs.cryptoData.keystorePassword.toCharArray());
			final var tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(keyStore);
			final var context = SSLContext.getInstance("TLS");
			context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
			return context.getServerSocketFactory();
		}
	}
	
	private static final void handleInput(Socket socket)
	{
		final var address = socket.getInetAddress().getHostAddress();
		System.out.printf("connected: %s\n", address);
		ClientConnection connection = null;
		try (final var iS = socket.getInputStream(); final var oS = socket.getOutputStream())
		{
			socket.setSoTimeout(250);
			final long Id = ByteBuffer.wrap(iS.readNBytes(Long.BYTES)).getLong();
			if (PROTOCOL_ID != Id)
			{
				System.out.printf("""
						Client provided wrong protocol id. Expected:
						%s but got
						%s instead.
						Ensure compatibility between your server and client versions!
						""", PROTOCOL_ID, Id);
			}
			else
			{
				final int	pwLength	= iS.read();
				final var	password	= new String(iS.readNBytes(pwLength));
				if (!Objects.equals(prefs.serverPassword, password))
					System.out.println("Authentication failed … invalid password!");
				else
				{
					socket.setSoTimeout(prefs.connectionIdleTimeout);
					try (final var iStream = new DataInputStream(iS); final var oStream = new DataOutputStream(oS))
					{
						connection = new ClientConnection(socket, iStream, oStream);
						synchronized (connections)
						{
							connections.add(connection);
						}
						while (keepListening && socket.isConnected())
							EventHandler.event(connection);
					}
				}
			}
		}
		catch (SocketTimeoutException e)
		{
			System.out.println("connection lost!");
		}
		catch (EOFException e)
		{
			// Nothing to do since this occurs when the client closes the connection.
		}
		catch (SocketException e)
		{
			// Nothing to do since the server closed all connected sockets.
		}
		catch (SSLHandshakeException e)
		{
			e.printStackTrace();
			System.out.printf("[WARN]\t%s:\t %s\n", e.getClass().getSimpleName(), e.getMessage());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (connection != null)
				synchronized (connections)
				{
					connections.remove(connection);
				}
		}
		
		try
		{
			if (!socket.isClosed())
				socket.close();
		}
		catch (IOException e)
		{}
		System.out.printf("disconnected: %s\n", address);
		EventHandler.releaseAll();
	}
}
