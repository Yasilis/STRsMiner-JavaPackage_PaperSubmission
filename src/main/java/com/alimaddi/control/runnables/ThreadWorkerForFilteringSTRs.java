package com.alimaddi.control.runnables;

import com.alimaddi.Utility.Utilities;
import com.alimaddi.control.DatabaseControllerForSTRs;
import com.alimaddi.control.DatabaseControllerForTranscripts;
import com.alimaddi.datatypes.STROrigin;
import com.alimaddi.model.STR;

import java.util.*;
import java.util.concurrent.Callable;

import static com.alimaddi.STRsProcess.packSTR;
import static com.alimaddi.Utility.Utilities.getDuration;
import static com.alimaddi.control.DataCollector.isCoreSTR;

public class ThreadWorkerForFilteringSTRs implements Callable<HashMap<Integer, HashSet<String>>>
{
    boolean onlyBiologicalSTRs;
    int speciesID;
    long startTime;
    long endTime;
    HashSet<STR> candidateSTRs = new HashSet<>();
    HashSet<STR> candidateForDelete = new HashSet<>();

    public ThreadWorkerForFilteringSTRs(int speciesID, boolean onlyBiologicalSTRs)
    {
        this.speciesID = speciesID;
        this.onlyBiologicalSTRs = onlyBiologicalSTRs;
    }

    @Override
    public HashMap<Integer, HashSet<String>> call() throws Exception
    {
        ArrayList<STR> listOfSTRs;
        HashSet<String> listOfDistinctSTRsSeq = new HashSet<>();

        startTime = System.currentTimeMillis();
        System.out.println("$ :: The process of Thread for species ID "
                                   + speciesID + " starts at " + new Date(startTime).toString() + ".");
        listOfSTRs = DatabaseControllerForSTRs.getAllSTRsInDB(speciesID);

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        listOfSTRs = new ArrayList<>();
//        String promoterTest = "AAAAAAAAAA";
//        for (Map.Entry<String, Integer> strCore : STRsProcess.calculateSTRs(promoterTest).entrySet())
//        {
//            STR strTemp = new STR(STROrigin.CDS_START_CODON,
//                               strCore.getKey(),
//                               strCore.getValue().shortValue(),
//                               "0000");
//            listOfSTRs.add(strTemp);
//        }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        System.out.println("$ :: Filtering start for species ID " + speciesID
                                   + " which have totally " + listOfSTRs.size() + " STRs in DB");

//        for (STR str : listOfSTRs)
//        {
//            String promoter = str.getTranscript().getCdsPromoter();
//
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
////            String promoter = promoterTest;
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//            String[] output = Utilities.decomposeSTR(str.getSequence());
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
//                                          true,
//                                          new Date(),
//                                          str.getTranscript());
//                    candidateSTRs.add(strTemp);
//                }
//
//                promoter = promoter.substring(Math.min(promoter.length(), repeatEnd - core.length()));
//            }
//        }
//
//        System.out.println("$ :: The result of filtering phase 1 for SpeciesId = "
//                                   + speciesID + " shows STRs/filtered = " +
//                                   listOfSTRs.size() + "/" + candidateSTRs.size() + " :: " +
//                                   String.format("100%%/%.2f%%", (float)candidateSTRs.size()/listOfSTRs.size() * 100));
//
//        for (STR coreSTR : candidateSTRs)
//        {
//            for (STR candidateSTR : candidateSTRs)
//            {
//                if (isCoreSTR(coreSTR.getSequence(), candidateSTR.getSequence()))
//                {
//                    candidateForDelete.add(candidateSTR);
//                }
//                else if (coreSTR.getTranscript().getTranscriptStableID().equals(candidateSTR.getTranscript().getTranscriptStableID()) &&
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
//        System.out.println("$ :: The result of filtering phase 2 for SpeciesId = "
//                                   + speciesID + " shows STRs/filtered = " +
//                                   listOfSTRs.size() + "/" + candidateSTRs.size() + " :: " +
//                                   String.format("100%%/%.2f%%", (float)candidateSTRs.size()/listOfSTRs.size() * 100));
//

//------------------------------------------------------------------------------------------------
//        for (STR candidateSTR : candidateSTRs)
//            listOfDistinctSTRsSeq.add(candidateSTR.getSequence());
        for (STR candidateSTR : listOfSTRs)
            listOfDistinctSTRsSeq.add(candidateSTR.getSequence());
//------------------------------------------------------------------------------------------------

        candidateSTRs.clear();
        listOfSTRs.clear();

        endTime = System.currentTimeMillis();
        System.out.println("$ :: The process of Thread for species ID "
                                   + speciesID + " ends at " + new Date(endTime).toString() + ".");
        System.out.println("############## \t The process of Thread for species ID " + speciesID
                                   + " ends in : (" + getDuration(startTime, endTime) + ") \t ##############");

        HashMap<Integer, HashSet<String>> result = new HashMap<>();
        result.put(speciesID, listOfDistinctSTRsSeq);
        return result;
    }
}
