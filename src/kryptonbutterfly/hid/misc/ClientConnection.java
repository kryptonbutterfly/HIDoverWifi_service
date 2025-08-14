package kryptonbutterfly.hid.misc;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ClientConnection implements Closeable
{
	private final Socket			socket;
	private final DataInputStream	iStream;
	public final DataOutputStream	oStream;
	private volatile long			lastMessage	= System.currentTimeMillis();
	
	public ClientConnection(Socket socket, DataInputStream iStream, DataOutputStream oStream)
	{
		this.socket		= socket;
		this.iStream	= iStream;
		this.oStream	= oStream;
	}
	
	public void update()
	{
		lastMessage = System.currentTimeMillis();
	}
	
	public boolean isTimedOut(long timeoutMs)
	{
		return lastMessage + timeoutMs < System.currentTimeMillis();
	}
	
	public Socket socket()
	{
		update();
		return socket;
	}
	
	public InetAddress getAddress()
	{
		return socket.getInetAddress();
	}
	
	public DataInputStream iStream()
	{
		update();
		return iStream;
	}
	
	@Override
	public void close() throws IOException
	{
		socket.close();
	}
	
	public boolean isClosed()
	{
		return socket.isClosed();
	}
}
