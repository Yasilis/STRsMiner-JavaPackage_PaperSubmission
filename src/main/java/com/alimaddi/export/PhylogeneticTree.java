package com.alimaddi.export;

import com.alimaddi.Utility.Reader;
import com.alimaddi.control.DatabaseControler;
import com.alimaddi.model.Species;
import com.sun.istack.NotNull;

import java.util.ArrayList;
import java.util.HashSet;

import static com.alimaddi.export.DataSetsStatistics.getActiveSpecies;

public class PhylogeneticTree
{
    public static void changeScientificNameToCommonName(String treeFormat)
    {
        StringBuilder newTree = new StringBuilder();
        String speciesInTree;
        int startIndexOfName = 0;
        int endIndexOfName = 0;
        int rowNumber = 0;
        ArrayList<Species> renamedSpeciesInTree = new ArrayList<>();
        ArrayList<String> remainedSpeciesInTree = new ArrayList<>();

        HashSet<Species> speciesList = DatabaseControler.getAllSpeciesFromDB();

        while (startIndexOfName < treeFormat.length())
        {
            char currentChar = treeFormat.charAt(startIndexOfName);
            if (!isAlphabeticChar(currentChar))
            {
                newTree.append(currentChar);
                startIndexOfName++;
                continue;
            }
            if (isEulerNumber(treeFormat.substring(startIndexOfName)))
            {
                newTree.append(currentChar);
                startIndexOfName++;
                continue;
            }

            endIndexOfName = treeFormat.indexOf(":");

            if (endIndexOfName == -1)
                System.out.println("Error Occurred!");

            speciesInTree = treeFormat.substring(startIndexOfName, endIndexOfName);

            Species foundSpecies = findSpecies(speciesList, speciesInTree);
            if (foundSpecies != null && !foundSpecies.getCommonName().isEmpty())
            {
                renamedSpeciesInTree.add(foundSpecies);
                String canonicalCommonName = foundSpecies.getCommonName();
                canonicalCommonName = canonicalCommonName.replace('(', '*');
                canonicalCommonName = canonicalCommonName.replace(')', '*');
                newTree.append(canonicalCommonName);
                newTree.append("#");
            }
            else
                remainedSpeciesInTree.add(speciesInTree.toLowerCase());

            newTree.append(speciesInTree);
            newTree.append(":");
            treeFormat = treeFormat.substring(endIndexOfName + 1);
            startIndexOfName = 0;
            endIndexOfName = 0;

            System.out.print(++rowNumber + "\t");
            System.out.print(speciesInTree);
            System.out.print("\t\t\t\t\t\t\t\t\t");
            System.out.println(foundSpecies != null ? foundSpecies.getCommonName() : "");
        }

        System.out.println(newTree);
        System.out.println("Number of rename = " + renamedSpeciesInTree.size());
        System.out.println("remained species in DB :");

        for (Species referenceSpecies : speciesList)
        {
            if (!renamedSpeciesInTree.contains(referenceSpecies))
                System.out.println(referenceSpecies);
        }
        System.out.println("remained species in tree (" + remainedSpeciesInTree.size() + ") :");
        System.out.println(remainedSpeciesInTree);
    }

    private static Species findSpecies(HashSet<Species> speciesList, String species)
    {
        for (Species referenceSpecies : speciesList)
        {
            if (referenceSpecies.getName().toLowerCase().equals(species.toLowerCase()))
                return referenceSpecies;
        }

        for (Species referenceSpecies : speciesList)
        {
            String[] refArray = referenceSpecies.getDisplayName().toLowerCase().split(" ");
            String[] targetArray = species.toLowerCase().split("_");
            if (refArray[refArray.length - 1].equals(targetArray[targetArray.length - 1]))
                return referenceSpecies;
        }

        for (Species referenceSpecies : speciesList)
        {
            String[] refArray = referenceSpecies.getName().toLowerCase().split("_");
            String[] targetArray = species.toLowerCase().split("_");
            if (refArray.length >= 2 &&
                    targetArray.length >= 2 &&
                    refArray[0].equals(targetArray[0]) &&
                    refArray[1].equals(targetArray[1]))
                return referenceSpecies;
        }

        for (Species referenceSpecies : speciesList)
        {
            String[] refArray = referenceSpecies.getName().toLowerCase().split("_");
            String[] targetArray = species.toLowerCase().split("_");
            if (refArray.length >= 2 &&
                    targetArray.length >= 2 &&
                    refArray[0].equals(targetArray[0]) &&
                    refArray[1].equals(targetArray[1]))
                return referenceSpecies;
        }

        return null;
    }

    private static boolean isEulerNumber(@NotNull String str)
    {
        if (str.length() < 3)
            return false;
        return str.charAt(0) == 'e' && str.charAt(1) == '-' && str.charAt(2) >= '0' && str.charAt(2) <= '9';
    }

    private static boolean isAlphabeticChar(char ch)
    {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
    }
}
