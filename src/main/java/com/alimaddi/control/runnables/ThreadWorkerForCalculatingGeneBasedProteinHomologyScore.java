package com.alimaddi.control.runnables;

import com.alimaddi.Utility.Utilities;
import com.alimaddi.control.DataAnalysis;
import com.alimaddi.control.DatabaseControllerForGenes;
import com.alimaddi.control.DatabaseControllerForSTRs;
import com.alimaddi.control.DatabaseControllerForTranscripts;
import com.alimaddi.datacontainer.STRFamily;
import com.alimaddi.datatypes.TranscriptSequenceType;
import com.alimaddi.model.Gene;
import com.alimaddi.model.STR;
import com.alimaddi.model.Transcript;

import java.util.*;
import java.util.concurrent.Callable;

import static com.alimaddi.Utility.Utilities.getDuration;

public class ThreadWorkerForCalculatingGeneBasedProteinHomologyScore implements Callable<ArrayList<ArrayList<String>>>
{
    private boolean checkValidate;
    private int referenceSpeciesID;
    private int secondarySpeciesID1;
    private int secondarySpeciesID2;
    private int secondarySpeciesID3;
    private int threadID;
    private long startTime;
    private long endTime;
    private float proteinHomologyScore1;
    private float proteinHomologyScore2;
    private float proteinHomologyScore3;
    private Gene referenceGene;
    private HashSet<Integer> desiredSpeciesID;


    public ThreadWorkerForCalculatingGeneBasedProteinHomologyScore(
            int threadID, int referenceSpeciesID, Gene referenceGene, HashSet<Integer> desiredSpeciesID,
            int secondarySpeciesID1, int secondarySpeciesID2, int secondarySpeciesID3, boolean checkValidate)
    {
        this.threadID = threadID;
        this.referenceSpeciesID = referenceSpeciesID;
        this.referenceGene = referenceGene;
        this.desiredSpeciesID = desiredSpeciesID;
        this.secondarySpeciesID1 = secondarySpeciesID1;
        this.secondarySpeciesID2 = secondarySpeciesID2;
        this.secondarySpeciesID3 = secondarySpeciesID3;
        this.checkValidate = checkValidate;
    }

    @Override
    public ArrayList<ArrayList<String>> call() throws Exception
    {
        startTime = System.currentTimeMillis();
        System.out.println("$ :: The process of calculate gene based protein homology score for gene "
                                   + referenceGene.getGeneName() + " (" + referenceGene.getGeneStableID() + ")" +
                                   " in thread id " + threadID + " starts at " + new Date(startTime).toString() + ".");

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        HashMap<Gene, ArrayList<String>> genesClass = new HashMap<>();
        genesClass.put(referenceGene, new ArrayList<>());
        HashSet<String> referenceTranscriptsStableIDPool = new HashSet<>();

        HashSet<Transcript> candidateTranscriptsStableIDs =
                DatabaseControllerForTranscripts.getAllTranscriptsOfGeneStableIDFromDB(referenceGene.getGeneStableID());

        if (checkValidate)
        {
            STRFamily strFamily = new STRFamily(referenceGene.getGeneName(), desiredSpeciesID);
            HashMap<Integer, ArrayList<String>> calculatedUniqueSTRs = strFamily.calculateUniqueSTRs();

            if (calculatedUniqueSTRs.get(referenceSpeciesID).isEmpty())
            {
                System.out.println("$ : candidate gene was not species specific STR Producer gene : " +
                                           referenceGene.getGeneName() + " | " + referenceGene.getGeneStableID());
                calculateProcessTime(result);
                return result;
            }

            System.out.println();
            System.out.println("$ : The reference gene " + referenceGene.getGeneName() + " with Gene Ensembl Stable ID " +
                                       referenceGene.getGeneStableID() +
                                       " is species specific STR producer genes.");
            System.out.println();

            for (Transcript transcript : candidateTranscriptsStableIDs)
            {
                HashSet<STR> strs =
                        DatabaseControllerForSTRs.getAllSTRsOfTranscriptStableIDFromDB(transcript.getTranscriptStableID());

                ArrayList<String> sequences = new ArrayList<>();
                for (STR str : strs)
                    sequences.add(str.getSequence());

                sequences.retainAll(calculatedUniqueSTRs.get(referenceSpeciesID));
                if (!sequences.isEmpty())
                {
                    genesClass.get(transcript.getGene()).add(transcript.getTranscriptStableID());
                    referenceTranscriptsStableIDPool.add(transcript.getTranscriptStableID());
                }
            }
        }
        else
        {
            for (Transcript transcript : candidateTranscriptsStableIDs)
            {
                HashSet<STR> strs =
                        DatabaseControllerForSTRs.getAllSTRsOfTranscriptStableIDFromDB(transcript.getTranscriptStableID());
                if (!strs.isEmpty())
                {
                    genesClass.get(transcript.getGene()).add(transcript.getTranscriptStableID());
                    referenceTranscriptsStableIDPool.add(transcript.getTranscriptStableID());
                }
            }
        }

        HashMap<String, String> referencePeptidesSequences = new HashMap<>();
        HashMap<String, String> tempPeptidesSequences =
                DatabaseControllerForTranscripts.getAllSequenceOfTranscriptStableIDsFromCloud(
                        new ArrayList<>(referenceTranscriptsStableIDPool),
                        TranscriptSequenceType.PEPTIDE_SEQUENCE, 1000, referenceSpeciesID,
                        true, true, false);
        if (tempPeptidesSequences != null && !tempPeptidesSequences.isEmpty())
        {
            referencePeptidesSequences.putAll(tempPeptidesSequences);
        }
        else
        {
            System.err.println("$ : error occurred during extraction peptides for reference gene and his transcripts : "
                                       + referenceTranscriptsStableIDPool.toString() + " in thread " + threadID);
            System.err.println("$ : No sequence was extracted" + " in thread " + threadID);
            calculateProcessTime(result);
            return result;
        }

        int index = 0;
        for (Gene gene : genesClass.keySet())
        {
            if (gene.getGeneName() == null || gene.getGeneName().isEmpty())
            {
                System.err.println("$ : error occurred! reference gene name is empty in thread " + threadID);
                calculateProcessTime(result);
                return result;
            }

            ArrayList<Gene> homologyGenes = DatabaseControllerForGenes.getAllHomologyGene(gene);
            ArrayList<Gene> filteredHomologyGenes =
                    DataAnalysis.filterHomologyGenes(++index, gene, homologyGenes, desiredSpeciesID);

            HashMap<Integer, ArrayList<Gene>> otherGeneClass = new HashMap<>();
            otherGeneClass.put(secondarySpeciesID1, new ArrayList<>());
            otherGeneClass.put(secondarySpeciesID2, new ArrayList<>());
            otherGeneClass.put(secondarySpeciesID3, new ArrayList<>());
            for (Gene filteredHomologyGene : filteredHomologyGenes)
            {
                if (filteredHomologyGene.getSpecies().getId() == secondarySpeciesID1)
                    otherGeneClass.get(secondarySpeciesID1).add(filteredHomologyGene);
                else if (filteredHomologyGene.getSpecies().getId() == secondarySpeciesID2)
                    otherGeneClass.get(secondarySpeciesID2).add(filteredHomologyGene);
                else if (filteredHomologyGene.getSpecies().getId() == secondarySpeciesID3)
                    otherGeneClass.get(secondarySpeciesID3).add(filteredHomologyGene);
            }

            if (otherGeneClass.get(secondarySpeciesID1).isEmpty() ||
                    otherGeneClass.get(secondarySpeciesID2).isEmpty() ||
                    otherGeneClass.get(secondarySpeciesID3).isEmpty())
            {
                System.err.println("$ : error occurred! There is at least one species which have " +
                                           "no homology gene for reference gene in thread " + threadID);
                calculateProcessTime(result);
                return result;
            }

            filteredHomologyGenes = new ArrayList<>();
            filteredHomologyGenes.addAll(otherGeneClass.get(secondarySpeciesID1));
            filteredHomologyGenes.addAll(otherGeneClass.get(secondarySpeciesID2));
            filteredHomologyGenes.addAll(otherGeneClass.get(secondarySpeciesID3));

            HashMap<Integer, ArrayList<String>> otherTranscriptsStableIDPool = new HashMap<>();
            HashMap<String, ArrayList<String>> otherTranscriptsStableIDToGenesStableID = new HashMap<>();
            for (Gene filteredHomologyGene : filteredHomologyGenes)
            {
                ArrayList<String> transcriptsStableIDs =
                        DatabaseControllerForTranscripts.getAllTranscriptsStableIDOfGeneStableIDFromDB(
                                filteredHomologyGene.getGeneStableID());

                for (String transcriptsStableID : transcriptsStableIDs)
                {
                    if (otherTranscriptsStableIDToGenesStableID.containsKey(transcriptsStableID))
                        otherTranscriptsStableIDToGenesStableID.get(transcriptsStableID)
                                                               .add(filteredHomologyGene.getGeneStableID());
                    else
                        otherTranscriptsStableIDToGenesStableID
                                .put(transcriptsStableID,
                                     new ArrayList<>(Collections.singleton(filteredHomologyGene.getGeneStableID())));

                }

                if (otherTranscriptsStableIDPool.containsKey(filteredHomologyGene.getSpecies().getId()))
                {
                    otherTranscriptsStableIDPool.get(filteredHomologyGene.getSpecies().getId())
                                                .addAll(transcriptsStableIDs);
                }
                else
                {
                    otherTranscriptsStableIDPool.put(
                            filteredHomologyGene.getSpecies().getId(), transcriptsStableIDs);
                }
            }

            System.err.println("$ : downloading peptides for transcripts of other species in thread " + threadID);
            HashMap<String, String> otherPeptidesSequences = new HashMap<>();
            for (Integer otherSpeciesID : otherTranscriptsStableIDPool.keySet())
            {
                if (otherTranscriptsStableIDPool.get(otherSpeciesID).isEmpty())
                {
                    System.err.println("$ : error occurred! There is at least one species which have " +
                                               "no transcript for homology gene to the reference gene in thread " +
                                               threadID);
                    calculateProcessTime(result);
                    return result;
                }

                tempPeptidesSequences =
                        DatabaseControllerForTranscripts.getAllSequenceOfTranscriptStableIDsFromCloud(
                                otherTranscriptsStableIDPool.get(otherSpeciesID),
                                TranscriptSequenceType.PEPTIDE_SEQUENCE, 1000,
                                otherSpeciesID,
                                true, true, false);

                if (tempPeptidesSequences != null && !tempPeptidesSequences.isEmpty())
                {
                    otherPeptidesSequences.putAll(tempPeptidesSequences);
                }
                else
                {
                    System.err.println("$ : error occurred during extraction peptides for homology gene of " +
                                               "reference gene and his transcripts : "
                                               + referenceTranscriptsStableIDPool.toString() + " in thread " + threadID);
                    System.err.println("No sequence was extracted" + " in thread " + threadID);
                    calculateProcessTime(result);
                    return result;
                }
            }

            System.out.println("$ : Peptides Sequences Of Base Transcripts of " + referenceGene.getGeneName() + " : ");
            System.out.println(referencePeptidesSequences.toString());
            System.out.println("$ : Peptides Sequences Of Other Transcripts of " + referenceGene.getGeneName() + " : ");
            System.out.println(otherPeptidesSequences.toString());
            System.out.println("$ ");

            for (String referenceTranscriptStableID : genesClass.get(gene))
            {
                String referenceTranscriptPeptide = referencePeptidesSequences
                        .get(gene.getGeneStableID() + "|" + referenceTranscriptStableID);

                if (referenceTranscriptPeptide == null || referenceTranscriptPeptide.isEmpty())
                {
                    System.err.println("base sequence is corrupted for transcript id = " +
                                               referenceTranscriptStableID + ". It is null : " +
                                               (referenceTranscriptPeptide == null) + " in thread " + threadID);
                    continue;
                }

                for (Integer otherSpeciesID : otherTranscriptsStableIDPool.keySet())
                {
                    for (String transcriptStableID : otherTranscriptsStableIDPool.get(otherSpeciesID))
                    {
                        for (String geneStableID : otherTranscriptsStableIDToGenesStableID.get(transcriptStableID))
                        {
                            String otherPeptideSequence = otherPeptidesSequences
                                    .get(geneStableID + "|" + transcriptStableID);
                            if (otherPeptideSequence == null || otherPeptideSequence.isEmpty())
                            {
                                System.err.println("candidate peptide sequence is corrupted for transcript id = "
                                                           + transcriptStableID + ". It is null : "
                                                           + (otherPeptideSequence == null)
                                                           + " in thread " + threadID);
                                continue;
                            }
                            System.out.println("$ : Before calculating protein pairwise score for " +
                                                       referenceTranscriptPeptide + " and " + otherPeptideSequence);
                            String score1;
                            String score2;
                            String score3;
                            score1 = Utilities.calculateProteinPairwiseScoreByNeedlemanWunsch(
                                    referenceTranscriptPeptide, otherPeptideSequence);

                            int endIndex1 = Math.min(referenceTranscriptPeptide.length(), 6);
                            int endIndex2 = Math.min(otherPeptideSequence.length(), 6);

                            score2 = Utilities.calculateProteinPairwiseScoreBySimple(
                                    referenceTranscriptPeptide.substring(0, endIndex1),
                                    otherPeptideSequence.substring(0, endIndex2), 1);
                            score3 = Utilities.calculateProteinPairwiseScoreBySimple(
                                    referenceTranscriptPeptide.substring(0, endIndex1),
                                    otherPeptideSequence.substring(0, endIndex2), 2);
                            System.out.println("$ : Calculated scores for " + referenceTranscriptPeptide + "and" +
                                                       otherPeptideSequence + " are :\tscore1 = " + score1 + "\t|\t" +
                                                       "score2 = " + score2 + "\t|\tscore3 = " + score3);
                            ArrayList<String> roundResult = new ArrayList<>();
                            roundResult.add(referenceGene.getGeneName());
                            roundResult.add("" + referenceSpeciesID);
                            roundResult.add(referenceGene.getGeneStableID());
                            roundResult.add(referenceTranscriptStableID);
                            roundResult.add(referenceTranscriptPeptide);
                            roundResult.add("" + otherSpeciesID);
                            roundResult.add(geneStableID);
                            roundResult.add(transcriptStableID);
                            roundResult.add(otherPeptideSequence);
                            roundResult.add(score1);
                            roundResult.add(score2);
                            roundResult.add(score3);
                            result.add(roundResult);
                        }
                    }
                }
            }
        }

        genesClass.clear();
        referenceTranscriptsStableIDPool.clear();

        endTime = System.currentTimeMillis();
        System.out.println("$ :: The process of Thread for gene name "
                                   + referenceGene.getGeneName() + " ends at " + new Date(endTime).toString()
                                   + " in thread " + threadID);
        System.out.println("############## \t The process of Thread " +
                                   threadID + " for gene name " + referenceGene.getGeneName()
                                   + " ends in : (" + getDuration(startTime, endTime) + ") \t ##############");

        System.out.println("$ : Result of gene name " + referenceGene.getGeneName() + " in thread " + threadID +
                                   " is " + result.toString() + " for protein pairwise Homology score.");

        return result;
    }

    private void calculateProcessTime(ArrayList<ArrayList<String>> result)
    {
        endTime = System.currentTimeMillis();
        System.out.println("$ :: The process of Thread for gene name "
                                   + referenceGene.getGeneName() + " ends at " + new Date(endTime).toString()
                                   + " in thread " + threadID);
        System.out.println("############## \t The process of Thread " +
                                   threadID + " for gene name " + referenceGene.getGeneName()
                                   + " ends in : (" + getDuration(startTime, endTime) + ") \t ##############");

        System.out.println("$ : Result of gene name " + referenceGene.getGeneName() + " in thread " + threadID +
                                   " is " + result.toString() + " for protein pairwise Homology score.");
    }
}
