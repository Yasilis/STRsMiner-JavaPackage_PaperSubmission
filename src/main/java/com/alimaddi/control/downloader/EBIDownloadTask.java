package com.alimaddi.control.downloader;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class EBIDownloadTask
{
    public static String submitJob(String baseURL, String data) throws IOException, InterruptedException
    {
        URL mURL = new URL(baseURL);
        HttpsURLConnection httpsConnection = (HttpsURLConnection)mURL.openConnection();
        httpsConnection.setRequestMethod("POST");
        httpsConnection.setDoOutput(true);
        httpsConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpsConnection.setRequestProperty("Accept", "text/plain");
        httpsConnection.setReadTimeout(60000);

        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = httpsConnection.getOutputStream();
        stream.write(out);

        int responseCode = httpsConnection.getResponseCode();

        if (responseCode != 200)
            throw new RuntimeException("Response code was not 200. Detected response was " + responseCode);

        InputStream response = httpsConnection.getInputStream();
        String jobID;
        Reader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(response, StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0)
            {
                builder.append(buffer, 0, read);
            }
            jobID = builder.toString();
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

        httpsConnection.disconnect();

        return jobID;
    }

    public static String GetJobStatus(String url) throws IOException, InterruptedException
    {
        String jobStatus = "";

        URL mURL = new URL(url);
        HttpsURLConnection httpsConnection = (HttpsURLConnection)mURL.openConnection();
        httpsConnection.setRequestMethod("GET");
        httpsConnection.setDoOutput(true);
        httpsConnection.setRequestProperty("Accept", "text/plain");
        httpsConnection.setReadTimeout(60000);

        int responseCode = httpsConnection.getResponseCode();

        if (responseCode != 200)
            throw new RuntimeException("Response code was not 200. Detected response was " + responseCode);

        InputStream response = httpsConnection.getInputStream();
        Reader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(response, StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0)
            {
                builder.append(buffer, 0, read);
            }
            jobStatus = builder.toString();
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
        httpsConnection.disconnect();

        return jobStatus;
    }

    public static StringBuilder GetResult(String url) throws IOException
    {
        URL mURL = new URL(url);
        HttpsURLConnection httpsConnection = (HttpsURLConnection)mURL.openConnection();
        httpsConnection.setRequestMethod("GET");
        httpsConnection.setDoOutput(true);
        httpsConnection.setRequestProperty("Accept", "text/plain");
        httpsConnection.setReadTimeout(60000);

        int responseCode = httpsConnection.getResponseCode();

        if (responseCode != 200)
            throw new RuntimeException("Response code was not 200. Detected response was " + responseCode);

        StringBuilder builder = new StringBuilder();
        InputStream response = httpsConnection.getInputStream();
        Reader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(response, StandardCharsets.UTF_8));
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0)
            {
                builder.append(buffer, 0, read);
            }
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
        httpsConnection.disconnect();

//        System.out.println(builder);


        return builder;
    }
}
