package com.alimaddi.control.downloader;


import com.alimaddi.datatypes.ConnectionContentType;
import com.alimaddi.datatypes.TranscriptSequenceType;

import java.io.IOException;
import java.util.HashSet;

public class Downloader
{

    //region Checked DB 3
    public String downloadAllSpecies()
    {
        String endpoint = URLGenerator.generateListOfAllSpecie();
        String result;

        try
        {
            result = DownloadTask.downloadURL(endpoint, ConnectionContentType.JSON);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("$ : Exception in process of fetch Species list from cloud! We will try again");
            result = downloadAllSpecies();
        }

        return result;
    }

    public String downloadGeneList(String geneDatasetName)
    {
        String endpoint = URLGenerator.generateGeneListOfDatasetName(geneDatasetName);
        String result;

        try
        {
            result = DownloadTask.downloadURL(endpoint, ConnectionContentType.TEXT);
        }
        catch (Exception e)
        {
            System.out.println("\n$ : Error occurs");
            e.printStackTrace();
            System.out.println("\n$ : Exception in process of fetch Gene list of " + geneDatasetName + " from cloud! We will try again");
            result = downloadGeneList(geneDatasetName);
        }

        return result;
    }
    //endregion

    public String downloadAllTranscriptsStableIDOfGeneStableIDFromCloud(String geneStableID)
    {
        String endpoint = URLGenerator.generateTranscriptListOfGeneStableID(geneStableID);
        String result = "";
        try
        {
            result = DownloadTask.downloadURL(endpoint, ConnectionContentType.JSON);
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Exception in process of fetch Transcript from cloud!");
        }

        return result;
    }

    public String downloadSequenceOfTranscriptStableID(String transcriptStableIDInCloud)
    {
        String endpoint = URLGenerator.generateTranscriptSequenceOfTranscriptStableID(transcriptStableIDInCloud);
        String result = "";
        try
        {
            result = DownloadTask.downloadURL(endpoint, ConnectionContentType.TEXT);
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public String downloadCDSOfTranscriptStableID(String transcriptStableIDInCloud)
    {
        String endpoint = URLGenerator.generateTranscriptCDSOfTranscriptStableID(transcriptStableIDInCloud);
        String result = "";
        try
        {
            result = DownloadTask.downloadURL(endpoint, ConnectionContentType.TEXT);
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public String downloadCDNAOfTranscriptStableID(String transcriptStableIDInCloud)
    {
        String endpoint = URLGenerator.generateTranscriptCDNAOfTranscriptStableID(transcriptStableIDInCloud);
        String result = "";
        try
        {
            result = DownloadTask.downloadURL(endpoint, ConnectionContentType.TEXT);
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    //region Checked DB 3
    public String downloadAllTranscriptsCDSUpstreamFlankOfGeneStableIDListFromCloud(
            HashSet<String> genesStableIDList, String geneDatasetName, int upstreamFlank, int baseURLIndex)
    {
        String endpoint = URLGenerator.generateTranscriptsUpstreamFlankListOfGenesStableIDList(
                genesStableIDList, geneDatasetName, upstreamFlank, baseURLIndex);
        String result;
        try
        {
            result = DownloadTask.downloadURL(endpoint, ConnectionContentType.TEXT);
        }
        catch (Exception e)
        {
            System.out.println("\n$ : Error occurs");
            e.printStackTrace();
            System.out.println("\n$ : Exception in process of fetch Transcript from cloud of " + geneDatasetName +
                                       "on " + upstreamFlank + " upstreamFlank!");
            result = downloadAllTranscriptsCDSUpstreamFlankOfGeneStableIDListFromCloud(
                    genesStableIDList, geneDatasetName, upstreamFlank, baseURLIndex);
        }

        return result;
    }
    //endregion

    public String downloadAllSequenceOfTranscriptStableIDListFromCloud(
            HashSet<String> TranscriptStableIDList, String geneDatasetName,
            TranscriptSequenceType sequenceType, int baseURLIndex, int maxTryNumber)
    {
        String endpoint = URLGenerator.generatePeptidesSequenceOfTranscriptStableIDList(
                TranscriptStableIDList, geneDatasetName, sequenceType, baseURLIndex);
        String result;
        try
        {
            if (maxTryNumber <= 0)
                return "";
            else if (maxTryNumber <= 4)
                Thread.sleep(1000);
            result = DownloadTask.downloadURL(endpoint, ConnectionContentType.TEXT);
        }
        catch (Exception e)
        {
            maxTryNumber--;
            System.out.println("\n$ : Error occurs");
            e.printStackTrace();
            System.out.println("\n$ : Exception in process during fetch " + sequenceType +
                                       " from the cloud for " + geneDatasetName +
                                       " and Transcript Stable IDs");
            result = downloadAllSequenceOfTranscriptStableIDListFromCloud(
                    TranscriptStableIDList, geneDatasetName, sequenceType, baseURLIndex, maxTryNumber);
        }

        return result;
    }

    public String downloadProteinPairwiseEmbossNeedleScore(
            String firstPeptide, String secondPeptide)
    {
        String result = "-";
        String jobID;

        try
        {
            String[] requestURL = URLGenerator.generateProteinPairwiseEmbossNeedleScoreSubmitJob(
                    firstPeptide, secondPeptide);
            jobID = EBIDownloadTask.submitJob(requestURL[0], requestURL[1]);
//        }
//        catch (Exception e)
//        {
//            System.out.println("\n$ : Error occurs");
//            //            e.printStackTrace();
//            System.out.println("\n$ : Exception occurred in process of submit job for first protein : " +
//                                       firstPeptide + " and second protein : " + secondPeptide);
//        }

            if (jobID == null || jobID.isEmpty())
                return downloadProteinPairwiseEmbossNeedleScore(firstPeptide, secondPeptide);

            String jobStatusRequestURL = URLGenerator.generateProteinPairwiseEmbossNeedleScoreStatusJob(jobID);
            int numberOfTry = 0;
            String jobStatus = "";
            while (true)
            {
                jobStatus = EBIDownloadTask.GetJobStatus(jobStatusRequestURL);
                System.out.println(jobStatus + " : " + jobID + " : " + numberOfTry);
                if (jobStatus.equals("FINISHED"))
                    break;

//                if (jobStatus.equals("FAILURE"))
//                    return downloadProteinPairwiseEmbossNeedleScore(firstPeptide, secondPeptide);

                if (++numberOfTry > 60)
                    break;

                Thread.sleep(1000);
            }

            if (!jobStatus.equals("FINISHED"))
                return result;

            String jobResultRequestURL = URLGenerator.generateProteinPairwiseEmbossNeedleScoreResultJob(jobID);
            StringBuilder resultOfRequest = EBIDownloadTask.GetResult(jobResultRequestURL);

            String[] lines = resultOfRequest.toString().split("\\n");
            for(String line: lines)
            {
                if (line.contains("# Score"))
                    result = line.trim().split(" ")[2];
            }
        }
        catch (Exception e)
        {
            System.out.println("\n$ : Error occurs");
            e.printStackTrace();
            System.out.println("\n$ : Exception in process during calculate score for first protein : " +
                                       firstPeptide + " and second protein : " + secondPeptide);
            return downloadProteinPairwiseEmbossNeedleScore(firstPeptide, secondPeptide);
        }

        return result;
    }
}
