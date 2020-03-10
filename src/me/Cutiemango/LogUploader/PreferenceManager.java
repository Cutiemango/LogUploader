package me.Cutiemango.LogUploader;

import java.io.File;
import java.util.prefs.Preferences;

public class PreferenceManager
{
	private static String LOG_DIRECTORY = null;
	private static Preferences pref = Preferences.userNodeForPackage(LogUploader.class);

	public static void loadPreference()
	{
		String directory = pref.get("directory", null);
		if (directory != null)
		{
			LOG_DIRECTORY = directory;
			System.out.println("Loaded directory from user preferences.");
		}
	}

	public static void setLogDirectory(String directory)
	{
		if (directory != null)
		{
			File f = new File(directory);
			if (f.exists() && f.isDirectory())
			{
				LOG_DIRECTORY = directory;
				System.out.println("Log directory set.");

				pref.put("directory", directory);
			}
		}
	}

	public static boolean hasDirectorySet()
	{
		return LOG_DIRECTORY != null;
	}

	public static String getLogDirectory()
	{
		return LOG_DIRECTORY;
	}
}
