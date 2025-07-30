package kryptonbutterfly.hid.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import kryptonbutterfly.hid.prefs.Prefs;

public abstract sealed class KeyToolArgs permits PrivateKeystoreArgs, PublicKeyStoreArgs
{
	private String	fileName;
	private String	password;
	
	public String fileName()
	{
		return fileName;
	}
	
	public final boolean fileName(BufferedReader reader) throws IOException
	{
		final var line = reader.readLine();
		if (line.isBlank())
			return false;
		
		final var file = new File(line);
		try
		{
			file.getCanonicalPath();
		}
		catch (IOException e)
		{
			return false;
		}
		this.fileName = line;
		return true;
	}
	
	public final String password()
	{
		return password;
	}
	
	public abstract boolean password(BufferedReader reader) throws IOException;
	
	final boolean password(String password) throws IOException
	{
		this.password = password;
		return true;
	}
	
	public abstract int execute(Prefs prefs) throws IOException, InterruptedException;
	
	protected static final AsyncReader printErrorOutput(Process process)
		throws IOException
	{
		return new AsyncReader(process.errorReader(), System.err);
	}
	
	protected static final AsyncReader printStdOutput(Process process)
		throws IOException
	{
		return new AsyncReader(process.inputReader(), System.out);
	}
}
