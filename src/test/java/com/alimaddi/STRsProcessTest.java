package com.alimaddi;

import com.alimaddi.Utility.Utilities;
import com.alimaddi.control.DatabaseControllerForGenes;
import com.alimaddi.control.DatabaseControllerForTranscripts;
import com.alimaddi.datatypes.STROrigin;
import com.alimaddi.model.Gene;
import com.alimaddi.model.STR;
import com.alimaddi.model.Transcript;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

class STRsProcessTest
{

    @BeforeEach
    void setUp()
    {
    }

    @AfterEach
    void tearDown()
    {
    }

    @Test
    void calculateSTRs()
    {
        HashMap<String,Integer> mlResult;
        TreeMap<String, Integer> mlsortedResult;
        HashSet<STR> biologicalResult;
        TreeSet<STR> biologicalSortedResult;

        biologicalSortedResult = new TreeSet<>((o1, o2) -> {
            String core1;
            String core2;
            int repeat1;
            int repeat2;

            String[] output;

            output = Utilities.decomposeSTR(o1.getSequence());
            core1 = output[0];
            repeat1 = Integer.parseInt(output[1]);

            output = Utilities.decomposeSTR(o2.getSequence());
            core2 = output[0];
            repeat2 = Integer.parseInt(output[1]);

            if (core1.equals(core2))
                return Integer.compare(repeat1, repeat2);
            else
                return core1.compareTo(core2);
        });
        mlsortedResult = new TreeMap<>((o1, o2) -> {
            String core1;
            String core2;
            int repeat1;
            int repeat2;

            String[] output = new String[]{"", ""};

            output = Utilities.decomposeSTR(o1);
            core1 = output[0];
            repeat1 = Integer.parseInt(output[1]);

            output = Utilities.decomposeSTR(o2);
            core2 = output[0];
            repeat2 = Integer.parseInt(output[1]);

            if (core1.equals(core2))
                return Integer.compare(repeat1, repeat2);
            else
                return core1.compareTo(core2);
        });

        HashSet<Gene> genes = DatabaseControllerForGenes
                .getAllGenesOfSpeciesIDFromDB(7);

        int counter = 0;
        for (Gene gene : genes)
        {
            HashSet<Transcript> transcripts = DatabaseControllerForTranscripts
                    .getAllTranscriptsOfGeneStableIDFromDB(gene.getGeneStableID());

            for (Transcript transcript : transcripts)
            {
//                String promoter = "AAAAAAAAAA";
                String promoter = transcript.getCdsPromoter();

                mlResult = STRsProcess.calculateSTRs(promoter);
                biologicalResult = STRsProcess.calculateBiologicalSTRs(transcript, STROrigin.CDS_START_CODON);

                biologicalSortedResult.addAll(biologicalResult);
                mlsortedResult.putAll(mlResult);

                System.out.println();
                System.out.println("#####################################################################################");
                System.out.println(promoter);
                //            System.out.println(biologicalResult.toString());
                //            System.out.println(mlResult.toString());
                System.out.println(mlsortedResult.toString());
                System.out.println("------------------------------------------------------------------------------------");
                mlsortedResult.clear();
                for (STR str : biologicalResult)
                    mlsortedResult.put(str.getSequence(), str.getStartLocus().size());
                System.out.println(mlsortedResult.toString());
                mlsortedResult.clear();
                System.out.println("#####################################################################################");
            }

            if (++counter > 5)
                break;
        }
    }
}