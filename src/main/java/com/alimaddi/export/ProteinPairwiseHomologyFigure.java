package com.alimaddi.export;

import com.alimaddi.Utility.Reader;
import com.alimaddi.control.DatabaseControler;
import com.alimaddi.datacontainer.ProteinPairwiseScore;

import java.util.*;

public class ProteinPairwiseHomologyFigure
{
    private String proteinPairwiseFile;
    private HashMap<String, Integer> speciesNameToIDMap = new HashMap<>();
    private HashMap<Integer, String> speciesIDToNameMap = new HashMap<>();

    public ProteinPairwiseHomologyFigure(String proteinPairwiseFile)
    {
        this.proteinPairwiseFile = proteinPairwiseFile;
    }

    public void filterBestProteinPairwise()
    {
        ArrayList<ProteinPairwiseScore> pairwiseList;


        pairwiseList = getListOfProteinPairwise(proteinPairwiseFile, 0);
        printBestProteinPairwiseScore(pairwiseList, 0);
        pairwiseList = getListOfProteinPairwise(proteinPairwiseFile, 1);
        printBestProteinPairwiseScore(pairwiseList, 1);
        pairwiseList = getListOfProteinPairwise(proteinPairwiseFile, 2);
        printBestProteinPairwiseScore(pairwiseList, 2);

    }

    private ArrayList<ProteinPairwiseScore> getListOfProteinPairwise(String proteinPairwiseFile, int baseScoreIndex)
    {
        ArrayList<ProteinPairwiseScore> result = new ArrayList<>();
        ArrayList<ArrayList<String>> rawData = Reader.readProteinPairwiseFile(proteinPairwiseFile);
        for (ArrayList<String> line : rawData)
        {
            ProteinPairwiseScore pairwise = new ProteinPairwiseScore(
                    Integer.parseInt(line.get(1)),
                    Integer.parseInt(line.get(5)),
                    baseScoreIndex,
                    Float.parseFloat(line.get(9)),
                    Float.parseFloat(line.get(10)),
                    Float.parseFloat(line.get(11)),
                    line.get(0),
                    DatabaseControler.getSpeciesFromDB(Integer.parseInt(line.get(1))).getCommonName(),
                    line.get(2),
                    line.get(3),
                    line.get(4),
                    DatabaseControler.getSpeciesFromDB(Integer.parseInt(line.get(5))).getCommonName(),
                    line.get(6),
                    line.get(7),
                    line.get(8));
            result.add(pairwise);
        }

        return result;
    }

    private void printBestProteinPairwiseScore(ArrayList<ProteinPairwiseScore> pairwiseList, int baseScoreIndex)
    {
        System.out.println("===============================================================");
        System.out.println("===================   Base Score Index " + baseScoreIndex + "   ====================");
        System.out.println("===============================================================");

        ArrayList<ProteinPairwiseScore> finalPairwiseList = new ArrayList<>();


        ProteinPairwiseScore bestLevel1;
        HashSet<ProteinPairwiseScore> filterLevel1 = new HashSet<>();
        for (ProteinPairwiseScore firstPairwiseLoop : pairwiseList)
        {
            bestLevel1 = firstPairwiseLoop;
            for (ProteinPairwiseScore secondPairwiseLoop : pairwiseList)
            {
                boolean isBaseScoreSmallerOrEqual = false;
                if (baseScoreIndex == 0)
                    isBaseScoreSmallerOrEqual = bestLevel1.getScore1() <= secondPairwiseLoop.getScore1();
                else if (baseScoreIndex == 1)
                    isBaseScoreSmallerOrEqual = bestLevel1.getScore2() <= secondPairwiseLoop.getScore2();
                else if (baseScoreIndex == 2)
                    isBaseScoreSmallerOrEqual = bestLevel1.getScore3() <= secondPairwiseLoop.getScore3();

                if (bestLevel1.getGeneName().equals(secondPairwiseLoop.getGeneName()) &&
                        bestLevel1.getReferenceGeneEnsemblID().equals(secondPairwiseLoop.getReferenceGeneEnsemblID()) &&
                        bestLevel1.getReferenceTranscriptEnsemblID().equals(secondPairwiseLoop.getReferenceTranscriptEnsemblID()) &&
                        bestLevel1.getTargetSpeciesID() == secondPairwiseLoop.getTargetSpeciesID() &&
                        isBaseScoreSmallerOrEqual
                )
                {
                    bestLevel1 = secondPairwiseLoop;
                }
            }
            filterLevel1.add(bestLevel1);
        }



        HashMap<String, Float> compact1Level2 = new HashMap<>();
        for (ProteinPairwiseScore firstFilterLevel1Loop : filterLevel1)
        {
            String geneName = firstFilterLevel1Loop.getGeneName();
            String geneEnsemblID = firstFilterLevel1Loop.getReferenceGeneEnsemblID();
            String transcriptEnsemblID = firstFilterLevel1Loop.getReferenceTranscriptEnsemblID();

            String key = geneName + "|" + geneEnsemblID + "|" + transcriptEnsemblID;

            if (baseScoreIndex == 0)
            {
                if (compact1Level2.containsKey(key))
                    compact1Level2.put(key, compact1Level2.get(key) + firstFilterLevel1Loop.getScore1());
                else
                    compact1Level2.put(key, firstFilterLevel1Loop.getScore1());
            }
            else if (baseScoreIndex == 1)
            {
                if (compact1Level2.containsKey(key))
                    compact1Level2.put(key, compact1Level2.get(key) + firstFilterLevel1Loop.getScore2());
                else
                    compact1Level2.put(key, firstFilterLevel1Loop.getScore2());
            }
            else
            {
                if (compact1Level2.containsKey(key))
                    compact1Level2.put(key, compact1Level2.get(key) + firstFilterLevel1Loop.getScore3());
                else
                    compact1Level2.put(key, firstFilterLevel1Loop.getScore3());
            }


        }
        HashMap<String, String> compact2Level2key = new HashMap<>();
        HashMap<String, Float> compact2Level2value = new HashMap<>();
        for (String key : compact1Level2.keySet())
        {
            String[] parts = key.split("\\|");
            if (compact2Level2value.containsKey(parts[0]))
            {
                if (compact2Level2value.get(parts[0]) <= compact1Level2.get(key))
                {
                    compact2Level2key.put(parts[0], parts[1] + "|" + parts[2]);
                    compact2Level2value.put(parts[0], compact1Level2.get(key));
                }
            }
            else
            {
                compact2Level2key.put(parts[0], parts[1] + "|" + parts[2]);
                compact2Level2value.put(parts[0], compact1Level2.get(key));
            }
        }


        for (String geneName : compact2Level2key.keySet())
        {
            float totalScore = compact2Level2value.get(geneName);
            String[] parts = compact2Level2key.get(geneName).split("\\|");
            String referenceGeneName = parts[0];
            String referenceTranscriptName = parts[1];

            for (ProteinPairwiseScore proteinPairwise : filterLevel1)
            {
                if (proteinPairwise.getGeneName().equals(geneName) &&
                        proteinPairwise.getReferenceGeneEnsemblID().equals(referenceGeneName) &&
                        proteinPairwise.getReferenceTranscriptEnsemblID().equals(referenceTranscriptName) &&
                        proteinPairwise.getTargetSpeciesID() == 96)
                {
                    if (baseScoreIndex == 0)
                        proteinPairwise.setTotalScore1(totalScore);
                    else if (baseScoreIndex == 1)
                        proteinPairwise.setTotalScore2(totalScore);
                    else
                        proteinPairwise.setTotalScore3(totalScore);

                    finalPairwiseList.add(proteinPairwise);
                    break;
                }
            }
            for (ProteinPairwiseScore proteinPairwise : filterLevel1)
            {
                if (proteinPairwise.getGeneName().equals(geneName) &&
                        proteinPairwise.getReferenceGeneEnsemblID().equals(referenceGeneName) &&
                        proteinPairwise.getReferenceTranscriptEnsemblID().equals(referenceTranscriptName) &&
                        proteinPairwise.getTargetSpeciesID() == 201)
                {
                    if (baseScoreIndex == 0)
                        proteinPairwise.setTotalScore1(totalScore);
                    else if (baseScoreIndex == 1)
                        proteinPairwise.setTotalScore2(totalScore);
                    else
                        proteinPairwise.setTotalScore3(totalScore);

                    finalPairwiseList.add(proteinPairwise);
                    break;
                }
            }
            for (ProteinPairwiseScore proteinPairwise : filterLevel1)
            {
                if (proteinPairwise.getGeneName().equals(geneName) &&
                        proteinPairwise.getReferenceGeneEnsemblID().equals(referenceGeneName) &&
                        proteinPairwise.getReferenceTranscriptEnsemblID().equals(referenceTranscriptName) &&
                        proteinPairwise.getTargetSpeciesID() == 277)
                {
                    if (baseScoreIndex == 0)
                        proteinPairwise.setTotalScore1(totalScore);
                    else if (baseScoreIndex == 1)
                        proteinPairwise.setTotalScore2(totalScore);
                    else
                        proteinPairwise.setTotalScore3(totalScore);

                    finalPairwiseList.add(proteinPairwise);
                    break;
                }
            }
        }

        System.out.println();
        System.out.println("------------------------   Final List   -----------------------");
        System.out.println();
        for (ProteinPairwiseScore proteinPairwiseScore : finalPairwiseList)
            System.out.println(proteinPairwiseScore.export());
        System.out.println("---------------------------------------------------------------");
        System.out.println("===============================================================");
    }

    public void fillMissedProteinPairwise()
    {
        ArrayList<ProteinPairwiseScore> pairwiseList;

        pairwiseList = getListOfBestProteinPairwiseScore(proteinPairwiseFile);
        printFillMissedProteinPairwise(pairwiseList);
    }

    private ArrayList<ProteinPairwiseScore> getListOfBestProteinPairwiseScore(String proteinPairwiseFile)
    {
        ArrayList<ProteinPairwiseScore> result = new ArrayList<>();
        ArrayList<ArrayList<String>> rawData = Reader.readProteinPairwiseFile(proteinPairwiseFile);
        for (ArrayList<String> line : rawData)
        {
            ProteinPairwiseScore pairwise = new ProteinPairwiseScore(
                    getSpicesID(line.get(1)),
                    getSpicesID(line.get(5)),
                    -1,
                    Float.parseFloat(line.get(9)),
                    Float.parseFloat(line.get(11)),
                    Float.parseFloat(line.get(13)),
                    line.get(0),
                    line.get(1),
                    line.get(2),
                    line.get(3),
                    line.get(4),
                    line.get(5),
                    line.get(6),
                    line.get(7),
                    line.get(8));
            pairwise.setTotalScore1(Float.parseFloat(line.get(10)));
            pairwise.setTotalScore2(Float.parseFloat(line.get(12)));
            pairwise.setTotalScore3(Float.parseFloat(line.get(14)));
            result.add(pairwise);
        }

        return result;
    }

    private int getSpicesID(String speciesName)
    {
        if (speciesNameToIDMap.containsKey(speciesName))
        {
            return speciesNameToIDMap.get(speciesName);
        }
        else
        {
            switch (speciesName)
            {
                case "human":
                    speciesNameToIDMap.put("human", 7);
                    return 7;
                case "mouse":
                    speciesNameToIDMap.put("mouse", 96);
                    return 96;
                case "Macaque":
                    speciesNameToIDMap.put("Macaque", 201);
                    return 201;
                case "chimpanzee":
                    speciesNameToIDMap.put("chimpanzee", 277);
                    return 277;
            }
        }
        return -1;
    }

    private void printFillMissedProteinPairwise(ArrayList<ProteinPairwiseScore> pairwiseList)
    {
        HashMap<String, ArrayList<ProteinPairwiseScore>> data = new HashMap<>();

        for (ProteinPairwiseScore pairwise : pairwiseList)
        {
            if (data.containsKey(pairwise.getGeneName()))
            {
                ArrayList<ProteinPairwiseScore> values = data.get(pairwise.getGeneName());
                if (values.size() >= 3)
                    throw new ArrayIndexOutOfBoundsException();

                if (values.contains(pairwise))
                    throw new ArrayIndexOutOfBoundsException();

                values.add(pairwise);
            }
            else
            {
                data.put(pairwise.getGeneName(), new ArrayList<>(Collections.singletonList(pairwise)));
            }
        }

        ArrayList<String> singlePairwiseGroup = new ArrayList<>();
        ArrayList<String> doublePairwiseGroup = new ArrayList<>();
        ArrayList<String> triplePairwiseGroup = new ArrayList<>();

        for (String key : data.keySet())
        {
            ArrayList<ProteinPairwiseScore> value = data.get(key);
            if (value.size() == 0 || value.size() > 3)
                throw new ArrayIndexOutOfBoundsException();
            else if (value.size() == 1)
                singlePairwiseGroup.add(key);
            else if (value.size() == 2)
                doublePairwiseGroup.add(key);
            else
                triplePairwiseGroup.add(key);
        }

        System.out.println("$ : Number of unique genes : " + data.size());
        System.out.println("$ : Number of triple item  : " + triplePairwiseGroup.size());
        System.out.println("$ : Number of double item  : " + doublePairwiseGroup.size());
        System.out.println("$ : Number of single item  : " + singlePairwiseGroup.size());

        if (data.size() == (triplePairwiseGroup.size() + doublePairwiseGroup.size() + singlePairwiseGroup.size()))
            System.out.println("There is no Error. (NO)");
        else
            System.out.println("There is an Error! (YES)");

        System.out.println("--------------------------------------------------------------");
        System.out.println("--------------------------  Result  --------------------------");
        System.out.println("--------------------------------------------------------------");

        HashSet<String> species = new HashSet<>();
        for (String key : data.keySet())
        {
            ArrayList<ProteinPairwiseScore> value = data.get(key);
            species.clear();

            if (value.size() == 3)
            {
                species.add(value.get(0).getTargetSpeciesName());
                species.add(value.get(1).getTargetSpeciesName());
                species.add(value.get(2).getTargetSpeciesName());
                if (species.size() != 3)
                    throw new ArrayIndexOutOfBoundsException();
            }
            else if (value.size() == 2)
            {
                species.add(value.get(0).getTargetSpeciesName());
                species.add(value.get(1).getTargetSpeciesName());
                if (species.size() != 2)
                    throw new ArrayIndexOutOfBoundsException();

                ProteinPairwiseScore missedPairwise =
                        new ProteinPairwiseScore(7, -2,-1,
                                                 0,0,0,
                                                 value.get(0).getGeneName(),
                                                 "human", "*", "", "",
                                                 "", "", "","");
                if (!species.contains("mouse"))
                {
                    missedPairwise.setTargetSpeciesID(getSpicesID("mouse"));
                    missedPairwise.setTargetSpeciesName("mouse");
                }
                else if (!species.contains("Macaque"))
                {
                    missedPairwise.setTargetSpeciesID(getSpicesID("Macaque"));
                    missedPairwise.setTargetSpeciesName("Macaque");
                }
                else if (!species.contains("chimpanzee"))
                {
                    missedPairwise.setTargetSpeciesID(getSpicesID("chimpanzee"));
                    missedPairwise.setTargetSpeciesName("chimpanzee");
                }

                missedPairwise.setTotalScore1(0);
                missedPairwise.setTotalScore2(0);
                missedPairwise.setTotalScore3(0);

                value.add(missedPairwise);
            }
            else if (value.size() == 1)
            {
                species.add(value.get(0).getTargetSpeciesName());

                ProteinPairwiseScore missedPairwise1 =
                        new ProteinPairwiseScore(7, -2,-1,
                                                 0,0,0,
                                                 value.get(0).getGeneName(),
                                                 "human", "**", "", "",
                                                 "", "", "","");
                ProteinPairwiseScore missedPairwise2 =
                        new ProteinPairwiseScore(7, -2,-1,
                                                 0,0,0,
                                                 value.get(0).getGeneName(),
                                                 "human", "**", "", "",
                                                 "", "", "","");
                if (species.contains("mouse"))
                {
                    missedPairwise1.setTargetSpeciesID(getSpicesID("Macaque"));
                    missedPairwise1.setTargetSpeciesName("Macaque");
                    missedPairwise2.setTargetSpeciesID(getSpicesID("chimpanzee"));
                    missedPairwise2.setTargetSpeciesName("chimpanzee");
                }
                else if (species.contains("Macaque"))
                {
                    missedPairwise1.setTargetSpeciesID(getSpicesID("mouse"));
                    missedPairwise1.setTargetSpeciesName("mouse");
                    missedPairwise2.setTargetSpeciesID(getSpicesID("chimpanzee"));
                    missedPairwise2.setTargetSpeciesName("chimpanzee");
                }
                else if (species.contains("chimpanzee"))
                {
                    missedPairwise1.setTargetSpeciesID(getSpicesID("Macaque"));
                    missedPairwise1.setTargetSpeciesName("Macaque");
                    missedPairwise2.setTargetSpeciesID(getSpicesID("mouse"));
                    missedPairwise2.setTargetSpeciesName("mouse");
                }

                missedPairwise1.setTotalScore1(0);
                missedPairwise1.setTotalScore2(0);
                missedPairwise1.setTotalScore3(0);
                missedPairwise2.setTotalScore1(0);
                missedPairwise2.setTotalScore2(0);
                missedPairwise2.setTotalScore3(0);

                value.add(missedPairwise1);
                value.add(missedPairwise2);
            }
            else
                throw new ArrayIndexOutOfBoundsException();

            float totalScore1 = value.get(0).getScore1() + value.get(1).getScore1() + value.get(2).getScore1();
            float totalScore2 = value.get(0).getScore2() + value.get(1).getScore2() + value.get(2).getScore2();
            float totalScore3 = value.get(0).getScore3() + value.get(1).getScore3() + value.get(2).getScore3();

            value.get(0).setTotalScore1(totalScore1);
            value.get(0).setTotalScore2(totalScore2);
            value.get(0).setTotalScore3(totalScore3);

            value.get(1).setTotalScore1(totalScore1);
            value.get(1).setTotalScore2(totalScore2);
            value.get(1).setTotalScore3(totalScore3);

            value.get(2).setTotalScore1(totalScore1);
            value.get(2).setTotalScore2(totalScore2);
            value.get(2).setTotalScore3(totalScore3);

            for (ProteinPairwiseScore pairwiseScore : value)
                System.out.println(pairwiseScore.export());
        }
    }
}
