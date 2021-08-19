package com.alimaddi.control.downloader;

import com.alimaddi.model.Species;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class URLGeneratorTest
{

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void generateListOfAllSpecie()
    {
        String test = URLGenerator.generateListOfAllSpecie();
        String original = "http://rest.ensembl.org/info/species?content-type=application/json";
        Assert.assertEquals(original, test);
    }

    @Test
    public void generateGeneListOfSpecie()
    {
        String datasetName = "hsapiens";
        String test = URLGenerator.generateGeneListOfDatasetName(datasetName);
        String original = "http://www.ensembl.org/biomart/martservice?query=<?xml version=\"1.0\" encoding" +
                "=\"UTF-8\"?><!DOCTYPE Query><Query  virtualSchemaName = \"default\" formatter = \"TSV\" " +
                "header = \"0\" uniqueRows = \"0\" count = \"\" datasetConfigVersion = \"0.6\" ><Dataset name =" +
                " \"hsapiens_gene_ensembl\" interface = \"default\" ><Filter name = \"biotype\" value = \"" +
                "protein_coding\"/><Attribute name = \"ensembl_gene_id\" /><Attribute name = \"gene_biotype\" />" +
                "<Attribute name = \"external_gene_name\" /></Dataset></Query>";
        Assert.assertEquals(original, test);
    }

}