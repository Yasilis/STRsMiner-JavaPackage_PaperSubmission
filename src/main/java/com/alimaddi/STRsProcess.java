package com.alimaddi;

import com.alimaddi.Utility.Utilities;
import com.alimaddi.datatypes.STROrigin;
import com.alimaddi.model.STR;
import com.alimaddi.model.Transcript;
import com.sun.istack.NotNull;

import java.util.*;

import static com.alimaddi.control.DataCollector.isCoreSTR;

public class STRsProcess
{

    public static HashMap<String,Integer> calculateSTRs(String promoter)
    {
        HashMap<String,Integer> results = new HashMap<>();

        int coreStart;
        int coreEnd;
        int repeatStart;
        int repeatEnd;
        String str;

        for (int i = 0; i <= promoter.length() - 2 ;i++)
        {
            coreStart = i;
            for (int j = 1 ; (i + j * 2) <= promoter.length() ; j++)
            {
                coreEnd = coreStart + j;

                repeatStart = coreEnd;
                repeatEnd = repeatStart + j;

                if (promoter.substring(coreStart, coreEnd).equals(
                        promoter.substring(repeatStart, repeatEnd)))
                {
                    str = packSTR(promoter.substring(coreStart, coreEnd), 2);
                    if (results.containsKey(str))
                        results.put(str, results.get(str) + 1);
                    else
                        results.put(str, 1);

                    for (int k = 1 ; (i + (k + 2) * (coreEnd - coreStart)) <= promoter.length() ; k++)
                    {
                        int jump = k * (coreEnd - coreStart);
                        if (promoter.substring(coreStart, coreEnd).equals(
                                promoter.substring(repeatStart + jump, repeatEnd + jump)))
                        {
                            str = (packSTR(promoter.substring(coreStart, coreEnd), 2 + k));
                            if (results.containsKey(str))
                                results.put(str, results.get(str) + 1);
                            else
                                results.put(str, 1);
                        }
                        else
                        {
                            break;
                        }
                    }
                }
            }
        }

        return results;
    }

//    public static HashMap<String,Integer> calculateAllSTRs(@NotNull Transcript transcript, @NotNull STROrigin origin)
//    {
////        HashMap<String,Integer> results = new HashMap<>();
//        HashSet<STR> results = new HashSet<>();
//
//        int coreStart;
//        int coreEnd;
//        int repeatStart;
//        int repeatEnd;
//        String promoter;
//        String sequence;
//
//        if (STROrigin.CDS_START_CODON.equals(origin) || STROrigin.CDS_EXCEPT_START_CODON.equals(origin))
//            promoter = transcript.getCdsPromoter();
//        else
//            promoter = transcript.getCdnaPromoter();
//
//        if (promoter == null)
//            promoter = "";
//
//        for (int i = 0; i <= promoter.length() - 2 ;i++)
//        {
//            coreStart = i;
//            for (int j = 1 ; (i + j * 2) <= promoter.length() ; j++)
//            {
//                coreEnd = coreStart + j;
//
//                repeatStart = coreEnd;
//                repeatEnd = repeatStart + j;
//
//                if (promoter.substring(coreStart, coreEnd).equals(
//                        promoter.substring(repeatStart, repeatEnd)))
//                {
//                    String coreSequence = promoter.substring(coreStart, coreEnd);
//                    STR strTemp = makeSTR(transcript, origin, coreSequence, 2, (short) coreStart);
//                    if (results.contains(strTemp))
//                    {
//                        for (STR result : results)
//                        {
//                            if (result.equals(strTemp))
//                            {
//                                result.addStartLocus((short)coreStart);
//                                break;
//                            }
//                        }
//                    }
//                    else
//                    {
//                        results.add(strTemp);
//                    }
//
//                    for (int k = 1 ; (i + (k + 2) * (coreEnd - coreStart)) <= promoter.length() ; k++)
//                    {
//                        int jump = k * (coreEnd - coreStart);
//                        if (promoter.substring(coreStart, coreEnd).equals(
//                                promoter.substring(repeatStart + jump, repeatEnd + jump)))
//                        {
//                            coreSequence = promoter.substring(coreStart, coreEnd);
//                            strTemp = makeSTR(transcript, origin, coreSequence, 2 + k, (short) coreStart);
//                            if (results.contains(strTemp))
//                            {
//                                for (STR result : results)
//                                {
//                                    if (result.equals(strTemp))
//                                    {
//                                        result.addStartLocus((short)coreStart);
//                                        break;
//                                    }
//                                }
//                            }
//                            else
//                            {
//                                results.add(strTemp);
//                            }
//                        }
//                        else
//                        {
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//
//        return results;
//    }

    public static HashSet<STR> calculateBiologicalSTRs(@NotNull Transcript transcript, @NotNull STROrigin origin)
    {
        HashSet<STR> candidateSTRs = new HashSet<>();
        HashSet<STR> candidateForDelete = new HashSet<>();

        int coreStart;
        int coreEnd;
        int repeatStart;
        int repeatEnd;
        int maxRepeat;
        String promoter;

        if (STROrigin.CDS_START_CODON.equals(origin) || STROrigin.CDS_EXCEPT_START_CODON.equals(origin))
            promoter = transcript.getCdsPromoter();
        else
            promoter = transcript.getCdnaPromoter();

        if (promoter == null)
            promoter = "";

        for (int i = 0; i <= promoter.length() - 2 ;i++)
        {
            coreStart = i;
            for (int j = 1 ; (i + j * 2) <= promoter.length() ; j++)
            {
                maxRepeat = 0;
                coreEnd = coreStart + j;

                repeatStart = coreEnd;
                repeatEnd = repeatStart + j;

                String coreSequence = promoter.substring(coreStart, coreEnd);
                if (coreSequence.equals(promoter.substring(repeatStart, repeatEnd)))
                {
                    maxRepeat = 2;

                    for (int k = 1 ; (i + (k + 2) * (coreEnd - coreStart)) <= promoter.length() ; k++)
                    {
                        int jump = k * (coreEnd - coreStart);
                        if (coreSequence.equals(promoter.substring(repeatStart + jump, repeatEnd + jump)))
                            maxRepeat = 2 + k;
                        else
                            break;
                    }

//                    boolean isBioSTR = Utilities.canBeBioSTR(coreSequence, maxRepeat);
                    if (Utilities.canBeBioSTR(coreSequence, maxRepeat))
                    {
                        STR strTemp =
                                makeSTR(transcript, origin, coreSequence, maxRepeat, true, (short)coreStart);

                        if (candidateSTRs.contains(strTemp))
                        {
                            for (STR candid : candidateSTRs)
                            {
                                if (candid.equals(strTemp))
                                {
                                    candid.addStartLocus((short)coreStart);
                                    break;
                                }
                            }
                        }
                        else
                        {
                            boolean useless = false;
                            for (STR candid : candidateSTRs)
                            {
                                if (Utilities.isContainerSTR(candid, strTemp))
                                {
                                    useless = true;
                                    break;
                                }
                            }
                            if (!useless)
                            {
                                candidateSTRs.add(strTemp);
                            }
                        }
                    }
                }
            }
        }

        for (STR reference : candidateSTRs)
        {
            for (STR candid : candidateSTRs)
            {
                if (Utilities.isContainerSTR(reference, candid))
                {
                    candidateForDelete.add(candid);
                }
                else if (!reference.getSequence().equals(candid.getSequence()))
                {
                    String[] output;

                    output = Utilities.decomposeSTR(reference.getSequence());
                    String coreOfReference = output[0];
                    int repeatOfReference = Integer.parseInt(output[1]);
                    String extendReferenceSTR =
                            String.join("", Collections.nCopies(repeatOfReference, coreOfReference));

                    output = Utilities.decomposeSTR(candid.getSequence());
                    String coreOfCandid = output[0];
                    int repeatOfCandid = Integer.parseInt(output[1]);
                    String extendCandidSTR =
                            String.join("", Collections.nCopies(repeatOfCandid, coreOfCandid));

                    String extendedCoreOfReference =
                            String.join("", Collections.nCopies(
                                    coreOfCandid.length() / coreOfReference.length(), coreOfReference));

                    if (coreOfCandid.length() % coreOfReference.length() == 0 &&
                            coreOfCandid.equals(extendedCoreOfReference) &&
                            extendReferenceSTR.indexOf(extendCandidSTR) == 0 &&
                            extendReferenceSTR.length() > extendCandidSTR.length())
                    {
                        candidateForDelete.add(candid);
                    }
                }
            }
        }
        candidateSTRs.removeAll(candidateForDelete);
        candidateForDelete.clear();

        return candidateSTRs;
    }

    private static STR makeSTR(
            Transcript parentTranscript, STROrigin origin, String core, int repeat, boolean isBioSTR, short startIndex)
    {
        String sequence = packSTR(core, repeat);
        STR str = new STR(origin, sequence, isBioSTR, new Date(), parentTranscript);
        str.addStartLocus(startIndex);
        return str;
    }


    public static String packSTR(String core, int repeat)
    {
        return "(" + core + ")" + repeat;
    }
}
