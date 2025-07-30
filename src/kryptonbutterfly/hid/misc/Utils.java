package kryptonbutterfly.hid.misc;

import static kryptonbutterfly.math.utils.range.Range.*;

import java.security.SecureRandom;

public final class Utils
{
	private static final String validChars = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
	
	private Utils()
	{
		throw new IllegalAccessError();
	}
	
	public static String generate(int passwordLength)
	{
		final var	rand	= new SecureRandom();
		final var	sb		= new StringBuilder(passwordLength);
		
		range(passwordLength).forEach(_i -> {
			final int i = rand.nextInt(validChars.length());
			sb.append(validChars.charAt(i));
		});
		
		return sb.toString();
	}
}
