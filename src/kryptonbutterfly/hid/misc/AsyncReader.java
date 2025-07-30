package kryptonbutterfly.hid.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public final class AsyncReader implements AutoCloseable
{
	private Thread					thread;
	private final BufferedReader	br;
	private volatile boolean		keepRunning	= true;
	
	AsyncReader(BufferedReader br, PrintStream out)
	{
		this.br = br;
		
		this.thread = Thread.startVirtualThread(() -> {
			while (keepRunning)
			{
				try
				{
					final var line = br.readLine();
					if (line != null)
						out.println(line);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public void close() throws IOException
	{
		keepRunning = false;
		thread.interrupt();
		br.close();
	}
}
