package com.alimaddi.control.runnables;

import com.alimaddi.control.DatabaseControllerForGenes;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static com.alimaddi.Utility.Utilities.getDuration;

public class ThreadWorkerForGettingGeneFromCloud implements Callable<HashMap<Integer, Integer[]>>
{
    long startTime;
    long endTime;
    int speciesID;
    int duplicateNumber = 0;
    int uniqueNumber = 0;
    int updateNumber = 0;

    public ThreadWorkerForGettingGeneFromCloud(int speciesID)
    {
        this.speciesID = speciesID;
    }

    @Override
    public HashMap<Integer, Integer[]> call() throws Exception
    {
        startTime = System.currentTimeMillis();
        System.out.println("$ :: The process of Thread for species ID "
                                   + speciesID + " starts at " + new Date(startTime).toString() + ".");

        int[] speciesResult = DatabaseControllerForGenes.updateGenesForSpeciesID(speciesID, false);

        uniqueNumber += speciesResult[0];
        duplicateNumber += speciesResult[1];
        updateNumber += speciesResult[2];

        endTime = System.currentTimeMillis();
        System.out.println("$ :: The process of Thread for species ID "
                                   + speciesID + " ends at " + new Date(endTime).toString() + ".");
        System.out.println("############## \t The process of Thread for species ID " + speciesID
                                   + " ends in : (" + getDuration(startTime, endTime) + ") \t ##############");

        HashMap<Integer, Integer[]> result = new HashMap<>();
        result.put(speciesID, new Integer[]{uniqueNumber, duplicateNumber, updateNumber});
        return result;
    }
}
