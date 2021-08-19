package com.alimaddi.control;

import com.alimaddi.Utility.Reader;
import com.alimaddi.Utility.Utilities;
import com.alimaddi.Utility.Writer;
import com.alimaddi.control.runnables.ThreadWorkerForCalculatingUniqueSTRsByGeneName;
import com.alimaddi.control.runnables.ThreadWorkerForFilteringSTRs;
import com.alimaddi.datacontainer.STRFamily;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DataCollector
{
//    public static int makeDataset(int speciesID)
//    {
//        HashSet<String> listofStrsSeq = new HashSet<>();
//        HashSet<String> listofCase = new HashSet<>();
//        HashMap<String, HashMap<String, Integer>> dataMatrix = new HashMap<>();
//        HashSet<STR> originStrs = DatabaseControllerForSTRs.getAllSTRsInDB();
//
//        ArrayList<STR> shufflePrimary = new ArrayList<>(originStrs);
//        originStrs.clear();
//        Collections.shuffle(shufflePrimary);
//
//        ArrayList<STR> strs = new ArrayList<>();
//        for (int i = 0 ; i < shufflePrimary.size()/1 ; i++)
//        {
//            strs.add(shufflePrimary.get(i));
//        }
//        shufflePrimary.clear();
//
//        for (STR str : strs)
//        {
//            listofStrsSeq.add(str.getSequence());
//        }
//        for (STR str : strs)
//        {
//            listofCase.add(str.getTranscriptStableId());
//        }
//        System.out.println("#Number of records * STRs types! : " + strs.size());
//        System.out.println("#Number of records(case/control)! : " + listofCase.size());
//        System.out.println("#Number of STRs types : " + listofStrsSeq.size());
//        listofStrsSeq.clear();
//        listofCase.clear();
////        HashMap<String, Integer> col = new HashMap<>();
////        col.put("label", 0);
////        for (String type : listofStrsSeq)
////        {
////            col.put(type, 0);
////        }
////        for (String mCase : listofCase)
////        {
////            HashMap<String, Integer> row = new HashMap<>(col);
////            dataMatrix.put(mCase, row);
////        }
////
////        int counter = 0;
////        for (STR str : strs)
////        {
////            counter ++;
////            String id = str.getTranscriptStableId();
////            String strSeq = str.getSequence();
////            Integer abundance = (int) str.getAbundance();
////            int label = str.getOrigin().getIndex();
////
////            dataMatrix.get(id).put(strSeq, abundance);
////            dataMatrix.get(id).put("label", label);
////            Utilities.showProgress(strs.size(), counter, 50, str.getSequence());
////        }
////
////        Writer.writeDataMatrix(dataMatrix);
////        System.out.println("DataMatrix constructed!");
//
//        Writer.writeDataMatrix(strs);
//
//        return strs.size();
//    }

    //region makeFingerPrintFile
    public static boolean makeFingerPrintFile() throws Exception
    {
        return makeFingerPrintFile(1, false);
    }
    public static boolean makeFingerPrintFile(int threadNumber, boolean isGeneBased) throws Exception
    {
        int i;
        int allSTRsDifferentTypeSize;
        String moreInfo;
        Boolean[][] matrix;
        Boolean[] unique;
        Boolean[] strWatchList;
        ArrayList<String> allSTRsDifferentType;
        HashSet<String> temp;
        Set<Integer> validSpecies;
        HashMap<String, Integer> strMapIndex = new HashMap<>();
        HashMap<Integer, Integer> speciesMapIndex = new HashMap<>();
        HashMap<Integer, ArrayList<String>> result = new HashMap<>();

        allSTRsDifferentType = DatabaseControllerForSTRs.getAllTypeOfSTRsInDB();
        temp = new HashSet<>(allSTRsDifferentType);
        if (temp.size() != allSTRsDifferentType.size())
        {
            temp.clear();
            allSTRsDifferentType.clear();
            System.err.println("$ : Error occurred! Duplication Data");
            return false;
        }
        else
        {
            temp.clear();
            System.out.println("\n$ : The process of calculating number of STRs different type done! ");
            System.out.println("\n$ : " + allSTRsDifferentType.size() + " STRs different type find");
        }

        HashMap<Integer, String> validDatasetsNamesIDFromDB =
                DatabaseControllerForGenes.getValidGenesDatasetsNamesInDB();
        HashMap<Integer, String> filteredDatasetsNamesIDFromDB =
                DatabaseControllerForGenes.getAppropriateValidDatasetsNamesIDFromDB(validDatasetsNamesIDFromDB);
        validSpecies = filteredDatasetsNamesIDFromDB.keySet();

        unique = new Boolean[allSTRsDifferentType.size()];
        strWatchList = new Boolean[allSTRsDifferentType.size()];
        Arrays.fill(unique, false);
        Arrays.fill(strWatchList, false);
        matrix = new Boolean[validSpecies.size()][allSTRsDifferentType.size()];
        for (Boolean[] row : matrix)
            Arrays.fill(row, false);

        i = 0;
        for (Integer id : validSpecies)
        {
            speciesMapIndex.put(id, i++);
            result.put(id, new ArrayList<>());
        }

        i = 0;
        for (String str : allSTRsDifferentType)
            strMapIndex.put(str, i++);


        //region filter STRs based on biological concepts
        int availableSystemCPUCores;
        if (threadNumber <= 0)
            availableSystemCPUCores = Runtime.getRuntime().availableProcessors() - 1;
        else
            availableSystemCPUCores = threadNumber;
        ExecutorService executorService = Executors.newFixedThreadPool(availableSystemCPUCores);

        System.out.println("\n$ : The filtering process start with " +
                                   availableSystemCPUCores + " available system CPU cores!\n");

        if (availableSystemCPUCores == 1)
        {
            moreInfo = " -> speciesId = 0 with STR count = 0";
            Utilities.showProgress(validSpecies.size(), 0, 50, moreInfo + "\t");
        }

        ArrayList<ThreadWorkerForFilteringSTRs> tasks = new ArrayList<>();
//        int a = 0;
        for (Integer id : validSpecies)
        {
            ThreadWorkerForFilteringSTRs task = new ThreadWorkerForFilteringSTRs(id, true);
            tasks.add(task);
//            if (++a == 25)
//                break;
        }

        List<Future<HashMap<Integer, HashSet<String>>>> allFilteredSTRs;
        try
        {
            allFilteredSTRs = executorService.invokeAll(tasks);
            for (Future<HashMap<Integer, HashSet<String>>> filteredSTRs : allFilteredSTRs)
            {
                Map.Entry<Integer, HashSet<String>> entry = filteredSTRs.get().entrySet().iterator().next();
                int speciesID = entry.getKey();
                System.out.println("$ : The species ID " + speciesID + " has " +
                                           entry.getValue().size() + " unique STRs.");
                for (String str : entry.getValue())
                {
                    matrix[speciesMapIndex.get(speciesID)][strMapIndex.get(str)] = true;
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
        //endregion
//        executorService.awaitTermination();

        //region filter STRs in the main thread!
//        i = 0;
//        HashSet<String> listOfDistinctSTRsSeq = new HashSet<>();
//        HashSet<STR> candidateSTRs = new HashSet<>();
//        HashSet<STR> candidateForDelete = new HashSet<>();
//        ArrayList<STR> listOfSTRs;
//        for (Integer id : validSpecies)
//        {
//            listOfSTRs = DatabaseControllerForSTRs.getAllSTRsInDB(id);
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////            listOfSTRs = new ArrayList<>();
////            String promoterTest = "AAAAAAAAAA";
////            for (Map.Entry<String, Integer> strCore : STRsProcess.calculateSTRs(promoterTest).entrySet())
////            {
////                STR strTemp = new STR(STROrigin.CDS_START_CODON,
////                                   strCore.getKey(),
////                                   strCore.getValue().shortValue(),
////                                   "0000");
////                listOfSTRs.add(strTemp);
////            }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//            if (availableSystemCPUCores == 1)
//            {
//                moreInfo = " -> speciesId = " + id + " with STR count = " + listOfSTRs.size() + " in DB " ;
//                Utilities.showProgress(validSpecies.size(), i, 50, moreInfo + "\t");
//            }
//
//            for (STR str : listOfSTRs)
//            {
//                String promoter = DatabaseControllerForTranscripts.getPromoterOfTranscriptsStableID(
//                        str.getTranscriptStableId(), STROrigin.CDS_START_CODON);
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
////                String promoter = promoterTest;
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                String[] output = Utilities.decomposeSTR(str.getSequence());
//                String core = output[0];
////                int repeat = Integer.parseInt(output[1]);
//
//                int coreStart;
//                int coreEnd;
//                int repeatStart;
//                int repeatEnd;
//                int localRepeat;
//
//                while (!promoter.isEmpty())
//                {
//                    localRepeat = 0;
//                    coreStart = promoter.indexOf(core);
//                    coreEnd = coreStart + core.length();
//                    repeatStart = coreEnd;
//                    repeatEnd = repeatStart + core.length();
//
//                    if (coreStart == -1 || repeatEnd > promoter.length())
//                        break;
//                    localRepeat++;
//
//                    while (repeatEnd <= promoter.length() && promoter.substring(coreStart, coreEnd)
//                            .equals(promoter.substring(repeatStart, repeatEnd)))
//                    {
//                        localRepeat++;
//                        repeatStart = repeatEnd;
//                        repeatEnd += core.length();
//                    }
//
//                    if (localRepeat >= 2 &&
//                            !(core.length() == 1 && localRepeat < 6) &&
//                            !(core.length() >= 2 && core.length() < 10 && localRepeat < 3)
//                    )
//                    {
//                        STR strTemp = new STR(STROrigin.CDS_START_CODON,
//                                              packSTR(core, localRepeat),
//                                              (short) 1,
//                                              str.getTranscriptStableId());
//                        candidateSTRs.add(strTemp);
//                    }
//
//                    promoter = promoter.substring(Math.min(promoter.length(), repeatEnd - core.length()));
//                }
//            }
//
//            if (availableSystemCPUCores == 1)
//            {
//                moreInfo = " -> speciesId = " + id + " :: STRs/filtered = " +
//                        listOfSTRs.size() + "/" + candidateSTRs.size() + " :: " +
//                        String.format("100%%/%.2f%%", (float)candidateSTRs.size()/listOfSTRs.size() * 100) + " ";
//                Utilities.showProgress(validSpecies.size(), i, 50, moreInfo + "\t");
//            }
//
//            for (STR coreSTR : candidateSTRs)
//            {
//                for (STR candidateSTR : candidateSTRs)
//                {
//                    if (isCoreSTR(coreSTR.getSequence(), candidateSTR.getSequence()))
//                    {
//                        candidateForDelete.add(candidateSTR);
//                    }
//                    else if (coreSTR.getTranscriptStableId().equals(candidateSTR.getTranscriptStableId()) &&
//                            !coreSTR.getSequence().equals(candidateSTR.getSequence()))
//                    {
////                        String extendCoreSTR = Utilities.makeSTRSequence(coreSTR.getSequence());
////                        String extendCandidateSTR = Utilities.makeSTRSequence(candidateSTR.getSequence());
//                        String[] output;
//
//                        output = Utilities.decomposeSTR(coreSTR.getSequence());
//                        String coreOfCore = output[0];
//                        int repeatOfCore = Integer.parseInt(output[1]);
//                        String extendCoreSTR =
//                                String.join("", Collections.nCopies(repeatOfCore, coreOfCore));
//
//                        output = Utilities.decomposeSTR(candidateSTR.getSequence());
//                        String coreOfCandidate = output[0];
//                        int repeatOfCandidate = Integer.parseInt(output[1]);
//                        String extendCandidateSTR =
//                                String.join("", Collections.nCopies(repeatOfCandidate, coreOfCandidate));
//
//                        String extendedCoreOfCore =
//                                String.join("", Collections.nCopies(
//                                        coreOfCandidate.length() / coreOfCore.length(), coreOfCore));
//
//                        if (coreOfCandidate.length() % coreOfCore.length() == 0 &&
//                                coreOfCandidate.equals(extendedCoreOfCore) &&
//                                extendCoreSTR.indexOf(extendCandidateSTR) == 0 &&
//                                extendCoreSTR.length() > extendCandidateSTR.length())
//                        {
//                            candidateForDelete.add(candidateSTR);
//                        }
//                    }
//                }
//            }
//            candidateSTRs.removeAll(candidateForDelete);
//            candidateForDelete.clear();
//
//            if (availableSystemCPUCores == 1)
//            {
//                moreInfo = " -> speciesId = " + id + " :: STRs/filtered = " +
//                        listOfSTRs.size() + "/" + candidateSTRs.size() + " :: " +
//                        String.format("100%%/%.2f%%", (float)candidateSTRs.size()/listOfSTRs.size() * 100) + " ";
//                Utilities.showProgress(validSpecies.size(), i, 50, moreInfo + "\t");
//            }
//
//            for (STR candidateSTR : candidateSTRs)
//                listOfDistinctSTRsSeq.add(candidateSTR.getSequence());
//
////            ArrayList<String> listOfSTRs = DatabaseControllerForSTRs.getAllTypeOfSTRsInDB(id);
//            for (String str : listOfDistinctSTRsSeq)
//            {
//                matrix[speciesMapIndex.get(id)][strMapIndex.get(str)] = true;
//            }
//
//            if (availableSystemCPUCores == 1)
//            {
//                moreInfo = " -> speciesId = " + id + " :: STRs/filtered = " +
//                        listOfSTRs.size() + "/" + listOfDistinctSTRsSeq.size() + " :: " +
//                        String.format("100/%.2f%%", (float)listOfDistinctSTRsSeq.size()/listOfSTRs.size() * 100) + " ";
//                Utilities.showProgress(validSpecies.size(), ++i, 50, moreInfo + "\t");
//            }
//
//
//            candidateSTRs.clear();
//            listOfSTRs.clear();
//            listOfDistinctSTRsSeq.clear();
//            //TODO : ***** delete this line for final release!
//            if (i == 110)
//                break;
//        }
        //endregion

        System.out.println("\n\n$ : Calculating unique STRs different type");

        if (isGeneBased)
        {
            executorService = Executors.newFixedThreadPool(availableSystemCPUCores);
            System.out.println("\n$ : The unique strs computing process based on gene names start with " +
                                       availableSystemCPUCores + " available system CPU cores!\n");
            HashSet<String> genesName = DatabaseControllerForGenes.getAllGenesName();
            System.out.println("$ : There are " + genesName.size() +
                                       " genes name in DB for all species!(valid and invalid)");

            HashMap<Integer, HashSet<String>> integratedUniqueSTRsForAllSpecies = new HashMap<>();
            if (availableSystemCPUCores == 1)
            {
                i = 0;
                for (String geneName : genesName)
                {
                    Utilities.showProgress(genesName.size(), i++, 50, "\t");
                    STRFamily strFamily = new STRFamily(geneName, validSpecies);

                    HashMap<Integer, ArrayList<String>> calculatedUniqueSTRs =
                            strFamily.calculateUniqueSTRs();
                    for (Integer speciesID : calculatedUniqueSTRs.keySet())
                    {
                        if (integratedUniqueSTRsForAllSpecies.containsKey(speciesID))
                        {
                            integratedUniqueSTRsForAllSpecies
                                    .get(speciesID).addAll(calculatedUniqueSTRs.get(speciesID));
                        }
                        else
                        {
                            integratedUniqueSTRsForAllSpecies
                                    .put(speciesID, new HashSet<>(calculatedUniqueSTRs.get(speciesID)));
                        }
                    }
                }
            }
            else
            {
                ArrayList<ThreadWorkerForCalculatingUniqueSTRsByGeneName> uniqueSTRsCalculatingTasks =
                        new ArrayList<>();

                for (String geneName : genesName)
                {
                    ThreadWorkerForCalculatingUniqueSTRsByGeneName task =
                            new ThreadWorkerForCalculatingUniqueSTRsByGeneName(geneName, validSpecies);
                    uniqueSTRsCalculatingTasks.add(task);
                }

                List<Future<HashMap<Integer, ArrayList<String>>>> allSpeciesUniqueSTRs;
                try
                {
                    allSpeciesUniqueSTRs = executorService.invokeAll(uniqueSTRsCalculatingTasks);
                    for (Future<HashMap<Integer, ArrayList<String>>> speciesUniqueSTRs : allSpeciesUniqueSTRs)
                    {
                        HashMap<Integer, ArrayList<String>> calculatedUniqueSTRs =
                                speciesUniqueSTRs.get();
                        for (Integer speciesID : calculatedUniqueSTRs.keySet())
                        {
                            if (integratedUniqueSTRsForAllSpecies.containsKey(speciesID))
                            {
                                integratedUniqueSTRsForAllSpecies
                                        .get(speciesID).addAll(calculatedUniqueSTRs.get(speciesID));
                            }
                            else
                            {
                                integratedUniqueSTRsForAllSpecies
                                        .put(speciesID, new HashSet<>(calculatedUniqueSTRs.get(speciesID)));
                            }
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
            }

            for (Integer speciesID : integratedUniqueSTRsForAllSpecies.keySet())
                result.get(speciesID).addAll(integratedUniqueSTRsForAllSpecies.get(speciesID));

            System.out.println("\n$ : ");
            for (int id : result.keySet())
                System.out.println("$ : id = " + id
                                           + "\t\t unique count = " + result.get(id).size());

            Writer.writeFingerPrintFile(result, "_MultiThread_GeneBased");
            Writer.writeFingerPrintFile(result, 1, 6, "_MultiThread_GeneBased");
            Writer.writeFingerPrintFile(result, 7, 9, "_MultiThread_GeneBased");
            Writer.writeFingerPrintFile(result, 10, 15, "_MultiThread_GeneBased");
            Writer.writeFingerPrintFile(result, 16, 120, "_MultiThread_GeneBased");

            makeBiologicallyUnique(result);//TODO : i comment it for now!
            Writer.writeFingerPrintFile(result, "_Bio_MultiThread_GeneBased");
            Writer.writeFingerPrintFile(result, 1, 6, "_Bio_MultiThread_GeneBased");
            Writer.writeFingerPrintFile(result, 7, 9, "_Bio_MultiThread_GeneBased");
            Writer.writeFingerPrintFile(result, 10, 15, "_Bio_MultiThread_GeneBased");
            Writer.writeFingerPrintFile(result, 16, 120, "_Bio_MultiThread_GeneBased");

            Writer.writeRepeatPercentage(result, "_Bio_MultiThread_GeneBased");
            Writer.writeRepeatPercentage(result, 1, 6, "_Bio_MultiThread_GeneBased");
            Writer.writeRepeatPercentage(result, 7, 9, "_Bio_MultiThread_GeneBased");
            Writer.writeRepeatPercentage(result, 10, 15, "_Bio_MultiThread_GeneBased");
            Writer.writeRepeatPercentage(result, 16, 120, "_Bio_MultiThread_GeneBased");
        }

        allSTRsDifferentTypeSize = allSTRsDifferentType.size();
        for (i = 0 ; i < allSTRsDifferentTypeSize ; i++)
        {
            Utilities.showProgress(allSTRsDifferentTypeSize, i, 50, "\t");

            int counter = 0;
            for (int j = 0 ; j < validSpecies.size() ; j++)
            {
                if (matrix[j][i])
                {
                    counter++;
                    if (counter > 1)
                        break;
                }
            }
            if (counter == 1)
                unique[i] = true;
        }

        System.out.println("\n$ : Calculating unique STRs for species");
        for (i = 0 ; i < unique.length ; i++)
        {
            Utilities.showProgress(unique.length, i, 50, "\t");

            if (unique[i])
            {
                for (int j = 0 ; j < validSpecies.size() ; j++)
                {
                    if (matrix[j][i])
                    {
                        Integer speciesID = findSpeciesIDByIndex(j, speciesMapIndex);
                        String str = findSTRByIndex(i, strMapIndex);
                        if (speciesID != null && str != null)
                            result.get(speciesID).add(str);
                        break;
                    }
                }
            }
        }

        System.out.println("\n$ : ");
        for (int id : result.keySet())
            System.out.println("$ : id = " + id
                                       + "\t\t unique count = " + result.get(id).size());

        Writer.writeFingerPrintFile(result, "_MultiThred");
        Writer.writeFingerPrintFile(result, 1, 6, "_MultiThread");
        Writer.writeFingerPrintFile(result, 7, 9, "_MultiThread");
        Writer.writeFingerPrintFile(result, 10, 15, "_MultiThread");
        Writer.writeFingerPrintFile(result, 16, 120, "_MultiThread");

        makeBiologicallyUnique(result);//TODO : i comment it for now!
        Writer.writeFingerPrintFile(result, "_Bio_MultiThread");
        Writer.writeFingerPrintFile(result, 1, 6, "_Bio_MultiThread");
        Writer.writeFingerPrintFile(result, 7, 9, "_Bio_MultiThread");
        Writer.writeFingerPrintFile(result, 10, 15, "_Bio_MultiThread");
        Writer.writeFingerPrintFile(result, 16, 120, "_Bio_MultiThread");

        Writer.writeRepeatPercentage(result, "_Bio_MultiThread");
        Writer.writeRepeatPercentage(result, 1, 6, "_Bio_MultiThread");
        Writer.writeRepeatPercentage(result, 7, 9, "_Bio_MultiThread");
        Writer.writeRepeatPercentage(result, 10, 15, "_Bio_MultiThread");
        Writer.writeRepeatPercentage(result, 16, 120, "_Bio_MultiThread");


        return true;
    }

//    private static void findUniqueSTRs(Boolean[][] totalSTRsMatrix,
//                                      HashMap<Integer, Integer> speciesMapIndex,
//                                      HashMap<String, Integer> strMapIndex,
//                                      Boolean[] strWatchList,
//                                      Boolean[] unique,
//                                      Set<Integer> validSpecies)
//    {
//
//
//    }

    private static Integer findSpeciesIDByIndex(int index, HashMap<Integer, Integer> speciesMapIndex)
    {
        for(int id : speciesMapIndex.keySet())
        {
            if (speciesMapIndex.get(id) == index)
                return id;
        }
        return null;
    }

    private static String findSTRByIndex(int index, HashMap<String, Integer> strMapIndex)
    {
        for (String str : strMapIndex.keySet())
            if (strMapIndex.get(str) == index)
                return str;
        return null;
    }

    private static void makeBiologicallyUnique(HashMap<Integer, ArrayList<String>> primaryResult)
    {
        HashSet<String> candidateForDelete = new HashSet<>();

        for (Integer speciesID : primaryResult.keySet())
        {
            ArrayList<String> temp = primaryResult.get(speciesID);
            for (String coreSTR : temp)
            {
                for (String candidateSTR : temp)
                {
                    if (isCoreSTR(coreSTR, candidateSTR))
                        candidateForDelete.add(candidateSTR);
                }
            }
            temp.removeAll(candidateForDelete);

            // delete A#<6 #<3
            String[] output;
            String core;
            int repeat;
            candidateForDelete.clear();
            for (String str : temp)
            {
                output = Utilities.decomposeSTR(str);
                core = output[0];
                repeat = Integer.parseInt(output[1]);

                if (core.length() == 1 && repeat < 6)
                    candidateForDelete.add(str);
//                else if (core.length() == 2 && repeat < 3)
//                    candidateForDelete.add(str);
                else if (core.length() >= 2 && core.length() < 10 && repeat < 3)
                    candidateForDelete.add(str);
            }
            temp.removeAll(candidateForDelete);

            candidateForDelete.clear();
        }
    }

    public static boolean isCoreSTR(String core, String child)
    {
        String extendCore;
        String extendChild;
        String coreOfCore;
        String coreOfChild;
        int repeatOfCore;
        int repeatOfChild;

        String[] output;

        if (core.equals(child))
            return false;

        output = Utilities.decomposeSTR(core);
        coreOfCore = output[0];
        repeatOfCore = Integer.parseInt(output[1]);

        output = Utilities.decomposeSTR(child);
        coreOfChild = output[0];
        repeatOfChild = Integer.parseInt(output[1]);

        extendCore = String.join("", Collections.nCopies(repeatOfCore, coreOfCore));
        extendChild = String.join("", Collections.nCopies(repeatOfChild, coreOfChild));

        return extendCore.equals(extendChild) && coreOfCore.length() < coreOfChild
                .length() && repeatOfCore > repeatOfChild;
    }
    //endregion

    public static boolean makeCompositionOfNocleotideFile(String filePath)
    {
        int[][] nucleotideComposition;
        String moreInfo;
        ArrayList<ArrayList<String>> fingerPrintFile;

        fingerPrintFile = Reader.readFingerPrintFile(filePath);


        // A, T, C, G, AA, AT, AC, AG, TA, TT, TC, TG, CA, CT, CC, CG, GA, GT, GC, GG
        // 0, 1, 2, 3, 4 , 5 , 6 , 7 , 8 , 9 , 10, 11, 12, 13, 14, 15, 16, 17, 18, 19
        nucleotideComposition = new int[fingerPrintFile.size()][20];
        for (int[] row : nucleotideComposition)
            Arrays.fill(row, 0);

        moreInfo = " -> speciesId = 0 finished";
        Utilities.showProgress(fingerPrintFile.size(), 0, 50, moreInfo + "\t");

        for (int i = 0; i < fingerPrintFile.size(); i++)
        {
            List<String> strs = fingerPrintFile.get(i).subList(2, fingerPrintFile.get(i).size());
            for (String str : strs)
            {
                String[] decompositionOfStr;
                decompositionOfStr = Utilities.decomposeSTR(str);
                String coreOfSTR = decompositionOfStr[0];
                int repeatOfSTR = Integer.parseInt(decompositionOfStr[1]);

                String sequence = String.join("", Collections.nCopies(repeatOfSTR, coreOfSTR));

                char firstIndex = sequence.charAt(0);
                switch (firstIndex)
                {
                    case 'A':
                        nucleotideComposition[i][0]++;
                        break;
                    case 'T':
                        nucleotideComposition[i][1]++;
                        break;
                    case 'C':
                        nucleotideComposition[i][2]++;
                        break;
                    case 'G':
                        nucleotideComposition[i][3]++;
                        break;
                }

                for (int j = 1; j < sequence.length(); j++)
                {
                    switch (sequence.charAt(j))
                    {
                        case 'A':
                            nucleotideComposition[i][0]++;
                            switch (sequence.charAt(j - 1))
                            {
                                case 'A':
                                    nucleotideComposition[i][4]++;
                                    break;
                                case 'T':
                                    nucleotideComposition[i][8]++;
                                    break;
                                case 'C':
                                    nucleotideComposition[i][12]++;
                                    break;
                                case 'G':
                                    nucleotideComposition[i][16]++;
                                    break;
                            }
                            break;
                        case 'T':
                            nucleotideComposition[i][1]++;
                            switch (sequence.charAt(j - 1))
                            {
                                case 'A':
                                    nucleotideComposition[i][5]++;
                                    break;
                                case 'T':
                                    nucleotideComposition[i][9]++;
                                    break;
                                case 'C':
                                    nucleotideComposition[i][13]++;
                                    break;
                                case 'G':
                                    nucleotideComposition[i][17]++;
                                    break;
                            }
                            break;
                        case 'C':
                            nucleotideComposition[i][2]++;
                            switch (sequence.charAt(j - 1))
                            {
                                case 'A':
                                    nucleotideComposition[i][6]++;
                                    break;
                                case 'T':
                                    nucleotideComposition[i][10]++;
                                    break;
                                case 'C':
                                    nucleotideComposition[i][14]++;
                                    break;
                                case 'G':
                                    nucleotideComposition[i][18]++;
                                    break;
                            }
                            break;
                        case 'G':
                            nucleotideComposition[i][3]++;
                            switch (sequence.charAt(j - 1))
                            {
                                case 'A':
                                    nucleotideComposition[i][7]++;
                                    break;
                                case 'T':
                                    nucleotideComposition[i][11]++;
                                    break;
                                case 'C':
                                    nucleotideComposition[i][15]++;
                                    break;
                                case 'G':
                                    nucleotideComposition[i][19]++;
                                    break;
                            }
                            break;
                    }
                }
            }

            moreInfo = " -> speciesId = " + fingerPrintFile.get(i).get(0) + " finished";
            Utilities.showProgress(fingerPrintFile.size(), 0, 50, moreInfo + "\t");
        }

        String sizes = filePath.substring(filePath.indexOf("@") + 1,
                                          filePath.lastIndexOf("@"));
        int minSize = Integer.parseInt(sizes.split(",")[0]);
        int maxSize = Integer.parseInt(sizes.split(",")[1]);

        Writer.writeCompositionOfNucleotidesFile(nucleotideComposition, fingerPrintFile,minSize,maxSize,"");

        return true;
    }

    public static ArrayList<String> filterBiologocalSTRs(int speciesID)
    {
        return null;
    }
}
