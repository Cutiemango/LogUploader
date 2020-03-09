package me.Cutiemango.LogUploader.uploader;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;

public class UploadHelper
{
	private static final String UPLOAD_URL = "https://dps.report/uploadContent";

	public static String upload(File f)
	{
		System.out.println("Uploading File: " + f.getName());
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost uploadFile = new HttpPost(UPLOAD_URL);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		try
		{
			builder.addBinaryBody("file", new FileInputStream(f), ContentType.APPLICATION_OCTET_STREAM, f.getName());
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		HttpEntity multipart = builder.build();
		uploadFile.setEntity(multipart);
		try
		{
			CloseableHttpResponse response = httpClient.execute(uploadFile);
			if (response.getStatusLine().getStatusCode() == 200)
			{
				String s = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
				Document doc = Jsoup.parseBodyFragment(s);
				for (Element element : doc.select("a[href]"))
					return element.attr("abs:href");
			}
			else
			{
				System.out.println("Error status code: " + response.getStatusLine().getStatusCode());
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return "Error while uploading log to the server";
	}
}
