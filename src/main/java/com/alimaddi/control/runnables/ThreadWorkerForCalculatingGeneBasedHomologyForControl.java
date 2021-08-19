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

public class ThreadWorkerForCalculatingGeneBasedHomologyForControl implements Callable<HashMap<String, ArrayList<Integer>>>
{
    private int speciesID;
    private int numberOfHomologyForMethod1 = 0;
    private int numberOfnonHomologyForMethod1 = 0;
    private int numberOfHomologyForMethod2 = 0;
    private int numberOfnonHomologyForMethod2 = 0;
    private int baseCorrupted = 0;
    private int threadID;
    private long startTime;
    private long endTime;
    private Gene referenceGene;
    private HashSet<Integer> desiredSpeciesID;

    public ThreadWorkerForCalculatingGeneBasedHomologyForControl(
            int threadID, int speciesID, Gene referenceGene, HashSet<Integer> desiredSpeciesID)
    {
        this.speciesID = speciesID;
        this.threadID = threadID;
        this.referenceGene = referenceGene;
        this.desiredSpeciesID = desiredSpeciesID;
    }

    @Override
    public HashMap<String, ArrayList<Integer>> call() throws Exception
    {
        HashMap<String, ArrayList<Integer>> result = new HashMap<>();
        result.put(referenceGene.getGeneStableID(), new ArrayList<>(
                Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));

        startTime = System.currentTimeMillis();
        System.out.println("$ :: The process of calculate gene based homology for control gene "
                                   + referenceGene.getGeneName() + "(" + referenceGene.getGeneStableID() + ")" +
                                   " in thread id " + threadID + " starts at " + new Date(startTime).toString() + ".");

        int genePairwiseCount = 0;
        int transcriptPairwiseCount = 0;

        HashSet<Gene> geneError = new HashSet<>();
        HashSet<String> transcriptsError = new HashSet<>();
        HashSet<Gene> totalGenesPool = new HashSet<>();
        HashMap<Gene, ArrayList<String>> genesClass = new HashMap<>();

        // I think i can remove these two if! because before are checked!
        STRFamily strFamily = new STRFamily(referenceGene.getGeneName(), desiredSpeciesID);
        if (strFamily.getSpeciesOwner().get(speciesID).isEmpty())
            return result;

        HashMap<Integer, ArrayList<String>> calculatedUniqueSTRs = strFamily.calculateUniqueSTRs();

        if (!calculatedUniqueSTRs.get(speciesID).isEmpty())
            return result;

        genesClass.put(referenceGene, new ArrayList<>());

        HashSet<String> referenceTranscriptsStableIDPool = new HashSet<>();

        HashSet<Transcript> candidateTranscriptsStableIDs =
                DatabaseControllerForTranscripts.getAllTranscriptsOfGeneStableIDFromDB(referenceGene.getGeneStableID());
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

        HashMap<String, String> referencePeptidesSequences = new HashMap<>();
        HashMap<String, String> tempPeptidesSequences =
                DatabaseControllerForTranscripts.getAllSequenceOfTranscriptStableIDsFromCloud(
                        new ArrayList<>(referenceTranscriptsStableIDPool),
                        TranscriptSequenceType.PEPTIDE_SEQUENCE, 10, speciesID,
                        true, true, false);
        if (tempPeptidesSequences != null && !tempPeptidesSequences.isEmpty())
        {
            referencePeptidesSequences.putAll(tempPeptidesSequences);
        }
        else
        {
            System.err.println("error occurred during extraction peptides for "
                                       + referenceTranscriptsStableIDPool.toString() + " in thread " + threadID);
            System.err.println("No sequence was extracted" + " in thread " + threadID);
        }

        int index = 0;
        for (Gene gene : genesClass.keySet())
        {
            if (gene.getGeneName() == null || gene.getGeneName().isEmpty())
            {
                geneError.add(gene);
                continue;
            }

            genePairwiseCount++;

            ArrayList<Gene> homologyGenes = DatabaseControllerForGenes.getAllHomologyGene(gene);
            ArrayList<Gene> filteredHomologyGenes =
                    DataAnalysis.filterHomologyGenes(++index, gene, homologyGenes, desiredSpeciesID);

            HashMap<Integer, ArrayList<String>> otherTranscriptsStableIDPool = new HashMap<>();
            for (Gene filteredHomologyGene : filteredHomologyGenes)
            {
                ArrayList<String> transcriptsStableIDs =
                        DatabaseControllerForTranscripts.getAllTranscriptsStableIDOfGeneStableIDFromDB(
                                filteredHomologyGene.getGeneStableID());
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

            HashMap<String, String> otherPeptidesSequences = new HashMap<>();
            for (Integer otherSpeciesID : otherTranscriptsStableIDPool.keySet())
            {
                tempPeptidesSequences =
                        DatabaseControllerForTranscripts.getAllSequenceOfTranscriptStableIDsFromCloud(
                                otherTranscriptsStableIDPool.get(otherSpeciesID),
                                TranscriptSequenceType.PEPTIDE_SEQUENCE, 10,
                                otherSpeciesID,
                                true, true, false);

                if (tempPeptidesSequences != null && !tempPeptidesSequences.isEmpty())
                {
                    otherPeptidesSequences.putAll(tempPeptidesSequences);
                }
                else
                {
                    System.err.println("error occurred during extraction peptides for "
                                               + otherTranscriptsStableIDPool.toString() + " in thread " + threadID);
                    System.err.println("No sequence was extracted" + " in thread " + threadID);
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
                    baseCorrupted++;
                    transcriptsError.add(referenceTranscriptStableID);
                    continue;
                }

                for (String candidateHomologyTranscript : otherPeptidesSequences.keySet())
                {
                    String candidateHomologyTranscriptPeptide = otherPeptidesSequences.get(candidateHomologyTranscript);
                    if (candidateHomologyTranscriptPeptide == null || candidateHomologyTranscriptPeptide.isEmpty())
                    {
                        System.err.println("candidate peptide sequence is corrupted for transcript id = "
                                                   + candidateHomologyTranscript + ". It is null : "
                                                   + (candidateHomologyTranscriptPeptide == null)
                                                   + " in thread " + threadID);

                        continue;
                    }

                    transcriptPairwiseCount++;

                    if (Utilities.isHomology(referenceTranscriptPeptide, candidateHomologyTranscriptPeptide, 1))
                    {
                        numberOfHomologyForMethod1++;
                    }
                    else
                    {
                        numberOfnonHomologyForMethod1++;
                    }
                    if (Utilities.isHomology(referenceTranscriptPeptide, candidateHomologyTranscriptPeptide, 2))
                    {
                        numberOfHomologyForMethod2++;
                    }
                    else
                    {
                        numberOfnonHomologyForMethod2++;
                    }
                }
            }
        }

        result.clear();
        result.put(referenceGene.getGeneStableID(), new ArrayList<>(
                Arrays.asList(numberOfHomologyForMethod1, numberOfnonHomologyForMethod1, baseCorrupted,
                              numberOfHomologyForMethod2, numberOfnonHomologyForMethod2, baseCorrupted,
                              genePairwiseCount, transcriptPairwiseCount, totalGenesPool.size(),
                              geneError.size(), transcriptsError.size())));
        genesClass.clear();
        referenceTranscriptsStableIDPool.clear();

        System.out.println("$ : There are " + geneError.size() + " gene(s) error : ");
        System.out.println(geneError.toString());

        System.out.println("$ : There are " + transcriptsError.size() + " transcript(s) error : ");
        System.out.println(transcriptsError.toString());

        System.out.println("$ : Calculation includes " + genePairwiseCount + " gene pairwise count for gene name " +
                                   referenceGene.getGeneName());
        System.out.println("$ : Calculation includes " + transcriptPairwiseCount + " transcript pairwise count for " +
                                   "gene name " + referenceGene.getGeneName());
        System.out.println("$ : Calculation includes " + totalGenesPool.size() + " genes for gene name " +
                                   referenceGene.getGeneName());

        endTime = System.currentTimeMillis();
        System.out.println("$ :: The process of Thread for gene name "
                                   + referenceGene.getGeneName() + " ends at " + new Date(endTime).toString()
                                   + " in thread " + threadID);
        System.out.println("############## \t The process of Thread " +
                                   threadID + " for gene name " + referenceGene.getGeneName()
                                   + " ends in : (" + getDuration(startTime, endTime) + ") \t ##############");

        System.out.println("$ : Result of gene name " + referenceGene.getGeneName() + " in thread id " + threadID +
                                   " is " + result.toString() + " for Homology and nonHomology Transcript.");

        return result;
    }
}
