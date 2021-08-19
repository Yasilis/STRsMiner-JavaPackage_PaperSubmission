package com.alimaddi.control.converter;

import com.alimaddi.model.Species;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class JSON2POJO
{
    //region Checked DB 3
    public ArrayList<Species> convertAllSpecies(String json)
    {
        ArrayList<Species> results = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            JsonNode rootNode = mapper.readTree(json);
            JsonNode species = rootNode.path("species");

            for (JsonNode sp : species)
            {
                Species tempSpecies = new Species();
                tempSpecies.setDisplayName(sp.path("display_name").asText());
                tempSpecies.setCommonName(sp.path("common_name").asText());
                tempSpecies.setName(sp.path("name").asText());
                tempSpecies.setLastUpdateTime(new Date());

                results.add(tempSpecies);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return results;
    }
    //endregion

    public HashMap<String, String> convertAllTranscriptsStableID(String json)
    {
        HashMap<String, String> results = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            JsonNode rootNode = mapper.readTree(json);

            for (JsonNode transcript : rootNode)
            {
                String transcriptStableID = transcript.path("transcript_id").asText();
                String transcriptBiotype = transcript.path("biotype").asText();
                results.put(transcriptStableID, transcriptBiotype);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Exception in process of converting Transcript to pojo");
        }

        return results;
    }
}
