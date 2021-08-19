package com.alimaddi.control.downloader;

import com.alimaddi.datatypes.ConnectionContentType;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadTask
{
    public static int requestCount = 0;
    public static long lastRequestTime = System.currentTimeMillis();

    public static String downloadURL(String url, ConnectionContentType contentType)
            throws MalformedURLException, IOException, InterruptedException
    {
        if (requestCount >= 15)
        { // check every 15
            long currentTime = System.currentTimeMillis();
            long diff = currentTime - lastRequestTime;
            //if less than a second then sleep for the remainder of the second
            if (diff < 1000)
            {
                Thread.sleep(1000 - diff);
            }
            //reset
            lastRequestTime = System.currentTimeMillis();
            requestCount = 0;
        }

        URL mURL = new URL(url);
        URLConnection connection = mURL.openConnection();
        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        httpConnection.setRequestProperty("Content-Type", contentType.getContentType());
        httpConnection.setReadTimeout(60000);
        requestCount++;

        int responseCode = httpConnection.getResponseCode();
        if (responseCode != 200)
        {
            if (responseCode == 429 && httpConnection.getHeaderField("Retry-After") != null)
            {
                double sleepFloatingPoint = Double.valueOf(httpConnection.getHeaderField("Retry-After"));
                double sleepMillis = 1000 * sleepFloatingPoint;
                Thread.sleep((long) sleepMillis);
                return downloadURL(url, contentType);
            }
            else if (responseCode == 500)
            {
                System.err.println("Server not response for url of : ");
                System.err.println(url);
            }
            else if (responseCode == 302)
            {
                System.err.println("The base URL is not acceptable for url of : ");
                System.err.println(url);
                return "";
            }
            throw new RuntimeException("Response code was not 200. Detected response was " + responseCode);
        }
        InputStream response = httpConnection.getInputStream();

        String output;
        Reader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(response, "UTF-8"));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0)
            {
                builder.append(buffer, 0, read);
            }
            output = builder.toString();
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException logOrIgnore)
                {
                    logOrIgnore.printStackTrace();
                }
            }
        }

        return output;
    }
}
