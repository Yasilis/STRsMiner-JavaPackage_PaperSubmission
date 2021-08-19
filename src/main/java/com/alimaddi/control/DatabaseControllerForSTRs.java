package com.alimaddi.control;

import com.alimaddi.STRsProcess;
import com.alimaddi.Utility.HibernateUtility;
import com.alimaddi.Utility.Utilities;
import com.alimaddi.control.runnables.ThreadWorkerForCalculatingSTRs;
import com.alimaddi.datatypes.STROrigin;
import com.alimaddi.model.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DatabaseControllerForSTRs
{
    private static final SessionFactory sessionFactory = HibernateUtility.getSessionFactory();

    public static long[] turboUpdateSTRs(int speciesID, boolean log)
    {
        long duplicateNumber = 0;
        long uniqueNumber = 0;
        long updateNumber = 0;
        long deleteNumber = 0;
        long genesCount = 0;
        long transcriptsCount = 0;
        int genesCounter = 0;
        int transcriptsCounter = 0;

        String moreInfo ;

        HashSet<String> listOfGenesStableIDOfSpecies;
        HashSet<Transcript> listOfTranscriptsOfGeneStableID;

        if (log)
        {
            System.out.println(
                    "\n$ : The Program is calculating the STRs for the Transcripts of genes of " +
                            "species ID = " + speciesID);

            genesCount = DatabaseControllerForGenes.getAllGenesCount(speciesID);
            transcriptsCount = DatabaseControllerForTranscripts.getAllTranscriptsCount(speciesID);

            moreInfo = " -> speciesId = " + speciesID + " -- (T/t|G/g) : " +
                    transcriptsCount + "/" + transcriptsCounter +
                    "|" + genesCount + "/" + genesCounter;
            Utilities.showProgress(transcriptsCount, transcriptsCounter, 50, moreInfo + "\t");
        }

        try
        {
            listOfGenesStableIDOfSpecies = DatabaseControllerForGenes.getAllGenesStableIDOfSpeciesIDFromDB(speciesID);
            for (String geneStableID : listOfGenesStableIDOfSpecies)
            {
                listOfTranscriptsOfGeneStableID = DatabaseControllerForTranscripts
                        .getAllTranscriptsOfGeneStableIDFromDB(geneStableID);

                for (Transcript transcript : listOfTranscriptsOfGeneStableID)
                {
                    HashSet<STR> strsInCDS;
                    HashSet<STR> strsInCDNA;

                    strsInCDS = STRsProcess.calculateBiologicalSTRs(transcript, STROrigin.CDS_START_CODON);
                    int[] cdsResult = insetStrs(strsInCDS, transcript.getTranscriptStableID());
                    uniqueNumber += cdsResult[0];
                    duplicateNumber += cdsResult[1];
                    updateNumber += cdsResult[2];
                    deleteNumber += cdsResult[3];
                    strsInCDS.clear();

                    strsInCDNA = STRsProcess.calculateBiologicalSTRs(transcript, STROrigin.CDNA_START_CODON);
                    int[] cdnaResult = insetStrs(strsInCDNA, transcript.getTranscriptStableID());
                    uniqueNumber += cdnaResult[0];
                    duplicateNumber += cdnaResult[1];
                    updateNumber += cdnaResult[2];
                    deleteNumber += cdnaResult[3];
                    strsInCDNA.clear();

                    transcriptsCounter++;
                }

                if (log)
                {
                    moreInfo = " -> speciesId = " + speciesID + " -- (T/t|G/g) : " +
                            transcriptsCount + "/" + transcriptsCounter +
                            "|" + genesCount + "/" + genesCounter;
                    Utilities.showProgress(transcriptsCount, transcriptsCounter, 50, moreInfo + "\t");
                }

                listOfTranscriptsOfGeneStableID.clear();
                genesCounter++;
//                if (genesCounter == 1000)
//                    break;
            }
            listOfGenesStableIDOfSpecies.clear();
//            if (speciesCounter == 1)
//                break;
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
            System.err.println("Entered number does not have correct format!");
        }


        return new long[]{uniqueNumber, duplicateNumber, updateNumber, deleteNumber};
    }

    public static long[] updateSTRs(int threadNumber) throws Exception
    {
        long duplicateNumber = 0;
        long uniqueNumber = 0;
        long updateNumber = 0;
        long deleteNumber = 0;
        int speciesCounter = 0;
        long genesCounter;
        long transcriptsCounter;

        String moreInfo ;

        HashMap<Integer, String> validGenesDatasetsNamesIDFromDB =
                DatabaseControllerForGenes.getValidGenesDatasetsNamesInDB();

        ArrayList<String> diffGenesDatasetsNames = new ArrayList<>(
                DatabaseControllerForGenes.getAllGenesDatasetsNamesFormDB().values());
        diffGenesDatasetsNames.removeAll(validGenesDatasetsNamesIDFromDB.values());
        System.out.print("\n$ : ------------------------------------------------------------------------------- \n" +
                                 "$ : " + diffGenesDatasetsNames.size() +
                                 " Genes datasets are not processed! Because there are not any obvious ID for them.\n" +
                                 "$ : In below you can see them. They are listed as :\n" +
                                 "$ : " + diffGenesDatasetsNames.toString() + "\n$ : " +
                                 "------------------------------------------------------------------------------- \n" +
                                 "\n\n");

        System.out.print("\n$ : The Program is calculating the STRs for the Transcripts\n");

        System.out.println("$ : Filtering valid Datasets Names ID From DB");
        HashMap<Integer, String> filteredDatasetsNamesIDFromDB =
                DatabaseControllerForGenes.getAppropriateValidDatasetsNamesIDFromDB(validGenesDatasetsNamesIDFromDB);
        System.out.println("$ : Filtering finished");
        System.out.print("\n$ : ------------------------------------------------------------------------------- \n" +
                                 "$ : " + filteredDatasetsNamesIDFromDB.size() +
                                 " Genes datasets are selected! They are listed as :\n" +
                                 "$ : " + filteredDatasetsNamesIDFromDB.toString() + "\n$ : " +
                                 "------------------------------------------------------------------------------- \n" +
                                 "\n\n");

        long genesCount = DatabaseControllerForGenes.getAllGenesCount();
        long transcriptsCount = DatabaseControllerForTranscripts.getAllTranscriptsCount();

        int availableSystemCPUThreads;
        if (threadNumber <= 0)
            availableSystemCPUThreads = Runtime.getRuntime().availableProcessors() - 1;
        else
            availableSystemCPUThreads = threadNumber;

        System.out.println("\n$ : The parallel retrieving Transcripts of species process start with " +
                                   availableSystemCPUThreads + " available system CPU threads!\n");

        if (availableSystemCPUThreads == 1)
        {
            for (int speciesID : filteredDatasetsNamesIDFromDB.keySet())
            {
                genesCounter = DatabaseControllerForGenes.getAllGenesCount(speciesID);
                transcriptsCounter = DatabaseControllerForTranscripts.getAllTranscriptsCount(speciesID);

                long[] result = turboUpdateSTRs(speciesID, true);

                uniqueNumber += result[0];
                duplicateNumber += result[1];
                updateNumber += result[2];
                deleteNumber += result[3];

                moreInfo = " -> speciesId = " + speciesID + " -- (T/t|G/g|S/s) : " +
                        transcriptsCount + "/" + transcriptsCounter +
                        "|" + genesCount + "/" + genesCounter +
                        "|" + filteredDatasetsNamesIDFromDB.size() + "/" + speciesCounter;
                Utilities.showProgress(transcriptsCount, transcriptsCounter, 50, moreInfo + "\t");
            }
            return new long[]{uniqueNumber, duplicateNumber, updateNumber, deleteNumber};
        }
        else
        {
            ExecutorService executorService = Executors.newFixedThreadPool(availableSystemCPUThreads);

            ArrayList<ThreadWorkerForCalculatingSTRs> tasks = new ArrayList<>();

            for (Integer speciesID : filteredDatasetsNamesIDFromDB.keySet())
            {
                ThreadWorkerForCalculatingSTRs task =
                        new ThreadWorkerForCalculatingSTRs(speciesID);
                tasks.add(task);
            }

            List<Future<HashMap<Integer, Long[]>>> allReportResults;
            try
            {
                allReportResults = executorService.invokeAll(tasks);
                for (Future<HashMap<Integer, Long[]>> result : allReportResults)
                {
                    Map.Entry<Integer, Long[]> entry = result.get().entrySet().iterator().next();
                    int speciesID = entry.getKey();
                    System.out.println("$ : For species ID " + speciesID +
                                               "is saved " + entry.getValue()[0] + " unique STRs in the DB " +
                                               "and has " + entry.getValue()[1] + " duplicated STRs " +
                                               "and " + entry.getValue()[2] + " updated STRs " +
                                               "and " + entry.getValue()[3] + " deleted STRs");

                    uniqueNumber += entry.getValue()[0];
                    duplicateNumber += entry.getValue()[1];
                    updateNumber += entry.getValue()[2];
                    deleteNumber += entry.getValue()[3];
                }
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
            finally
            {
                executorService.shutdown();
            }
            return new long[]{uniqueNumber, duplicateNumber, updateNumber, deleteNumber};
        }
    }

    //region Checked
//    public static long[] updateSTRs()
//    {
//        long duplicateNumber = 0;
//        long uniqueNumber = 0;
//        long updateNumber = 0;
//        long deleteNumber = 0;
//        int speciesCounter;
//        int genesCounter;
//        int transcriptsCounter;
//
//        String moreInfo ;
//
//        HashSet<String> listOfGeneForSpecies;
//        ArrayList<String> listOfTranscriptForGene;
//
//
//        System.out.println("\n$ : The Program is calculating the STRs for the Transcripts");
//        try
//        {
//            HashMap<Integer, String> validGenesDatasetsNamesIDFromDB_ =
//                    DatabaseControllerForGenes.getValidGenesDatasetsNamesInDB();
//            HashMap<Integer, String> filteredDatasetsNamesIDFromDB =
//                    DatabaseControllerForGenes.getAppropriateValidDatasetsNamesIDFromDB(validGenesDatasetsNamesIDFromDB_);
//
//            long genesCount = DatabaseControllerForGenes.getAllGenesCount();
//            long transcriptsCount = DatabaseControllerForTranscripts.getAllTranscriptsCount();
//
//            speciesCounter = 0;
//            genesCounter = 0;
//            transcriptsCounter = 0;
//            for (int speciesID : validGenesDatasetsNamesIDFromDB.keySet())
//            {
//                moreInfo = " -> speciesId = " + speciesID + " -- (T/t|G/g|S/s) : " +
//                        transcriptsCount + "/" + transcriptsCounter +
//                        "|" + genesCount + "/" + genesCounter +
//                        "|" + validGenesDatasetsNamesIDFromDB.size() + "/" + speciesCounter;
//                Utilities.showProgress(transcriptsCount, transcriptsCounter, 50, moreInfo + "\t");
//
//                listOfGeneForSpecies = DatabaseControllerForGenes.getAllgenesStableIDOfSpeciesIDFromDB(speciesID);
//                for (String geneStableID : listOfGeneForSpecies)
//                {
//                    listOfTranscriptForGene = DatabaseControllerForTranscripts
//                            .getAllTranscriptsStableIDOfGeneStableIDFromDB(geneStableID);
//
//                    for (String transcriptStableID : listOfTranscriptForGene)
//                    {
//                        String cdsPromoter;
//                        String cdnaPromoter;
//
//                        HashMap<String,Integer> strsInCDS;
//                        HashMap<String,Integer> strsInCDNA;
//
//                        cdsPromoter = DatabaseControllerForTranscripts
//                                .getPromoterOfTranscriptsStableID(transcriptStableID, STROrigin.CDS_START_CODON);
//                        cdnaPromoter = DatabaseControllerForTranscripts
//                                .getPromoterOfTranscriptsStableID(transcriptStableID, STROrigin.CDNA_START_CODON);
//
//                        if (cdsPromoter != null && !cdsPromoter.isEmpty())
//                        {
//                            strsInCDS = STRsProcess.calculateSTRs(cdsPromoter);
//                            int[] cdsResult = insetStrs(strsInCDS, STROrigin.CDS_START_CODON, transcriptStableID);
//                            uniqueNumber += cdsResult[0];
//                            duplicateNumber += cdsResult[1];
//                            updateNumber += cdsResult[2];
//                            deleteNumber += cdsResult[3];
//                            strsInCDS.clear();
//                        }
//
//                        if (cdnaPromoter != null && !cdnaPromoter.isEmpty())
//                        {
//                            strsInCDNA = STRsProcess.calculateSTRs(cdnaPromoter);
//                            int[] cdnaResult = insetStrs(strsInCDNA, STROrigin.CDNA_START_CODON, transcriptStableID);
//                            uniqueNumber += cdnaResult[0];
//                            duplicateNumber += cdnaResult[1];
//                            updateNumber += cdnaResult[2];
//                            deleteNumber += cdnaResult[3];
//                            strsInCDNA.clear();
//                        }
//                        transcriptsCounter++;
//                    }
//                    moreInfo = " -> speciesId = " + speciesID + " -- (T/t|G/g|S/s) : " +
//                            transcriptsCount + "/" + transcriptsCounter +
//                            "|" + genesCount + "/" + genesCounter +
//                            "|" + validGenesDatasetsNamesIDFromDB.size() + "/" + speciesCounter;
//                    Utilities.showProgress(transcriptsCount, transcriptsCounter, 50, moreInfo + "\t");
//                    listOfTranscriptForGene.clear();
//                    genesCounter++;
////                    if (genesCounter == 1000)
////                        break;
//                }
//                listOfGeneForSpecies.clear();
//                speciesCounter++;
////                if (speciesCounter == 1)
////                    break;
//            }
//        }
//        catch (NumberFormatException e)
//        {
//            e.printStackTrace();
//            System.err.println("Entered number does not have correct format!");
//        }
//
//        return new long[]{uniqueNumber, duplicateNumber, updateNumber, deleteNumber};
//    }

    private static int[] insetStrs(HashSet<STR> strs, String transcriptStableID)
    {
        int duplicateNumber = 0;
        int uniqueNumber = 0;
        int updateNumber = 0;
        int deleteNumber = 0;

        HashSet<STR> strsInDB;
        HashSet<STR> newSTRs = new HashSet<>();

        strsInDB = getAllSTRsOfTranscriptStableIDFromDB(transcriptStableID);

        for (STR str : strs)
        {
            newSTRs.add(str);
            if (strsInDB.contains(str))
            {
                for (STR ori : strsInDB)
                {
                    if (ori.equals(str))
                    {
                        if (ori.getStartLocus().size() == str.getStartLocus().size() &&
                                ori.getStartLocus().containsAll(str.getStartLocus()))
                        {
                            duplicateNumber++;
                        }
                        else
                        {
                            ori.getStartLocus().clear();
                            ori.addAllStartLoci(str.getStartLocus());
                            insertOrUpdateSTRs(ori);
                            updateNumber++;
                        }
                        break;
                    }
                }
            }
            else
            {
                insertOrUpdateSTRs(str);
                uniqueNumber++;
            }
        }

//        strsInDB.removeAll(newSTRs);
//        if (!strsInDB.isEmpty())
//        {
//            deleteSTRs(strsInDB);
//            deleteNumber = strsInDB.size();
//        }
        return new int[]{uniqueNumber, duplicateNumber, updateNumber, deleteNumber};
    }

    public static HashSet<STR> getAllSTRsOfTranscriptStableIDFromDB(String transcriptStableID)
    {
        Transaction tx = null;
        HashSet<STR> results = new HashSet<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<STR> query = builder.createQuery(STR.class);
            Root<STR> root = query.from(STR.class);
            query.select(root).where(builder.equal(root.join(STR_.transcript).get(Transcript_.transcriptStableID), transcriptStableID));
            List<STR> allSTRsStableID = session.createQuery(query).getResultList();
            tx.commit();
            results.addAll(allSTRsStableID);
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return results;
    }

    private static void insertOrUpdateSTRs(STR str)
    {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            session.saveOrUpdate(str);
            tx.commit();
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void deleteSTRs(HashSet<STR> garbage)
    {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            for (STR str : garbage)
                session.delete(str);
            tx.commit();
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    //endregion

    public static void clearSTRsTable()
    {
        HashSet<STR> allSTRs = getAllSTRsInDB();
        deleteSTRs(allSTRs);
    }

//    public static long[] updateSTRsInOut()
//    {
//        int duplicateNumber = 0;
//        int uniqueNumber = 0;
//
//        int speciesID;
//
//        HashSet<String> listOfGeneForSpecies;
//        ArrayList<String> listOfAllControlSequencesForGene;
//
//
//        for (int i = 0 ; i < 5 ; i++)
//            System.out.print("\n");
//        System.out.print("The Program is calculating the STRs for the Transcripts\n");
//        System.out.print("\n");
//        System.out.print("Please Enter a species ID for starting the calculation : ");
//        try
//        {
//            Scanner in = new Scanner(System.in);
//            speciesID = in.nextInt();
//
//            listOfGeneForSpecies = DatabaseControllerForGenes.getAllgenesStableIDOfSpeciesIDFromDB(speciesID);
//
//            int counter = 0;
//            for (String geneStableID : listOfGeneForSpecies)
//            {
//                listOfAllControlSequencesForGene = DatabaseControllerForTranscripts
//                        .getAllControlSequencesOfGeneStableIDFromDB(geneStableID);
//
//                for (String controlSequenceID : listOfAllControlSequencesForGene)
//                {
//                    String cdsPromoter;
//                    String cdnaPromoter;
//
//                    HashMap<String,Integer> strsInCDS;
//                    HashMap<String,Integer> strsInCDNA;
//
//                    cdsPromoter = DatabaseControllerForTranscripts
//                            .getPromoterOfTranscriptsStableID(controlSequenceID, STROrigin.CDS_EXCEPT_START_CODON);
//                    cdnaPromoter = DatabaseControllerForTranscripts
//                            .getPromoterOfTranscriptsStableID(controlSequenceID, STROrigin.CDNA_EXCEPT_START_CODON);
//
//                    if (cdsPromoter != null && !cdsPromoter.equals(""))
//                    {
//                        strsInCDS = STRsProcess.calculateSTRs(cdsPromoter);
//                        int[] cdsResult = insetStrs(strsInCDS, STROrigin.CDS_EXCEPT_START_CODON, controlSequenceID);
//                        uniqueNumber += cdsResult[0];
//                        duplicateNumber += cdsResult[1];
//                        strsInCDS.clear();
//                    }
//
//                    if (cdnaPromoter != null && !cdnaPromoter.equals(""))
//                    {
//                        strsInCDNA = STRsProcess.calculateSTRs(cdnaPromoter);
//                        int[] cdnaResult = insetStrs(strsInCDNA, STROrigin.CDNA_EXCEPT_START_CODON, controlSequenceID);
//                        uniqueNumber += cdnaResult[0];
//                        duplicateNumber += cdnaResult[1];
//                        strsInCDNA.clear();
//                    }
//                }
//                counter ++;
//                String moreInfo = "Genes | speciesId = " + speciesID + " . #added STRs(uniq = "
//                        + uniqueNumber + " dupli = " + duplicateNumber
//                        + ") | #added transcript = " + listOfAllControlSequencesForGene.size();
//                Utilities.showProgress(listOfGeneForSpecies.size(), counter, 50, moreInfo + "\t");
//            }
//
//        }
//        catch (NumberFormatException e)
//        {
//            e.printStackTrace();
//            System.err.println("Entered number does not have correct format!");
//        }
//
//        return new long[]{uniqueNumber, duplicateNumber};
//    }

    public static HashSet<STR> getAllSTRsInDB()
    {
        Transaction tx = null;
        HashSet<STR> results = new HashSet<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<STR> query = builder.createQuery(STR.class);
            Root<STR> root = query.from(STR.class);
            query.select(root);
            List<STR> allStrs = session.createQuery(query).getResultList();
            tx.commit();
            results.addAll(allStrs);
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return results;
    }

    public static ArrayList<STR> getAllSTRsInDB(int speciesID)
    {
        Transaction tx = null;
        ArrayList<STR> results = new ArrayList<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<STR> query = builder.createQuery(STR.class);
            Root<STR> root = query.from(STR.class);
            query.select(root)
                 .where(builder.equal(root.join(STR_.transcript).join(Transcript_.gene).join(Gene_.species).get(Species_.ID), speciesID));
            List<STR> allSTRs = session.createQuery(query).getResultList();
            tx.commit();
            results.addAll(allSTRs);
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }

        return results;
    }

    public static long getAllSTRsCountInDB(int speciesID)
    {
        Transaction tx = null;
        long results = 0;

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Long> query = builder.createQuery(Long.class);
            Root<STR> root = query.from(STR.class);
            query.select(builder.count(root))
                 .where(builder.equal(root.join(STR_.transcript).join(Transcript_.gene).join(Gene_.species).get(Species_.ID), speciesID));
            results = session.createQuery(query).getSingleResult();
            tx.commit();
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }

        return results;
    }



    public static ArrayList<String> getAllSTRsSequenceInDB(int speciesID, String geneName)
    {
        Transaction tx = null;
        ArrayList<String> results = new ArrayList<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<String> query = builder.createQuery(String.class);
            Root<STR> root = query.from(STR.class);
            Predicate speciesItem = builder.equal(root.join(STR_.transcript).join(Transcript_.gene).join(Gene_.species).get(Species_.ID), speciesID);
            Predicate geneNameItem = builder.equal(root.join(STR_.transcript).join(Transcript_.gene).get(Gene_.geneName), geneName);
            query.select(root.get(STR_.sequence)).distinct(true)
                 .where(builder.and(speciesItem, geneNameItem));
            List<String> allSTRsSequence = session.createQuery(query).getResultList();
            tx.commit();
            results.addAll(allSTRsSequence);
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }

        return results;
    }

    public static HashMap<Integer, ArrayList<String>> getAllSTRsSequenceInDB(String geneName)
    {
        Transaction tx = null;
        HashMap<Integer, ArrayList<String>> results = new HashMap<>();
        List<Object[]> allSTRsSequence = new ArrayList<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Object[]> query = builder.createQuery(Object[].class);
            Root<STR> root = query.from(STR.class);
            query.multiselect(
                    root.join(STR_.transcript).join(Transcript_.gene).join(Gene_.species).get(Species_.ID),
                    root.get(STR_.sequence))
                 .distinct(true)
                 .where(builder.equal(root.join(STR_.transcript).join(Transcript_.gene).get(Gene_.geneName), geneName));

            allSTRsSequence = session.createQuery(query).getResultList();
            tx.commit();
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }

        for (Object[] row : allSTRsSequence)
        {
            Integer speciesID = (Integer)row[0];
            String strSequence = (String)row[1];

            if (results.containsKey(speciesID))
            {
                results.get(speciesID).add(strSequence);
            }
            else
            {
                ArrayList<String> strsList = new ArrayList<>();
                strsList.add(strSequence);
                results.put(speciesID, strsList);
            }
        }

        return results;
    }

    //region Checked
    public static ArrayList<String> getAllTypeOfSTRsInDB()
    {
        Transaction tx = null;
        ArrayList<String> results;
        List<String> listOfSTRsSequences = null;

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
//            listOfSTRsSequences = session
//                    .createQuery("select sequence from STR group by sequence", String.class).list();
            listOfSTRsSequences = session
                    .createQuery("select distinct sequence from STR", String.class).list();
            tx.commit();
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }
        if (listOfSTRsSequences == null)
            results = new ArrayList<>();
        else
            results = new ArrayList<>(listOfSTRsSequences);

        if (listOfSTRsSequences != null && !listOfSTRsSequences.isEmpty())
            listOfSTRsSequences.clear();

        return results;
    }

    public static ArrayList<String> getAllTypeOfSTRsInDB(int speciesID)
    {
        Transaction tx = null;
        ArrayList<String> results = new ArrayList<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<String> query = builder.createQuery(String.class);
            Root<STR> root = query.from(STR.class);
            query.select(root.get(STR_.sequence)).distinct(true)
                 .where(builder.equal(root.join(STR_.transcript).join(Transcript_.gene).join(Gene_.species).get(Species_.ID), speciesID));
            List<String> allSTRsSequence = session.createQuery(query).getResultList();
            tx.commit();
            results.addAll(allSTRsSequence);
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }

        return results;
    }

    //endregion

    public static ArrayList<String> getProducerTranscripts(int speciesID, ArrayList<String> listOfSTRs)
    {
        Transaction tx = null;
        ArrayList<String> results;
        List<String> transcripts = null;

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            transcripts = session
                    .createQuery("select distinct s.transcriptStableId from STR as s inner join Transcript as t on " +
                                         "s.transcriptStableId = t.transcriptStableID inner join Gene as g on " +
                                         "t.geneStableID = g.geneStableID where g.speciesId= :speciesId " +
                                         "and s.sequence in (:sequences)", String.class)
                    .setParameter("speciesId", speciesID)
                    .setParameterList("sequences", listOfSTRs).list();
            tx.commit();
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }
        if (transcripts == null)
            results = null;
        else
            results = new ArrayList<>(transcripts);

        if (transcripts != null && !transcripts.isEmpty())
            transcripts.clear();

        return results;
    }
}
