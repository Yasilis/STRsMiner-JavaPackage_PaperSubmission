package com.alimaddi.control.runnables;

import com.alimaddi.control.DatabaseControllerForSTRs;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static com.alimaddi.Utility.Utilities.getDuration;

public class ThreadWorkerForCalculatingSTRs implements Callable<HashMap<Integer, Long[]>>
{
    long startTime;
    long endTime;
    int speciesID;
    long duplicateNumber = 0;
    long uniqueNumber = 0;
    long updateNumber = 0;
    long deleteNumber = 0;

    public ThreadWorkerForCalculatingSTRs(int speciesID)
    {
        this.speciesID = speciesID;
    }

    @Override
    public HashMap<Integer, Long[]> call() throws Exception
    {
        startTime = System.currentTimeMillis();
        System.out.println("$ :: The process of Thread for species ID "
                                   + speciesID + " starts at " + new Date(startTime).toString() + ".");

        long[] speciesResult = DatabaseControllerForSTRs.turboUpdateSTRs(speciesID, false);

        uniqueNumber += speciesResult[0];
        duplicateNumber += speciesResult[1];
        updateNumber += speciesResult[2];
        deleteNumber += speciesResult[3];

        endTime = System.currentTimeMillis();
        System.out.println("$ :: The process of Thread for species ID "
                                   + speciesID + " ends at " + new Date(endTime).toString() + ".");
        System.out.println("############## \t The process of Thread for species ID " + speciesID
                                   + " ends in : (" + getDuration(startTime, endTime) + ") \t ##############");

        HashMap<Integer, Long[]> result = new HashMap<>();
        result.put(speciesID, new Long[]{uniqueNumber, duplicateNumber, updateNumber, deleteNumber});
        return result;
    }
}
