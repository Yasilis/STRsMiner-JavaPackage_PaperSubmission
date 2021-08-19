package com.alimaddi.control.runnables;

import com.alimaddi.datacontainer.STRFamily;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.alimaddi.Utility.Utilities.getDuration;

public class ThreadWorkerForCalculatingUniqueSTRsByGeneName implements Callable<HashMap<Integer, ArrayList<String>>>
{
    private final String geneName;
    private final Set<Integer> validSpecies;

    public ThreadWorkerForCalculatingUniqueSTRsByGeneName(String geneName, Set<Integer> validSpecies)
    {
        this.geneName = geneName;
        this.validSpecies = validSpecies;
    }

    @Override
    public HashMap<Integer, ArrayList<String>> call()
    {
        long startTime = System.currentTimeMillis();
        System.out.println("$ :: The process of Thread for gene name "
                                   + geneName + " starts at " + new Date(startTime).toString() + ".");

        STRFamily strFamily = new STRFamily(geneName, validSpecies);
        HashMap<Integer, ArrayList<String>> calculatedUniqueSTRs =
                strFamily.calculateUniqueSTRs();

        long endTime = System.currentTimeMillis();
        System.out.println("$ :: The process of Thread for gene name "
                                   + geneName + " ends at " + new Date(endTime).toString() + ".");
        System.out.println("############## \t The process of Thread for gene name " + geneName
                                   + " ends in : (" + getDuration(startTime, endTime) + ") \t ##############");

        return calculatedUniqueSTRs;
    }
}
