package com.alimaddi.datacontainer;

import com.alimaddi.Utility.Utilities;
import com.alimaddi.control.DatabaseControler;
import com.alimaddi.control.DatabaseControllerForSTRs;
import com.alimaddi.model.STR;

import java.util.*;

public class STRFamily
{
    private String geneName;
    private HashSet<Integer> validSpecies;
    private HashMap<Integer, ArrayList<String>> speciesOwner;
    private HashMap<Integer, ArrayList<String>> uniqueSTRs;

    public STRFamily(String geneName, Set<Integer> validSpecies)
    {
        this.geneName = geneName;
        this.validSpecies = new HashSet<>(validSpecies);
        this.speciesOwner = new HashMap<>();
        this.uniqueSTRs = new HashMap<>();

        HashMap<Integer, ArrayList<String>> totalSpeciesOwner =
                DatabaseControllerForSTRs.getAllSTRsSequenceInDB(geneName);

        for (Integer speciesID : totalSpeciesOwner.keySet())
        {
            if (validSpecies.contains(speciesID))
                speciesOwner.put(speciesID, totalSpeciesOwner.get(speciesID));
        }

        /*
        HashSet<String> genesName;
        genesName = DatabaseControllerForGenes.getAllGenesName();
        System.out.println(genesName.size());
        ArrayList<String> names = new ArrayList<>(genesName);
        names = new ArrayList<>(names.subList(1, 2));
        System.out.println(names);

        ArrayList<Integer> speciesID =
                DatabaseControler.getAllSpeciesIDsOfGenesNameList(names);
        System.out.println(speciesID);
        System.out.println("-----------------------------");
        System.out.println(speciesID.size());
        speciesID.retainAll(validSpecies);
        System.out.println(speciesID.size());
        System.out.println(speciesID);

        ArrayList<String> strsSequence = DatabaseControllerForSTRs.getAllSTRsSequenceInDB(7, "cdan1");
        System.out.println("strsSequence size = " + strsSequence.size());
        System.out.println(strsSequence);

        STRFamily family = new STRFamily("cdan1", validSpecies);
        System.out.println(family.getSpeciesOwner());
        System.out.println("-----");
        System.out.println(family.calculateUniqueSTRs());
         */
    }

    public String getGeneName()
    {
        return geneName;
    }

    public HashSet<Integer> getValidSpecies()
    {
        return validSpecies;
    }

    public HashMap<Integer, ArrayList<String>> getSpeciesOwner()
    {
        return speciesOwner;
    }

    public HashMap<Integer, ArrayList<String>> getUniqueSTRs()
    {
        return uniqueSTRs;
    }

    public HashMap<Integer, ArrayList<String>> calculateUniqueSTRs()
    {
        uniqueSTRs.clear();
        HashMap<Integer, ArrayList<String>> shadow = new HashMap<>();
        HashMap<Integer, ArrayList<String>> uniqueCores = new HashMap<>();

        for (Integer speciesID : speciesOwner.keySet())
        {
            ArrayList<String> originalSTRs = speciesOwner.get(speciesID);
            ArrayList<String> coresSTRs = new ArrayList<>();

            for (String originalSTR : originalSTRs)
                coresSTRs.add(Utilities.decomposeSTR(originalSTR)[0]);

            shadow.put(speciesID, coresSTRs);
        }

//        System.out.println("----- shadow 1 -----");
//        System.out.println(shadow);

        for (Integer speciesID : shadow.keySet())
        {
            ArrayList<String> copyOfCoresSTRs = new ArrayList<>(shadow.get(speciesID));

            for (Integer secondLoopSpeciesID : shadow.keySet())
            {
                if (speciesID.equals(secondLoopSpeciesID))
                    continue;

                copyOfCoresSTRs.removeAll(shadow.get(secondLoopSpeciesID));

                if (copyOfCoresSTRs.isEmpty())
                    break;
            }

            uniqueCores.put(speciesID, copyOfCoresSTRs);
        }

        // shadow 1 and shadow two must be equal!
//        System.out.println("----- shadow 2 -----");
//        System.out.println(shadow);
//        System.out.println("----- uniqueCores -----");
//        System.out.println(uniqueCores);

        for (Integer speciesID : speciesOwner.keySet())
        {
            ArrayList<String> speciesUniqueSTRs = new ArrayList<>();

            if (!uniqueCores.get(speciesID).isEmpty())
            {
                for (String originalSTR : speciesOwner.get(speciesID))
                {
                    if (uniqueCores.get(speciesID).contains(Utilities.decomposeSTR(originalSTR)[0]))
                        speciesUniqueSTRs.add(originalSTR);
                }
            }
            uniqueSTRs.put(speciesID, speciesUniqueSTRs);
        }

//        System.out.println("----- uniqueSTRs -----");
//        System.out.println(uniqueSTRs);
        return uniqueSTRs;
    }
}
