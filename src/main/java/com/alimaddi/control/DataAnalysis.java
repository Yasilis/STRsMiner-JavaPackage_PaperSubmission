package com.alimaddi.control;

import com.alimaddi.Utility.Reader;
import com.alimaddi.control.runnables.*;
import com.alimaddi.datacontainer.STRFamily;
import com.alimaddi.datatypes.STROrigin;
import com.alimaddi.datatypes.TranscriptSequenceType;
import com.alimaddi.export.DataSetsStatistics;
import com.alimaddi.model.Gene;
import com.alimaddi.model.Transcript;
import com.alimaddi.statisticaltest.FisherExactTest;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.alimaddi.Utility.Utilities.getDuration;

public class DataAnalysis
{
    private static final long seed = 1608239613080L;

    public static boolean makeTranscriptAnalysisFile(
            boolean geneBased,
            String limitedSTRsSpecificFilePath, String totalSTRsSpecificFilePath,
            int speciesID, int fold, int threadNumber)
            throws Exception
    {
        int tempRowCounter;
        String moreInfo;
        ArrayList<Integer> activeSpecies;
        ArrayList<ArrayList<String>> limitedFingerPrintFile;
        ArrayList<ArrayList<String>> totalFingerPrintFile;
        ArrayList<String> limitedSpeciesSpecificSTRProducerTranscriptsStableIDs;
        ArrayList<String> totalSpeciesSpecificSTRProducerTranscriptsStableIDs;
        ArrayList<String> allBiologicalSTRs;
        ArrayList<String> excludedTranscriptsStableIDsInDB = new ArrayList<>();
        ArrayList<String> controlTranscript;
        ArrayList<Integer>randomIndices;
        HashSet<String> limitedSpeciesSpecificTranscriptsPool;
        HashSet<String> totalSpeciesSpecificTranscriptsPool;
        HashSet<String> totalTranscriptsPool;
        HashMap<String, String> geneExonsSequences;
        HashMap<String, String> cdnasSequences;
        HashMap<String, String> codingsSequences;
        HashMap<String, String> peptidesSequences;
        HashMap<String, String> promoterSequences = new HashMap<>();


        limitedFingerPrintFile = Reader.readFingerPrintFile(limitedSTRsSpecificFilePath);
        totalFingerPrintFile = Reader.readFingerPrintFile(totalSTRsSpecificFilePath);

        limitedSpeciesSpecificSTRProducerTranscriptsStableIDs =
                getTranscriptsPool(speciesID, limitedFingerPrintFile, geneBased);
        totalSpeciesSpecificSTRProducerTranscriptsStableIDs =
                getTranscriptsPool(speciesID, totalFingerPrintFile, geneBased);

        if (limitedSpeciesSpecificSTRProducerTranscriptsStableIDs == null)
            return false;

        allBiologicalSTRs = getAllBiologicalSTRs(speciesID);

        System.out.println("$ : The species ID " + speciesID + " has " +
                                   allBiologicalSTRs.size() + " biological STRs type.");

        ArrayList<String> totalSTRProducerTranscriptsStableIDs
                = DatabaseControllerForTranscripts.getAllSTRProducerTranscriptsStableIDOfSpecies(speciesID, allBiologicalSTRs);

        limitedSpeciesSpecificTranscriptsPool = new HashSet<>(limitedSpeciesSpecificSTRProducerTranscriptsStableIDs);
        totalSpeciesSpecificTranscriptsPool = new HashSet<>(totalSpeciesSpecificSTRProducerTranscriptsStableIDs);
        totalTranscriptsPool = new HashSet<>(totalSTRProducerTranscriptsStableIDs);
        System.out.println("\n$ : All producer transcripts of species ID " + speciesID + " was extracted. ");
        System.out.println("$ : There are " + limitedSpeciesSpecificTranscriptsPool.size()
                                   + " limited STR specific producer Transcripts which have been found of "
                                   + getSTRsPool(speciesID, limitedFingerPrintFile).size() + " STRs.");
        System.out.println("$ : There are " + totalSpeciesSpecificTranscriptsPool.size()
                                   + " total STR specific producer Transcripts which have been found of "
                                   + getSTRsPool(speciesID, totalFingerPrintFile).size() + " STRs.");
        System.out.println("$ : There are " + totalTranscriptsPool.size()
                                   + " total STR producer Transcripts which have been found of "
                                   + allBiologicalSTRs.size() + " biological STRs.");

        HashSet <Transcript> totalTranscriptsOfSpecies
                = DatabaseControllerForTranscripts.getAllTranscriptsOfSpeciesIDFromDB(speciesID); //TODO : 7 => speciesID
        System.out.println("$ : There are totally " + totalTranscriptsOfSpecies.size()
                                   + " Transcripts in DB for species ID " + speciesID + ".");

        excludedTranscriptsStableIDsInDB = new ArrayList<>();
        for (Transcript transcript : totalTranscriptsOfSpecies)
            excludedTranscriptsStableIDsInDB.add(transcript.getTranscriptStableID());
        excludedTranscriptsStableIDsInDB.removeAll(totalTranscriptsPool);
        System.out.println("$ : There are totally " + excludedTranscriptsStableIDsInDB.size()
                                   + " Transcripts remain in DB for species ID " + speciesID
                                   + " which are not STR producer.");
        System.out.println();
        System.out.println();
        System.out.println("################################################################################");
        System.out.println("$ : The list of all Transcripts for species ID " + speciesID
                                   + " which are not STR producer.");
        for (String id : excludedTranscriptsStableIDsInDB)
            System.out.println(id);
        System.out.println("################################################################################");
        System.out.println();
        System.out.println();

        //region Analyse Part 1
//        System.out.println("################################################################################");
//        geneExonsSequences = DatabaseControllerForTranscripts.getAllSequenceOfTranscriptStableIDsFromCloud(
//                limitedSpeciesSpecificSTRProducerTranscriptsStableIDs, TranscriptSequenceType.EXON_SEQUENCE, 20);
//        cdnasSequences = DatabaseControllerForTranscripts.getAllSequenceOfTranscriptStableIDsFromCloud(
//                limitedSpeciesSpecificSTRProducerTranscriptsStableIDs, TranscriptSequenceType.CDNA_SEQUENCE, 20);
//        codingsSequences = DatabaseControllerForTranscripts.getAllSequenceOfTranscriptStableIDsFromCloud(
//                limitedSpeciesSpecificSTRProducerTranscriptsStableIDs, TranscriptSequenceType.CODING_SEQUENCE, 20);
//        peptidesSequences = DatabaseControllerForTranscripts.getAllSequenceOfTranscriptStableIDsFromCloud(
//                limitedSpeciesSpecificSTRProducerTranscriptsStableIDs, TranscriptSequenceType.PEPTIDE_SEQUENCE, 20);
//        for (String transcriptsStableID : limitedSpeciesSpecificSTRProducerTranscriptsStableIDs)
//            promoterSequences.put(transcriptsStableID, DatabaseControllerForTranscripts
//                    .getPromoterOfTranscriptsStableID(transcriptsStableID, STROrigin.CDS_START_CODON));
//
//        System.out.println("\n====================================");
////        if (geneExonsSequences != null && cdnasSequences != null &&
////                codingsSequences != null && peptidesSequences != null)// &&
//////                geneExonsSequences.size() == cdnasSequences.size() &&
//////                geneExonsSequences.size() == codingsSequences.size() &&
//////                geneExonsSequences.size() == peptidesSequences.size() &&
//////                geneExonsSequences.size() == promoterSequences.size())
////        {
//            System.out.println("size of geneExonsSequences = " + geneExonsSequences.size());
//            System.out.println("size of cdnasSequences = " + cdnasSequences.size());
//            System.out.println("size of codingsSequences = " + codingsSequences.size());
//            System.out.println("size of peptidesSequences = " + peptidesSequences.size());
//            System.out.println("------------------------------------------");
//
//            tempRowCounter = 0;
//            for (String transcript : peptidesSequences.keySet())
//            {
//                System.out.println("$ : Transcript number : " + (++tempRowCounter));
//                System.out.println("$ : Transcript ID : " + transcript);
//                System.out.println("$ : " + geneExonsSequences.get(transcript));
//                System.out.println("$ : " + cdnasSequences.get(transcript));
//                System.out.println("$ : " + codingsSequences.get(transcript));
//                System.out.println("$ : " + peptidesSequences.get(transcript));
//                System.out.println("------------------------------------------");
//            }
//
//            int nonMet = 0;
//            for (String peptide : peptidesSequences.values())
//            {
//                if (!peptide.isEmpty() && peptide.charAt(0) != 'M')
//                    nonMet++;
//            }
//            System.out.println(String.format("\n\n$ : Percentage of non-M peptides : %.2f%%   (%d/%d)"
//                    , ((float)nonMet/peptidesSequences.size()) * 100, nonMet, peptidesSequences.size()));
//
//            geneExonsSequences.clear();
//            cdnasSequences.clear();
//            codingsSequences.clear();
//            peptidesSequences.clear();
////        }
////        else
////        {
////            System.err.println("\n$ : sequences length is not as same as each other in analysis part 1!");
////        }
//        System.out.println("################################################################################");
//        System.out.println();
//        System.out.println();
        //endregion

        //region Analyse Part 2
//        System.out.println();
//        System.out.println();
//        System.out.println("################################################################################");
//        tempRowCounter = 0;
//        for (String transcript : promoterSequences.keySet())
//        {
//            System.out.println("$ : Transcript number : " + (++tempRowCounter));
//            System.out.println("$ : Transcript ID : " + transcript);
//            System.out.println("$ : Promoter Sequences : " + promoterSequences.get(transcript));
//            ArrayList<String> uniqueSTRs = getSTRsPool(speciesID, limitedFingerPrintFile);
//            ArrayList<String> allSTRs = new ArrayList<>();
//
//            for (STR str : DatabaseControllerForSTRs.getAllSTRsOfTranscriptStableIDFromDB(transcript))
//                allSTRs.add(str.getSequence());
//
//            uniqueSTRs.retainAll(filter(allSTRs, promoterSequences.get(transcript)));
//
//            int i = 0;
//            for (String str : uniqueSTRs)
//                System.out.println("$ : " + (++i) +  " : " + str);
//
//            System.out.println("----------------------------------------------------------------------------");
//            uniqueSTRs.clear();
//            allSTRs.clear();
//        }
//        System.out.println("################################################################################");
//        System.out.println();
//        System.out.println();
//        promoterSequences.clear();
        //endregion

        //region analysis part 3
//        if (!excludedTranscriptsStableIDsInDB.isEmpty())
//        {
//            geneExonsSequences = DatabaseControllerForTranscripts.getAllSequenceOfTranscriptStableIDsFromCloud(
//                    excludedTranscriptsStableIDsInDB, TranscriptSequenceType.EXON_SEQUENCE, 20);
//            cdnasSequences = DatabaseControllerForTranscripts.getAllSequenceOfTranscriptStableIDsFromCloud(
//                    excludedTranscriptsStableIDsInDB, TranscriptSequenceType.CDNA_SEQUENCE, 20);
//            codingsSequences = DatabaseControllerForTranscripts.getAllSequenceOfTranscriptStableIDsFromCloud(
//                    excludedTranscriptsStableIDsInDB, TranscriptSequenceType.CODING_SEQUENCE, 20);
//            peptidesSequences = DatabaseControllerForTranscripts.getAllSequenceOfTranscriptStableIDsFromCloud(
//                    excludedTranscriptsStableIDsInDB, TranscriptSequenceType.PEPTIDE_SEQUENCE, 20);
//            for (String transcriptsStableID : excludedTranscriptsStableIDsInDB)
//                promoterSequences.put(transcriptsStableID, DatabaseControllerForTranscripts
//                        .getPromoterOfTranscriptsStableID(transcriptsStableID, STROrigin.CDS_START_CODON));
//
//            System.out.println("\n====================================");
//            if (geneExonsSequences != null && cdnasSequences != null &&
//                    codingsSequences != null && peptidesSequences != null) //&&
////                    geneExonsSequences.size() == cdnasSequences.size() &&
////                    geneExonsSequences.size() == codingsSequences.size() &&
////                    geneExonsSequences.size() == peptidesSequences.size() &&
////                    geneExonsSequences.size() == promoterSequences.size())
//            {
//                for (String transcript : peptidesSequences.keySet())
//                {
//                    System.out.println("$ : " + transcript);
//                    System.out.println("$ : " + geneExonsSequences.get(transcript));
//                    System.out.println("$ : " + cdnasSequences.get(transcript));
//                    System.out.println("$ : " + codingsSequences.get(transcript));
//                    System.out.println("$ : " + peptidesSequences.get(transcript));
//                    System.out.println("------------------------------------------");
//                }
//
//                int nonMet = 0;
//                for (String peptide : peptidesSequences.values())
//                {
//                    if (!peptide.isEmpty() && peptide.charAt(0) != 'M')
//                        nonMet++;
//                }
//                System.out.println(String.format("\n\n$ : Percentage of non-M peptides : %.2f%%   (%d/%d)"
//                        , ((float)nonMet/peptidesSequences.size()) * 100, nonMet, peptidesSequences.size()));
//
//                geneExonsSequences.clear();
//                cdnasSequences.clear();
//                codingsSequences.clear();
//                peptidesSequences.clear();
//                promoterSequences.clear();
//            }
//            else
//            {
//                System.err.println("\n$ : sequences length is not as same as each other in analysis part 3!");
//            }
//        }
        //endregion

        System.out.println();
        System.out.println();
        System.out.println("###################################################################################");
        System.out.println("#############################   Start Gene Homology   #############################");
        System.out.println("###################################################################################");

        ArrayList<Integer> round0;
        ArrayList<ArrayList<Integer>> resultOfControls = new ArrayList<>();

        if (geneBased)
        {
            HashSet<Integer> desiredSpeciesID = new HashSet<>(DataSetsStatistics.getActiveSpecies(totalFingerPrintFile));
            ArrayList<String> strsClass = getSTRsPool(speciesID, limitedFingerPrintFile);
            ArrayList<String> totalSTRsClass = getSTRsPool(speciesID, totalFingerPrintFile);
//            HashSet<Gene> totalGenesPool = new HashSet<>();

            System.out.println();
            System.out.println("$ : There are " + desiredSpeciesID.size() + " desired species IDs.");
            System.out.println();

            ArrayList<Gene> limitedSpeciesSpecificSTRProducerGenes = new ArrayList<>();
            ArrayList<Gene> candidateLimitedSpeciesSpecificSTRProducerGenes =
                    DatabaseControllerForGenes.getAllSTRProducerGenes(speciesID, strsClass);
            for (Gene gene : candidateLimitedSpeciesSpecificSTRProducerGenes)
            {
                STRFamily strFamily = new STRFamily(gene.getGeneName(), desiredSpeciesID);
                HashMap<Integer, ArrayList<String>> calculatedUniqueSTRs = strFamily.calculateUniqueSTRs();

                if (!calculatedUniqueSTRs.get(speciesID).isEmpty())
                    limitedSpeciesSpecificSTRProducerGenes.add(gene);
            }

            System.out.println();
            System.out.println("$ : There are " + limitedSpeciesSpecificSTRProducerGenes.size() +
                                       " limited species specific STR producer genes.");
            System.out.println();

            round0 = calculateGeneBasedHomologyForCase(
                    limitedSpeciesSpecificSTRProducerGenes, threadNumber, speciesID, desiredSpeciesID);

            HashSet<Gene> speciesSpecificSTRProducerGenes = new HashSet<>();
            ArrayList<Gene> candidateSpeciesSpecificSTRProducerGenes =
                    DatabaseControllerForGenes.getAllSTRProducerGenes(speciesID, totalSTRsClass);
            for (Gene gene : candidateSpeciesSpecificSTRProducerGenes)
            {
                STRFamily strFamily = new STRFamily(gene.getGeneName(), desiredSpeciesID);
                HashMap<Integer, ArrayList<String>> calculatedUniqueSTRs = strFamily.calculateUniqueSTRs();

                if (!calculatedUniqueSTRs.get(speciesID).isEmpty())
                    speciesSpecificSTRProducerGenes.add(gene);
            }

            int speciesSpecificSTRProducerGenesCount = speciesSpecificSTRProducerGenes.size();
            int round0GeneCount = round0.get(6);

            System.out.println("$ : ---------------- validation start -----------------");
            System.out.println("$ : There are " + speciesSpecificSTRProducerGenesCount +
                                       " species specific STR Producer Genes ");
            System.out.println("$ : Genes poll size in round0 has " + round0GeneCount + " genes.");
            System.out.println("$ : ----------------- validation end ------------------");

            System.out.println("$ : ------------------------------------");
            System.out.println("$ : There are " + speciesSpecificSTRProducerGenesCount +
                                       " species specific STR Producer Genes : ");
            for (Gene gene : speciesSpecificSTRProducerGenes)
                System.out.println("$ : " + gene.getGeneStableID() + "\t | \t" + gene.getGeneName());
            System.out.println("$ : ------------------------------------");

            ArrayList<String> allTypeOfSTRs = DatabaseControllerForSTRs.getAllTypeOfSTRsInDB(speciesID);
            ArrayList<Gene> nonSpeciesSpecificSTRProducerGenes =
                    DatabaseControllerForGenes.getAllSTRProducerGenes(speciesID, allTypeOfSTRs);
            System.out.println("$ : There are totally " + nonSpeciesSpecificSTRProducerGenes.size() +
                                       " STR Producer Genes in species ID " + speciesID + ".");
            nonSpeciesSpecificSTRProducerGenes.removeAll(speciesSpecificSTRProducerGenes);
            System.out.println("$ : There are totally " + nonSpeciesSpecificSTRProducerGenes.size() +
                                       " non species specific STR Producer Genes in species ID " + speciesID + ".");


            // ------------------------------- start calculating control result ----------------------------------------
            Collections.shuffle(nonSpeciesSpecificSTRProducerGenes);
            Random randomGenerator = new Random(seed);
            for (int i = 0 ; i < fold ; i++)
            {

                long startTime = System.currentTimeMillis();
                System.out.println("$ : Control Round " + (i + 1) + " start at " + new Date(startTime).toString());

                ArrayList<Integer> randomGeneIndices = getRandomVector(nonSpeciesSpecificSTRProducerGenes.size(),
                                                round0GeneCount,
                                                randomGenerator);
                System.out.println("$ : random indices of Control Round " + (i + 1) + " : " +
                                           randomGeneIndices.toString());

                ArrayList<Gene> referenceGeneForControlRounds = new ArrayList<>();
                for (Integer index : randomGeneIndices)
                    referenceGeneForControlRounds.add(nonSpeciesSpecificSTRProducerGenes.get(index));

                if (i != 8 && i != 9 && i != 10)
                {
                    resultOfControls
                            .add(calculateGeneBasedHomologyForControl(new ArrayList<>(), threadNumber,
                                                                      speciesID, desiredSpeciesID));
                }
                else
                {
                    resultOfControls
                            .add(calculateGeneBasedHomologyForControl(referenceGeneForControlRounds, threadNumber,
                                                                      speciesID, desiredSpeciesID));
                }

                randomGeneIndices.clear();
                referenceGeneForControlRounds.clear();

                long endTime = System.currentTimeMillis();
                System.out.println("$ : Control Round " + (i + 1) + " end at " + new Date(endTime).toString());
                System.out.println("$ : Control Round " + (i + 1) + " for " + getDuration(startTime, endTime));
            }

            System.out.println("#####################################################");
            System.out.println("#####################################################");
        }
        else
        {
            ArrayList<Gene> genePool =
                    DatabaseControllerForTranscripts.getProducerGenes(limitedSpeciesSpecificSTRProducerTranscriptsStableIDs);

            System.out.println("$ : There are " + genePool.size() + " genes which the limited specific STR producer " +
                                       "Transcripts belong them.\n");

            ArrayList<Integer> filteredSpeciesID = DataSetsStatistics.getActiveSpecies(totalFingerPrintFile);

            HashMap<String, String> peptidesSequencesOfBaseTranscripts =
                    DatabaseControllerForTranscripts.getAllSequenceOfTranscriptStableIDsFromCloud(
                            limitedSpeciesSpecificSTRProducerTranscriptsStableIDs,
                            TranscriptSequenceType.PEPTIDE_SEQUENCE, 10);

            System.out.println();
            System.out.println();
            System.out.println("$ : peptides Sequences Of Base Transcripts :");
            System.out.println(peptidesSequencesOfBaseTranscripts.toString());
            System.out.println();
            System.out.println();

            tempRowCounter = 0;
            for (Gene gene : genePool)
            {
                if (gene.getSpecies().getId() != speciesID)
                {
                    System.out.println("$ : gene pool is not pure! gene id " + gene
                            .getSpecies().getId() + " is in the gene pool of " + speciesID + "!");
                    break;
                }

                if (gene.getGeneName() == null || gene.getGeneName().isEmpty())
                    continue;

                ArrayList<Gene> homologyGenes = DatabaseControllerForGenes.getAllHomologyGene(gene);
                ArrayList<Gene> filteredhomologyGenes =
                        filterHomologyGenes(++tempRowCounter, gene, homologyGenes, new HashSet<>(filteredSpeciesID));
            }

            ///////////////////////////////////////
           int duplicateTranscript = 0;
            HashSet<String> promotersType = new HashSet<>();
            for (String id : excludedTranscriptsStableIDsInDB)
            {
                promotersType.add(DatabaseControllerForTranscripts.getPromoterOfTranscriptsStableID(
                        id, STROrigin.CDS_START_CODON));
            }
            System.out.println();
            System.out.println("-----------------------------------------------------------------------------------------");
            System.out.println("excluded size = " + excludedTranscriptsStableIDsInDB.size());
            System.out.println("unique size = " + promotersType.size());
            System.out.println("duplicated size = " + (excludedTranscriptsStableIDsInDB.size() - promotersType.size()));
            System.out.println("-----------------------------------------------------------------------------------------");
            ///////////////////////////////////////

//            HashMap<String, ArrayList<String>> dublicationTranscripts = calculateDuplicationAnalysis(
//                    limitedSpeciesSpecificSTRProducerTranscriptsStableIDs);

            round0 = calculateHomology(limitedSpeciesSpecificSTRProducerTranscriptsStableIDs,
                                                          threadNumber, speciesID,
                                                      filteredSpeciesID, peptidesSequencesOfBaseTranscripts);

            Random randomGenerator = new Random(seed);
            controlTranscript = new ArrayList<>();
            for (int i = 0 ; i < fold ; i++)
            {
                long startTime = System.currentTimeMillis();
                System.out.println("$ : Control Round " + (i + 1) + " start at " + new Date(startTime).toString());

                randomIndices = getRandomVector(excludedTranscriptsStableIDsInDB.size(),
                                                  limitedSpeciesSpecificSTRProducerTranscriptsStableIDs.size(),
                                                  randomGenerator);
                System.out.println("$ : random indices of Control Round " + (i + 1) + " : " + randomIndices.toString());

                ///////////////////////////////////////////

                promotersType.clear();
                for (Integer randomIndex : randomIndices)
                {
                    String promoterTemp = DatabaseControllerForTranscripts.getPromoterOfTranscriptsStableID(
                            excludedTranscriptsStableIDsInDB.get(randomIndex), STROrigin.CDS_START_CODON);
                    if (promotersType.contains(promoterTemp))
                        duplicateTranscript++;
                    else
                        promotersType.add(promoterTemp);
                }
                System.out.println("Number of duplicated control Transcript = " + duplicateTranscript);

                duplicateTranscript = 0;
                for (int j = 0; j < randomIndices.size() - 1; j+=2)
                {
                    String promoterTemp1 = DatabaseControllerForTranscripts.getPromoterOfTranscriptsStableID(
                            excludedTranscriptsStableIDsInDB.get(randomIndices.get(j)), STROrigin.CDS_START_CODON);
                    String promoterTemp2 = DatabaseControllerForTranscripts.getPromoterOfTranscriptsStableID(
                            excludedTranscriptsStableIDsInDB.get(randomIndices.get(j + 1)), STROrigin.CDS_START_CODON);
                    if (promoterTemp1.equals(promoterTemp2))
                        duplicateTranscript++;
                }
                System.out.println("Number of paired duplicated control Transcript = " + duplicateTranscript);
                ///////////////////////////////////////////

                for (Integer index : randomIndices)
                {
                    controlTranscript.add(excludedTranscriptsStableIDsInDB.get(index));
                }

                HashMap<String, String> peptidesSequencesOfControlTranscripts =
                        DatabaseControllerForTranscripts.getAllSequenceOfTranscriptStableIDsFromCloud(
                                controlTranscript,
                                TranscriptSequenceType.PEPTIDE_SEQUENCE, 10);


                resultOfControls.add(calculateHomology(controlTranscript, threadNumber, speciesID,
                                                          filteredSpeciesID, peptidesSequencesOfControlTranscripts));

                randomIndices.clear();
                controlTranscript.clear();
                peptidesSequencesOfControlTranscripts.clear();

                long endTime = System.currentTimeMillis();
                System.out.println("$ : Control Round " + (i + 1) + " end at " + new Date(endTime).toString());
                System.out.println("$ : Control Round " + (i + 1) + " for " + getDuration(startTime, endTime));
            }

            System.out.println("#####################################################");
            System.out.println("#####################################################");
        }

        int caseMaxEvents = 0;
        for (ArrayList<Integer> resultOfControl : resultOfControls)
            caseMaxEvents = Math.max(
                    caseMaxEvents, Math.max(
                            resultOfControl.get(0) + resultOfControl.get(1),
                            resultOfControl.get(3) + resultOfControl.get(4)));

        FisherExactTest fisherExactTest = new FisherExactTest(
                Math.max(round0.get(0) + round0.get(1), round0.get(3) + round0.get(4)) + caseMaxEvents);

        System.out.println("$ : Case\t = \t" + round0.toString());

        ArrayList<Float> meanOfControl = new ArrayList<>();
        meanOfControl.add(0f);
        meanOfControl.add(0f);
        meanOfControl.add(0f);
        meanOfControl.add(0f);
        meanOfControl.add(0f);
        meanOfControl.add(0f);
        for (int i = 0; i < fold; i++)
        {
            double p_value1 = fisherExactTest.getP(round0.get(0), round0.get(1),
                                                   resultOfControls.get(i).get(0), resultOfControls.get(i).get(1));
            double p_value2 = fisherExactTest.getP(round0.get(3), round0.get(4),
                                                   resultOfControls.get(i).get(3), resultOfControls.get(i).get(4));

//            System.out.println("$ : Control " + (i + 1) + "\t = \t" + resultOfControls.get(i).toString() +
//                                       "\t | \tP-value(1) = " +
//                                       (new DecimalFormat("#.#######").format(p_value1)) +
//                                       " \t\t\t\t\t p-value(2) =  " +
//                                       (new DecimalFormat("#.#######").format(p_value2)));

            System.out.println("$ : Control " + (i + 1) + "\t = \t" + resultOfControls.get(i).toString() +
                                       "\t | \tP-value(1) = " + p_value1 +
                                       " \t\t\t\t\t p-value(2) =  " + p_value2);

            meanOfControl.set(0, meanOfControl.get(0) + resultOfControls.get(i).get(0));
            meanOfControl.set(1, meanOfControl.get(1) + resultOfControls.get(i).get(1));
            meanOfControl.set(2, meanOfControl.get(2) + resultOfControls.get(i).get(2));
            meanOfControl.set(3, meanOfControl.get(3) + resultOfControls.get(i).get(3));
            meanOfControl.set(4, meanOfControl.get(4) + resultOfControls.get(i).get(4));
            meanOfControl.set(5, meanOfControl.get(5) + resultOfControls.get(i).get(5));
        }
        meanOfControl.set(0, meanOfControl.get(0) / fold);
        meanOfControl.set(1, meanOfControl.get(1) / fold);
        meanOfControl.set(2, meanOfControl.get(2) / fold);
        meanOfControl.set(3, meanOfControl.get(3) / fold);
        meanOfControl.set(4, meanOfControl.get(4) / fold);
        meanOfControl.set(5, meanOfControl.get(5) / fold);

        double p_value1 = fisherExactTest.getP(round0.get(0), round0.get(1),
                                               Math.round(meanOfControl.get(0)), Math.round(meanOfControl.get(1)));
        double p_value2 = fisherExactTest.getP(round0.get(3), round0.get(4),
                                               Math.round(meanOfControl.get(3)), Math.round(meanOfControl.get(4)));

//        System.out.println("$ : Total ctrl\t = \t" + meanOfControl.toString() + "\t | \t" +
//                                   "P-value(1) = " + (new DecimalFormat("#.#######").format(p_value1)) +
//                                   " \t p-value(2) =  " +  (new DecimalFormat("#.#######").format(p_value2)));

        System.out.println("$ : Total ctrl\t = \t" + meanOfControl.toString() + "\t | \t" +
                                   "P-value(1) = " + p_value1 +
                                   " \t p-value(2) =  " +  p_value2);

        System.out.println();


        System.out.println("#####################################################");
        System.out.println("#####################################################");
        System.out.println();
//        boolean haveHomolog;
//        int processedTranscript = 0;
//        int numberOfHomolog = 0;
//        int numberOfnonHomolog = 0;
//        for (String transcriptsStableID : limitedSpeciesSpecificSTRProducerTranscriptsStableIDs)
//        {
//            ArrayList<Gene> gene = DatabaseControllerForTranscripts.getProducerGenes(new ArrayList<>(
//                    Collections.singletonList(transcriptsStableID)));
//
//            if (gene.size() != 1)
//            {
//                System.out.println("$ : There is a duplicate gene or there is not any gene for "
//                                           + transcriptsStableID + " !");
//                break;
//            }
//
//            System.out.println("$ : " + (processedTranscript++) + "/" +
//                                       limitedSpeciesSpecificSTRProducerTranscriptsStableIDs.size() +
//                                       " process start fot transcript id = " + transcriptsStableID
//                                       + " and gene id = " + gene.get(0).getGeneStableID() + " !");
//
//
//            ArrayList<Gene> homologyGenes = DatabaseControllerForGenes.getAllHomologyGene(gene.get(0));
//            ArrayList<Gene> filteredHomologyGenes = new ArrayList<>();
//            for (Gene homologyGene : homologyGenes)
//            {
//                if (filteredSpeciesID.contains(homologyGene.getSpeciesId()) && homologyGene.getSpeciesId() != speciesID)
//                    filteredHomologyGenes.add(homologyGene);
//            }
//
//            HashMap<String, String> totalPeptideSequences = new HashMap<>();
////            ArrayList<String> candidateHomologyTranscripts = new ArrayList<>();
//            for (Gene filteredHomologyGene : filteredHomologyGenes)
//            {
//                ArrayList<String> tempTranscriptIDs = DatabaseControllerForTranscripts
//                        .getAllTranscriptsStableIDOfGeneStableIDFromDB(filteredHomologyGene.getGeneStableID());
////                candidateHomologyTranscripts.addAll(tempTranscriptIDs);
//
//                if (!tempTranscriptIDs.isEmpty())
//                {
//                    HashMap<String, String> tempPeptidesSequencesOfTranscripts =
//                            DatabaseControllerForTranscripts.getAllSequenceOfTranscriptStableIDsFromCloud(
//                                    tempTranscriptIDs, TranscriptSequenceType.PEPTIDE_SEQUENCE, 10,
//                                    filteredHomologyGene.getSpeciesId(), false, false);
//
//                    if (tempPeptidesSequencesOfTranscripts != null && !tempPeptidesSequencesOfTranscripts.isEmpty())
//                    {
//                        totalPeptideSequences.putAll(tempPeptidesSequencesOfTranscripts);
//                    }
//                    else
//                    {
//                        System.err.println("error occurred during extraction peptides for "
//                                                   + tempTranscriptIDs.toString());
//                        System.err.println("No sequence was extracted");
//                    }
//                }
//                else
//                {
//                    System.err.println("error occurred during extraction Transcript IDs for "
//                                               + filteredHomologyGene.getGeneStableID());
//                    System.err.println("No ID was extracted");
//                }
//            }
//
//            String baseTranscriptPeptide = peptidesSequencesOfBaseTranscripts.get(transcriptsStableID);
//
//            haveHomolog = false;
//            for (String candidateHomologyTranscript : totalPeptideSequences.keySet())
//            {
//                String candidateHomologyTranscriptPeptide = totalPeptideSequences.get(candidateHomologyTranscript);
//                if (isHomolog(baseTranscriptPeptide, candidateHomologyTranscriptPeptide))
//                {
//                    haveHomolog = true;
//                    break;
//                }
//            }
//
//            if (haveHomolog)
//                numberOfHomolog++;
//            else
//                numberOfnonHomolog++;
//
//
//
//
//
//
//
//        }

        System.out.println("###################################################################################");
        System.out.println("#############################  end of Gene Homology  ##############################");
        System.out.println("###################################################################################");





        return true;
    }

    public static ArrayList<Gene> filterHomologyGenes(
            int index, Gene referenceGene, ArrayList<Gene> primaryHomologyGenes, HashSet<Integer> desiredSpeciesID)
    {
        ArrayList<Gene> filteredHomologyGenes = new ArrayList<>();

        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("$ : Item " + index);
        System.out.println("$ : Gene ID : " + referenceGene.getGeneStableID());
        System.out.println("$ : Gene Name : " + referenceGene.getGeneName());
        System.out.println("$ : Gene species : " + referenceGene.getSpecies().getId());

        for (Gene homologyGene : primaryHomologyGenes)
        {
            if (desiredSpeciesID.contains(homologyGene.getSpecies().getId()) &&
                    homologyGene.getSpecies().getId() != referenceGene.getSpecies().getId())
                filteredHomologyGenes.add(homologyGene);
        }

        System.out.println("$ : There are " + filteredHomologyGenes.size()
                                   + " gene homologies in desired species");

        System.out.print("$ : [");
        for (Gene filteredHomologyGene : filteredHomologyGenes)
        {
            System.out.print(filteredHomologyGene.getSpecies().getId() + "-");
        }
        System.out.print("]");
        System.out.println();
        System.out.println("-----------------------------------------------------------------------------------");

        return filteredHomologyGenes;
    }

    private static ArrayList<String> getTranscriptsPool(
            int speciesID, ArrayList<ArrayList<String>> limitedFingerPrintFile, boolean geneBased)
    {
        ArrayList<String> strs = getSTRsPool(speciesID, limitedFingerPrintFile);

        if (strs.size() == 0)
        {
            System.err.println("\n$ : Error occurred! Either species ID " + speciesID + " is not valid, desired or " +
                                       "there is not in our DataBase!");
            return null;
        }

        if (geneBased)
        {
            System.out.println("\n$ : All unique gene based STRs of species ID " + speciesID + " was extracted. ");
            System.out.println("$ : There are " + strs.size() + " genes based STRs have been found.\n");
        }
        else
        {
            System.out.println("\n$ : All unique STRs of species ID " + speciesID + " was extracted. ");
            System.out.println("$ : There are " + strs.size() + " STRs have been found.\n");
        }

//        ArrayList<String> transcriptsStableIDs = DatabaseControllerForSTRs.getProducerTranscripts(speciesID, strs);
        //TODO : check equality of upper and down line !!!
        return DatabaseControllerForTranscripts.getAllSTRProducerTranscriptsStableIDOfSpecies(speciesID, strs);
    }

    private static ArrayList<String> getSTRsPool(int speciesID, ArrayList<ArrayList<String>> fingerPrintFile)
    {
        ArrayList<String> strs = null;
        for (ArrayList<String> row : fingerPrintFile)
        {
            if (Integer.parseInt(row.get(0)) == speciesID)
            {
                strs = new ArrayList<>(row.subList(2, row.size()));
                break;
            }
        }


        return (strs == null ? new ArrayList<>() : strs);
    }

//    private static HashSet<String> filter(ArrayList<String> listOfSTRs, String promoterSequences)
//    {
//        HashSet<String> listOfDistinctSTRsSeq = new HashSet<>();
//        HashSet<STR> candidateSTRs = new HashSet<>();
//        HashSet<STR> candidateForDelete = new HashSet<>();
//
//        for (String str : listOfSTRs)
//        {
//            String promoter = promoterSequences;
//            String[] output = Utilities.decomposeSTR(str);
//            String core = output[0];
//
//            int coreStart;
//            int coreEnd;
//            int repeatStart;
//            int repeatEnd;
//            int localRepeat;
//
//            while (!promoter.isEmpty())
//            {
//                localRepeat = 0;
//                coreStart = promoter.indexOf(core);
//                coreEnd = coreStart + core.length();
//                repeatStart = coreEnd;
//                repeatEnd = repeatStart + core.length();
//
//                if (coreStart == -1 || repeatEnd > promoter.length())
//                    break;
//                localRepeat++;
//
//                while (repeatEnd <= promoter.length() && promoter.substring(coreStart, coreEnd)
//                        .equals(promoter.substring(repeatStart, repeatEnd)))
//                {
//                    localRepeat++;
//                    repeatStart = repeatEnd;
//                    repeatEnd += core.length();
//                }
//
//                if (localRepeat >= 2 &&
//                        !(core.length() == 1 && localRepeat < 6) &&
//                        !(core.length() >= 2 && core.length() < 10 && localRepeat < 3)
//                )
//                {
//                    STR strTemp = new STR(STROrigin.CDS_START_CODON,
//                                          packSTR(core, localRepeat),
//                                          (short) 1,
//                                          "0000000");
//                    candidateSTRs.add(strTemp);
//                }
//
//                promoter = promoter.substring(Math.min(promoter.length(), repeatEnd - core.length()));
//            }
//        }
//
//        for (STR coreSTR : candidateSTRs)
//        {
//            for (STR candidateSTR : candidateSTRs)
//            {
//                if (isCoreSTR(coreSTR.getSequence(), candidateSTR.getSequence()))
//                {
//                    candidateForDelete.add(candidateSTR);
//                }
//                else if (coreSTR.getTranscriptStableId().equals(candidateSTR.getTranscriptStableId()) &&
//                        !coreSTR.getSequence().equals(candidateSTR.getSequence()))
//                {
//                    String[] output;
//
//                    output = Utilities.decomposeSTR(coreSTR.getSequence());
//                    String coreOfCore = output[0];
//                    int repeatOfCore = Integer.parseInt(output[1]);
//                    String extendCoreSTR =
//                            String.join("", Collections.nCopies(repeatOfCore, coreOfCore));
//
//                    output = Utilities.decomposeSTR(candidateSTR.getSequence());
//                    String coreOfCandidate = output[0];
//                    int repeatOfCandidate = Integer.parseInt(output[1]);
//                    String extendCandidateSTR =
//                            String.join("", Collections.nCopies(repeatOfCandidate, coreOfCandidate));
//
//                    String extendedCoreOfCore =
//                            String.join("", Collections.nCopies(
//                                    coreOfCandidate.length() / coreOfCore.length(), coreOfCore));
//
//                    if (coreOfCandidate.length() % coreOfCore.length() == 0 &&
//                            coreOfCandidate.equals(extendedCoreOfCore) &&
//                            extendCoreSTR.indexOf(extendCandidateSTR) == 0 &&
//                            extendCoreSTR.length() > extendCandidateSTR.length())
//                    {
//                        candidateForDelete.add(candidateSTR);
//                    }
//                }
//            }
//        }
//        candidateSTRs.removeAll(candidateForDelete);
//        candidateForDelete.clear();
//
//        for (STR candidateSTR : candidateSTRs)
//            listOfDistinctSTRsSeq.add(candidateSTR.getSequence());
//
//        candidateSTRs.clear();
//        listOfSTRs.clear();
//
//        return listOfDistinctSTRsSeq;
//    }

    private static ArrayList<String> getAllBiologicalSTRs(int speciesID)
    {
        ArrayList<String> result = null;
        //region filter STRs based on biological concepts
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        ArrayList<ThreadWorkerForFilteringSTRs> tasks = new ArrayList<>();
        tasks.add(new ThreadWorkerForFilteringSTRs(speciesID, true));

        List<Future<HashMap<Integer, HashSet<String>>>> allFilteredSTRs;
        try
        {
            allFilteredSTRs = executorService.invokeAll(tasks);
            for (Future<HashMap<Integer, HashSet<String>>> filteredSTRs : allFilteredSTRs)
            {
                Map.Entry<Integer,HashSet<String>> entry = filteredSTRs.get().entrySet().iterator().next();
                result = new ArrayList<>(entry.getValue());
            }
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        finally
        {
            executorService.shutdown();
        }

        if (result == null)
            return new ArrayList<>();

        return result;
        //endregion
    }

    private static ArrayList<Integer> calculateHomology(ArrayList<String> transcriptsStableIDs, int threadNumber ,
                                                        int speciesID, ArrayList<Integer> filteredSpeciesID,
                                                        HashMap<String, String> peptidesSequencesOfBaseTranscripts      )
    {
        ArrayList<Integer> result = new ArrayList<>();
        result.add(0);
        result.add(0);
        result.add(0);
        result.add(0);
        result.add(0);
        result.add(0);

        //region calculate
        int availableSystemCPUCores;
        if (threadNumber <= 0)
            availableSystemCPUCores = Runtime.getRuntime().availableProcessors() - 1;
        else
            availableSystemCPUCores = threadNumber;
        ExecutorService executorService = Executors.newFixedThreadPool(availableSystemCPUCores);

        System.out.println("\n$ : The calculation homology process start with " +
                                   availableSystemCPUCores + " available system CPU cores!\n");

        ArrayList<ThreadWorkerForGetSequenceOfPeptides> tasks = new ArrayList<>();
        int i = 0;
        for (String transcriptsStableID : transcriptsStableIDs)
        {
            ThreadWorkerForGetSequenceOfPeptides task = new ThreadWorkerForGetSequenceOfPeptides(
                    transcriptsStableID, i++, speciesID, filteredSpeciesID, peptidesSequencesOfBaseTranscripts);
            tasks.add(task);
        }

        List<Future<HashMap<String, ArrayList<Integer>>>> allCountOfHomology;
        try
        {
            allCountOfHomology = executorService.invokeAll(tasks);
            for (Future<HashMap<String, ArrayList<Integer>>> countOfHomology : allCountOfHomology)
            {
                Map.Entry<String, ArrayList<Integer>> entry = countOfHomology.get().entrySet().iterator().next();

                String transcriptID = entry.getKey();
                System.out.println("$ : The transcript ID " + transcriptID + " has \t" +
                                           entry.getValue().get(0) + " \thomology and\t " +
                                           entry.getValue().get(1) + " \tnon homology\t " +
                                           entry.getValue().get(2) + " \tcorrupted transcript for method 1.");
                System.out.println("$ : The transcript ID " + transcriptID + " has \t" +
                                           entry.getValue().get(3) + " \thomology and\t " +
                                           entry.getValue().get(4) + " \tnon homology\t " +
                                           entry.getValue().get(5) + " \tcorrupted transcript for method 2.");

                result.set(0, result.get(0) + entry.getValue().get(0));
                result.set(1, result.get(1) + entry.getValue().get(1));
                result.set(2, result.get(2) + entry.getValue().get(2));
                result.set(3, result.get(3) + entry.getValue().get(3));
                result.set(4, result.get(4) + entry.getValue().get(4));
                result.set(5, result.get(5) + entry.getValue().get(5));
            }
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        finally
        {
            executorService.shutdown();
        }

        return result;
        //endregion
    }

    private static ArrayList<Integer> calculateGeneBasedHomologyForCase(
            ArrayList<Gene> genesClass, int threadNumber , int speciesID, HashSet<Integer> desiredSpeciesID)
    {
        long startTime = System.currentTimeMillis();

        ArrayList<Integer> result = new ArrayList<>(Arrays.asList(0,0,0,0,0,0,0,0,0,0,0));

        //region calculate

        int availableSystemCPUCores;
        if (threadNumber <= 0)
            availableSystemCPUCores = Runtime.getRuntime().availableProcessors() - 1;
        else
            availableSystemCPUCores = threadNumber;
        ExecutorService executorService = Executors.newFixedThreadPool(availableSystemCPUCores);

        System.out.println("\n$ : The calculation gene based homology process start with " +
                                   availableSystemCPUCores + " available system CPU cores!\n");

        ArrayList<ThreadWorkerForCalculatingGeneBasedHomologyForCase> tasks = new ArrayList<>();
        int i = 0;
        for (Gene gene : genesClass)
        {
            ThreadWorkerForCalculatingGeneBasedHomologyForCase
                    task = new ThreadWorkerForCalculatingGeneBasedHomologyForCase(
                    i++, speciesID, gene, desiredSpeciesID);
            tasks.add(task);
        }

        List<Future<HashMap<String, ArrayList<Integer>>>> allCountOfHomology;
        try
        {
            allCountOfHomology = executorService.invokeAll(tasks);
            for (Future<HashMap<String, ArrayList<Integer>>> countOfHomology : allCountOfHomology)
            {
                Map.Entry<String, ArrayList<Integer>> entry = countOfHomology.get().entrySet().iterator().next();

                String geneStableID = entry.getKey();
                System.out.println("$ : The gene stable ID " + geneStableID + " has \t" +
                                           entry.getValue().get(0) + " \thomology and\t " +
                                           entry.getValue().get(1) + " \tnon homology\t " +
                                           entry.getValue().get(2) + " \tcorrupted base transcript for method 1.");
                System.out.println("$ : The gene stable ID " + geneStableID + " has \t" +
                                           entry.getValue().get(3) + " \thomology and\t " +
                                           entry.getValue().get(4) + " \tnon homology\t " +
                                           entry.getValue().get(5) + " \tcorrupted base transcript for method 2.");

                result.set(0, result.get(0) + entry.getValue().get(0));
                result.set(1, result.get(1) + entry.getValue().get(1));
                result.set(2, result.get(2) + entry.getValue().get(2));
                result.set(3, result.get(3) + entry.getValue().get(3));
                result.set(4, result.get(4) + entry.getValue().get(4));
                result.set(5, result.get(5) + entry.getValue().get(5));
                result.set(6, result.get(6) + entry.getValue().get(6));
                result.set(7, result.get(7) + entry.getValue().get(7));
                result.set(8, result.get(8) + entry.getValue().get(8));
                result.set(9, result.get(9) + entry.getValue().get(9));
                result.set(10, result.get(10) + entry.getValue().get(10));
            }
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        finally
        {
            executorService.shutdown();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("$ :: The process of Thread for calculating gene based homology for case "
                                   + " ends at " + new Date(endTime).toString());
        System.out.println("############## \t The process of calculating gene based homology for case "
                                   + " ends in : (" + getDuration(startTime, endTime) + ") \t ##############");

        return result;
        //endregion
    }

    private static ArrayList<Integer> calculateGeneBasedHomologyForControl(
            ArrayList<Gene> referenceGeneForControlRound, int threadNumber , int speciesID,
            HashSet<Integer> desiredSpeciesID)
    {
        long startTime = System.currentTimeMillis();

        ArrayList<Integer> result = new ArrayList<>();
        result.add(0);
        result.add(0);
        result.add(0);
        result.add(0);
        result.add(0);
        result.add(0);
        result.add(0);
        result.add(0);
        result.add(0);
        result.add(0);
        result.add(0);

        //region calculate

        int availableSystemCPUCores;
        if (threadNumber <= 0)
            availableSystemCPUCores = Runtime.getRuntime().availableProcessors() - 1;
        else
            availableSystemCPUCores = threadNumber;
        ExecutorService executorService = Executors.newFixedThreadPool(availableSystemCPUCores);

        System.out.println("\n$ : The calculation gene based homology process start with " +
                                   availableSystemCPUCores + " available system CPU cores!\n");

        ArrayList<ThreadWorkerForCalculatingGeneBasedHomologyForControl> tasks = new ArrayList<>();
        int i = 0;
        for (Gene gene : referenceGeneForControlRound)
        {
            ThreadWorkerForCalculatingGeneBasedHomologyForControl
                    task = new ThreadWorkerForCalculatingGeneBasedHomologyForControl(
                    i++, speciesID, gene, desiredSpeciesID);
            tasks.add(task);
        }

        List<Future<HashMap<String, ArrayList<Integer>>>> allCountOfHomology;
        try
        {
            allCountOfHomology = executorService.invokeAll(tasks);
            for (Future<HashMap<String, ArrayList<Integer>>> countOfHomology : allCountOfHomology)
            {
                Map.Entry<String, ArrayList<Integer>> entry = countOfHomology.get().entrySet().iterator().next();

                String str = entry.getKey();
                System.out.println("$ : The STR " + str + " has \t" +
                                           entry.getValue().get(0) + " \thomology and\t " +
                                           entry.getValue().get(1) + " \tnon homology\t " +
                                           entry.getValue().get(2) + " \tcorrupted base transcript for method 1.");
                System.out.println("$ : The STR " + str + " has \t" +
                                           entry.getValue().get(3) + " \thomology and\t " +
                                           entry.getValue().get(4) + " \tnon homology\t " +
                                           entry.getValue().get(5) + " \tcorrupted base transcript for method 2.");

                result.set(0, result.get(0) + entry.getValue().get(0));
                result.set(1, result.get(1) + entry.getValue().get(1));
                result.set(2, result.get(2) + entry.getValue().get(2));
                result.set(3, result.get(3) + entry.getValue().get(3));
                result.set(4, result.get(4) + entry.getValue().get(4));
                result.set(5, result.get(5) + entry.getValue().get(5));
                result.set(6, result.get(6) + entry.getValue().get(6));
                result.set(7, result.get(7) + entry.getValue().get(7));
                result.set(8, result.get(8) + entry.getValue().get(8));
                result.set(9, result.get(9) + entry.getValue().get(9));
                result.set(10, result.get(10) + entry.getValue().get(10));
            }
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        finally
        {
            executorService.shutdown();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("$ :: The process of Thread for calculating gene based homology for control "
                                   + " ends at " + new Date(endTime).toString());
        System.out.println("############## \t The process of calculating gene based homology for case "
                                   + " ends in : (" + getDuration(startTime, endTime) + ") \t ##############");

        return result;
        //endregion
    }

    private static ArrayList<Integer> getRandomVector(int range, int number, Random randomGenerator)
    {
//        HashSet<Integer> repo = new HashSet<>();
        ArrayList<Integer> repo = new ArrayList<>();
        while (repo.size() != number)
            repo.add(randomGenerator.nextInt(range));
        return new ArrayList<>(repo);
    }

    public static void countSTRCoreFrequency(String limitedSTRsSpecificFilePath, int speciesID)
    {
        ArrayList<ArrayList<String>> limitedFingerPrintFile;
        ArrayList<String> strs;
        String[] output = new String[6];

        limitedFingerPrintFile = Reader.readFingerPrintFile(limitedSTRsSpecificFilePath);

        strs = getSTRsPool(speciesID, limitedFingerPrintFile);

        int i = 0;
        for (String str : strs)
        {
            System.out.println("--------------------------------------------------------------------------------------------------");
            System.out.println("$ : item = " + (++i));
            for (ArrayList<String> row : DatabaseControllerReport.getAllOccurrenceOf(str))
            {
                if (row.get(5).equals(str))
                    System.out.println("$ : ** " + row.toString() + " **");
                else
                    System.out.println("$ : " + row.toString());
            }
        }
    }

    public static void analysisMethionineForSpecies(int speciesID) throws Exception
    {
        HashSet <Transcript> totalTranscriptsOfSpecies
                = DatabaseControllerForTranscripts.getAllTranscriptsOfSpeciesIDFromDB(speciesID);
        System.out.println("$ : There are totally " + totalTranscriptsOfSpecies.size()
                                   + " Transcripts in DB for species ID " + speciesID + ".");

        ArrayList<String> totalTranscriptIDList = new ArrayList<>();
        for (Transcript transcript : totalTranscriptsOfSpecies)
            totalTranscriptIDList.add(transcript.getTranscriptStableID());
        totalTranscriptsOfSpecies.clear();
        totalTranscriptsOfSpecies = null;

        //region Analyse Part 1
        HashMap<String, String> peptidesSequences;
        peptidesSequences = DatabaseControllerForTranscripts.getAllSequenceOfTranscriptStableIDsFromCloud(
                totalTranscriptIDList, TranscriptSequenceType.PEPTIDE_SEQUENCE, 20,
                speciesID, true, false, true);

        System.out.println("\n====================================");
        System.out.println("size of peptidesSequences = " + peptidesSequences.size());
        System.out.println("------------------------------------------");

        int i = 0;
        for (String transcript : peptidesSequences.keySet())
        {
            System.out.println("$ : item = " + (++i));
            System.out.println("$ : " + transcript);
            System.out.println("$ : " + peptidesSequences.get(transcript));
            System.out.println("------------------------------------------");
        }

        int nonMet = 0;
        for (String peptide : peptidesSequences.values())
        {
            if (!peptide.isEmpty() && peptide.charAt(0) != 'M')
                nonMet++;
        }
        System.out.println(String.format("\n\n$ : Percentage of non-M peptides : %.2f%%   (%d/%d)"
                , ((float)nonMet/peptidesSequences.size()) * 100, nonMet, peptidesSequences.size()));

        peptidesSequences.clear();
        //endregion
    }

    public static void calculatedHomologyDistribution(String transcriptIDFilePath, int fold)
    {
        int seqLength = 10;
        float[] weights= new float []{10, 10, 10, 10, 10, 10, 10, 10, 10, 10};
        int iteration = 10000;
        float threshold = 20;
        float msxScore = 100;
        float step = 10;

        HashMap<String, String> allPeptidesSequences;
        HashMap<Character, Integer> firstAminoAcidContribution = new HashMap<>();
        ArrayList<String> filteredPeptide;

        allPeptidesSequences = Reader.transcriptIDFile(transcriptIDFilePath);
        System.out.println("$ : size of peptidesSequences = " + allPeptidesSequences.size());

        // calculate first amino acid contribution
        for (String sequence : allPeptidesSequences.values())
        {
            if (sequence != null && !sequence.isBlank())
            {
                if (firstAminoAcidContribution.containsKey(sequence.charAt(0)))
                    firstAminoAcidContribution.put(sequence.charAt(0), firstAminoAcidContribution.get(sequence.charAt(0)) + 1);
                else
                    firstAminoAcidContribution.put(sequence.charAt(0), 1);
            }
        }
        int temp = 0;
        for (Integer count : firstAminoAcidContribution.values())
            temp += count;
        System.out.println("$ : first amino acid includes " + firstAminoAcidContribution.size() + " type(s) for "  + temp + " sequences");
        System.out.println("$ : " + firstAminoAcidContribution.toString());
        System.out.printf("$ : There are %.2f %% Met (%d)%n", (float)firstAminoAcidContribution.get('M') / temp * 100,
                          firstAminoAcidContribution.get('M'));
        System.out.printf("$ : There are %.2f %% X (%d)%n", (float)firstAminoAcidContribution.get('X') / temp * 100,
                          firstAminoAcidContribution.get('X'));
        System.out.printf("$ : There are %.2f %% Other (%d)%n",
                          ((float)temp - firstAminoAcidContribution.get('X') - firstAminoAcidContribution.get('M')) / temp * 100,
                          temp - firstAminoAcidContribution.get('X') - firstAminoAcidContribution.get('M'));

        filteredPeptide = filterXPeptide(allPeptidesSequences, seqLength);
        System.out.println("$ : " + filteredPeptide.size() + " peptides filtered and " +
                                   allPeptidesSequences.size() + " peptides remained!");

        ArrayList<String> keyList = new ArrayList<>(allPeptidesSequences.keySet());
        Collections.shuffle(keyList, new Random(13));
        Random randomGenerator = new Random(seed);

        for (int i = 0 ; i < fold ; i++)
        {
//            HashMap<String, String> selectedPeptidesSequences = new HashMap<>();
            ArrayList<Integer> randomIndices = getRandomVector(allPeptidesSequences.size(), iteration, randomGenerator);
            System.out.println("$ : size of randomIndices = " + randomIndices.size());

//            for (Integer index : randomIndices)
//            {
//                selectedPeptidesSequences.put(keyList.get(index), allPeptidesSequences.get(keyList.get(index)));
//            }
            System.out.println("$ : Peptides Sequences were selected!");
            int [] matches = null;
            for (float j = msxScore ; j >= threshold ; j-= step)
            {
                matches = calculateMatches(allPeptidesSequences, keyList, randomIndices, seqLength, j, weights);


                System.out.println("============================================================================================");
                System.out.println("$ : Sequence length = " + seqLength);
                System.out.println("$ : Fold = " + i + " \t | \t Minimum score = " + j);
                System.out.println("$ : The weights = " + Arrays.toString(weights));
                System.out.println("$ : origin array is = " + Arrays.toString(matches));
                System.out.println();
                System.out.println("$ : Total number of matched for at least " + j + " matched : ");
                System.out.print("$ : [");
                for (int k = 0; k < seqLength; k++)
                    System.out.print("\t" + (k + 1) + "\t");
                System.out.print("\t]");
                System.out.println();
                System.out.print("$ : [");
                for (int k = 0; k < seqLength; k++)
                    System.out.printf("\t%.2f", (float)matches[k] / matches[seqLength] * 100);
                System.out.print("\t]");
                System.out.println();
                System.out.println();
                System.out.printf("percent of matched %.2f%% (%d) and non-matched %.2f%% (%d)%n",
                                  (float)matches[seqLength] / (matches[seqLength] + matches[seqLength + 1]) * 100,
                                  matches[seqLength],
                                  (float)matches[seqLength + 1] / (matches[seqLength] + matches[seqLength + 1]) * 100,
                                  matches[seqLength + 1]);

                System.out.println("============================================================================================");
            }

            System.out.println("############################################################################################");
            System.out.println("$ : The matched distribution is :");
            int[] matchedDistribution = Arrays.copyOfRange(matches, seqLength + 2, matches.length);
            System.out.println("$ : without normalize\t:" + Arrays.toString(matchedDistribution));
            System.out.println("$ : with normalize\t:");
            System.out.print("$ : [");
            for (int k = 0; k < matchedDistribution.length; k++)
                System.out.print("\t" + k + "\t");
            System.out.print("\t]");
            System.out.println();
            System.out.print("$ : [");
            for (int k = 0; k < matchedDistribution.length; k++)
                System.out.printf("\t%.2f", (float)matchedDistribution[k] / (iteration / 2) * 100);
            System.out.print("\t]");
            System.out.println();
            System.out.println("############################################################################################");



            randomIndices.clear();
//            selectedPeptidesSequences.clear();
        }
    }

    private static ArrayList<String> filterXPeptide(HashMap<String, String> peptidesSequences, int requiredLength)
    {
        ArrayList<String> removedPeptidesKye = new ArrayList<>();
        for (String key : peptidesSequences.keySet())
        {
            String peptide = peptidesSequences.get(key);
            if (peptide == null ||
                    peptide.isBlank() ||
                    peptide.substring(0, Math.min(requiredLength, peptide.length())).indexOf('X') != -1)
                removedPeptidesKye.add(key);
        }

        for (String key : removedPeptidesKye)
            peptidesSequences.remove(key);

        return removedPeptidesKye;
    }

    private static int[] calculateMatches(
            HashMap<String, String> peptidesSequences,
            ArrayList<String> keyList,
            ArrayList<Integer> randomIndices,
            int seqLength,
            float minimumScore,
            float[] weights)
    {
        int [] result = new int[seqLength + 2 + (seqLength + 1)];
        Arrays.fill(result, 0);

        for (int num = 0 ; num < randomIndices.size() - 1 ; num+=2 )
        {
            String primaryPeptide1;
            String primaryPeptide2;

            primaryPeptide1 = peptidesSequences.get(keyList.get(randomIndices.get(num)));
            primaryPeptide2 = peptidesSequences.get(keyList.get(randomIndices.get(num + 1)));

            String peptide1 = primaryPeptide1.substring(1, Math.min(primaryPeptide1.length(), seqLength + 1));
            String peptide2 = primaryPeptide2.substring(1, Math.min(primaryPeptide2.length(), seqLength + 1));

            boolean [] matches = calculateMatch(peptide1, peptide2);
            int numberOfMatches = calculateNumberOfMatch(matches);
            float matchScore = calculateMatchScore(matches, weights);

            result[seqLength + 2 + numberOfMatches]++;

            if (minimumScore <=  matchScore)
            {
                for (int i = 0; i < matches.length; i++)
                {
                    if (matches[i])
                        result[i]++;
                }
                result[seqLength]++;
            }
        }

        result[seqLength + 1] = randomIndices.size() / 2 - result[seqLength];
        return result;
    }

    private static boolean[] calculateMatch(String peptide1, String peptide2)
    {
        boolean [] matches = new boolean[Math.max(peptide1.length(), peptide2.length())];
        Arrays.fill(matches, false);

        for (int i = 0; i < Math.min(peptide1.length(), peptide2.length()); i++)
        {
            if (peptide1.charAt(i) == peptide2.charAt(i))
                matches[i] = true;
        }

        return matches;
    }

    private static int calculateNumberOfMatch(boolean[] matchResult)
    {
        int result = 0;

        for (boolean match : matchResult)
        {
            if (match)
                result++;
        }

        return result;
    }

    private static float calculateMatchScore(boolean[] matchResult, float[] weights)
    {
        float score = 0;

        for (int i = 0; i < matchResult.length; i++)
        {
            if (matchResult[i])
                score += weights[i];
        }

        return score;
    }

    private static HashMap<String, ArrayList<String>> calculateDuplicationAnalysis(ArrayList<String> transcriptStableIDs)
            throws Exception
    {
        HashMap<String, ArrayList<String>> duplicateTranscripts = new HashMap<>();
        HashMap<String, String> promoters = new HashMap<>();

        for (String transcriptStableID : new HashSet<>(transcriptStableIDs))
        {
            promoters.put(transcriptStableID,
                          DatabaseControllerForTranscripts
                                  .getTranscriptPromoterOfTranscriptID(transcriptStableID, 120).get(0));
        }

        for (String transcriptStableID : promoters.keySet())
        {
            if (duplicateTranscripts.containsKey(promoters.get(transcriptStableID)))
                duplicateTranscripts.get(promoters.get(transcriptStableID)).add(transcriptStableID);
            else
                duplicateTranscripts.put(promoters.get(transcriptStableID),
                                         new ArrayList<>(Collections.singleton(transcriptStableID)));

        }

        return duplicateTranscripts;
    }

    public static boolean proteinPairwiseScore(
            String limitedSTRsSpecificFilePath, String totalSTRsSpecificFilePath,
            int referenceSpeciesID, int secondarySpeciesID1, int secondarySpeciesID2, int secondarySpeciesID3,
            int threadNumber)
            throws Exception
    {
        int tempRowCounter;
        boolean geneBased = true;
        String moreInfo;
        ArrayList<Integer> activeSpecies;
        ArrayList<ArrayList<String>> totalFingerPrintFile;
        ArrayList<ArrayList<String>> limitedFingerPrintFile;
        ArrayList<String> limitedSpeciesSpecificSTRProducerTranscriptsStableIDs;
        ArrayList<String> totalSpeciesSpecificSTRProducerTranscriptsStableIDs;
        ArrayList<String> allBiologicalSTRs;
        ArrayList<String> excludedTranscriptsStableIDsInDB = new ArrayList<>();
        ArrayList<String> controlTranscript;
        ArrayList<Integer>randomIndices;
        HashSet<String> limitedSpeciesSpecificTranscriptsPool;
        HashSet<String> totalSpeciesSpecificTranscriptsPool;
        HashSet<String> totalTranscriptsPool;
        HashMap<String, String> geneExonsSequences;
        HashMap<String, String> cdnasSequences;
        HashMap<String, String> codingsSequences;
        HashMap<String, String> peptidesSequences;
        HashMap<String, String> promoterSequences = new HashMap<>();


        limitedFingerPrintFile = Reader.readFingerPrintFile(limitedSTRsSpecificFilePath);
        totalFingerPrintFile = Reader.readFingerPrintFile(totalSTRsSpecificFilePath);

        limitedSpeciesSpecificSTRProducerTranscriptsStableIDs =
                getTranscriptsPool(referenceSpeciesID, limitedFingerPrintFile, geneBased);
        totalSpeciesSpecificSTRProducerTranscriptsStableIDs =
                getTranscriptsPool(referenceSpeciesID, totalFingerPrintFile, geneBased);

        if (limitedSpeciesSpecificSTRProducerTranscriptsStableIDs == null)
            return false;

        allBiologicalSTRs = getAllBiologicalSTRs(referenceSpeciesID);

        System.out.println("$ : The species ID " + referenceSpeciesID + " has " +
                                   allBiologicalSTRs.size() + " biological STRs type.");

        ArrayList<String> totalSTRProducerTranscriptsStableIDs
                = DatabaseControllerForTranscripts.getAllSTRProducerTranscriptsStableIDOfSpecies(referenceSpeciesID, allBiologicalSTRs);

        limitedSpeciesSpecificTranscriptsPool = new HashSet<>(limitedSpeciesSpecificSTRProducerTranscriptsStableIDs);
        totalSpeciesSpecificTranscriptsPool = new HashSet<>(totalSpeciesSpecificSTRProducerTranscriptsStableIDs);
        totalTranscriptsPool = new HashSet<>(totalSTRProducerTranscriptsStableIDs);
        System.out.println("\n$ : All producer transcripts of species ID " + referenceSpeciesID + " was extracted. ");
        System.out.println("$ : There are " + limitedSpeciesSpecificTranscriptsPool.size()
                                   + " limited STR specific producer Transcripts which have been found of "
                                   + getSTRsPool(referenceSpeciesID, limitedFingerPrintFile).size() + " STRs.");
        System.out.println("$ : There are " + totalSpeciesSpecificTranscriptsPool.size()
                                   + " total STR specific producer Transcripts which have been found of "
                                   + getSTRsPool(referenceSpeciesID, totalFingerPrintFile).size() + " STRs.");
        System.out.println("$ : There are " + totalTranscriptsPool.size()
                                   + " total STR producer Transcripts which have been found of "
                                   + allBiologicalSTRs.size() + " biological STRs.");

        HashSet <Transcript> totalTranscriptsOfSpecies
                = DatabaseControllerForTranscripts.getAllTranscriptsOfSpeciesIDFromDB(referenceSpeciesID); //TODO : 7 => referenceSpeciesID
        System.out.println("$ : There are totally " + totalTranscriptsOfSpecies.size()
                                   + " Transcripts in DB for species ID " + referenceSpeciesID + ".");

        excludedTranscriptsStableIDsInDB = new ArrayList<>();
        for (Transcript transcript : totalTranscriptsOfSpecies)
            excludedTranscriptsStableIDsInDB.add(transcript.getTranscriptStableID());
        excludedTranscriptsStableIDsInDB.removeAll(totalTranscriptsPool);
        System.out.println("$ : There are totally " + excludedTranscriptsStableIDsInDB.size()
                                   + " Transcripts remain in DB for species ID " + referenceSpeciesID
                                   + " which are not STR producer.");
        System.out.println();
        System.out.println();
        System.out.println("################################################################################");
        System.out.println("$ : The list of all Transcripts for species ID " + referenceSpeciesID
                                   + " which are not STR producer.");
//        for (String id : excludedTranscriptsStableIDsInDB)
//            System.out.println(id);
        System.out.println("################################################################################");
        System.out.println();
        System.out.println();

        System.out.println();
        System.out.println();
        System.out.println("###################################################################################");
        System.out.println("#############################   Start Gene Homology   #############################");
        System.out.println("###################################################################################");

//        ArrayList<Integer> round0;
//        ArrayList<ArrayList<Integer>> resultOfControls = new ArrayList<>();

        if (geneBased)
        {
            long startTime = System.currentTimeMillis();
            ArrayList<ArrayList<String>> result = new ArrayList<>();

            HashSet<Integer> desiredSpeciesID = new HashSet<>(DataSetsStatistics.getActiveSpecies(totalFingerPrintFile));
            ArrayList<String> strsClass = getSTRsPool(referenceSpeciesID, limitedFingerPrintFile);
            ArrayList<String> totalSTRsClass = getSTRsPool(referenceSpeciesID, totalFingerPrintFile);

            System.out.println();
            System.out.println("$ : There are " + desiredSpeciesID.size() + " desired species IDs.");
            System.out.println();

            ArrayList<Gene> candidateLimitedSpeciesSpecificSTRProducerGenes =
                    DatabaseControllerForGenes.getAllSTRProducerGenes(referenceSpeciesID, strsClass);


            //region calculate
            int availableSystemCPUCores;
            if (threadNumber <= 0)
                availableSystemCPUCores = Runtime.getRuntime().availableProcessors() - 1;
            else
                availableSystemCPUCores = threadNumber;

            System.out.println("\n$ : The calculation gene based protein pairwise homology process start with " +
                                       availableSystemCPUCores + " available system CPU cores!\n");

            int i = 0;
            i = calculateProteinPairwiseHomologyScoreByEMBOSSNeedle(
                    referenceSpeciesID, secondarySpeciesID1, secondarySpeciesID2, secondarySpeciesID3,
                    desiredSpeciesID, candidateLimitedSpeciesSpecificSTRProducerGenes,
                    availableSystemCPUCores, i, true);

            long endTime = System.currentTimeMillis();
            System.out.println("$ :: The process for calculating gene based protein pairwise homology"
                                       + " ends at " + new Date(endTime).toString());
            System.out.println("############## \t The process of calculating gene based protein pairwise homology for case"
                                       + " ends in : (" + getDuration(startTime, endTime) + ") \t ##############");

            //endregion

            startTime = System.currentTimeMillis();
            ArrayList<Gene> candidateSpeciesSpecificSTRProducerGenes =
                    DatabaseControllerForGenes.getAllSTRProducerGenes(referenceSpeciesID, totalSTRsClass);
            HashSet<Gene> speciesSpecificSTRProducerGenes =
                    filterSpeciesSpecificSTRProducerGenes(referenceSpeciesID, candidateSpeciesSpecificSTRProducerGenes,
                                                          desiredSpeciesID, availableSystemCPUCores);

            int speciesSpecificSTRProducerGenesCount = speciesSpecificSTRProducerGenes.size();

            System.out.println("$ : ------------------------------------");
            System.out.println("$ : There are " + speciesSpecificSTRProducerGenesCount +
                                       " species specific STR Producer Genes : ");
            for (Gene gene : speciesSpecificSTRProducerGenes)
                System.out.println("$ : " + gene.getGeneStableID() + "\t | \t" + gene.getGeneName());
            System.out.println("$ : ------------------------------------");

            ArrayList<String> allTypeOfSTRs = DatabaseControllerForSTRs.getAllTypeOfSTRsInDB(referenceSpeciesID);
            ArrayList<Gene> nonSpeciesSpecificSTRProducerGenes =
                    DatabaseControllerForGenes.getAllSTRProducerGenes(referenceSpeciesID, allTypeOfSTRs);
            System.out.println("$ : There are totally " + nonSpeciesSpecificSTRProducerGenes.size() +
                                       " STR Producer Genes in species ID " + referenceSpeciesID + ".");
            nonSpeciesSpecificSTRProducerGenes.removeAll(speciesSpecificSTRProducerGenes);
            System.out.println("$ : There are totally " + nonSpeciesSpecificSTRProducerGenes.size() +
                                       " non species specific STR Producer Genes in species ID " + referenceSpeciesID + ".");


            // ------------------------------- start calculating control result ----------------------------------------
            Collections.shuffle(nonSpeciesSpecificSTRProducerGenes);
            Random randomGenerator = new Random(seed);

            System.out.println("$ : Control Round start at " + new Date(startTime).toString());

            ArrayList<Integer> randomGeneIndices = getRandomVector(nonSpeciesSpecificSTRProducerGenes.size(),
                                                                   1500,
                                                                   randomGenerator);
//            ArrayList<Integer> randomGeneIndices = getRandomVector(nonSpeciesSpecificSTRProducerGenes.size(),
//                                                                   3,
//                                                                   randomGenerator);
            System.out.println("$ : random indices of Control Round : " + randomGeneIndices.toString());

            ArrayList<Gene> referenceGeneForControlRounds = new ArrayList<>();
            for (Integer index : randomGeneIndices)
                referenceGeneForControlRounds.add(nonSpeciesSpecificSTRProducerGenes.get(index));

            System.out.println("$ : Control Round Result :");
            i = calculateProteinPairwiseHomologyScoreByEMBOSSNeedle(
                    referenceSpeciesID, secondarySpeciesID1, secondarySpeciesID2, secondarySpeciesID3,
                    desiredSpeciesID, referenceGeneForControlRounds,
                    availableSystemCPUCores, ++i, false);

            randomGeneIndices.clear();
            referenceGeneForControlRounds.clear();

            endTime = System.currentTimeMillis();
            System.out.println("$ : Control Round end at " + new Date(endTime).toString());
            System.out.println("############## \t The process of calculating gene based protein pairwise homology for control"
                                       + " ends in : (" + getDuration(startTime, endTime) + ") \t ##############");
            System.out.println("#####################################################");
            System.out.println("#####################################################");
        }

        System.out.println();


        System.out.println("#####################################################");
        System.out.println("#####################################################");
        System.out.println();


        System.out.println("###################################################################################");
        System.out.println("#############################  end of Gene Homology  ##############################");
        System.out.println("###################################################################################");

        return true;
    }

    private static int calculateProteinPairwiseHomologyScoreByEMBOSSNeedle(
            int referenceSpeciesID, int secondarySpeciesID1, int secondarySpeciesID2, int secondarySpeciesID3,
            HashSet<Integer> desiredSpeciesID, ArrayList<Gene> candidateGenes,
            int availableSystemCPUCores, int i, boolean checkValidate)
    {
        ExecutorService executorService = Executors.newFixedThreadPool(availableSystemCPUCores);

        ArrayList<ThreadWorkerForCalculatingGeneBasedProteinHomologyScore> tasks = new ArrayList<>();
        for (Gene gene : candidateGenes)
        {
            ThreadWorkerForCalculatingGeneBasedProteinHomologyScore
                    task = new ThreadWorkerForCalculatingGeneBasedProteinHomologyScore(
                    i++, referenceSpeciesID, gene, desiredSpeciesID, secondarySpeciesID1, secondarySpeciesID2,
                    secondarySpeciesID3, checkValidate);
            tasks.add(task);
        }

        List<Future<ArrayList<ArrayList<String>>>> allScoreOfHomology;
        try
        {
            allScoreOfHomology = executorService.invokeAll(tasks);
            for (Future<ArrayList<ArrayList<String>>> oneRoundScores : allScoreOfHomology)
            {
                for (ArrayList<String> score : oneRoundScores.get())
                {
                    if (score.isEmpty())
                        continue;
                    System.out.println("$ : The calculated Score line is = " + score.toString());
                }
            }
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        finally
        {
            executorService.shutdown();
        }
        return i;
    }

    private static HashSet<Gene> filterSpeciesSpecificSTRProducerGenes(
            int referenceSpeciesID, ArrayList<Gene> candidateGenes,
            HashSet<Integer> desiredSpeciesID, int availableSystemCPUCores)
    {
        HashSet<Gene> filteredGenes = new HashSet<>();

        ExecutorService executorService = Executors.newFixedThreadPool(availableSystemCPUCores);

        int i = 0;
        ArrayList<ThreadWorkerForFilteringSpeciesSpecificSTRProducerGenes> tasks = new ArrayList<>();
        for (Gene gene : candidateGenes)
        {
            ThreadWorkerForFilteringSpeciesSpecificSTRProducerGenes
                    task = new ThreadWorkerForFilteringSpeciesSpecificSTRProducerGenes(
                    ++i, referenceSpeciesID, gene, desiredSpeciesID);
            tasks.add(task);
        }

        List<Future<Gene>> speciesSpecificSTRProducerGenes;
        try
        {
            speciesSpecificSTRProducerGenes = executorService.invokeAll(tasks);
            for (Future<Gene> geneFuture : speciesSpecificSTRProducerGenes)
            {
                Gene gene = geneFuture.get();
                if (gene != null)
                    filteredGenes.add(gene);
            }
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        finally
        {
            executorService.shutdown();
        }
        return filteredGenes;
    }

}
