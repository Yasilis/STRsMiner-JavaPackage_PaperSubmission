package com.alimaddi.control.converter;

import com.alimaddi.control.DatabaseControllerForGenes;
import com.alimaddi.datatypes.STROrigin;
import com.alimaddi.model.Gene;
import com.alimaddi.model.Transcript;

import java.util.*;

public class FastaToTranscript
{
    //region Checked DB 3
    public static HashSet<Transcript> readTranscripts(String fasta, STROrigin origin, String type)
    {
        HashSet<Transcript> results = new HashSet<>();
        Stack<String> repo = new Stack<>();
        String header;
        String sequence;
        String line;
        try (Scanner scanner = new Scanner(fasta))
        {
            while (scanner.hasNextLine())
            {
                line = scanner.nextLine();
                if (isHeader(line))
                {
                    sequence = "";
                    //make previous transcript
                    while (!repo.empty())
                    {
                        if (repo.peek().isEmpty())
                        {
                            repo.pop();
                            continue;
                        }
                        if (repo.peek().charAt(0) == '>')
                        {
                            header = repo.pop();
                            //make transcript
                            String[] parts;
                            parts = header.substring(1).trim().split("\\||\\s+");

                            sequence = getAppropriateSequence(origin, sequence);

                            Transcript transcript = makeTranscript(parts[0], parts[1], sequence, type, origin);
                            results.add(transcript);
                        }
                        else
                        {
                            sequence = repo.pop() + sequence;
                        }
                    }


                    repo.push(line);
                }
                else
                {
                    repo.push(line);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Failed to read fasta string");
        }
        return results;
    }
    //endregion

    private static String getAppropriateSequence(STROrigin origin, String sequence)
    {
        String result = "";
        String seq;
        switch (origin)
        {
            case CDS_START_CODON:
            case CDNA_START_CODON:
                result = sequence.replace("N","");
                break;

            case CDS_EXCEPT_START_CODON:
            case CDNA_EXCEPT_START_CODON:
                if (sequence.length() > 120)
                {
                    seq = sequence.substring(0, sequence.length() - 120); //TODO : check this line!!!! I think this is range!
                    if (seq.length() > 120)
                    {
                        result = seq.substring(0, 120).replace("N", "");
                    }
                    else
                    {
                        result = seq.replace("N","");
                    }
                }
                break;
        }
        return result;
    }

    //region Checked DB 3
    private static Transcript makeTranscript(String geneStableID, String TranscriptStableID, String sequence,
            String type, STROrigin origin)
    {
        Transcript transcript = null;
        Gene parentGene = DatabaseControllerForGenes.getGeneFromDB(geneStableID);

        if (parentGene == null)
            throw new IllegalArgumentException("There is not any Gene in the DB with gene stable ID = " + geneStableID);

        switch (origin)
        {
            case CDS_START_CODON:
                transcript = new Transcript(
                        TranscriptStableID,
                        type,
                        "",
                        "",
                        "",
                        sequence,
                        "",
                        "",
                        new Date(),
                        parentGene);
                break;
            case CDS_EXCEPT_START_CODON:
                transcript = new Transcript(
                        "UFO" + TranscriptStableID,
                        "",
                        "",
                        "",
                        "",
                        sequence,
                        "",
                        "",
                        new Date(),
                        parentGene);
                break;
            case CDNA_START_CODON:
                transcript = new Transcript(
                        TranscriptStableID,
                        type,
                        "",
                        "",
                        "",
                        "",
                        "",
                        sequence,
                        new Date(),
                        parentGene);
                break;
            case CDNA_EXCEPT_START_CODON:
                transcript = new Transcript(
                        "UFO" + TranscriptStableID,
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        sequence,
                        new Date(),
                        parentGene);
                break;
        }

        return transcript;
    }

    private static boolean isHeader(String str)
    {
        return str != null && !str.isEmpty() && str.charAt(0) == '>';
    }
    //endregion

    public static HashMap<String, String> readUsualSequences(String sequencesFasta, int maxLength)
    {
        HashMap<String, String> results = new HashMap<>();
        Stack<String> repo = new Stack<>();
        String header;
        String sequence;
        String line;
        try (Scanner scanner = new Scanner(sequencesFasta))
        {
            while (scanner.hasNextLine() || !repo.empty())
            {
                if (scanner.hasNextLine())
                    line = scanner.nextLine();
                else
                    line = ">";
                if (isHeader(line))
                {
                    sequence = "";
                    //make previous peptide
                    while (!repo.empty())
                    {
                        if (repo.peek().isEmpty())
                        {
                            repo.pop();
                            continue;
                        }
                        if (repo.peek().charAt(0) == '>')
                        {
                            header = repo.pop();
                            //make transcript
                            String[] parts;
                            parts = header.substring(1).trim().split("\\||\\s+");

                            sequence = sequence.substring(0, Math.min(maxLength, sequence.length()));

//                            results.put(parts[1], sequence);
                            results.put(header.substring(1).trim(), sequence);
                        }
                        else
                        {
                            sequence = repo.pop() + sequence;
                        }
                    }

                    if (line.length() > 1)
                        repo.push(line);
                }
                else
                {
                    repo.push(line);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Failed to read fasta string");
        }
        return results;
    }
}

