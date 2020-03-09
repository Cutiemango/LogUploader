package me.Cutiemango.LogUploader;

import me.Cutiemango.LogUploader.app.Application;

public class LogUploader
{
	private static Application app;

	public static void main(String[] args)
	{
		app = new Application("LogUploader", 960, 600);
	}

	public static Application getInstance()
	{
		return app;
	}
}
