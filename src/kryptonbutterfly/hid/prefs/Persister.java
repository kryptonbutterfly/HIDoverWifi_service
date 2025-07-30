package kryptonbutterfly.hid.prefs;

import static java.nio.file.StandardOpenOption.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class Persister
{
	private Persister()
	{
		throw new IllegalAccessError();
	}
	
	private static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.serializeNulls()
		.create();
	
	private static final HashMap<Class<?>, File> map = new HashMap<>();
	
	public static <T> T load(File file, Class<T> type)
	{
		map.put(type, file);
		if (file.exists())
		{
			try
			{
				final var json = Files.readString(file.toPath());
				return GSON.fromJson(json, type);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		try
		{
			return type.getConstructor().newInstance();
		}
		catch (
			InstantiationException
			| IllegalAccessException
			| IllegalArgumentException
			| InvocationTargetException
			| NoSuchMethodException
			| SecurityException e)
		{
			e.printStackTrace();
			System.exit(-1);
			return null;
		}
	}
	
	private static <T> void persistElement(T data)
	{
		try
		{
			final var file = map.get(data.getClass());
			Files.writeString(file.toPath(), GSON.toJson(data), CREATE, WRITE, TRUNCATE_EXISTING);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void persist(Object... data)
	{
		for (final var e : data)
			persistElement(e);
	}
}
