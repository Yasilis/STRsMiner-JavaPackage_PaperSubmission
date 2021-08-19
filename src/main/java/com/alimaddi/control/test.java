//package com.alimaddi.control;
//
//import java.net.URL;
//import java.net.URLConnection;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.io.BufferedReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.IOException;
//import java.io.Reader;
//import net.minidev.json.*;
//import net.minidev.json.parser.*;
//
//public class test {
//
//    public static final String SERVER = "http://rest.ensembl.org";
////    public static final JSONParser PARSER = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
//
//    public static int requestCount = 0;
//    public static long lastRequestTime = System.currentTimeMillis();
//
//    public static void main(String[] args) throws Exception {
//        String species, symbol;
//        if(args.length == 2) {
//            species = args[0];
//            symbol = args[1];
//        }
//        else if(args.length == 1) {
//            species = args[0];
//            symbol = "BRAF";
//        }
//        else {
//            species = "human";
//            symbol = "BRAF";
//        }
//
////        JSONArray variants = getVariants(species, symbol);
////        for(Object variantObject: variants) {
////            JSONObject variant = (JSONObject)variantObject;
////            String srName = (String)variant.get("seq_region_name");
////            Number start = (Number)variant.get("start");
////            Number end = (Number)variant.get("end");
////            Number strand = (Number)variant.get("strand");
////            String id = (String)variant.get("id");
////            String consequence = (String)variant.get("id");
////            String output = String.format("%s:%d-%d:%d ==> %s (%s)", srName, start, end, strand, id, consequence);
////            System.out.println(output);
////        }
//    }
//
//    public static JSONArray getVariants(String species, String symbol) throws ParseException, MalformedURLException, IOException, InterruptedException {
//        String id = getGeneStableID(species, symbol);
//        return (JSONArray) getJSON("/overlap/id/"+id+"?feature=variation");
//    }
//
//    public static String getGeneStableID(String species, String symbol) throws ParseException, MalformedURLException, IOException, InterruptedException {
//        String endpoint = "/xrefs/symbol/"+species+"/"+symbol+"?object_type=gene";
//        JSONArray genes = (JSONArray) getJSON(endpoint);
//        if(genes.isEmpty()) {
//            throw new RuntimeException("Got nothing for endpoint "+endpoint);
//        }
//        JSONObject gene = (JSONObject)genes.get(0);
//        return (String)gene.get("id");
//    }
//
//    public static Object getJSON(String endpoint) throws ParseException, MalformedURLException, IOException, InterruptedException {
//        String jsonString = getContent(endpoint);
//        return PARSER.parse(jsonString);
//    }
//
////    public static String downloadURL(String endpoint) throws MalformedURLException, IOException, InterruptedException {
////
////        if(requestCount == 15) { // check every 15
////            long currentTime = System.currentTimeMillis();
////            long diff = currentTime - lastRequestTime;
////            //if less than a second then sleep for the remainder of the second
////            if(diff < 1000) {
////                Thread.sleep(1000 - diff);
////            }
////            //reset
////            lastRequestTime = System.currentTimeMillis();
////            requestCount = 0;
////        }
////
////        URL url = new URL(SERVER+endpoint);
////        URLConnection connection = url.openConnection();
////        HttpURLConnection httpConnection = (HttpURLConnection)connection;
////        httpConnection.setRequestProperty("Content-Type", "application/json");
////        requestCount++;
////
////        InputStream response = httpConnection.getInputStream();
////        int responseCode = httpConnection.getResponseCode();
////
////        if(responseCode != 200) {
////            if(responseCode == 429 && httpConnection.getHeaderField("Retry-After") != null) {
////                double sleepFloatingPoint = Double.valueOf(httpConnection.getHeaderField("Retry-After"));
////                double sleepMillis = 1000 * sleepFloatingPoint;
////                Thread.sleep((long)sleepMillis);
////                return downloadURL(endpoint);
////            }
////            throw new RuntimeException("Response code was not 200. Detected response was "+responseCode);
////        }
////
////        String output;
////        Reader reader = null;
////        try {
////            reader = new BufferedReader(new InputStreamReader(response, "UTF-8"));
////            StringBuilder builder = new StringBuilder();
////            char[] buffer = new char[8192];
////            int read;
////            while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
////                builder.append(buffer, 0, read);
////            }
////            output = builder.toString();
////        }
////        finally {
////            if (reader != null) {
////                try {
////                    reader.close();
////                }
////                catch (IOException logOrIgnore) {
////                    logOrIgnore.printStackTrace();
////                }
////            }
////        }
////
////        return output;
////    }
//}
