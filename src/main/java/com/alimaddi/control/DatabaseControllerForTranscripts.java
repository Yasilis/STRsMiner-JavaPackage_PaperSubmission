package com.alimaddi.control;

import com.alimaddi.control.runnables.ThreadWorkerForGettingGeneFromCloud;
import com.alimaddi.control.runnables.ThreadWorkerForGettingTranscriptFromCloud;
import com.alimaddi.datatypes.STROrigin;
import com.alimaddi.Utility.HibernateUtility;
import com.alimaddi.Utility.Utilities;
import com.alimaddi.control.converter.FastaToTranscript;
import com.alimaddi.control.downloader.Downloader;
import com.alimaddi.datatypes.TranscriptSequenceType;
import com.alimaddi.model.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DatabaseControllerForTranscripts
{
    private static final SessionFactory sessionFactory = HibernateUtility.getSessionFactory();

    //region Checked DB 3
    public static long[] updateTranscripts(int upstreamFlank, HashSet<String> genesStableID, boolean log) throws Exception
    {
        long duplicateNumber = 0;
        long uniqueNumber = 0;
        long updateNumber = 0;

        if (log)
            System.out.println("\n$ : The Program is updating the database for Transcripts of batch genes for " +
                                       upstreamFlank + " upstreamFlank");

        Downloader downloader = new Downloader();

        ArrayList<Integer> speciesIDs =
                DatabaseControler.getAllSpeciesIDsOfGenesList(new ArrayList<>(genesStableID));

        if (speciesIDs.size() != 1)
            throw new IllegalArgumentException("\n$ : Genes list are not valid or belong to more than one species!");
        int speciesID = speciesIDs.get(0);

        HashMap<Integer, String> validGenesDatasetsNamesIDFromDB =
                DatabaseControllerForGenes.getValidGenesDatasetsNamesInDB();
        String datasetName = validGenesDatasetsNamesIDFromDB.get(speciesID);

        int baseURLIndex = 0;
        String cdsFasta = downloader.downloadAllTranscriptsCDSUpstreamFlankOfGeneStableIDListFromCloud(
                genesStableID, datasetName, upstreamFlank, baseURLIndex);

        while (cdsFasta.isEmpty() || cdsFasta.charAt(0) != '>')
        {
            System.err.println("\n$ : cdsFasta is empty or error occurred!");
            System.err.println("$ : cdsFasta ==> " + cdsFasta);
            System.err.println("$ : SpeciesID/database/flank : " + speciesID + "/"
                                       + validGenesDatasetsNamesIDFromDB.get(speciesID) + "/" + upstreamFlank);
            System.out.println("\n$ : Unacceptable result for (" + datasetName + ") database from cloud!");
            baseURLIndex = (++baseURLIndex) % 4;
            cdsFasta = downloader.downloadAllTranscriptsCDSUpstreamFlankOfGeneStableIDListFromCloud(
                    genesStableID, datasetName, upstreamFlank, baseURLIndex);
        }

        HashSet<Transcript> cdsTranscriptsInCloud;
        if (upstreamFlank <= 120)
        {
            cdsTranscriptsInCloud = FastaToTranscript
                    .readTranscripts(cdsFasta, STROrigin.CDS_START_CODON, "protein_coding");
        }
        else
        {
            cdsTranscriptsInCloud = FastaToTranscript
                    .readTranscripts(cdsFasta, STROrigin.CDS_EXCEPT_START_CODON, "protein_coding");
        }

        cdsFasta = "";

//                String cdnaFasta = downloader.downloadAllTranscriptsCDNAUpstreamFlankOfGeneStableIDListFromCloud(
//                        chunk, datasetsNamesIDFromDB.get(speciesID), 125);
//                HashSet<Transcript> cdnaTranscriptsInCloud = FastaToTranscript.readTranscripts(cdnaFasta, STROrigin.CDNA_START_CODON);
//                cdnaFasta = "";
//
//                HashSet<Transcript> mergedTranscripts = mergeTranscripts(cdsTranscriptsInCloud, cdnaTranscriptsInCloud);
        HashSet<Transcript> mergedTranscripts = cdsTranscriptsInCloud;
//                cdsTranscriptsInCloud.clear();
//                cdnaTranscriptsInCloud.clear();

        boolean sw;
        HashSet<Transcript> allTranscriptsInDBForSpeciesID = getAllTranscriptsOfSpeciesIDFromDB(speciesID);
        for (Transcript candidate : mergedTranscripts)
        {
            if (allTranscriptsInDBForSpeciesID.contains(candidate))
            {
                for (Transcript ori : allTranscriptsInDBForSpeciesID)
                {
                    if (ori.equals(candidate))
                    {
                        sw = false;
                        if (!Objects.equals(ori.getNucleotideSequence(), candidate.getNucleotideSequence()))
                        {
                            ori.setNucleotideSequence(candidate.getNucleotideSequence());
                            sw = true;
                        }
                        if (!Objects.equals(ori.getPeptideSequence(), candidate.getPeptideSequence()))
                        {
                            ori.setPeptideSequence(candidate.getPeptideSequence());
                            sw = true;
                        }
                        if (!Objects.equals(ori.getCds(), candidate.getCds()))
                        {
                            ori.setCds(candidate.getCds());
                            sw = true;
                        }
                        if (!Objects.equals(ori.getCdsPromoter(), candidate.getCdsPromoter()))
                        {
                            ori.setCdsPromoter(candidate.getCdsPromoter());
                            // TODO : ***** update STRs
                            sw = true;
                        }
                        if (!Objects.equals(ori.getCdna(), candidate.getCdna()))
                        {
                            ori.setCdna(candidate.getCdna());
                            sw = true;
                        }
                        if (!Objects.equals(ori.getCdnaPromoter(), candidate.getCdnaPromoter()))
                        {
                            ori.setCdnaPromoter(candidate.getCdnaPromoter());
                            // TODO : ***** update STRs
                            sw = true;
                        }
                        if (sw)
                        {
                            ori.setLastUpdateTime(new Date());
                            updateNumber++;
                        }
                        else
                            duplicateNumber++;
                        break;
                    }
                }
            }
            else
            {
                allTranscriptsInDBForSpeciesID.add(candidate);
                uniqueNumber++;
            }
        }
        mergedTranscripts.clear();

        bulkInsertOrUpdate(allTranscriptsInDBForSpeciesID, false);
        allTranscriptsInDBForSpeciesID.clear();

        return new long[]{uniqueNumber, duplicateNumber, updateNumber};
    }

    public static long[] updateTranscripts(int upstreamFlank, int speciesID, boolean log) throws Exception
    {
        long duplicateNumber = 0;
        long uniqueNumber = 0;
        long updateNumber = 0;
        long genesCounter = 0;

        if (log)
            System.out.println("\n$ : The Program is updating the database for Transcripts of genes for " +
                                       upstreamFlank + " upstreamFlank of species ID = " + speciesID);

        HashMap<Integer, String> validGenesDatasetsNamesIDFromDB =
                DatabaseControllerForGenes.getValidGenesDatasetsNamesInDB();

        if (!validGenesDatasetsNamesIDFromDB.containsKey(speciesID))
            throw new IllegalArgumentException("The species ID = " + speciesID +
                                                       " doesn't have valid gene dataset name!");

        HashSet<String> genesStableID = DatabaseControllerForGenes.getAllGenesStableIDOfSpeciesIDFromDB(speciesID);
        ArrayList<HashSet<String>> dividedGenesStableID = Utilities.split(genesStableID, 300);

        for (HashSet<String> chunk : dividedGenesStableID)
        {
            long[] result = updateTranscripts(upstreamFlank, chunk, false);

            uniqueNumber += result[0];
            duplicateNumber += result[1];
            updateNumber += result[2];

            if (log)
            {
                genesCounter += chunk.size();
                Utilities.showProgress(genesStableID.size(), genesCounter, 50,
                                       " : SpeciesID/DatasetName = (" +
                                               speciesID + "/" + validGenesDatasetsNamesIDFromDB.get(speciesID) + ")");
            }
        }

        return new long[]{uniqueNumber, duplicateNumber, updateNumber};
    }

    public static long[] turboUpdateTranscripts(int upstreamFlank, int speciesID, boolean log) throws Exception
    {
        long duplicateNumber = 0;
        long uniqueNumber = 0;
        long updateNumber = 0;
        long genesCounter = 0;

        if (log)
            System.out.println("\n$ : The Program is updating the database for Transcripts of genes for " +
                                       upstreamFlank + " upstreamFlank of species ID = " + speciesID);

        HashMap<Integer, String> validGenesDatasetsNamesIDFromDB =
                DatabaseControllerForGenes.getValidGenesDatasetsNamesInDB();

        if (!validGenesDatasetsNamesIDFromDB.containsKey(speciesID))
            throw new IllegalArgumentException("The species ID = " + speciesID +
                                                       " doesn't have valid gene dataset name!");

        String datasetName = validGenesDatasetsNamesIDFromDB.get(speciesID);
        HashSet<String> genesStableID = DatabaseControllerForGenes.getAllGenesStableIDOfSpeciesIDFromDB(speciesID);
        HashSet<Transcript> allTranscriptsInDBForSpeciesID = getAllTranscriptsOfSpeciesIDFromDB(speciesID);
        Downloader downloader = new Downloader();
        long genesCount = genesStableID.size();

        ArrayList<HashSet<String>> dividedGenesStableID = Utilities.split(genesStableID, 300);
        for (HashSet<String> chunk : dividedGenesStableID)
        {
            int baseURLIndex = 0;
            String cdsFasta = downloader.downloadAllTranscriptsCDSUpstreamFlankOfGeneStableIDListFromCloud(
                    chunk, datasetName, upstreamFlank, baseURLIndex);

            while (cdsFasta.isEmpty() || cdsFasta.charAt(0) != '>')
            {
                System.err.println("\n$ : cdsFasta is empty or error occurred!");
                System.err.println("$ : cdsFasta ==> " + cdsFasta);
                System.err.println("$ : SpeciesID/database/flank : " + speciesID + "/"
                                           + datasetName + "/" + upstreamFlank);
                System.out.println("\n$ : Unacceptable result for (" + datasetName + ") database from cloud!");
                baseURLIndex = (++baseURLIndex) % 4;
                cdsFasta = downloader.downloadAllTranscriptsCDSUpstreamFlankOfGeneStableIDListFromCloud(
                        chunk, datasetName, upstreamFlank, baseURLIndex);
            }

            HashSet<Transcript> cdsTranscriptsInCloud;
            if (upstreamFlank <= 120)
            {
                cdsTranscriptsInCloud = FastaToTranscript
                        .readTranscripts(cdsFasta, STROrigin.CDS_START_CODON, "protein_coding");
            }
            else
            {
                cdsTranscriptsInCloud = FastaToTranscript
                        .readTranscripts(cdsFasta, STROrigin.CDS_EXCEPT_START_CODON, "protein_coding");
            }

            cdsFasta = "";

//            String cdnaFasta = downloader.downloadAllTranscriptsCDNAUpstreamFlankOfGeneStableIDListFromCloud(
//                    chunk, datasetsNamesIDFromDB.get(speciesID), 125);
//            HashSet<Transcript> cdnaTranscriptsInCloud = FastaToTranscript.readTranscripts(cdnaFasta, STROrigin.CDNA_START_CODON);
//            cdnaFasta = "";
//
//            HashSet<Transcript> mergedTranscripts = mergeTranscripts(cdsTranscriptsInCloud, cdnaTranscriptsInCloud);
            HashSet<Transcript> mergedTranscripts = cdsTranscriptsInCloud;
//            cdsTranscriptsInCloud.clear();
//            cdnaTranscriptsInCloud.clear();

            boolean sw;
            for (Transcript candidate : mergedTranscripts)
            {
                if (allTranscriptsInDBForSpeciesID.contains(candidate))
                {
                    for (Transcript ori : allTranscriptsInDBForSpeciesID)
                    {
                        if (ori.equals(candidate))
                        {
                            sw = false;
                            if (!Objects.equals(ori.getNucleotideSequence(), candidate.getNucleotideSequence()))
                            {
                                ori.setNucleotideSequence(candidate.getNucleotideSequence());
                                sw = true;
                            }
                            if (!Objects.equals(ori.getPeptideSequence(), candidate.getPeptideSequence()))
                            {
                                ori.setPeptideSequence(candidate.getPeptideSequence());
                                sw = true;
                            }
                            if (!Objects.equals(ori.getCds(), candidate.getCds()))
                            {
                                ori.setCds(candidate.getCds());
                                sw = true;
                            }
                            if (!Objects.equals(ori.getCdsPromoter(), candidate.getCdsPromoter()))
                            {
                                ori.setCdsPromoter(candidate.getCdsPromoter());
                                // TODO : ***** update STRs
                                sw = true;
                            }
                            if (!Objects.equals(ori.getCdna(), candidate.getCdna()))
                            {
                                ori.setCdna(candidate.getCdna());
                                sw = true;
                            }
                            if (!Objects.equals(ori.getCdnaPromoter(), candidate.getCdnaPromoter()))
                            {
                                ori.setCdnaPromoter(candidate.getCdnaPromoter());
                                // TODO : ***** update STRs
                                sw = true;
                            }
                            if (sw)
                            {
                                ori.setLastUpdateTime(new Date());
                                updateNumber++;
                            }
                            else
                                duplicateNumber++;
                            break;
                        }
                    }
                }
                else
                {
                    allTranscriptsInDBForSpeciesID.add(candidate);
                    uniqueNumber++;
                }
            }
            mergedTranscripts.clear();

            if (log)
            {
                genesCounter += chunk.size();
                Utilities.showProgress(genesCount, genesCounter, 50,
                                       " : SpeciesID/DatasetName = " + speciesID + "/" + datasetName +  " (" +
                                               genesCount + "/" + genesCounter + " Species processed)");
            }
        }

        bulkInsertOrUpdate(allTranscriptsInDBForSpeciesID, false);
        allTranscriptsInDBForSpeciesID.clear();

        return new long[]{uniqueNumber, duplicateNumber, updateNumber};
    }

    public static long[] updateTranscripts(int upstreamFlank, boolean log, int threadNumber) throws Exception
    {
        long duplicateNumber = 0;
        long uniqueNumber = 0;
        long updateNumber = 0;
        long speciesCounter = 0;
        long genesCounter;

        if (log)
            System.out.println("\n$ : The Program is updating the database for Transcripts of genes for " +
                                       upstreamFlank + " upstreamFlank");

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

        System.out.print("\n$ : The Program is updating the database for Transcript of remains species\n");

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
                String datasetName = filteredDatasetsNamesIDFromDB.get(speciesID);
                genesCounter = DatabaseControllerForGenes.getAllGenesCount(speciesID);
                long[] result = turboUpdateTranscripts(upstreamFlank, speciesID, true);

                uniqueNumber += result[0];
                duplicateNumber += result[1];
                updateNumber += result[2];

                Utilities.showProgress(genesCount, genesCounter, 50,
                                       " : SpeciesID/DatasetName = " + speciesID + "/" +
                                               datasetName +  " (" + speciesCounter + "/"
                                               + filteredDatasetsNamesIDFromDB.size() + " Species processed)");
                speciesCounter++;
            }
            return new long[]{uniqueNumber, duplicateNumber, updateNumber};
        }
        else
        {
            ExecutorService executorService = Executors.newFixedThreadPool(availableSystemCPUThreads);

            ArrayList<ThreadWorkerForGettingTranscriptFromCloud> tasks = new ArrayList<>();

            for (Integer speciesID : filteredDatasetsNamesIDFromDB.keySet())
            {
                ThreadWorkerForGettingTranscriptFromCloud task =
                        new ThreadWorkerForGettingTranscriptFromCloud(upstreamFlank, speciesID);
                tasks.add(task);
            }

            List<Future<HashMap<Integer, Integer[]>>> allReportResults;
            try
            {
                allReportResults = executorService.invokeAll(tasks);
                for (Future<HashMap<Integer, Integer[]>> result : allReportResults)
                {
                    Map.Entry<Integer, Integer[]> entry = result.get().entrySet().iterator().next();
                    int speciesID = entry.getKey();
                    System.out.println("$ : For species ID " + speciesID +
                                               "is saved " + entry.getValue()[0] + " unique genes in the DB " +
                                               "and has " + entry.getValue()[1] + " duplicated genes " +
                                               "and " + entry.getValue()[2] + " updated genes");

                    uniqueNumber += entry.getValue()[0];
                    duplicateNumber += entry.getValue()[1];
                    updateNumber += entry.getValue()[2];
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
            return new long[]{uniqueNumber, duplicateNumber, updateNumber};
        }
    }

    private static void bulkInsertOrUpdate(HashSet<Transcript> bulkTranscripts, boolean showProgress)
    {
        long counter = 0;
        Transaction tx = null;

        try (Session session = sessionFactory.openSession())
        {
            if (showProgress)
                System.out.println("\n$ : The Program is inserting/updating the database for new Transcript");

            tx = session.beginTransaction();
            for (Transcript transcript : bulkTranscripts)
            {
                session.saveOrUpdate(transcript);
                counter++;
                if (showProgress)
                    Utilities.showProgress(bulkTranscripts.size(), counter, 50,
                                       " ---> (" + bulkTranscripts.size() + "/" + counter + ")");
            }
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

    static HashSet<Transcript> getAllTranscriptsOfSpeciesIDFromDB(int speciesID)
    {
        Transaction tx = null;
        HashSet<Transcript> results = new HashSet<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Transcript> query = builder.createQuery(Transcript.class);
            Root<Transcript> root = query.from(Transcript.class);
            query.select(root)
                 .where(builder.equal(root.join(Transcript_.gene).join(Gene_.species).get(Species_.ID), speciesID));
            List<Transcript> allTranscript = session.createQuery(query).getResultList();
            tx.commit();
            results.addAll(allTranscript);
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }

        return results;
    }

    public static long getAllTranscriptsCount(int speciesID)
    {
        Transaction tx = null;
        long results = 0;

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Long> query = builder.createQuery(Long.class);
            Root<Transcript> root = query.from(Transcript.class);
            query.select(builder.count(root))
                 .where(builder.equal(root.join(Transcript_.gene).join(Gene_.species).get(Species_.ID), speciesID));
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

    public static long getAllTranscriptsCount()
    {
        Transaction tx = null;
        long results = 0;

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Long> query = builder.createQuery(Long.class);
            Root<Transcript> root = query.from(Transcript.class);
            query.select(builder.count(root));
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

    public static HashSet<Transcript> getAllTranscriptsOfGeneStableIDFromDB(String geneStableID)
    {
        Transaction tx = null;
        HashSet<Transcript> results = new HashSet<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Transcript> query = builder.createQuery(Transcript.class);
            Root<Transcript> root = query.from(Transcript.class);
            query.select(root).where(builder.equal(root.join(Transcript_.gene).get(Gene_.geneStableID), geneStableID));
            List<Transcript> allTranscriptsStableID = session.createQuery(query).getResultList();
            tx.commit();
            results.addAll(allTranscriptsStableID);
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

    public static ArrayList<String> getAllTranscriptsStableIDOfGenesStableIDsFromDB(List<String> genesStableIDList)
    {
        Transaction tx = null;
        ArrayList<String> results = new ArrayList<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<String> query = builder.createQuery(String.class);
            Root<Transcript> root = query.from(Transcript.class);
            query.select(root.get(Transcript_.transcriptStableID)).distinct(true)
                 .where(root.join(Transcript_.gene).get(Gene_.geneStableID).in(genesStableIDList));
            List<String> allTranscriptsStableID = session.createQuery(query).getResultList();
            tx.commit();
            results.addAll(allTranscriptsStableID);
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

    public static ArrayList<String> getAllTranscriptsStableIDOfGeneStableIDFromDB(String geneStableID)
    {
        // It doesn't need hash set because, we get hash set of Transcript and Transcript stable ids are unique!
        ArrayList<String> results = new ArrayList<>();

        for (Transcript transcript : getAllTranscriptsOfGeneStableIDFromDB(geneStableID))
            results.add(transcript.getTranscriptStableID());

        return results;
    }
    //endregion

    //region Checked
    public static String getPromoterOfTranscriptsStableID(String transcriptStableID, STROrigin origin)
    {
        Transaction tx = null;
        String result = "";

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<String> query = builder.createQuery(String.class);
            Root<Transcript> root = query.from(Transcript.class);

            switch (origin)
            {
                case CDS_START_CODON:
                case CDS_EXCEPT_START_CODON:
                    query.select(root.get(Transcript_.cdsPromoter))
                         .where(builder.equal(root.get(Transcript_.transcriptStableID), transcriptStableID));
                    break;
                case CDNA_START_CODON:
                case CDNA_EXCEPT_START_CODON:
                    query.select(root.get(Transcript_.cdnaPromoter))
                         .where(builder.equal(root.get(Transcript_.transcriptStableID), transcriptStableID));
                    break;
            }
            String promoter = session.createQuery(query).getSingleResult();
            tx.commit();
            result = promoter;
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }

        return result;
    }
    //endregion


    public static ArrayList<String> getAllControlSequencesOfGeneStableIDFromDB(String geneStableID)
    {
        List<String> results = null;
        Session session = sessionFactory.openSession();
        Transaction tx = null;

        try
        {
            tx = session.beginTransaction();
            results = session.createQuery(
                    "select e.transcriptStableID from Transcript as e where e.geneStableID = :geneStableID and e.type = :trnsType",
                    String.class).setParameter("geneStableID", geneStableID).setParameter("trnsType", "").list();
            tx.commit();
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }
        finally
        {
            session.close();
        }
        if (results == null)
            return new ArrayList<>();
        else
            return new ArrayList<>(results);
    }

//    public static long[] updateTranscriptsForSpeciesID(int upstreamFlank, int speciesID)
//    {
//        long duplicateNumber = 0;
//        long uniqueNumber = 0;
//        long updateNumber = 0;
//        long counter = 0;
//
//        System.out.println("\n\n$ : The Program is updating the database for " + upstreamFlank + " upstreamFlank"
//                                   + " of speciesID = " + speciesID);
//
//        Downloader downloader = new Downloader();
//        HashMap<Integer, String> validGenesDatasetsNamesIDFromDB = DatabaseControllerForGenes.getValidGenesDatasetsNamesInDB();
//
//        if (!validGenesDatasetsNamesIDFromDB.containsKey(speciesID))
//        {
//            System.out.println("\n$ : The speciesID = " + speciesID + " is not valid. ");
//            return new long[]{uniqueNumber, duplicateNumber, updateNumber};
//        }
//
//        long genesCount = DatabaseControllerForGenes.getAllGenesCount(speciesID);
//        String datasetName = validGenesDatasetsNamesIDFromDB.get(speciesID);
//        HashSet<Transcript> allTranscriptsInDBForSpeciesID = getAllTranscriptsOfSpeciesIDFromDB(speciesID);
//
//        HashSet<String> genesStableID = DatabaseControllerForGenes.getAllgenesStableIDOfSpeciesIDFromDB(speciesID);
//        ArrayList<HashSet<String>> dividedGenesStableID = Utilities.split(genesStableID, 100);
//
//        for (HashSet<String> chunk : dividedGenesStableID)
//        {
//            String cdsFasta = downloader.downloadAllTranscriptsCDSUpstreamFlankOfGeneStableIDListFromCloud(chunk,
//                    datasetName, upstreamFlank);
//            while (cdsFasta.isEmpty() || cdsFasta.charAt(0) != '>')
//            {
//                System.err.println("\n$ : cdsFasta is empty or error occurred!");
//                System.err.println("$ : cdsFasta ==> " + cdsFasta);
//                System.err.println("$ : SpeciesID/database/flank : " + speciesID + "/"
//                        + validGenesDatasetsNamesIDFromDB.get(speciesID) + "/" + upstreamFlank);
//                System.out.println("\n$ : Unacceptable result for (" + datasetName + ") database from cloud!");
//                cdsFasta = downloader.downloadAllTranscriptsCDSUpstreamFlankOfGeneStableIDListFromCloud(chunk,
//                        datasetName, upstreamFlank);
//            }
//            HashSet<Transcript> cdsTranscriptsInCloud;
//            if (upstreamFlank <= 120)
//            {
//                cdsTranscriptsInCloud = FastaToTranscript
//                        .readTranscripts(cdsFasta, STROrigin.CDS_START_CODON, "protein_coding");
//            }
//            else
//            {
//                cdsTranscriptsInCloud = FastaToTranscript
//                        .readTranscripts(cdsFasta, STROrigin.CDS_EXCEPT_START_CODON, "protein_coding");
//            }
//
//            cdsFasta = "";
//
//            //                String cdnaFasta = downloader.downloadAllTranscriptsCDNAUpstreamFlankOfGeneStableIDListFromCloud(
//            //                        chunk, datasetsNamesIDFromDB.get(speciesID), 125);
//            //                HashSet<Transcript> cdnaTranscriptsInCloud = FastaToTranscript.readTranscripts(cdnaFasta, STROrigin.CDNA_START_CODON);
//            //                cdnaFasta = "";
//            //
//            //                HashSet<Transcript> chunkTranscripts = mergeTranscripts(cdsTranscriptsInCloud, cdnaTranscriptsInCloud);
//            HashSet<Transcript> chunkTranscripts = cdsTranscriptsInCloud;
//            //                cdsTranscriptsInCloud.clear();
//            //                cdnaTranscriptsInCloud.clear();
//
//            boolean sw;
//            for (Transcript candidate : chunkTranscripts)
//            {
//                if (allTranscriptsInDBForSpeciesID.contains(candidate))
//                {
//                    for (Transcript ori : allTranscriptsInDBForSpeciesID)
//                    {
//                        if (ori.equals(candidate))
//                        {
//                            sw = false;
//                            if (!Objects.equals(ori.getCdsPromoter(), candidate.getCdsPromoter()))
//                            {
//                                ori.setCdsPromoter(candidate.getCdsPromoter());
//                                sw = true;
//                            }
//                            if (!Objects.equals(ori.getCdnaPromoter(), candidate.getCdnaPromoter()))
//                            {
//                                ori.setCdnaPromoter(candidate.getCdnaPromoter());
//                                sw = true;
//                            }
//                            if (sw)
//                                updateNumber++;
//                        }
//                    }
//                    duplicateNumber++;
//                }
//                else
//                {
//                    allTranscriptsInDBForSpeciesID.add(candidate);
//                    uniqueNumber++;
//                }
//            }
//            chunkTranscripts.clear();
//
//            counter += chunk.size();
//            Utilities.showProgress(genesCount, counter, 50,
//                                   " ---> (" + genesCount + "/" + counter + " Genes processed for "
//                                           + datasetName + ")");
//        }
//
//        bulkInsertOrUpdate(allTranscriptsInDBForSpeciesID, false);
//        allTranscriptsInDBForSpeciesID.clear();
//
//        return new long[]{uniqueNumber, duplicateNumber, updateNumber};
//    }

    public static ArrayList<String> getTranscriptPromoterOfTranscriptID(String transcriptID, int upstreamFlank)
            throws Exception
    {
        Downloader downloader = new Downloader();
        HashMap<Integer, String> validGenesDatasetsNamesIDFromDB =
                DatabaseControllerForGenes.getValidGenesDatasetsNamesInDB();

        ArrayList<Integer> speciesIDs = getAllSpeciesIDsOfTranscriptList(
                new ArrayList<>(Collections.singleton(transcriptID)));

        int speciesID;
        if (speciesIDs.size() != 1)
        {
            System.out.println("\n$ : The transcriptStableID = " + transcriptID + " belong to more than one species or " +
                                       " there is not any species fot it. species size = " + speciesIDs.size());
            return new ArrayList<>(Arrays.asList("", ""));
        }
        else
            speciesID = speciesIDs.get(0);

        if (!validGenesDatasetsNamesIDFromDB.containsKey(speciesID))
        {
            System.out.println("\n$ : The speciesID = " + speciesID + " for transcriptID = " +
                                       transcriptID + " is not valid. ");
            return new ArrayList<>(Arrays.asList("", ""));
        }

        String datasetName = validGenesDatasetsNamesIDFromDB.get(speciesID);

        ArrayList<String> geneStableIDs = DatabaseControllerForGenes.getAllGeneStableIDsOfTranscriptList(
                new ArrayList<>(Collections.singleton(transcriptID)));

        String geneStableID;
        if (geneStableIDs.size() != 1)
        {
            System.out.println("\n$ : The transcriptStableID = " + transcriptID + " belong to more than one gene or " +
                                       " there is not any parent gene fot it. genesStableID size = " +
                                       geneStableIDs.size());
            return new ArrayList<>(Arrays.asList("", ""));
        }
        else
            geneStableID = geneStableIDs.get(0);

        int baseURLIndex = 0;
        String cdsFasta = downloader.downloadAllTranscriptsCDSUpstreamFlankOfGeneStableIDListFromCloud(
                new HashSet<>(Collections.singleton(geneStableID)),
                datasetName, upstreamFlank, baseURLIndex);
        while (cdsFasta.isEmpty() || cdsFasta.charAt(0) != '>')
        {
            System.err.println("\n$ : cdsFasta is empty or error occurred!");
            System.err.println("$ : cdsFasta ==> " + cdsFasta);
            System.err.println("$ : SpeciesID/database/flank : " + speciesID + "/"
                                       + validGenesDatasetsNamesIDFromDB.get(speciesID) + "/" + upstreamFlank);
            System.out.println("\n$ : Unacceptable result for (" + datasetName + ") database from cloud!");

            baseURLIndex = (++baseURLIndex) % 4;
            cdsFasta = downloader.downloadAllTranscriptsCDSUpstreamFlankOfGeneStableIDListFromCloud(
                    new HashSet<>(Collections.singleton(geneStableID)),
                    datasetName, upstreamFlank, baseURLIndex);
        }
        HashSet<Transcript> cdsTranscriptsInCloud;
        if (upstreamFlank <= 120)
        {
            cdsTranscriptsInCloud = FastaToTranscript
                    .readTranscripts(cdsFasta, STROrigin.CDS_START_CODON, "protein_coding");
        }
        else
        {
            cdsTranscriptsInCloud = FastaToTranscript
                    .readTranscripts(cdsFasta, STROrigin.CDS_EXCEPT_START_CODON, "protein_coding");
        }

        cdsFasta = "";

//        String cdnaFasta = downloader.downloadAllTranscriptsCDNAUpstreamFlankOfGeneStableIDListFromCloud(
//                chunk, datasetsNamesIDFromDB.get(speciesID), 125);
//        HashSet<Transcript> cdnaTranscriptsInCloud = FastaToTranscript.readTranscripts(cdnaFasta, STROrigin.CDNA_START_CODON);
//        cdnaFasta = "";
//
//        HashSet<Transcript> chunkTranscripts = mergeTranscripts(cdsTranscriptsInCloud, cdnaTranscriptsInCloud);

        if (cdsTranscriptsInCloud.size() != 1)
        {
            System.out.println("\n$ : The transcriptStableID = " + transcriptID + " belong to more than one " +
                                       "transcript or there is not any transcript fot it. transcript size in DB = " +
                                       cdsTranscriptsInCloud.size());
            return new ArrayList<>(Arrays.asList("", ""));
        }

        return new ArrayList<>(Arrays.asList(
                cdsTranscriptsInCloud.iterator().next().getCdsPromoter(),
                ""));
    }

    public static HashMap<String, String> getAllSequenceOfTranscriptStableIDsFromCloud(
            ArrayList<String> transcriptStableIDs, TranscriptSequenceType sequenceType, int maxLength) throws Exception
    {
        return getAllSequenceOfTranscriptStableIDsFromCloud(transcriptStableIDs, sequenceType, maxLength,
                                                            null, false, true, false);
    }
     public static HashMap<String, String> getAllSequenceOfTranscriptStableIDsFromCloud(
            ArrayList<String> transcriptStableIDs, TranscriptSequenceType sequenceType, int maxLength,
            Integer speciesID, boolean isEmptyResultValid, boolean showLog, boolean showProgress) throws Exception
     {
//        Integer speciesID;
        HashMap<String, String> sequencesInCloud;

        if (showLog)
        {
            System.out.println("\n$ : The Program is fetching " + sequenceType.name() + ".");
        }

        if (speciesID == null)
        {
            if (showLog)
            {
                System.out.println("\n$ : The Program is checking species ID for list of Transcripts.");
            }
            ArrayList<Integer> speciesIDs =
                    DatabaseControllerForTranscripts.getAllSpeciesIDsOfTranscriptList(transcriptStableIDs);

            if (showLog)
            {
                System.out.println("$ : checking species ID for list of Transcripts finished.");
            }

            if (speciesIDs.isEmpty())
            {
                System.err.println("$ : The transcripts IDs are not valid or are not available in the DB!");
                System.err.println("$ : The valid species ID does not recognize for them in the DB!");
                return null;
            }
            else if (speciesIDs.size() > 1)
            {
                System.err.println("$ : The transcripts IDs belong to more than one species in the DB!");
                System.err.println("$ : The species includes = " + speciesIDs.toString());
                return null;
            }

            speciesID = speciesIDs.get(0);
        }

        if (showLog)
        {
            System.out.println("$ : The transcripts IDs belong to the speciesID = " + speciesID + ".");
        }

        Downloader downloader = new Downloader();
        HashMap<Integer, String> validGenesDatasetsNamesIDFromDB = DatabaseControllerForGenes.getValidGenesDatasetsNamesInDB();

        if (!validGenesDatasetsNamesIDFromDB.containsKey(speciesID))
        {
            System.out.println("\n$ : The speciesID = " + speciesID + " is not valid. ");
            return null;
        }

        String datasetName = validGenesDatasetsNamesIDFromDB.get(speciesID);

        ArrayList<HashSet<String>> dividedTranscriptsStableID = Utilities.split(
                new HashSet<>(transcriptStableIDs), 100); // TODO :split length 2 => 100

        sequencesInCloud = new HashMap<>();
        int counter = 0;
        for (HashSet<String> chunk : dividedTranscriptsStableID)
        {
            int baseURLIndex = 0;
            String sequencesFasta =
                    downloader.downloadAllSequenceOfTranscriptStableIDListFromCloud(
                            chunk, datasetName, sequenceType, baseURLIndex, 8);
            while (sequencesFasta.isEmpty() || sequencesFasta.charAt(0) != '>')
            {
                if (showLog)
                {
                    System.err.println("\n$ : sequencesFasta is empty or error occurred!");
                    System.err.println("$ : sequencesFasta ==> " + sequencesFasta);
                    System.err.println("$ : SpeciesID/database : " + speciesID + "/"
                                               + validGenesDatasetsNamesIDFromDB.get(speciesID));
                    System.out.println("\n$ : Unacceptable result for (" + datasetName + ") database from cloud!");
                }
                if (isEmptyResultValid && baseURLIndex == 3 && (sequencesFasta.isEmpty() || sequencesFasta.charAt(0) != '>'))
                {
                    sequencesFasta = "";
                    break;
                }

                baseURLIndex = (++baseURLIndex) % 4;
                sequencesFasta =
                        downloader.downloadAllSequenceOfTranscriptStableIDListFromCloud(
                                chunk, datasetName, sequenceType, baseURLIndex, 8);
            }
            if (!sequencesFasta.isEmpty())
                sequencesInCloud.putAll(FastaToTranscript.readUsualSequences(sequencesFasta, maxLength));

            sequencesFasta = "";

            if (showProgress)
            {
            counter += chunk.size();
            Utilities.showProgress(transcriptStableIDs.size(), counter, 50,
                                   " ---> (" + transcriptStableIDs.size() + "/" + counter +
                                           " Transcripts processed for " + datasetName + ")");
            }
        }

        return sequencesInCloud;
    }

    private static ArrayList<Integer> getAllSpeciesIDsOfTranscriptList(ArrayList<String> transcriptsStableIDs)
    {
        Transaction tx = null;
        ArrayList<Integer> results = new ArrayList<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Integer> query = builder.createQuery(Integer.class);
            Root<Species> root = query.from(Species.class);
            query.select(root.get(Species_.ID))
                 .distinct(true)
                 .where(root.join(Species_.genesSet)
                            .join(Gene_.transcriptsSet)
                            .get(Transcript_.transcriptStableID).in(transcriptsStableIDs));
            List<Integer> allTranscriptStableIDs = session.createQuery(query).getResultList();
            tx.commit();
            results.addAll(allTranscriptStableIDs);
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }

        return results;
    }

    public static ArrayList<String> getAllSTRProducerTranscriptsStableIDOfSpecies(int speciesID)
    {
        Transaction tx = null;
        ArrayList<String> results = new ArrayList<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            //noinspection rawtypes
            Query query = session.createQuery(
                    "select distinct t.transcriptStableID from Transcript t inner join Gene g " +
                            "on t.geneStableID = g.geneStableID inner join STR s on s.transcriptStableId = t.transcriptStableID " +
                            "where g.speciesId = :speciesId ", String.class)
                    .setParameter("speciesId", speciesID);
            //noinspection unchecked
            List<String> allSpeciesIDs = query.list();
            tx.commit();
            results.addAll(allSpeciesIDs);
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }

        return results;
    }

    public static ArrayList<String> getAllSTRProducerTranscriptsStableIDOfSpecies(
            int speciesID, List<String> listOfSTRs)
    {
        Transaction tx = null;
        ArrayList<String> results = new ArrayList<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<String> query = builder.createQuery(String.class);
            Root<Transcript> root = query.from(Transcript.class);
            Predicate speciesItem = builder.equal(
                    root.join(Transcript_.gene).join(Gene_.species).get(Species_.ID), speciesID);
            Predicate strSet = root.join(Transcript_.strsSet).get(STR_.sequence).in(listOfSTRs);
            query.select(root.get(Transcript_.transcriptStableID)).distinct(true)
                 .where(builder.and(speciesItem, strSet));
            List<String> allTranscriptsStableID = session.createQuery(query).getResultList();
            tx.commit();
            results.addAll(allTranscriptsStableID);
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }

        return results;
    }

    public static ArrayList<Transcript> getAllSTRProducerTranscripts(int speciesID, List<String> listOfSTRs)
    {
        Transaction tx = null;
        ArrayList<Transcript> results = new ArrayList<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Transcript> query = builder.createQuery(Transcript.class);
            Root<Transcript> root = query.from(Transcript.class);
            Predicate speciesItem = builder.equal(
                    root.join(Transcript_.gene).join(Gene_.species).get(Species_.ID), speciesID);
            Predicate strSet = root.join(Transcript_.strsSet).get(STR_.sequence).in(listOfSTRs);
            query.select(root).distinct(true)
                 .where(builder.and(speciesItem, strSet));
            List<Transcript> allTranscripts = session.createQuery(query).getResultList();
            tx.commit();
            results.addAll(allTranscripts);
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }

        return results;
    }

    //    public static long[] updateTranscriptsForGeneID(int upstreamFlank, String geneID)
//    {
//        long duplicateNumber = 0;
//        long uniqueNumber = 0;
//
//        Downloader downloader = new Downloader();
//
//        HashMap<Integer, String> datasetsNamesIDFromDB = new HashMap<>(
//                DatabaseControllerForGenes.getAllGenesDatasetsNamesFormDB());
//
//        HashSet<Transcript> allTranscriptsInDBForSpeciesID = getAllTranscriptsOfSpeciesIDFromDB(speciesID);
//
//
//        HashSet<String> genesStableID = new HashSet<>();
//        genesStableID.add(geneID);
//        ArrayList<HashSet<String>> splitedGenesStableID = Utilities.split(genesStableID, 100);
//
//        for (HashSet<String> chunk : splitedGenesStableID)
//        {
//            String cdsFasta = downloader.downloadAllTranscriptsCDSUpstreamFlankOfGeneStableIDListFromCloud(chunk,
//                                                                                                           datasetsNamesIDFromDB
//                                                                                                                   .get(speciesID),
//                                                                                                           upstreamFlank);
//            while (cdsFasta.isEmpty() || cdsFasta.charAt(0) != '>')
//            {
//                System.err.println("cdsFasta is empty or error occurred!");
//                cdsFasta = downloader.downloadAllTranscriptsCDSUpstreamFlankOfGeneStableIDListFromCloud(chunk,
//                                                                                                        datasetsNamesIDFromDB
//                                                                                                                .get(speciesID),
//                                                                                                        upstreamFlank);
//            }
//            HashSet<Transcript> cdsTranscriptsInCloud;
//            if (upstreamFlank <= 120)
//            {
//                cdsTranscriptsInCloud = FastaToTranscript
//                        .readTranscripts(cdsFasta, STROrigin.CDS_START_CODON, "protein_coding");
//            }
//            else
//            {
//                cdsTranscriptsInCloud = FastaToTranscript
//                        .readTranscripts(cdsFasta, STROrigin.CDS_EXCEPT_START_CODON, "protein_coding");
//            }
//
//            cdsFasta = "";
//
//            //                String cdnaFasta = downloader.downloadAllTranscriptsCDNAUpstreamFlankOfGeneStableIDListFromCloud(
//            //                        chunk, datasetsNamesIDFromDB.get(speciesID), 125);
//            //                HashSet<Transcript> cdnaTranscriptsInCloud = FastaToTranscript.readTranscripts(cdnaFasta, STROrigin.CDNA_START_CODON);
//            //                cdnaFasta = "";
//            //
//            //                HashSet<Transcript> chunkTranscripts = mergeTranscripts(cdsTranscriptsInCloud, cdnaTranscriptsInCloud);
//            HashSet<Transcript> chunkTranscripts = cdsTranscriptsInCloud;
//            //                cdsTranscriptsInCloud.clear();
//            //                cdnaTranscriptsInCloud.clear();
//
//            for (Transcript candidate : chunkTranscripts)
//            {
//                if (allTranscriptsInDBForSpeciesID.contains(candidate))
//                {
//                    for (Transcript ori : allTranscriptsInDBForSpeciesID)
//                    {
//                        if (ori.equals(candidate))
//                        {
//                            if (!Objects.equals(ori.getCdsPromoter(), candidate.getCdsPromoter()))
//                                ori.setCdsPromoter(candidate.getCdsPromoter());
//                            if (!Objects.equals(ori.getCdnaPromoter(), candidate.getCdnaPromoter()))
//                                ori.setCdnaPromoter(candidate.getCdnaPromoter());
//                        }
//                    }
//                    duplicateNumber++;
//                }
//                else
//                {
//                    allTranscriptsInDBForSpeciesID.add(candidate);
//                    uniqueNumber++;
//                }
//            }
//            chunkTranscripts.clear();
//
//            counter += chunk.size();
//        }
//
//            bulkInsertOrUpdate(allTranscriptsInDBForSpeciesID);
//            allTranscriptsInDBForSpeciesID.clear();
//
//        return new long[]{uniqueNumber, duplicateNumber};
//    }

    public static ArrayList<Gene> getProducerGenes(ArrayList<String> transcriptsStableIDs)
    {
        Transaction tx = null;
        ArrayList<Gene> results = new ArrayList<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Gene> query = builder.createQuery(Gene.class);
            Root<Gene> root = query.from(Gene.class);
            query.select(root)
                 .where(root.join(Gene_.transcriptsSet).get(Transcript_.transcriptStableID).in(transcriptsStableIDs));
            List<Gene> allGenes = session.createQuery(query).getResultList();
            tx.commit();
            results.addAll(allGenes);
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }

        return results;
    }
}
