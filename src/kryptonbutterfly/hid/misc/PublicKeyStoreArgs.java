package kryptonbutterfly.hid.misc;

import static kryptonbutterfly.hid.Constants.*;

import java.io.BufferedReader;
import java.io.IOException;

import kryptonbutterfly.hid.prefs.Prefs;

public final class PublicKeyStoreArgs extends KeyToolArgs
{
	@Override
	public boolean password(BufferedReader reader) throws IOException
	{
		final var password = reader.readLine();
		if (password.isBlank())
			return password("public");
		return password(password);
	}
	
	@Override
	public int execute(Prefs prefs) throws IOException, InterruptedException
	{
		final var	p1			= Runtime.getRuntime()
			.exec(
				new String[] {
					"keytool",
					"-export",
					"-alias",
					CERTIFICATE_ALIAS,
					"-keystore",
					prefs.cryptoData.keystoreFile,
					"-storepass",
					prefs.cryptoData.keystorePassword,
				});
		final var	exported	= p1.getInputStream().readAllBytes();
		int			exit;
		try (final var err = printErrorOutput(p1))
		{
			exit = p1.waitFor();
		}
		if (exit != 0)
			return exit;
		
		final var p2 = Runtime.getRuntime()
			.exec(
				new String[] {
					"keytool",
					"-import",
					"-noprompt",
					"-alias",
					CERTIFICATE_ALIAS,
					"-keystore",
					fileName(),
					"-storepass",
					password(),
					"-storetype",
					"PKCS12"
				});
		p2.getOutputStream().write(exported);
		p2.getOutputStream().close();
		try (
			final var out = printStdOutput(p2);
			final var err = printErrorOutput(p2))
		{
			return p2.waitFor();
		}
	}
}
