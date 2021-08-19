package com.alimaddi.Utility;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Ali-Maddi
 *
 */
public class Reader
{
    //region Checked DB 3
    public static ArrayList<String> readLinesOfTXTFile(String fileName)
    {
        InputStream stream;
        ArrayList<String> results = new ArrayList<>();
        try
        {
            stream = new FileInputStream(fileName);
            BufferedReader bufferedReader;
            try
            {
                bufferedReader = new BufferedReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8));
                try
                {
                    String line;
                    while ((line = bufferedReader.readLine()) != null)
                    {
                        if (line.trim().equals(""))
                            continue;

                        results.add(line.trim());
                    }
                }
                catch (IOException ex)
                {
                    System.err.println("Failed to read result of : " + fileName);
                }
                finally
                {
                    bufferedReader.close();
                }
            }
            catch (UnsupportedEncodingException ex)
            {
                System.err.println("Unsupported Encoding : " + fileName);
            }
            catch (IOException ex)
            {
                System.err.println("IO error while closing buffer/reader : " + fileName);
            }
            finally
            {
                stream.close();
            }
        }
        catch (FileNotFoundException ex)
        {
            System.err.println("file not found : " + fileName);
        }
        catch (IOException ex)
        {
            System.err.println("IO error while closing stream : " + fileName);
        }

        return results;
    }

    public static ArrayList<String> readGeneDatasetNameList(String fileName)
    {
        ArrayList<String> results = new ArrayList<>();
        ArrayList<String> raw = readLinesOfTXTFile(fileName);

        // drop header of the file!
        raw.remove(0);

        for (String line : raw)
        {
//            if (line.charAt(0) == '#' || line.charAt(0) == '%')
//                continue;
//
//            int key = Integer.parseInt(line.trim());
//            line = line.replaceAll("\\t", " ");
//            parts = line.trim().split("\\s+");

            results.add(line.split("\\$")[0]);
        }

        return results;
    }

    public static ArrayList<Integer> readAppropriateSpeciesID(String fileName)
    {
        ArrayList<Integer> results = new ArrayList<>();
        ArrayList<String> raw = readLinesOfTXTFile(fileName);

        // drop header of the file!
        raw.remove(0);

        for (String line : raw)
            results.add(Integer.parseInt(line.split(",")[0]));

        return results;
    }
    //endregion

    public static ArrayList<ArrayList<String>> readFingerPrintFile(String filePath)
    {
        InputStream stream;
        ArrayList<ArrayList<String>> results = new ArrayList<>();
        try
        {
            stream = new FileInputStream(filePath);
            BufferedReader bufferedReader;
            try
            {
                bufferedReader = new BufferedReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8));
                try
                {
                    String line;
                    String[] parts;

                    // drop header of the file!
                    bufferedReader.readLine();

                    while ((line = bufferedReader.readLine()) != null)
                    {
                        if (line.equals(""))
                            continue;

                        parts = line.trim().split(",");

                        results.add(new ArrayList<>(Arrays.asList(parts)));
                    }
                }
                catch (IOException ex)
                {
                    System.err.println("Failed to read result of : "
                                               + filePath);
                }
                finally
                {
                    bufferedReader.close();
                }
            }
            catch (UnsupportedEncodingException ex)
            {
                System.err.println("Unsupported Encoding : "
                                           + filePath);
            }
            catch (IOException ex)
            {
                System.err.println("IO error while closing buffer"
                                           + "/reader : " + filePath);
            }
            finally
            {
                stream.close();
            }
        }
        catch (FileNotFoundException ex)
        {
            System.err.println("file not found : " + filePath);
        }
        catch (IOException ex)
        {
            System.err.println("IO error while closing stream : "
                                       + filePath);
        }

        return results;
    }

    public static HashMap<String, String> transcriptIDFile(String filePath)
    {
        InputStream stream;
        HashMap<String, String> results = new HashMap<>();
        try
        {
            stream = new FileInputStream(filePath);
            BufferedReader bufferedReader;
            try
            {
                bufferedReader = new BufferedReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8));
                try
                {
                    String line2;
                    String line3;
                    String[] parts2;
                    String[] parts3;

                    // drop header of the file!
                    bufferedReader.readLine();

                    while ((bufferedReader.readLine()) != null &&
                            (line2 = bufferedReader.readLine()) != null &&
                            (line3 = bufferedReader.readLine()) != null &&
                            (bufferedReader.readLine()) != null)
                    {
                        parts2 = line2.trim().split(" ");
                        parts3 = line3.trim().split(" ");

                        results.put(parts2[parts2.length - 1], parts3[parts3.length - 1]);
                    }
                }
                catch (IOException ex)
                {
                    System.err.println("Failed to read result of : "
                                               + filePath);
                }
                finally
                {
                    bufferedReader.close();
                }
            }
            catch (UnsupportedEncodingException ex)
            {
                System.err.println("Unsupported Encoding : "
                                           + filePath);
            }
            catch (IOException ex)
            {
                System.err.println("IO error while closing buffer"
                                           + "/reader : " + filePath);
            }
            finally
            {
                stream.close();
            }
        }
        catch (FileNotFoundException ex)
        {
            System.err.println("file not found : " + filePath);
        }
        catch (IOException ex)
        {
            System.err.println("IO error while closing stream : "
                                       + filePath);
        }

        return results;
    }

    /**
     * The file does not have any header!
     * @param filePath
     * @return
     */
    public static ArrayList<ArrayList<String>> readProteinPairwiseFile(String filePath)
    {
        InputStream stream;
        ArrayList<ArrayList<String>> results = new ArrayList<>();
        try
        {
            stream = new FileInputStream(filePath);
            BufferedReader bufferedReader;
            try
            {
                bufferedReader = new BufferedReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8));
                try
                {
                    String line;
                    String[] parts;

                    while ((line = bufferedReader.readLine()) != null)
                    {
                        if (line.equals(""))
                            continue;

                        parts = line.trim().split(",");

                        results.add(new ArrayList<>(Arrays.asList(parts)));
                    }
                }
                catch (IOException ex)
                {
                    System.err.println("Failed to read result of : "
                                               + filePath);
                }
                finally
                {
                    bufferedReader.close();
                }
            }
            catch (UnsupportedEncodingException ex)
            {
                System.err.println("Unsupported Encoding : "
                                           + filePath);
            }
            catch (IOException ex)
            {
                System.err.println("IO error while closing buffer"
                                           + "/reader : " + filePath);
            }
            finally
            {
                stream.close();
            }
        }
        catch (FileNotFoundException ex)
        {
            System.err.println("file not found : " + filePath);
        }
        catch (IOException ex)
        {
            System.err.println("IO error while closing stream : "
                                       + filePath);
        }

        return results;
    }
}

