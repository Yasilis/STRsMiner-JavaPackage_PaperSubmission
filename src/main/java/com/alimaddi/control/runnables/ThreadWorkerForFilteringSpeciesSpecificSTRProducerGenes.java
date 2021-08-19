package com.alimaddi.control.runnables;

import com.alimaddi.datacontainer.STRFamily;
import com.alimaddi.model.Gene;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;

import static com.alimaddi.Utility.Utilities.getDuration;

public class ThreadWorkerForFilteringSpeciesSpecificSTRProducerGenes implements Callable<Gene>
{
    private final int referenceSpeciesID;
    private final int threadID;
    private final Gene referenceGene;
    private final HashSet<Integer> desiredSpeciesID;

    public ThreadWorkerForFilteringSpeciesSpecificSTRProducerGenes(
            int threadID, int referenceSpeciesID, Gene referenceGene, HashSet<Integer> desiredSpeciesID)
    {
        this.referenceSpeciesID = referenceSpeciesID;
        this.threadID = threadID;
        this.referenceGene = referenceGene;
        this.desiredSpeciesID = desiredSpeciesID;
    }

    @Override
    public Gene call()
    {
        long startTime = System.currentTimeMillis();
        System.out.println("$ :: The process of filter gene for recognizing species specific STR producer gene "
                                   + referenceGene.getGeneName() + " (" + referenceGene.getGeneStableID() + ")" +
                                   " in thread id " + threadID + " starts at " + new Date(startTime).toString() + ".");

        STRFamily strFamily = new STRFamily(referenceGene.getGeneName(), desiredSpeciesID);
        HashMap<Integer, ArrayList<String>> calculatedUniqueSTRs = strFamily.calculateUniqueSTRs();

        long endTime = System.currentTimeMillis();
        System.out.println("$ :: The process of Thread for gene name "
                                   + referenceGene.getGeneName() + " ends at " + new Date(endTime).toString()
                                   + " in thread " + threadID);
        System.out.println("############## \t The process of Thread " +
                                   threadID + " for gene name " + referenceGene.getGeneName()
                                   + " ends in : (" + getDuration(startTime, endTime) + ") \t ##############");



        if (!calculatedUniqueSTRs.get(referenceSpeciesID).isEmpty())
        {
            System.out.println("$ : Result of thread id " + threadID + " shows that the " + referenceGene
                    .getGeneName() + " gene is a species specific STR producer gene");
            return referenceGene;
        }
        else
        {
            System.out.println("$ : Result of thread id " + threadID + " shows that the " + referenceGene
                    .getGeneName() + " gene is NOT a species specific STR producer gene");
            return null;
        }
    }
}
