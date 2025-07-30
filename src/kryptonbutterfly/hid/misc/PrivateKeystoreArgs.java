package kryptonbutterfly.hid.misc;

import static kryptonbutterfly.hid.Constants.*;

import java.io.BufferedReader;
import java.io.IOException;

import kryptonbutterfly.hid.prefs.Prefs;

public final class PrivateKeystoreArgs extends KeyToolArgs
{
	private String	subject;
	private String	validForDays;
	
	public boolean subject(BufferedReader reader) throws IOException
	{
		final var line = reader.readLine();
		// TODO validate line
		
		this.subject = line;
		return true;
	}
	
	public boolean validityForDays(BufferedReader reader) throws IOException
	{
		final var line = reader.readLine();
		if (line.isBlank())
		{
			validForDays = Integer.toString(90);
			return true;
		}
		
		final int days;
		try
		{
			days = Integer.parseInt(line);
		}
		catch (NumberFormatException e)
		{
			System.out.printf("%s is not a valid number.\n", line);
			return false;
		}
		if (days < 1)
		{
			System.out.printf("Certificate must be valid for at least 1 day!");
			return false;
		}
		validForDays = Integer.toString(days);
		
		return true;
	}
	
	public String validForDays()
	{
		return validForDays;
	}
	
	@Override
	public boolean password(BufferedReader reader) throws IOException
	{
		final var line = reader.readLine();
		if (line.isBlank())
		{
			final var generated = Utils.generate(32);
			System.out.println(generated);
			return password(generated);
		}
		return password(line);
	}
	
	@Override
	public int execute(Prefs prefs) throws IOException, InterruptedException
	{
		final var process = Runtime.getRuntime()
			.exec(
				new String[] {
					"keytool",
					"-genkeypair",
					"-alias",
					CERTIFICATE_ALIAS,
					"-keyalg",
					"RSA",
					"-keysize",
					"8192",
					"-keystore",
					fileName(),
					"-dname",
					subject,
					"-storepass",
					password(),
					"-validity",
					validForDays
				});
		try (
			final var err = printErrorOutput(process);
			final var out = printStdOutput(process))
		{
			return process.waitFor();
		}
	}
}
