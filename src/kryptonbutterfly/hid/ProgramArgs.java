package kryptonbutterfly.hid;

import kryptonbutterfly.args.ArgsProperties;
import kryptonbutterfly.args.Argument;
import kryptonbutterfly.args.IArgs;

@ArgsProperties
public final class ProgramArgs implements IArgs
{
	@Argument(name = "s", info = "Run as a service.")
	public boolean asService = false;
	
	@Override
	public String programInfo()
	{
		return "HIDoverWifi Server";
	}
}
