package kryptonbutterfly.hid;

import java.io.BufferedReader;
import java.util.function.BiConsumer;

import kryptonbutterfly.cli_utils.CliHandler;
import kryptonbutterfly.cli_utils.commands.Command;
import kryptonbutterfly.hid.misc.Utils;
import kryptonbutterfly.math.utils.limit.LimitFloat;
import kryptonbutterfly.math.utils.limit.LimitInt;

public class ParamCommands
{
	private static record Param(
		String aboutGet,
		String aboutSet,
		Runnable getter,
		BiConsumer<String, BufferedReader> setter)
	{}
	
	private static enum Params
	{
		port(port()),
		bind(bind()),
		action(action()),
		print(print()),
		scroll_speed(scrollSpeed()),
		accel(accel()),
		password(serverPassword()),
		timeout(timeout());
		
		private final Param param;
		
		Params(Param param)
		{
			this.param = param;
		}
		
		public String valueName()
		{
			return name().replace("_", "-");
		}
		
		static Params fromParam(String param)
		{
			try
			{
				return valueOf(param.replace("-", "_"));
			}
			catch (IllegalArgumentException e)
			{
				return null;
			}
		}
	}
	
	public static void registerCommands(CliHandler handler)
	{
		final var template = "%-14s%s\n";
		
		final var	nameGet	= "get";
		final var	nameSet	= "set";
		
		final var	usageGet	= new StringBuilder();
		final var	usageSet	= new StringBuilder();
		
		for (final var param : Params.values())
		{
			usageGet.append(template.formatted(param.valueName(), param.param.aboutGet));
			usageSet.append(template.formatted(param.valueName(), param.param.aboutSet));
		}
		
		handler.registerTerminalCommand(nameGet, new Command((s, _r) -> {
			s = s.trim();
			
			final var p = Params.fromParam(s);
			if (p == null)
				return false;
			
			p.param.getter.run();
			return true;
		}, usageGet.toString()));
		
		handler.registerTerminalCommand(nameSet, new Command((s, reader) -> {
			final int occ = CliHandler.indexOfWhiteSpace(s);
			if (occ == -1)
				return false;
			
			final var p = Params.fromParam(s.substring(0, occ));
			if (p == null)
				return false;
			
			final var remainder = s.substring(occ).trim();
			p.param.setter.accept(remainder, reader);
			return true;
		}, usageSet.toString()));
	}
	
	private static Param port()
	{
		final int	min	= 1;
		final int	max	= 0xFFFF;
		return new Param(
			" — The port to listen on.",
			" %-14s — The port to listen on. (min = %d, max = %d)".formatted("[PORT]", min, max),
			() -> System.out.println(HIDoverWifi.prefs.port),
			(s, _r) ->
			{
				try
				{
					final var value = Integer.parseInt(s);
					LimitInt.assertLimit(min, value, max, "port");
					HIDoverWifi.prefs.port = value;
				}
				catch (NumberFormatException e)
				{
					System.out.printf(
						"Expected a number between %d and %d, but got '%s' instead.\n",
						min,
						max,
						s);
				}
			});
	}
	
	private static Param bind()
	{
		return new Param(
			" — The network interface to bind the server to.",
			" %-14s — The network interface to bind the server to.".formatted("[ADDRESS]"),
			() -> System.out.println(HIDoverWifi.prefs.binding),
			(s, _r) -> HIDoverWifi.prefs.binding = "null".equals(s) ? null : s);
	}
	
	private static Param action()
	{
		return new Param(
			" — Whether incoming events will be acted upon.",
			" %-14s — Whether to act upon incoming events.".formatted("[ACT]"),
			() -> System.out.println(HIDoverWifi.prefs.performAction),
			(s, _r) -> HIDoverWifi.prefs.performAction = !"false".equals(s));
	}
	
	private static Param print()
	{
		return new Param(
			" — Whether to print info about incoming events.",
			" %-14s — Whether to act upon incoming events.".formatted("[PRINT]"),
			() -> System.out.println(HIDoverWifi.prefs.printAction),
			(s, _r) -> HIDoverWifi.prefs.printAction = "true".equals(s));
	}
	
	private static Param scrollSpeed()
	{
		final int	min	= 1;
		final int	max	= 64;
		return new Param(
			" — The factor to slow scrolling down by.",
			" %-14s — The factor to slow scolling down by. (min = %d, max = %d".formatted("[SPEED]", min, max),
			() -> System.out.println(HIDoverWifi.prefs.scrollSlowdown),
			(s, _r) ->
			{
				try
				{
					final var value = Integer.parseInt(s);
					LimitInt.assertLimit(min, value, max, "scroll-speed");
					HIDoverWifi.prefs.scrollSlowdown = value;
				}
				catch (NumberFormatException e)
				{
					System.out.printf(
						"Expected a number between %d and %d, but got '%s' instaed.\n",
						min,
						max,
						s);
				}
			});
	}
	
	private static Param accel()
	{
		final float	min	= 1F;
		final float	max	= 5F;
		
		return new Param(
			" — The exponent to apply to the raw mouse movement.",
			" %-14s — The exponent to apply to the raw mouse movement. (min = %f, max = %f)"
				.formatted("[ACCEL]", min, max),
			() -> System.out.println(HIDoverWifi.prefs.mouseAcceleration),
			(s, _r) ->
			{
				try
				{
					final var value = Float.parseFloat(s);
					LimitFloat.assertLimit(min, value, max, "accel");
					HIDoverWifi.prefs.mouseAcceleration = value;
				}
				catch (NumberFormatException e)
				{
					System.out.printf(
						"Expected a number between %f amd %f, but fot '%s' instead.\n",
						min,
						max,
						s);
				}
			});
	}
	
	private static Param serverPassword()
	{
		final int	min		= 8;
		final int	rMin	= 14;
		
		return new Param(
			" — The password required to authenticate with this server.",
			" %-14s — The new password to authenticate with this server.".formatted("[PASSWORD]"),
			() -> System.out.println(HIDoverWifi.prefs.serverPassword),
			(s, _r) ->
			{
				s = s.trim();
				if (s.isEmpty())
					HIDoverWifi.prefs.serverPassword = Utils.generate(32);
				else if (s.length() < min)
					System.out.printf("Server password must at least be %d characters long!\n", min);
				else if (s.length() < rMin)
					System.out.printf("The server password must at least be %d characters long!\n", rMin);
				else
				{
					HIDoverWifi.prefs.serverPassword = s;
					System.out.println("Server password changed!");
				}
			});
	}
	
	private static Param timeout()
	{
		return new Param(
			" — The idle timeout in milliseconds before client connections get closed.",
			" %-14s — The idle timeout in milliseconds before client connections get closed.".formatted("[TIMEOUT]"),
			() -> System.out.println(HIDoverWifi.prefs.connectionIdleTimeout),
			(s, _r) ->
			{
				s = s.trim();
				if (s.isEmpty())
					HIDoverWifi.prefs.connectionIdleTimeout = Constants.DEFAULT_IDLE_TIMEOUT;
				else
					try
					{
						final int value = Integer.parseInt(s);
						if (value <= 0)
							System.out.println("Timeout must be > 0");
						else
							HIDoverWifi.prefs.connectionIdleTimeout = value;
					}
					catch (NumberFormatException e)
					{
						System.out.printf("Execpted a number > 0, but got %s instead!\n", s);
					}
			});
	}
}
