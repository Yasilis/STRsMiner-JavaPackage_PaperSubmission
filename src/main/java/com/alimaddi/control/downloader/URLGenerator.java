package com.alimaddi.control.downloader;

import com.alimaddi.datatypes.TranscriptSequenceType;
import com.alimaddi.model.Species;

import java.util.HashSet;

public class URLGenerator
{
    //region Checked DB 3
    public static String generateListOfAllSpecie()
    {
        String baseURL = "http://rest.ensembl.org";
        String query = "/info/species?content-type=application/json";
        return baseURL + query;
    }

    public static String generateGeneListOfDatasetName(String geneDatasetName)
    {
        String baseURL = "http://www.ensembl.org/biomart/martservice?query=";
        String queryPart1 = "%3C?xml%20version=%221.0%22%20encoding=%22UTF-8%22?%3E%3C!DOCTYPE%20Query%3E%3CQuery%20virtualSchemaName%20=%20%22default%22%20formatter%20=%20%22TSV%22%20header%20=%20%220%22%20uniqueRows%20=%20%220%22%20count%20=%20%22%22%20datasetConfigVersion%20=%20%220.6%22%20%3E%3CDataset%20name%20=%20%22";
//                "hsapiens" +
        String queryPart2 = "%22%20interface%20=%20%22default%22%20%3E%3CFilter%20name%20=%20%22biotype%22%20value%20=%20%22protein_coding%22/%3E%3CAttribute%20name%20=%20%22ensembl_gene_id%22%20/%3E%3CAttribute%20name%20=%20%22gene_biotype%22%20/%3E%3CAttribute%20name%20=%20%22external_gene_name%22%20/%3E%3C/Dataset%3E%3C/Query%3E";

        return baseURL + queryPart1 + geneDatasetName + queryPart2;
    }
    //endregion

    public static String generateTranscriptListOfGeneStableID(String geneStableID)
    {
        String baseURL = "http://rest.ensembl.org";
        String queryPart1 = "/overlap/id/";
        String queryPart2 = "?content-type=application/json;feature=transcript;biotype=protein_coding";
        return baseURL + queryPart1 + geneStableID + queryPart2;
    }

    public static String generateTranscriptSequenceOfTranscriptStableID(String transcriptStableIDInCloud)
    {
        String baseURL = "http://rest.ensembl.org";
        String queryPart1 = "/sequence/id/";
        String queryPart2 = "?content-type=text/plain";
        return baseURL + queryPart1 + transcriptStableIDInCloud + queryPart2;
    }

    public static String generateTranscriptCDSOfTranscriptStableID(String transcriptStableIDInCloud)
    {
        String baseURL = "http://rest.ensembl.org";
        String queryPart1 = "/sequence/id/";
        String queryPart2 = "?type=cds;content-type=text/plain";
        return baseURL + queryPart1 + transcriptStableIDInCloud + queryPart2;
    }

    public static String generateTranscriptCDNAOfTranscriptStableID(String transcriptStableIDInCloud)
    {
        String baseURL = "http://rest.ensembl.org";
        String queryPart1 = "/sequence/id/";
        String queryPart2 = "?type=cdna;content-type=text/plain";
        return baseURL + queryPart1 + transcriptStableIDInCloud + queryPart2;
    }

    //region Checked DB 3
    public static String generateTranscriptsUpstreamFlankListOfGenesStableIDList(
            HashSet<String> genesStableIDList, String geneDatasetName, int upstreamFlank, int baseURLIndex)
    {
        StringBuilder genes = new StringBuilder();
        for (String gene : genesStableIDList)
            genes.append(gene).append(",");

        if (genes.length() > 0)
            genes.deleteCharAt(genes.length()-1);
//        String baseURL = "http://www.ensembl.org/biomart/martservice?query=";
//
//        String queryPart1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE Query><Query  virtualSchemaName = \"default\" formatter = \"FASTA\" header = \"0\" uniqueRows = \"0\" count = \"\" datasetConfigVersion = \"0.6\" ><Dataset name = \"";
////                            "hsapiens_gene_ensembl" +
//        String queryPart2 = "\" interface = \"default\" ><Filter name = \"upstream_flank\" value = \"";
////                            "125" +
//        String queryPart3 = "\"/><Filter name = \"transcript_biotype\" value = \"protein_coding\"/><Filter name = \"biotype\" value = \"protein_coding\"/><Filter name = \"ensembl_gene_id\" value = \"";
////                "ENSG00000180016,ENSG00000215203,ENSG00000179029" +
//        String queryPart4 = "\"/><Attribute name = \"ensembl_gene_id\" /><Attribute name = \"ensembl_transcript_id\" /><Attribute name = \"coding_transcript_flank\" /></Dataset></Query>";

        String baseURL = getBaseURL(baseURLIndex);

        String queryPart1 = "%3C?xml%20version=%221.0%22%20encoding=%22UTF-8%22?%3E%3C!DOCTYPE%20Query%3E%3CQuery%20%20virtualSchemaName%20=%20%22default%22%20formatter%20=%20%22FASTA%22%20header%20=%20%220%22%20uniqueRows%20=%20%220%22%20count%20=%20%22%22%20datasetConfigVersion%20=%20%220.6%22%20%3E%3CDataset%20name%20=%20%22";
        String queryPart2 = "%22%20interface%20=%20%22default%22%20%3E%3CFilter%20name%20=%20%22upstream_flank%22%20value%20=%20%22";
        String queryPart3 = "%22/%3E%3CFilter%20name%20=%20%22transcript_biotype%22%20value%20=%20%22protein_coding%22/%3E%3CFilter%20name%20=%20%22biotype%22%20value%20=%20%22protein_coding%22/%3E%3CFilter%20name%20=%20%22ensembl_gene_id%22%20value%20=%20%22";
        String queryPart4 = "%22/%3E%3CAttribute%20name%20=%20%22ensembl_gene_id%22%20/%3E%3CAttribute%20name%20=%20%22ensembl_transcript_id%22%20/%3E%3CAttribute%20name%20=%20%22coding_transcript_flank%22%20/%3E%3C/Dataset%3E%3C/Query%3E";

        return baseURL + queryPart1 + geneDatasetName + queryPart2 + upstreamFlank + queryPart3 + genes + queryPart4;
    }
    //endregion

    public static String generatePeptidesSequenceOfTranscriptStableIDList(
            HashSet<String> transcriptStableIDList, String geneDatasetName,
            TranscriptSequenceType sequenceType, int baseURLIndex)
    {
        StringBuilder transcripts = new StringBuilder();
        for (String transcript : transcriptStableIDList)
            transcripts.append(transcript).append(",");

        if (transcripts.length() > 0)
            transcripts.deleteCharAt(transcripts.length()-1);

        String baseURL = getBaseURL(baseURLIndex);

        String queryPart1 = "%3C?xml%20version=%221.0%22%20encoding=%22UTF-8%22?%3E%3C!DOCTYPE%20Query%3E%3CQuery%20%20virtualSchemaName%20=%20%22default%22%20formatter%20=%20%22FASTA%22%20header%20=%20%220%22%20uniqueRows%20=%20%220%22%20count%20=%20%22%22%20datasetConfigVersion%20=%20%220.6%22%20%3E%3CDataset%20name%20=%20%22";
        String queryPart2 = "%22%20interface%20=%20%22default%22%20%3E%3CFilter%20name%20=%20%22transcript_biotype%22%20value%20=%20%22protein_coding%22/%3E%3CFilter%20name%20=%20%22ensembl_transcript_id%22%20value%20=%20%22";
        String queryPart3 = "%22/%3E%3CAttribute%20name%20=%20%22";
        String queryPart4 = "%22%20/%3E%3CAttribute%20name%20=%20%22ensembl_gene_id%22%20/%3E%3CAttribute%20name%20=%20%22ensembl_transcript_id%22%20/%3E%3C/Dataset%3E%3C/Query%3E";


        switch (sequenceType)
        {
            case EXON_SEQUENCE:
                return baseURL + queryPart1 + geneDatasetName + queryPart2 + transcripts
                        + queryPart3 + "gene_exon" + queryPart4;
            case CDNA_SEQUENCE:
                return baseURL + queryPart1 + geneDatasetName + queryPart2 + transcripts
                        + queryPart3 + "cdna" + queryPart4;
            case CODING_SEQUENCE:
                return baseURL + queryPart1 + geneDatasetName + queryPart2 + transcripts
                        + queryPart3 + "coding" + queryPart4;
            case PEPTIDE_SEQUENCE:
            default:
                return baseURL + queryPart1 + geneDatasetName + queryPart2 + transcripts
                        + queryPart3 + "peptide" + queryPart4;
        }
    }

    private static String getBaseURL(int baseIndex)
    {
        switch (baseIndex)
        {
            case 0:
                return "http://www.ensembl.org/biomart/martservice?query=";
            case 1:
                return "http://uswest.ensembl.org/biomart/martservice?query=";
            case 2:
                return "http://asia.ensembl.org/biomart/martservice?query=";
            default:
                return "http://useast.ensembl.org/biomart/martservice?query=";
        }
    }

    public static String[] generateProteinPairwiseEmbossNeedleScoreSubmitJob(String firstPeptide, String secondPeptide)
    {
        String baseURL = "https://www.ebi.ac.uk/Tools/services/rest/emboss_needle/run";
        String data = "email=ali.m.a.maddi%40gmail.com&asequence=" + firstPeptide + "&bsequence=" + secondPeptide;
        return new String[]{baseURL, data};
    }

    public static String generateProteinPairwiseEmbossNeedleScoreStatusJob(String jobID)
    {
        String baseURL = "https://www.ebi.ac.uk/Tools/services/rest/emboss_needle/status/";
        return baseURL + jobID;
    }

    public static String generateProteinPairwiseEmbossNeedleScoreResultJob(String jobID)
    {
        String baseURL = "https://www.ebi.ac.uk/Tools/services/rest/emboss_needle/result/";
        String resultType = "/out";
        return baseURL + jobID + resultType;
    }
}
