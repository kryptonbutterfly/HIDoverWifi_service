package kryptonbutterfly.hid.misc;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.IOException;

import kryptonbutterfly.hid.HIDService;

public final class EventHandler
{
	private static final Robot robot = robot();
	
	private static int scrollYaccu = 0;
	
	private static final Robot robot()
	{
		try
		{
			return new Robot();
		}
		catch (AWTException e)
		{
			System.err.println(e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	public static final void event(ClientConnection connection) throws IOException
	{
		final var	iStream	= connection.iStream();
		final var	type	= iStream.readUTF();
		switch (type)
		{
			case "MOVE" -> move(iStream.readInt(), iStream.readInt());
			case "BUTTON" -> button(iStream.readUTF(), iStream.readBoolean());
			case "CLICK" -> click(iStream.readUTF());
			case "DOUBLE" -> doubleClick(iStream.readUTF());
			case "SCROLL" -> scroll(iStream.readInt(), iStream.readInt());
			case "KEY" -> keyboardKey(iStream.readUTF(), iStream.readBoolean());
			case "TYPE" -> keyboardType(iStream.readUTF());
			case "TEXT" -> keyboardText(iStream.readUTF());
			case "KEEP_ALIVE" -> {}
			default -> System.err.printf("unexpected action: '%s'\n", type);
		}
	}
	
	private static final void move(int dx, int dy) throws IOException
	{
		if (HIDService.prefs.printAction)
			System.out.printf("move(%d, %d)\n", dx, dy);
		if (HIDService.prefs.performAction)
		{
			final var loc = MouseInfo.getPointerInfo().getLocation();
			
			final double	absX	= Math.pow(Math.abs(dx), HIDService.prefs.mouseAcceleration);
			final double	absY	= Math.pow(Math.abs(dy), HIDService.prefs.mouseAcceleration);
			final double	accX	= Integer.signum(dx) * absX;
			final double	accY	= Integer.signum(dy) * absY;
			
			robot.mouseMove(loc.x - (int) accX, loc.y - (int) accY);
		}
	}
	
	private static final void button(String button, boolean down)
	{
		final var btn = Button.getButton(button);
		if (btn == null)
			return;
		if (HIDService.prefs.printAction)
		{
			if (down)
				System.out.printf("press(%s)\n", button);
			else
				System.out.printf("release(%s)\n", button);
		}
		if (HIDService.prefs.performAction)
		{
			if (down)
				robot.mousePress(btn.btn);
			else
				robot.mouseRelease(btn.btn);
			
		}
	}
	
	private static final void keyboardKey(String key, boolean down)
	{
		final var keyboardKey = KeyStroke.getKey(key);
		if (keyboardKey == null)
			return;
		
		if (HIDService.prefs.printAction)
		{
			if (down)
				System.out.printf("press key '%s'\n", key);
			else
				System.out.printf("release key '%s'\n", key);
		}
		
		if (HIDService.prefs.performAction)
		{
			final var action = down ? "keydown" : "keyup";
			try
			{
				switch (keyboardKey)
				{
					case KeyStroke.SUPER ->
							Runtime.getRuntime().exec(new String[] { "xdotool", action, "Super" }).waitFor();
					default -> {
						if (down)
							robot.keyPress(keyboardKey.key);
						else
							robot.keyRelease(keyboardKey.key);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private static final void keyboardText(String text)
	{
		if (HIDService.prefs.printAction)
			System.out.printf("type '%s'\n", text);
		
		try
		{
			if (HIDService.prefs.performAction)
				Runtime.getRuntime().exec(new String[] { "xdotool", "type", text }).waitFor();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static final void keyboardType(String key)
	{
		final var keyboardKey = KeyStroke.getKey(key);
		if (keyboardKey == null)
			return;
		
		if (HIDService.prefs.printAction)
			System.out.printf("type key '%s'\n", key);
		
		if (HIDService.prefs.performAction)
		{
			robot.keyPress(keyboardKey.key);
			robot.keyRelease(keyboardKey.key);
		}
	}
	
	private static final void click(String button)
	{
		final var btn = Button.getButton(button);
		if (btn == null)
			return;
		
		if (HIDService.prefs.printAction)
			System.out.printf("click(%s)\n", button);
		
		if (HIDService.prefs.performAction)
		{
			robot.mousePress(btn.btn);
			robot.mouseRelease(btn.btn);
		}
		
	}
	
	private static final void doubleClick(String button)
	{
		final var btn = Button.getButton(button);
		if (btn == null)
			return;
		
		if (HIDService.prefs.printAction)
			System.out.printf("doubleClick(%s)\n", button);
		
		if (HIDService.prefs.performAction)
		{
			robot.mousePress(btn.btn);
			robot.mouseRelease(btn.btn);
			robot.mousePress(btn.btn);
			robot.mouseRelease(btn.btn);
		}
	}
	
	private static final void scroll(int x, int y)
	{
		if (HIDService.prefs.printAction)
			System.out.printf("scroll(%d, %d)\n", x, y);
		
		if (HIDService.prefs.performAction)
			if (y != 0)
			{
				if (y > 0 && scrollYaccu > 0 || scrollYaccu < 0)
				{
					y += scrollYaccu;
				}
				int dY = y / HIDService.prefs.scrollSlowdown;
				scrollYaccu = y % HIDService.prefs.scrollSlowdown;
				robot.mouseWheel(-dY);
			}
	}
	
	private static enum Button
	{
		LEFT(InputEvent.BUTTON1_DOWN_MASK),
		MIDDLE(InputEvent.BUTTON2_DOWN_MASK),
		RIGHT(InputEvent.BUTTON3_DOWN_MASK);
		
		private final int btn;
		
		Button(int btn)
		{
			this.btn = btn;
		}
		
		static Button getButton(String btn)
		{
			final var res = Button.valueOf(btn);
			if (res == null)
				System.err.printf("Unexpected Button: '%s'\n", btn);
			return res;
		}
	}
	
	public static void releaseAll()
	{
		for (var btn : Button.values())
			robot.mouseRelease(btn.btn);
		
		for (var key : KeyStroke.values())
			robot.keyRelease(key.key);
	}
}
