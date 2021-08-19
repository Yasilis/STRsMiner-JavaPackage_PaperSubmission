package com.alimaddi.Utility;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilitiesTest
{
    private String referencePeptide1 = "ABCDEFGHI";
    private String referencePeptide2 = "ABC";
    private String referencePeptide3 = "ABGFC";
    private String otherPeptide1 = "ADCDEFGHI";
    private String otherPeptide2 = "ADC";
    private String otherPeptide3 = "";

    @BeforeEach
    void setUp()
    {
        referencePeptide1 = "ABCDEFGHI";
        otherPeptide2 = "ADCDEFGHI";
    }

    @AfterEach
    void tearDown()
    {
    }

    @Test
    void calculateProteinPairwiseScoreBySimple()
    {
        String score1;
        String score2;
        int endIndex1 = Math.min(referencePeptide1.length(), 6);
        int endIndex2 = Math.min(otherPeptide1.length(), 6);

        score1 = Utilities.calculateProteinPairwiseScoreBySimple(
                referencePeptide1.substring(0, endIndex1),
                otherPeptide1.substring(0, endIndex2), 1);
        score2 = Utilities.calculateProteinPairwiseScoreBySimple(
                referencePeptide1.substring(0, endIndex1),
                otherPeptide1.substring(0, endIndex2), 2);
        System.out.println("score1 = " + score1 + " \t|\t score2 = " + score2);

        endIndex1 = Math.min(referencePeptide2.length(), 6);
        endIndex2 = Math.min(otherPeptide2.length(), 6);

        score1 = Utilities.calculateProteinPairwiseScoreBySimple(
                referencePeptide2.substring(0, endIndex1),
                otherPeptide2.substring(0, endIndex2), 1);
        score2 = Utilities.calculateProteinPairwiseScoreBySimple(
                referencePeptide2.substring(0, endIndex1),
                otherPeptide2.substring(0, endIndex2), 2);
        System.out.println("score1 = " + score1 + " \t|\t score2 = " + score2);

        endIndex1 = Math.min(referencePeptide3.length(), 6);
        endIndex2 = Math.min(otherPeptide3.length(), 6);

        score1 = Utilities.calculateProteinPairwiseScoreBySimple(
                referencePeptide3.substring(0, endIndex1),
                otherPeptide3.substring(0, endIndex2), 1);
        score2 = Utilities.calculateProteinPairwiseScoreBySimple(
                referencePeptide3.substring(0, endIndex1),
                otherPeptide3.substring(0, endIndex2), 2);
        System.out.println("score1 = " + score1 + " \t|\t score2 = " + score2);
    }

    @Test
    void calculateProteinPairwiseScoreByNeedlemanWunsch()
    {
    }

}