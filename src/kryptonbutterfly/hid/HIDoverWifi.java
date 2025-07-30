package kryptonbutterfly.hid;

import static kryptonbutterfly.hid.Constants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import kryptonbutterfly.args.ArgsParser;
import kryptonbutterfly.cli_utils.CliHandler;
import kryptonbutterfly.cli_utils.commands.Command;
import kryptonbutterfly.cli_utils.commands.CommandClear;
import kryptonbutterfly.hid.misc.PrivateKeystoreArgs;
import kryptonbutterfly.hid.misc.PublicKeyStoreArgs;
import kryptonbutterfly.hid.prefs.Persister;
import kryptonbutterfly.hid.prefs.Prefs;

public final class HIDoverWifi
{
	static Prefs prefs;
	
	public static void main(String[] args)
	{
		final var	parser	= new ArgsParser();
		final var	parsed	= parser.parse(ProgramArgs::new, args);
		if (parsed.asService)
			HIDService.startService();
		else
			startConfigMode();
	}
	
	private static volatile boolean keepRunning = true;
	
	private static void startConfigMode()
	{
		prefs = Persister.load(CONFIG_FILE, Prefs.class);
		
		final var handler = new CliHandler();
		handler.registerTerminalCommand("clear", new CommandClear());
		exit(handler);
		createCertificate(handler);
		generatePublic(handler);
		ParamCommands.registerCommands(handler);
		
		handler.listen(() -> keepRunning);
		
		Persister.persist(prefs);
	}
	
	private static final void exit(CliHandler handler)
	{
		final var	name	= "exit";
		final var	usage	= """
				Stops this process.
				""";
		handler.registerTerminalCommand(name, new Command((_s, _r) -> {
			keepRunning = false;
			return true;
		}, usage));
	}
	
	private static final void createCertificate(CliHandler handler)
	{
		final var	name	= GENERATE_PRIVATE_CERT_COMMAND;
		final var	usage	= """
				Generate X509 SSL Certificate.
				""";
		handler.registerTerminalCommand(name, new Command(HIDoverWifi::generateX509, usage));
	}
	
	private static final void generatePublic(CliHandler handler)
	{
		final var	name	= "public";
		final var	usage	= """
				Generate a certificate required by clients in order to connect with ths machine.
				""";
		handler.registerTerminalCommand(name, new Command(HIDoverWifi::generatePublic, usage));
	}
	
	private static final boolean generateX509(String _s, BufferedReader terminalReader)
	{
		try
		{
			final var args = new PrivateKeystoreArgs();
			
			System.out.println("Keystore file name (.jks):");
			while (!args.fileName(terminalReader))
				System.out.println("Please enter a valid filename!");
			
			System.out.println("Certificate subject:");
			while (!args.subject(terminalReader))
				System.out.println("Please enter a valid subject!");
			
			System.out.println("Days the certificate should by valid. [90]:");
			while (!args.validityForDays(terminalReader))
				System.out.println("");
			
			System.out.println("Keystore password(ASCII). [generate]:");
			args.password(terminalReader);
			System.out.println("Generating. Please wait …");
			
			final int exitCode = args.execute(prefs);
			if (exitCode != 0)
			{
				System.out.println("Something seems to have gone wrong while generating the certificate and keystore.");
				System.out.println("Update preference anyways? (y/N):");
				final var action = terminalReader.readLine();
				if (!action.toUpperCase().startsWith("Y"))
					return true;
			}
			System.out.println("Keystore created.");
			prefs.cryptoData.keystoreFile		= args.fileName();
			prefs.cryptoData.keystorePassword	= args.password();
		}
		catch (InterruptedException | IOException e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	private static final boolean generatePublic(String _s, BufferedReader terminalReader)
	{
		try
		{
			final var args = new PublicKeyStoreArgs();
			
			System.out.println("keystore file name (.p12)");
			while (!args.fileName(terminalReader))
				System.out.println("Please enter a valid filename!");
			
			System.out.println("keystore password [public]:");
			args.password(terminalReader);
			
			final var	file		= new File(args.fileName());
			final int	exitCode	= args.execute(prefs);
			if (exitCode != 0)
				System.out.printf("failed to generate public keystore: %s\n", file);
			else
				System.out.printf("Public keystore generated: %s\n", file);
		}
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}
		return true;
	}
}
