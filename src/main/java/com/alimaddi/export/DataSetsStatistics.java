package com.alimaddi.export;

import com.alimaddi.Utility.Reader;
import com.alimaddi.Utility.Utilities;
import com.alimaddi.control.DatabaseControler;
import com.alimaddi.control.DatabaseControllerForGenes;
import com.alimaddi.control.DatabaseControllerForSTRs;
import com.alimaddi.control.DatabaseControllerForTranscripts;
import com.alimaddi.model.Species;

import java.util.*;

public class DataSetsStatistics
{
    public static void printStatistics(String strsSpecificFilePath)
    {
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println("sorted list of desired species name :");
        printDesiredSpeciesNames(strsSpecificFilePath);
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println("table of species and their count of genes, transcripts and TRs :");
        printDBCountStatistic(strsSpecificFilePath);
        System.out.println("-------------------------------------------------------------------------------------");

    }

    private static void printDBCountStatistic(String strsSpecificFilePath)
    {
        SortedMap<String, ArrayList<Long>> table = new TreeMap<>();
        TreeMap<String, Integer> sortedSpeciesNameID = getSortedDesiredSpeciesNameID(strsSpecificFilePath);
        for (String speciesName : sortedSpeciesNameID.keySet())
        {
            ArrayList<Long> values = new ArrayList<>(List.of((long)sortedSpeciesNameID.get(speciesName), 0L, 0L, 0L));
            table.put(speciesName, values);
        }

        int counter = 0;
        Utilities.showProgress(table.size(), counter, 50, "" + "\t\t\t");
        for (String species : table.keySet())
        {
            ArrayList<Long> values = table.get(species);
            if (values.isEmpty())
                continue;

            int speciesID = values.get(0).intValue();
            Long geneCount = DatabaseControllerForGenes.getAllGenesCount(speciesID);
            Long transcriptCount = DatabaseControllerForTranscripts.getAllTranscriptsCount(speciesID);
            Long trCount = DatabaseControllerForSTRs.getAllSTRsCountInDB(speciesID);
            values.set(1, geneCount);
            values.set(2, transcriptCount);
            values.set(3, trCount);
            Utilities.showProgress(table.size(), ++counter, 50, "" + "\t\t\t");
        }

        System.out.println();
        System.out.println("Species ID" + "," + "Species Name" + "," + "Gene Count" + "," + "Transcript Count" +
                                   "," + "TR Count");
        for (String speciesName : table.keySet())
        {
            System.out.println(table.get(speciesName).get(0) + "," +
                                       speciesName + "," +
                                       table.get(speciesName).get(1) + "," +
                                       table.get(speciesName).get(2) + "," +
                                       table.get(speciesName).get(3));
        }

    }

    public static void printDesiredSpeciesNames(String strsSpecificFilePath)
    {
        TreeMap<String, Integer> sortedSpeciesNameID = getSortedDesiredSpeciesNameID(strsSpecificFilePath);
        TreeSet<String> sortedSpeciesName = new TreeSet<>(sortedSpeciesNameID.keySet());
        String output = sortedSpeciesName.toString();

        System.out.println(output);
        System.out.println(sortedSpeciesName.size() + " species name was printed!");
    }

    public static TreeMap<String, Integer> getSortedDesiredSpeciesNameID(String strsSpecificFilePath)
    {
        ArrayList<ArrayList<String>> totalFingerPrintFile = Reader.readFingerPrintFile(strsSpecificFilePath);
        HashSet<Integer> desiredSpeciesID = new HashSet<>(getActiveSpecies(totalFingerPrintFile));
        ArrayList<Species> allSpecies = DatabaseControler.getSpeciesFromDB(desiredSpeciesID);
        TreeMap<String, Integer> sortedSpeciesName = new TreeMap<>();
        for (Species species : allSpecies)
        {
            sortedSpeciesName.put(species.getDisplayName(), species.getId());
        }

        return sortedSpeciesName;
    }





    public static ArrayList<Integer> getActiveSpecies(ArrayList<ArrayList<String>> fingerPrintFile)
    {
        ArrayList<Integer> result = new ArrayList<>();
        for (ArrayList<String> row : fingerPrintFile)
        {
            if (Integer.parseInt(row.get(1)) != 0)
            {
                result.add(Integer.parseInt(row.get(0)));
            }
        }


        return result;
    }

}
