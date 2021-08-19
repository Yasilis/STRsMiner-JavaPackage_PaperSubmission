package com.alimaddi.control.runnables;

import com.alimaddi.Utility.Utilities;
import com.alimaddi.control.DatabaseControllerForGenes;
import com.alimaddi.control.DatabaseControllerForTranscripts;
import com.alimaddi.datatypes.TranscriptSequenceType;
import com.alimaddi.model.Gene;

import java.util.*;
import java.util.concurrent.Callable;

import static com.alimaddi.Utility.Utilities.getDuration;

public class ThreadWorkerForGetSequenceOfPeptides implements Callable<HashMap<String, ArrayList<Integer>>>
{
    boolean haveHomolog;
    int speciesID;
    int numberOfHomologForMethod1 = 0;
    int numberOfnonHomologForMethod1 = 0;
    int numberOfHomologForMethod2 = 0;
    int numberOfnonHomologForMethod2 = 0;
    int baseCorrupted = 0;
    int threadID;
    long startTime;
    long endTime;
    String transcriptsStableID;
    ArrayList<Integer> filteredSpeciesID;
    HashMap<String, String> peptidesSequencesOfBaseTranscripts;

    public ThreadWorkerForGetSequenceOfPeptides(String transcriptsStableID, int threadID, int speciesID,
                                                ArrayList<Integer> filteredSpeciesID,
                                                HashMap<String, String> peptidesSequencesOfBaseTranscripts)
    {
        this.transcriptsStableID = transcriptsStableID;
        this.threadID = threadID;
        this.speciesID = speciesID;
        this.filteredSpeciesID = new ArrayList<>(filteredSpeciesID);
        this.peptidesSequencesOfBaseTranscripts = new HashMap<>(peptidesSequencesOfBaseTranscripts);
    }

    @Override
    public HashMap<String, ArrayList<Integer>> call() throws Exception
    {
        HashMap<String, ArrayList<Integer>> result = new HashMap<>();
        startTime = System.currentTimeMillis();
        System.out.println("$ :: The process of calculate homology for transcript ID "
                                   + transcriptsStableID + " in thread id " +
                                   threadID + " starts at " + new Date(startTime).toString() + ".");


        ArrayList<Gene> gene = DatabaseControllerForTranscripts.getProducerGenes(new ArrayList<>(
                Collections.singletonList(transcriptsStableID)));

        if (gene.size() != 1)
        {
            throw new IllegalStateException("$ : There is a duplicate gene or there is not any gene for "
                                                    + transcriptsStableID + " !");
//            result.put(transcriptsStableID, new ArrayList<>(Arrays.asList(numberOfHomolog, numberOfnonHomolog)));
//            return result;
        }

        ArrayList<Gene> homologyGenes = DatabaseControllerForGenes.getAllHomologyGene(gene.get(0));
        ArrayList<Gene> filteredHomologyGenes = new ArrayList<>();
        for (Gene homologyGene : homologyGenes)
        {
            if (filteredSpeciesID.contains(homologyGene.getSpecies().getId()) && homologyGene.getSpecies().getId() != speciesID)
                filteredHomologyGenes.add(homologyGene);
        }

        HashMap<String, String> totalPeptideSequences = new HashMap<>();
        //            ArrayList<String> candidateHomologyTranscripts = new ArrayList<>();
        for (Gene filteredHomologyGene : filteredHomologyGenes)
        {
            ArrayList<String> tempTranscriptIDs = DatabaseControllerForTranscripts
                    .getAllTranscriptsStableIDOfGeneStableIDFromDB(filteredHomologyGene.getGeneStableID());
            //                candidateHomologyTranscripts.addAll(tempTranscriptIDs);

            if (!tempTranscriptIDs.isEmpty())
            {
                HashMap<String, String> tempPeptidesSequencesOfTranscripts =
                        DatabaseControllerForTranscripts.getAllSequenceOfTranscriptStableIDsFromCloud(
                                tempTranscriptIDs, TranscriptSequenceType.PEPTIDE_SEQUENCE, 10,
                                filteredHomologyGene.getSpecies().getId(), true, true, false);

                if (tempPeptidesSequencesOfTranscripts != null && !tempPeptidesSequencesOfTranscripts.isEmpty())
                {
                    totalPeptideSequences.putAll(tempPeptidesSequencesOfTranscripts);
                }
                else
                {
                    System.err.println("error occurred during extraction peptides for "
                                               + tempTranscriptIDs.toString() + " in thread " + threadID);
                    System.err.println("No sequence was extracted" + " in thread " + threadID);
                }
            }
            else
            {
                System.err.println("error occurred during extraction Transcript IDs for "
                                           + filteredHomologyGene.getGeneStableID() + " in thread " + threadID);
                System.err.println("No ID was extracted" + " in thread " + threadID);
            }
        }

        String baseTranscriptPeptide = peptidesSequencesOfBaseTranscripts.
                get(gene.get(0).getGeneStableID() + "|" + transcriptsStableID);

        if (baseTranscriptPeptide == null || baseTranscriptPeptide.isEmpty())
        {
            System.err.println("base sequence is corrupted for transcript id = " + transcriptsStableID +
                                       ". It is null : " + (baseTranscriptPeptide == null) + " in thread " + threadID);
            baseCorrupted++;
        }
        else
        {
//            haveHomolog = false;
            for (String candidateHomologyTranscript : totalPeptideSequences.keySet())
            {
                String candidateHomologyTranscriptPeptide = totalPeptideSequences.get(candidateHomologyTranscript);
                if (candidateHomologyTranscriptPeptide == null || candidateHomologyTranscriptPeptide.isEmpty())
                {
                    System.err.println("candidate peptide sequence is corrupted for transcript id = "
                                               + transcriptsStableID + ". It is null : "
                                               + (candidateHomologyTranscriptPeptide == null)
                                               + " in thread " + threadID);

                    continue;
                }
                if (Utilities.isHomology(baseTranscriptPeptide, candidateHomologyTranscriptPeptide, 1))
                {
                    numberOfHomologForMethod1++;
                }
                else
                {
                    numberOfnonHomologForMethod1++;
                }
                if (Utilities.isHomology(baseTranscriptPeptide, candidateHomologyTranscriptPeptide, 2))
                {
                    numberOfHomologForMethod2++;
                }
                else
                {
                    numberOfnonHomologForMethod2++;
                }
            }

        }

        result.put(transcriptsStableID, new ArrayList<>(
                Arrays.asList(numberOfHomologForMethod1, numberOfnonHomologForMethod1, baseCorrupted,
                              numberOfHomologForMethod2, numberOfnonHomologForMethod2, baseCorrupted)));
//        System.out.println("$ : " + (processedTranscript++) + "/" +
//                                   limitedSpeciesSpecificSTRProducerTranscriptsStableIDs.size() +
//                                   " process start fot transcript id = " + transcriptsStableID
//                                   + " and gene id = " + gene.get(0).getGeneStableID() + " !");

        endTime = System.currentTimeMillis();
        System.out.println("$ :: The process of Thread for transcript ID "
                                   + transcriptsStableID + " ends at " + new Date(endTime).toString()
                                   + " in thread " + threadID);
        System.out.println("############## \t The process of Thread " +
                                   threadID + " for transcript ID " + transcriptsStableID
                                   + " ends in : (" + getDuration(startTime, endTime) + ") \t ##############");

        System.out.println("$ : Result of transcript id " + transcriptsStableID + " in thread id " + threadID +
                                   " is " + result.toString() + " for Homology and nonHomology Transcript.");

        return result;
    }
}
