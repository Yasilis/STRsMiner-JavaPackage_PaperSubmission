package com.alimaddi;

import com.alimaddi.control.*;
import com.alimaddi.export.DataSetsStatistics;
import com.alimaddi.export.PhylogeneticTree;
import com.alimaddi.export.ProteinPairwiseHomologyFigure;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static com.alimaddi.Utility.Utilities.getDuration;
import static com.alimaddi.control.DatabaseControler.updateSpecies;


public class Main
{
    public static void main(String[] args) throws Exception //TODO : delete throws and add try/catch
    {
        int[] updateSpeciesResults = {0, 0, 0, 0};
        int[] updateGenesResult = {0, 0, 0, 0};
        long[] updateTranscriptResult = {0, 0, 0, 0};
        long[] updateSTRResult = {0, 0, 0, 0};

        long startTime = System.currentTimeMillis();
        long endTime;

        boolean geneBased;

        int id;
        int input_1;
        int value2;
        int value3;
        int value4;
        int threadNumber;
        int speciesID;
        int upstreamFlank;
        int downstreamFlank;
        String line;

        String filePath1;
        String filePath2;

        String filename = args[0];
        InputStream stream = new FileInputStream(System.getProperty("user.dir") + "/" + filename);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

        input_1 = Integer.parseInt(bufferedReader.readLine());
        ProteinPairwiseHomologyFigure figure;
//        Scanner in = new Scanner(System.in);
//        int input_1 = in.nextInt();
        switch (input_1)
        {
            //region Checked DB 3
            case 0:
                System.out.println(makeHelp());
                break;
            case 1:
                DatabaseControler.printAllSpecies();
                break;
            case 2:
                updateSpeciesResults = updateSpecies();
                break;
            case 3:
                threadNumber = Integer.parseInt(bufferedReader.readLine());
                updateGenesResult = DatabaseControllerForGenes.updateGenes(threadNumber);
                break;
            case 4:
                speciesID = Integer.parseInt(bufferedReader.readLine());
                updateGenesResult = DatabaseControllerForGenes.updateGenesForSpeciesID(speciesID, true);//4
                break;
            case 5:
                upstreamFlank = Integer.parseInt(bufferedReader.readLine());
                threadNumber = Integer.parseInt(bufferedReader.readLine());
                updateTranscriptResult = DatabaseControllerForTranscripts
                        .updateTranscripts(upstreamFlank, true, threadNumber);//120
//                updateTranscriptResult = DatabaseControlerForTranscripts.updateTranscripts(10000);
                break;
            //endregion
            case 6:
//                speciesID = Integer.parseInt(bufferedReader.readLine());
//                upstreamFlank = Integer.parseInt(bufferedReader.readLine());
//                speciesID = Integer.parseInt(bufferedReader.readLine());
//                updateTranscriptResult = DatabaseControllerForTranscripts
//                        .updateTranscriptsForSpeciesID(upstreamFlank, speciesID);//120
                break;
            case 7:
//                input_1 = Integer.parseInt(bufferedReader.readLine());
//                id = Integer.parseInt(bufferedReader.readLine());
//                updateTranscriptResult = DatabaseControlerForTranscripts.updateTranscriptsForGeneID(input_1, id);//120
                break;
            //region Checked
            case 8:
                threadNumber = Integer.parseInt(bufferedReader.readLine());
                updateSTRResult = DatabaseControllerForSTRs.updateSTRs(threadNumber);
                break;
            //endregion
            case 9:
                DatabaseControllerForSTRs.clearSTRsTable();
                //                id = Integer.parseInt(bufferedReader.readLine());
                //                updateSTRResult = DatabaseControllerForSTRs.updateSTRsForSpeciesID(id);
                break;
            case 10:
                //                id = Integer.parseInt(bufferedReader.readLine());
                //                updateSTRResult = DatabaseControllerForSTRs.updateSTRsForGeneID(id);
                break;
            case 11:
                //                id = Integer.parseInt(bufferedReader.readLine());
                //                updateSTRResult = DatabaseControllerForSTRs.updateSTRsForTranscriptID(id);
                break;
            case 12:
//                updateSTRResult = DatabaseControllerForSTRs.updateSTRsInOut();
//                break;
            case 13:
                //                id = Integer.parseInt(bufferedReader.readLine());
                //                updateSTRResult = DatabaseControllerForSTRs.updateSTRsInOutForSpeciesID(id);
                break;
            case 14:
                //                id = Integer.parseInt(bufferedReader.readLine());
                //                updateSTRResult = DatabaseControllerForSTRs.updateSTRsInOutForGeneID(id);
                break;
            case 15:
                //                id = Integer.parseInt(bufferedReader.readLine());
                //                updateSTRResult = DatabaseControllerForSTRs.updateSTRsInOutForTranscriptID(id);
                break;
            case 16:
//                input_1 = Integer.parseInt(bufferedReader.readLine());
//                DataCollector.makeDataset(input_1);//4
                break;
            case 17:
                DataCollector.makeFingerPrintFile();
                break;
            case 18:
                filePath1 = bufferedReader.readLine();
                DataCollector.makeCompositionOfNocleotideFile(filePath1);
                break;
            case 19:
                input_1 = Integer.parseInt(bufferedReader.readLine());
                value2 = Integer.parseInt(bufferedReader.readLine());
                threadNumber = Integer.parseInt(bufferedReader.readLine());
                geneBased = Boolean.parseBoolean(bufferedReader.readLine());
                filePath1 = bufferedReader.readLine();
                filePath2 = bufferedReader.readLine();
                DataAnalysis.makeTranscriptAnalysisFile(geneBased, filePath1, filePath2, input_1, value2, threadNumber);
                break;
            case 20:
                input_1 = Integer.parseInt(bufferedReader.readLine());
                filePath1 = bufferedReader.readLine();
                DataAnalysis.countSTRCoreFrequency(filePath1, input_1);
                break;
            case 21:
                input_1 = Integer.parseInt(bufferedReader.readLine());
                DataAnalysis.analysisMethionineForSpecies(input_1);
                break;
            case 22:
                filePath1 = bufferedReader.readLine();
                DataAnalysis.calculatedHomologyDistribution(filePath1, 1);
                break;

            ///////////////////////////////// Multi Threading /////////////////////////////////

            case 50:
                input_1 = Integer.parseInt(bufferedReader.readLine());
                DataCollector.filterBiologocalSTRs(input_1);
                break;
            case 67:
                threadNumber = Integer.parseInt(bufferedReader.readLine());
                DataCollector.makeFingerPrintFile(threadNumber, false);
                break;
            case 68:
                threadNumber = Integer.parseInt(bufferedReader.readLine());
                DataCollector.makeFingerPrintFile(threadNumber, true);
                break;
            case 101:
                filePath1 = bufferedReader.readLine();
                DataSetsStatistics.printStatistics(filePath1);
                break;
            case 102:
                line = bufferedReader.readLine();
                PhylogeneticTree.changeScientificNameToCommonName(line);
                break;
            case 103:
                input_1 = Integer.parseInt(bufferedReader.readLine());
                value2 = Integer.parseInt(bufferedReader.readLine());
                value3 = Integer.parseInt(bufferedReader.readLine());
                value4 = Integer.parseInt(bufferedReader.readLine());
                threadNumber = Integer.parseInt(bufferedReader.readLine());
                filePath1 = bufferedReader.readLine();
                filePath2 = bufferedReader.readLine();
                DataAnalysis.proteinPairwiseScore(filePath1, filePath1, input_1, value2, value3, value4, threadNumber);
                break;
            case 104:
                filePath1 = bufferedReader.readLine();
                figure = new ProteinPairwiseHomologyFigure(filePath1);
                figure.filterBestProteinPairwise();
                break;
            case 105:
                filePath1 = bufferedReader.readLine();
                figure = new ProteinPairwiseHomologyFigure(filePath1);
                figure.fillMissedProteinPairwise();
                break;
        }


        endTime = System.currentTimeMillis();
        // TODO 1 : make system log!
        System.out.println(" ");
        System.out.println("######################################################################################");
        System.out.println("######################################################################################");
        System.out.println("######################################\tReport\t######################################");
        System.out.println("######################################################################################");
        System.out.println("######################################################################################");
        System.out.println("## \t  ");
        System.out.println("## \t  ");
        System.out.println("## \t Number of Species which added to the DB\t\t : " + updateSpeciesResults[0]);
        System.out.println("## \t Number of Species which are duplicated\t\t\t : " + updateSpeciesResults[1]);

        System.out.println("## \t Number of Gene which added to the DB\t\t\t : " + updateGenesResult[0]);
        System.out.println("## \t Number of Gene which are duplicated\t\t\t : " + updateGenesResult[1]);
        System.out.println("## \t Number of Gene which are updated\t\t\t : " + updateGenesResult[2]);

        System.out.println("## \t Number of Transcript which added to the DB\t\t : " + updateTranscriptResult[0]);
        System.out.println("## \t Number of Transcript which are duplicated\t\t : " + updateTranscriptResult[1]);
        System.out.println("## \t Number of Transcript which are updated\t\t\t : " + updateTranscriptResult[2]);

        System.out.println("## \t Number of STRs which added to the DB\t\t\t : " + updateSTRResult[0]);
        System.out.println("## \t Number of STRs which are duplicated\t\t\t : " + updateSTRResult[1]);
        System.out.println("## \t Number of STRs which are updated\t\t\t : " + updateSTRResult[2]);
        System.out.println("## \t Number of STRs which are deleted\t\t\t : " + updateSTRResult[3]);
        System.out.println("## \t  ");
        System.out.println("## \t  ");
        System.out.println("## \t Duration : " + getDuration(startTime, endTime));
        System.out.println("## \t  ");
        System.out.println("######################################################################################");
        System.out.println(" ");
        System.out.println(" ");
        System.out.println(" ");

    }

    private static String makeHelp()
    {
        return "Help :\n"
                + "[0]  : Print Help\n"
                + "[1]  : print All Species\n"
                + "[2]  : update Species\n"
                + "[3]  : update Genes\n"
                + "[4]  : update Genes For Species id *\n"
                + "[5]  : update Transcripts for flank *\n"
                + "[6]  : update Transcripts For Species ID\n" + "\t\tupdateTranscriptsForSpeciesID(120, id)\n"
                + "[7]  : \n"
                + "[8]  : \n"
                + "[9]  : \n"
                + "[10] : \n"
                + "[11] : \n"
                + "[12] : \n"
                + "[13] : \n"
                + "[14] : \n"
                + "[15] : \n";
    }



}
