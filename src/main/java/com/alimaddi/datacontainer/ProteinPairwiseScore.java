package com.alimaddi.datacontainer;

import java.util.Objects;

public class ProteinPairwiseScore
{
    private int referenceSpeciesID;
    private int targetSpeciesID;
    private int baseScoreIndex;
    private float score1;
    private float score2;
    private float score3;
    private float totalScore1;
    private float totalScore2;
    private float totalScore3;
    private String geneName;
    private String referenceSpeciesName;
    private String referenceGeneEnsemblID;
    private String referenceTranscriptEnsemblID;
    private String referencePeptideSeq;
    private String targetSpeciesName;
    private String targetGeneEnsemblID;
    private String targetTranscriptEnsemblID;
    private String targetPeptideSeq;


    public ProteinPairwiseScore(
            int referenceSpeciesID, int targetSpeciesID, int baseScoreIndex, float score1, float score2, float score3,
            String geneName, String referenceSpeciesName,
            String referenceGeneEnsemblID, String referenceTranscriptEnsemblID, String referencePeptideSeq,
            String targetSpeciesName, String targetGeneEnsemblID, String targetTranscriptEnsemblID,
            String targetPeptideSeq)
    {
        this.referenceSpeciesID = referenceSpeciesID;
        this.targetSpeciesID = targetSpeciesID;
        this.baseScoreIndex = baseScoreIndex;
        this.score1 = score1;
        this.score2 = score2;
        this.score3 = score3;
        this.geneName = geneName;
        this.referenceSpeciesName = referenceSpeciesName;
        this.referenceGeneEnsemblID = referenceGeneEnsemblID;
        this.referenceTranscriptEnsemblID = referenceTranscriptEnsemblID;
        this.referencePeptideSeq = referencePeptideSeq;
        this.targetSpeciesName = targetSpeciesName;
        this.targetGeneEnsemblID = targetGeneEnsemblID;
        this.targetTranscriptEnsemblID = targetTranscriptEnsemblID;
        this.targetPeptideSeq = targetPeptideSeq;
    }

    public int getReferenceSpeciesID()
    {
        return referenceSpeciesID;
    }

    public void setReferenceSpeciesID(int referenceSpeciesID)
    {
        this.referenceSpeciesID = referenceSpeciesID;
    }

    public int getTargetSpeciesID()
    {
        return targetSpeciesID;
    }

    public void setTargetSpeciesID(int targetSpeciesID)
    {
        this.targetSpeciesID = targetSpeciesID;
    }

    public int getBaseScoreIndex()
    {
        return baseScoreIndex;
    }

    public void setBaseScoreIndex(int baseScoreIndex)
    {
        this.baseScoreIndex = baseScoreIndex;
    }

    public float getScore1()
    {
        return score1;
    }

    public void setScore1(float score1)
    {
        this.score1 = score1;
    }

    public float getScore2()
    {
        return score2;
    }

    public void setScore2(float score2)
    {
        this.score2 = score2;
    }

    public float getScore3()
    {
        return score3;
    }

    public void setScore3(float score3)
    {
        this.score3 = score3;
    }

    public float getTotalScore1()
    {
        return totalScore1;
    }

    public void setTotalScore1(float totalScore1)
    {
        this.totalScore1 = totalScore1;
    }

    public float getTotalScore2()
    {
        return totalScore2;
    }

    public void setTotalScore2(float totalScore2)
    {
        this.totalScore2 = totalScore2;
    }

    public float getTotalScore3()
    {
        return totalScore3;
    }

    public void setTotalScore3(float totalScore3)
    {
        this.totalScore3 = totalScore3;
    }

    public String getGeneName()
    {
        return geneName;
    }

    public void setGeneName(String geneName)
    {
        this.geneName = geneName;
    }

    public String getReferenceSpeciesName()
    {
        return referenceSpeciesName;
    }

    public void setReferenceSpeciesName(String referenceSpeciesName)
    {
        this.referenceSpeciesName = referenceSpeciesName;
    }

    public String getReferenceGeneEnsemblID()
    {
        return referenceGeneEnsemblID;
    }

    public void setReferenceGeneEnsemblID(String referenceGeneEnsemblID)
    {
        this.referenceGeneEnsemblID = referenceGeneEnsemblID;
    }

    public String getReferenceTranscriptEnsemblID()
    {
        return referenceTranscriptEnsemblID;
    }

    public void setReferenceTranscriptEnsemblID(String referenceTranscriptEnsemblID)
    {
        this.referenceTranscriptEnsemblID = referenceTranscriptEnsemblID;
    }

    public String getReferencePeptideSeq()
    {
        return referencePeptideSeq;
    }

    public void setReferencePeptideSeq(String referencePeptideSeq)
    {
        this.referencePeptideSeq = referencePeptideSeq;
    }

    public String getTargetSpeciesName()
    {
        return targetSpeciesName;
    }

    public void setTargetSpeciesName(String targetSpeciesName)
    {
        this.targetSpeciesName = targetSpeciesName;
    }

    public String getTargetGeneEnsemblID()
    {
        return targetGeneEnsemblID;
    }

    public void setTargetGeneEnsemblID(String targetGeneEnsemblID)
    {
        this.targetGeneEnsemblID = targetGeneEnsemblID;
    }

    public String getTargetTranscriptEnsemblID()
    {
        return targetTranscriptEnsemblID;
    }

    public void setTargetTranscriptEnsemblID(String targetTranscriptEnsemblID)
    {
        this.targetTranscriptEnsemblID = targetTranscriptEnsemblID;
    }

    public String getTargetPeptideSeq()
    {
        return targetPeptideSeq;
    }

    public void setTargetPeptideSeq(String targetPeptideSeq)
    {
        this.targetPeptideSeq = targetPeptideSeq;
    }

//    @Override
//    public boolean equals(Object o)
//    {
//        if (this == o)
//            return true;
//        if (!(o instanceof ProteinPairwiseScore))
//            return false;
//
//        ProteinPairwiseScore that = (ProteinPairwiseScore) o;
//
//        if (getReferenceSpeciesID() != that.getReferenceSpeciesID())
//            return false;
//        if (getTargetSpeciesID() != that.getTargetSpeciesID())
//            return false;
//        if (Float.compare(that.getScore(), getScore()) != 0)
//            return false;
//        if (!getGeneName().equals(that.getGeneName()))
//            return false;
//        if (!getReferenceGeneEnsemblID().equals(that.getReferenceGeneEnsemblID()))
//            return false;
//        if (!getReferenceTranscriptEnsemblID().equals(that.getReferenceTranscriptEnsemblID()))
//            return false;
//        if (!getTargetGeneEnsemblID().equals(that.getTargetGeneEnsemblID()))
//            return false;
//        return getTargetTranscriptEnsemblID().equals(that.getTargetTranscriptEnsemblID());
//    }
//
//    @Override
//    public int hashCode()
//    {
//        int result = getReferenceSpeciesID();
//        result = 31 * result + getTargetSpeciesID();
//        result = 31 * result + (getScore() != +0.0f ? Float.floatToIntBits(getScore()) : 0);
//        result = 31 * result + getGeneName().hashCode();
//        result = 31 * result + getReferenceGeneEnsemblID().hashCode();
//        result = 31 * result + getReferenceTranscriptEnsemblID().hashCode();
//        result = 31 * result + getTargetGeneEnsemblID().hashCode();
//        result = 31 * result + getTargetTranscriptEnsemblID().hashCode();
//        return result;
//    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof ProteinPairwiseScore))
            return false;
        ProteinPairwiseScore that = (ProteinPairwiseScore) o;

        boolean isBaseScoreEqual = false;
        if (baseScoreIndex == 0)
            isBaseScoreEqual = Float.compare(that.getScore1(), getScore1()) == 0;
        else if (baseScoreIndex == 1)
            isBaseScoreEqual = Float.compare(that.getScore2(), getScore2()) == 0;
        else if (baseScoreIndex == 2)
            isBaseScoreEqual = Float.compare(that.getScore3(), getScore3()) == 0;
        else if (baseScoreIndex == -1)
            isBaseScoreEqual = true;

        return getReferenceSpeciesID() == that.getReferenceSpeciesID() &&
                getTargetSpeciesID() == that.getTargetSpeciesID() &&

                isBaseScoreEqual &&

                getGeneName().equals(that.getGeneName()) &&
                getReferenceGeneEnsemblID().equals(that.getReferenceGeneEnsemblID()) &&
                getReferenceTranscriptEnsemblID().equals(that.getReferenceTranscriptEnsemblID()) &&
                getTargetGeneEnsemblID().equals(that.getTargetGeneEnsemblID()) &&
                getTargetTranscriptEnsemblID().equals(that.getTargetTranscriptEnsemblID());
    }

    @Override
    public int hashCode()
    {
        float score = getScore1();
        if (baseScoreIndex == 0)
            score = getScore1();
        else if (baseScoreIndex == 1)
            score = getScore2();
        else if (baseScoreIndex == 2)
            score = getScore3();

        return Objects.hash(getReferenceSpeciesID(), getTargetSpeciesID(), score, getGeneName(),
                            getReferenceGeneEnsemblID(), getReferenceTranscriptEnsemblID(), getTargetGeneEnsemblID(),
                            getTargetTranscriptEnsemblID());
    }

    @Override
    public String toString()
    {
        return "ProteinPairwiseScore{" + "referenceSpeciesID=" + referenceSpeciesID + ", targetSpeciesID=" + targetSpeciesID + ", baseScore=" + baseScoreIndex + ", score1=" + score1 + ", score2=" + score2 + ", score3=" + score3 + ", totalScore1=" + totalScore1 + ", totalScore2=" + totalScore2 + ", totalScore3=" + totalScore3 + ", geneName='" + geneName + '\'' + ", referenceSpeciesName='" + referenceSpeciesName + '\'' + ", referenceGeneEnsemblID='" + referenceGeneEnsemblID + '\'' + ", referenceTranscriptEnsemblID='" + referenceTranscriptEnsemblID + '\'' + ", referencePeptideSeq='" + referencePeptideSeq + '\'' + ", targetSpeciesName='" + targetSpeciesName + '\'' + ", targetGeneEnsemblID='" + targetGeneEnsemblID + '\'' + ", targetTranscriptEnsemblID='" + targetTranscriptEnsemblID + '\'' + ", targetPeptideSeq='" + targetPeptideSeq + '\'' + '}';
    }

    public String export()
    {
        return geneName + "," + referenceSpeciesName + "," + referenceGeneEnsemblID + "," +
                referenceTranscriptEnsemblID + "," + referencePeptideSeq + "," + targetSpeciesName + "," +
                targetGeneEnsemblID + "," + targetTranscriptEnsemblID + "," + targetPeptideSeq + "," +
                score1 + "," + String.format("%.2f", totalScore1) + "," +
                score2 + "," + String.format("%.2f", totalScore2) + "," +
                score3 + "," + String.format("%.2f", totalScore3);
    }
}
