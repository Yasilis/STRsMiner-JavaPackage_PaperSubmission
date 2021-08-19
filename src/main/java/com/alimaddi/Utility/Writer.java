package com.alimaddi.Utility;

import com.alimaddi.model.STR;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Ali-Maddi
 *
 */
public class Writer
{

    public static void writeDataMatrix(HashMap<String, HashMap<String, Integer>> dataMatrix)
    {
        File file = new File("./assets/input_20.txt");
        FileOutputStream stream;
        try
        {
            stream = new FileOutputStream(file);
            PrintWriter wr = new PrintWriter(stream);
            for (Map.Entry<String, HashMap<String, Integer>> entry : dataMatrix.entrySet())
            {
                String id = entry.getKey();
                HashMap<String, Integer> cols = entry.getValue();
                wr.print(id + "\t");
                wr.println(cols.toString());
            }
            wr.flush();
            stream.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

//    public static void writeDataMatrix(ArrayList<STR> strs)
//    {
//        File file;
//        FileOutputStream stream = null;
//        PrintWriter wr = null;
//        HashSet<String> listofStrsSeq = new HashSet<>();
//        HashMap<String, HashMap<String, Short>> data = new HashMap<>();
//
//        SimpleDateFormat formatter= new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");
//        Date date = new Date(System.currentTimeMillis());
//        String path = "./inputOfR/" + formatter.format(date) + "__sparse_matrix.csv";
//        file = new File("./inputOfR/");
//        file.mkdir();
//        file = new File(path);
//
//        try
//        {
//            stream = new FileOutputStream(file);
//            wr = new PrintWriter(stream);
//
//            for (STR str : strs)
//            {
//                if (data.containsKey(str.getTranscriptStableId()))
//                {
//                    if (data.get(str.getTranscriptStableId()).containsKey(str.getSequence()))
//                    {
//                        data.get(str.getTranscriptStableId()).put(str.getSequence()
//                                , (short) (data.get(str.getTranscriptStableId()).get(str.getSequence())
//                                        + str.getAbundance()));
//                    }
//                    else
//                    {
//                        data.get(str.getTranscriptStableId()).put(str.getSequence(), str.getAbundance());
//                    }
//
//                }
//                else
//                {
//                    HashMap<String, Short> temp = new HashMap<>();
//                    temp.put(str.getSequence(), str.getAbundance());
//                    data.put(str.getTranscriptStableId(), temp);
//                }
//            }
//
//            for (STR str : strs)
//            {
//                listofStrsSeq.add(str.getSequence());
//            }
//            ArrayList<String> colNames = new ArrayList<>(listofStrsSeq);
//            listofStrsSeq.clear();
//            Collections.sort(colNames);
//
//            StringBuilder line = new StringBuilder();
//            line.append("IDs");
//            for (String name : colNames)
//                line.append("\t" + name);
//
//            //print line!
//            wr.println(line);
//
//            line.setLength(0);
//            line.trimToSize();
//            for (Map.Entry<String, HashMap<String, Short>> row : data.entrySet())
//            {
//                line.append(row.getKey());
//
//                for (String name : colNames)
//                {
//                    if (row.getValue().containsKey(name))
//                    {
//                        line.append("\t" + row.getValue().get(name));
//                    }
//                    else
//                    {
//                        line.append("\t0");
//                    }
//                }
//
//                //print line!
//                wr.println(line);
//
//                line.setLength(0);
//                line.trimToSize();
//            }
//
//
//            if (wr != null)
//                wr.flush();
//            if (stream != null)
//                stream.close();
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//    }

    public static void writeFingerPrintFile(HashMap<Integer, ArrayList<String>> fingerPrintData, String moreInfo)
    {
        writeFingerPrintFile(fingerPrintData,-1, -1, moreInfo);
    }

    public static void writeFingerPrintFile(HashMap<Integer, ArrayList<String>> fingerPrintData,
                                            int minSize, int maxSize, String moreInfo)
    {
        File file;
        FileOutputStream stream;
        PrintWriter wr;

        SimpleDateFormat formatter= new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");
        Date date = new Date(System.currentTimeMillis());
        String path = "./report/" + formatter.format(date) + "__@" + minSize + "," + maxSize +
                "@_" + moreInfo + "_simple_finger_Print_file.csv";
        file = new File("./report/");

        try
        {
            if (!file.exists() && !file.mkdir())
                throw new IOException();
            file = new File(path);

            stream = new FileOutputStream(file);
            wr = new PrintWriter(stream);

            StringBuilder line = new StringBuilder();
            line.append("SpeciesID,Specific STRs Count,STRs");

            //print line!
            wr.println(line);

            line.setLength(0);
            line.trimToSize();
            ArrayList<String> filteredSTRs = new ArrayList<>();
            for (Map.Entry<Integer, ArrayList<String>> row : fingerPrintData.entrySet())
            {
                line.append(row.getKey()).append(",");

                row.getValue().sort(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2)
                    {
                        String core1;
                        String core2;
                        int repeat1;
                        int repeat2;

                        String[] output;

                        output = Utilities.decomposeSTR(o1);
                        core1 = output[0];
                        repeat1 = Integer.parseInt(output[1]);

                        output = Utilities.decomposeSTR(o2);
                        core2 = output[0];
                        repeat2 = Integer.parseInt(output[1]);

                        if (core1.equals(core2))
                            return Integer.compare(repeat1, repeat2);
                        else if (core1.length() == core2.length())
                            return core1.compareTo(core2);
                        else
                            return Integer.compare(core1.length(), core2.length());
                    }
                });

                if (maxSize == -1)
                {
                    line.append(row.getValue().size());
                    for (String str : row.getValue())
                        line.append(",").append(str);
                }
                else if (minSize <= maxSize)
                {
                    for (String str : row.getValue())
                    {
                        String core = Utilities.decomposeSTR(str)[0];
                        if (core.length() >= minSize && core.length() <= maxSize)
                            filteredSTRs.add(str);
                    }
                    line.append(filteredSTRs.size());
                    for (String filteredSTR : filteredSTRs)
                        line.append(",").append(filteredSTR);
                    filteredSTRs.clear();
                }


                //print line!
                wr.println(line);

                line.setLength(0);
                line.trimToSize();
            }

            wr.flush();
            stream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void writeRepeatPercentage(HashMap<Integer, ArrayList<String>> result, String moreInfo)
    {
        writeRepeatPercentage(result,-1, -1, moreInfo);
    }

    public static void writeRepeatPercentage(HashMap<Integer, ArrayList<String>> result,
                                            int minSize, int maxSize, String moreInfo)
    {
        ArrayList<Float> repeatPercentage;
        File file;
        FileOutputStream stream;
        PrintWriter wr;

        SimpleDateFormat formatter= new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");
        Date date = new Date(System.currentTimeMillis());
        String path = "./report/" + formatter.format(date) + "__@" + minSize + "," + maxSize +
                "@_" + moreInfo + "_simple_repeat_percentage.csv";
        file = new File("./report/");

        try
        {
            if (!file.exists() && !file.mkdir())
                throw new IOException();
            file = new File(path);

            stream = new FileOutputStream(file);
            wr = new PrintWriter(stream);

            StringBuilder line = new StringBuilder();
            line.append("SpeciesID,Specific STRs Count");

            for (int i = 0 ; i < 121 ; i++)
                line.append(",").append(i);

            //print line!
            wr.println(line);

            line.setLength(0);
            line.trimToSize();
            for (Map.Entry<Integer, ArrayList<String>> row : result.entrySet())
            {
//                line.append(row.getKey()).append(",").append(row.getValue().size());
                line.append(row.getKey()).append(",0");

                if (maxSize == -1)
                {
                    repeatPercentage = makeRepeatPercentage(row.getValue());
//                    for (int i = 0 ; i < repeatPercentage.size() ; i++)
                    for (Float percent : repeatPercentage)
                        line.append(",").append(percent);
                }
                else if (minSize <= maxSize)
                {
                    ArrayList<String> filteredSTRs = new ArrayList<>();
                    for (String str : row.getValue())
                    {
                        String core = Utilities.decomposeSTR(str)[0];
                        if (core.length() >= minSize && core.length() <= maxSize)
                            filteredSTRs.add(str);
                    }

                    repeatPercentage = makeRepeatPercentage(filteredSTRs);
//                    for (int i = 0 ; i < repeatPercentage.size() ; i++)
                    for (Float percent : repeatPercentage)
                        line.append(",").append(percent);

                }


                //print line!
                wr.println(line);

                line.setLength(0);
                line.trimToSize();
            }

            wr.flush();
            stream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static ArrayList<Float> makeRepeatPercentage(ArrayList<String> filteredSTRs)
    {
        HashMap<Integer, ArrayList<Float>> repeatPercentage = new HashMap<>();
        ArrayList<Float> percentage = new ArrayList<>(Collections.nCopies(121, 0.0f));
        for (String str : filteredSTRs)
        {
            int index = Integer.parseInt(Utilities.decomposeSTR(str)[1]);
            percentage.set(index, percentage.get(index) + 1);
        }


        return percentage;
    }


    public static void writeCompositionOfNucleotidesFile(int[][] nucleotidesComposition,
                                             ArrayList<ArrayList<String>> fingerPrintFile,
                                                         int minSize, int maxSize, String moreInfo)
    {
        File file;
        FileOutputStream stream;
        PrintWriter wr;

        SimpleDateFormat formatter= new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");
        Date date = new Date(System.currentTimeMillis());
        String path = "./report/" + formatter.format(date) + "__@" + minSize + "," + maxSize +
                "@_" + moreInfo + "_nucleotides_composition.csv";
        file = new File("./report/");

        try
        {
            if (!file.exists() && !file.mkdir())
                throw new IOException();
            file = new File(path);

            stream = new FileOutputStream(file);
            wr = new PrintWriter(stream);

            StringBuilder line = new StringBuilder();
            line.append("SpeciesID,Specific STRs Count,A,T,C,G,AA,AT,AC,AG,TA,TT,TC,TG,CA,CT,CC,CG,GA,GT,GC,GG,STRs");

            //print line!
            wr.println(line);

            line.setLength(0);
            line.trimToSize();
            for (int i = 0; i < fingerPrintFile.size(); i++)
            {
                ArrayList<String> row = fingerPrintFile.get(i);
                int totalNucleotides =
                        nucleotidesComposition[i][0] +
                        nucleotidesComposition[i][1] +
                        nucleotidesComposition[i][2] +
                        nucleotidesComposition[i][3];
                line.append(row.get(0)).append(",").append(row.size() - 2);
                for (int j = 0; j < 20; j++)
                {
                    line.append(",").append(String.format("%d (%.2f%%)", nucleotidesComposition[i][j],
                                            ((float)nucleotidesComposition[i][j] / totalNucleotides * 100)));
                }

                for (String str : row.subList(2, row.size()))
                {
                    line.append(",").append(str);
                }

                //print line!
                wr.println(line);

                line.setLength(0);
                line.trimToSize();
            }

            wr.flush();
            stream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
