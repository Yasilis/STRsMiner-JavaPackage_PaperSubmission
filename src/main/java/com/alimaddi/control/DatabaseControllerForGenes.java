package com.alimaddi.control;

import com.alimaddi.Utility.HibernateUtility;
import com.alimaddi.Utility.Reader;
import com.alimaddi.Utility.Utilities;
import com.alimaddi.control.downloader.Downloader;
import com.alimaddi.control.runnables.ThreadWorkerForGettingGeneFromCloud;
import com.alimaddi.model.*;
import org.hibernate.*;
import org.hibernate.query.Query;

import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DatabaseControllerForGenes
{
    private static final SessionFactory sessionFactory = HibernateUtility.getSessionFactory();


    //region Checked DB 3
    public static int[] updateGenesForSpeciesID(int speciesID, boolean log) throws Exception
    {
        if (log)
            System.out.println("\n\n$ : The Program is updating the database for Genes of speciesID = " + speciesID);

        int duplicateNumber = 0;
        int uniqueNumber = 0;
        int updateNumber = 0;

        Downloader downloader = new Downloader();
        ArrayList<Gene> checkForDuplication = new ArrayList<>();

        HashMap<Integer, String> datasetsNamesIDFromDB = getAllGenesDatasetsNamesFormDB();
        String datasetName = datasetsNamesIDFromDB.get(speciesID);
        if (isValidGeneDatasetName(datasetName))
        {
            StringBuilder result = new StringBuilder(downloader.downloadGeneList(datasetName));
            while (!isResultCorrect(result))
            {
                System.err.println("\n$ : Error occurs");
                System.err.println("\n$ : " + result);
                System.out.println("\n$ : Unacceptable result for fetching genes of " +
                                           "(" + datasetName + ") database from cloud!");
                result = new StringBuilder(downloader.downloadGeneList(datasetName));
            }
            HashSet<Gene> genesInDB = getAllGenesOfSpeciesIDFromDB(speciesID);

            Scanner scan = new Scanner(result.toString());
            int counter = 0;
            int i = 0;
            while (scan.hasNextLine())
            {
                String line = "";
                try
                {
                    line = scan.nextLine();
                    counter += line.length() + 2;//TODO : This counter is not sync with data :) . Please correct it!
                    i++;
                    String[] parts = line.trim().split("\\t");
                    Gene gene = new Gene();
                    gene.setGeneStableID(parts[0]);
                    gene.setType(parts[1]);
                    if (parts.length > 2)
                        gene.setGeneName(parts[2]);
                    gene.setLastUpdateTime(new Date());
                    gene.setSpecies(DatabaseControler.getSpeciesFromDB(speciesID));

                    if (genesInDB.contains(gene))
                    {
                        if (gene.getGeneName() != null && !gene.getGeneName().isEmpty())
                            checkForDuplication.add(gene);
                        duplicateNumber++;
                    }
                    else
                    {
                        DatabaseControler.insertOrUpdate(gene);
                        uniqueNumber++;
                    }
                    if (log)
                        Utilities.showProgress(result.length(), counter, 50,
                                               datasetsNamesIDFromDB.get(speciesID) +
                                                       "(" + i + " genes updated)");
                }
                catch (Exception e)
                {
                    System.out.println("\n$ : line = " + line);
                    e.printStackTrace();
                }
            }
            if (log)
                System.out.println("\n$ : The final check to update duplicated Genes of speciesID = " + speciesID);

            counter = 0;
            for (Gene gene : checkForDuplication)
            {
                counter ++;
                if (shouldItBeUpdated(gene, genesInDB))
                {
                    update(gene);
                    updateNumber++;
                }
                if (log)
                    Utilities.showProgress(checkForDuplication.size(), counter, 50,
                                       datasetsNamesIDFromDB.get(speciesID)
                                               + "(" + checkForDuplication.size() + "/" +
                                               counter + " gene names updated)");
            }
            genesInDB.clear();
            checkForDuplication.clear();
        }
        else
        {
            if (!datasetsNamesIDFromDB.containsKey(speciesID))
                System.out.println("\n$ : The speciesID = " + speciesID + " is not valid. ");
            else
            {
                System.out.println("\n$ : The speciesID = " + speciesID + " for species = " +
                                           DatabaseControler.getSpeciesFromDB(speciesID).getName() +
                                           " does not have valid genes database name (" +
                                           datasetsNamesIDFromDB.get(speciesID) + ")");
            }
        }
        return new int[]{uniqueNumber, duplicateNumber, updateNumber};
    }

    public static int[] updateGenes(int threadNumber) throws Exception
    {
        System.out.println("\n\n$ : The Program is updating the database for Genes of species");

        int duplicateNumber = 0;
        int uniqueNumber = 0;
        int updateNumber = 0;

        HashMap<Integer, String> validDatasetsNamesIDFromDB = getValidGenesDatasetsNamesInDB();

        ArrayList<String> diffGenesDatasetsNames = new ArrayList<>(getAllGenesDatasetsNamesFormDB().values());
        diffGenesDatasetsNames.removeAll(validDatasetsNamesIDFromDB.values());
        System.out.print("\n$ : ------------------------------------------------------------------------------- \n" +
                                 "$ : " + diffGenesDatasetsNames.size() +
                                 " Genes datasets are not processed! Because there are not any obvious ID for them.\n" +
                                 "$ : In below you can see them. They are listed as :\n" +
                                 "$ : " + diffGenesDatasetsNames.toString() + "\n$ : " +
                                 "------------------------------------------------------------------------------- \n" +
                                 "\n\n");

        System.out.print("\n$ : The Program is updating the database for Genes of remains species\n");

        System.out.println("$ : Filtering valid Datasets Names ID From DB");
        HashMap<Integer, String> filteredDatasetsNamesIDFromDB =
                getAppropriateValidDatasetsNamesIDFromDB(validDatasetsNamesIDFromDB);
        System.out.println("$ : Filtering finished");
        System.out.print("\n$ : ------------------------------------------------------------------------------- \n" +
                                 "$ : " + filteredDatasetsNamesIDFromDB.size() +
                                 " Genes datasets are selected! They are listed as :\n" +
                                 "$ : " + filteredDatasetsNamesIDFromDB.toString() + "\n$ : " +
                                 "------------------------------------------------------------------------------- \n" +
                                 "\n\n");

        int availableSystemCPUThreads;
        if (threadNumber <= 0)
            availableSystemCPUThreads = Runtime.getRuntime().availableProcessors() - 1;
        else
            availableSystemCPUThreads = threadNumber;

        System.out.println("\n$ : The parallel retrieving genes of species process start with " +
                                   availableSystemCPUThreads + " available system CPU threads!\n");


        int counter = 0;
        if (availableSystemCPUThreads == 1)
        {
            for (Integer id : filteredDatasetsNamesIDFromDB.keySet())
            {
                Utilities.showProgress(filteredDatasetsNamesIDFromDB.size(), counter, 50, filteredDatasetsNamesIDFromDB.get(id) + "\t\t");

                int[] speciesResult = updateGenesForSpeciesID(id, false);

                uniqueNumber += speciesResult[0];
                duplicateNumber += speciesResult[1];
                updateNumber += speciesResult[2];
                counter ++;

                Utilities.showProgress(filteredDatasetsNamesIDFromDB.size(), counter, 50, filteredDatasetsNamesIDFromDB.get(id) + "\t\t");

            }
            return new int[]{uniqueNumber, duplicateNumber, updateNumber};
        }
        else
        {
            ExecutorService executorService = Executors.newFixedThreadPool(availableSystemCPUThreads);

            ArrayList<ThreadWorkerForGettingGeneFromCloud> tasks = new ArrayList<>();

            for (Integer speciesID : filteredDatasetsNamesIDFromDB.keySet())
            {
                ThreadWorkerForGettingGeneFromCloud task = new ThreadWorkerForGettingGeneFromCloud(speciesID);
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
            return new int[]{uniqueNumber, duplicateNumber, updateNumber};
        }
    }

    public static HashMap<Integer, String> getAppropriateValidDatasetsNamesIDFromDB(
            HashMap<Integer, String> validDatasetsNamesIDFromDB)
    {
        HashMap<Integer, String> result = new HashMap<>();
        ArrayList<Integer> filteredSpeciesID =
                DatabaseControler.getFilteredSpeciesIDs("./assets/FavoriteSpecies.csv");
        for (Integer speciesID : validDatasetsNamesIDFromDB.keySet())
        {
//            if (speciesID == 7)
            if (filteredSpeciesID.contains(speciesID))
                result.put(speciesID, validDatasetsNamesIDFromDB.get(speciesID));
        }
        return result;
    }

    private static boolean shouldItBeUpdated(Gene gene, HashSet<Gene> source)
    {
        for (Gene ori : source)
        {
            if (gene.equals(ori))
            {
                // We know gene name is not null or empty!
                return !gene.getGeneName().equals(ori.getGeneName());
            }
        }
        return false;
    }

    private static boolean isResultCorrect(StringBuilder result)
    {
        if (result == null || result.length() == 0)
            return false;
        if (result.length() >= 11 && result.substring(0, 11).equals("Query ERROR"))
            return false;
        return result.substring(0, 3).equals("ENS") ||
                result.charAt(0) == 'Y' ||
                result.charAt(0) == 'Q' ||
                result.substring(0, 3).equals("MGP") ||
                result.substring(0, 6).equals("WBGene") ||
                result.substring(0, 4).equals("FBgn");
    }

    public static Gene getGeneFromDB(String geneStableID)
    {
        Transaction tx = null;
        Gene result = null;

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Gene> query = builder.createQuery(Gene.class);
            Root<Gene> root = query.from(Gene.class);
            query.select(root).where(builder.equal(root.get(Gene_.geneStableID), geneStableID));
            result = session.createQuery(query).getSingleResult();
            tx.commit();
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }
        catch (Exception e) //TODO : **** adding global catch Exeption for double check in all try catch!
        {
            e.printStackTrace();
        }

        return result;
    }

    public static HashSet<Gene> getAllGenesOfSpeciesIDFromDB(int speciesID)
    {
        Transaction tx = null;
        HashSet<Gene> results = new HashSet<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Gene> query = builder.createQuery(Gene.class);
            Root<Gene> root = query.from(Gene.class);
            query.select(root).where(builder.equal(root.join(Gene_.species).get(Species_.ID), speciesID));
            List<Gene> allGenesOfID = session.createQuery(query).getResultList();
            tx.commit();
            results.addAll(allGenesOfID);
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

    public static HashMap<Integer, String> getAllGenesDatasetsNamesFormDB()
    {
        Transaction tx = null;
        HashMap<Integer, String> results = new HashMap<>();
        List<Object[]> allSpeciesIDName = new ArrayList<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Object[]> query = builder.createQuery(Object[].class);
            Root<Species> root = query.from(Species.class);
            query.multiselect(root.get(Species_.ID), root.get(Species_.NAME));
            allSpeciesIDName = session.createQuery(query).getResultList();
            tx.commit();
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }

        for (Object[] row : allSpeciesIDName)
        {
            Integer speciesID = (Integer)row[0];
            String speciesName = (String)row[1];

            String[] parts = speciesName.trim().split("_");
            if (parts.length == 1)
                results.put(speciesID, "" + parts[0] + "_gene_ensembl");
            else if (parts.length == 2)
                results.put(speciesID, "" + parts[0].charAt(0) + parts[1] + "_gene_ensembl");
            else if (parts.length == 3)
                results.put(speciesID, "" + parts[0].charAt(0) + parts[1].charAt(0) + parts[2] + "_gene_ensembl");
            else
                results.put(speciesID, "" + parts[0].charAt(0) + parts[1].charAt(0) + parts[parts.length-1] + "_gene_ensembl");
        }

        return results;
    }

    public static HashSet<String> getAllGenesStableIDOfSpeciesIDFromDB(int speciesID)
    {
        HashSet<String> results = new HashSet<>();

        for (Gene gene : getAllGenesOfSpeciesIDFromDB(speciesID))
        {
            results.add(gene.getGeneStableID());
        }

//        Transaction tx = null;
//        try (Session session = sessionFactory.openSession())
//        {
//            tx = session.beginTransaction();
//            CriteriaBuilder builder = session.getCriteriaBuilder();
//            CriteriaQuery<Species> query = builder.createQuery(Species.class);
//            Root<Species> root = query.from(Species.class);
//            root.fetch(Species_.genesSet, JoinType.INNER);
//            query.select(root).where(builder.equal(root.get(Species_.ID), speciesID));
//            Species species = session.createQuery(query).getSingleResult();
//            Set<Gene> genes = species.getGenesSet();
//            for (Gene gene : genes)
//            {
//                results.add(gene.getGeneStableID());
//            }
//            tx.commit();
//        }
//        catch (HibernateException he)
//        {
//            if (tx != null)
//                tx.rollback();
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }

        return results;
    }
    //endregion

    public static ArrayList<Integer> getAllSpeciesIDinGeneTableFromDB()
    {
        Transaction tx = null;
        List<Integer> results = null;

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            results = session
                    .createQuery("select e.speciesId from Gene as e group by e.speciesId", Integer.class).list();
            tx.commit();
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }
        if (results == null)
            return new ArrayList<>();
        else
            return new ArrayList<>(results);
    }


    //region Checked DB 3
    public static long getAllGenesCount()
    {
        Transaction tx = null;
        long results = 0;

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Long> query = builder.createQuery(Long.class);
            Root<Gene> root = query.from(Gene.class);
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

    public static long getAllGenesCount(int speciesID)
    {
        Transaction tx = null;
        long results = 0;

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Long> query = builder.createQuery(Long.class);
            Root<Gene> root = query.from(Gene.class);
            query.select(builder.count(root)).where(builder.equal(root.get(Gene_.species).get(Species_.ID), speciesID));
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

    public static boolean isValidGeneDatasetName(String datasetName) throws Exception
    {
        HashMap<Integer, String> validGenesDatasetsNamesInDB = getValidGenesDatasetsNamesInDB();
        return validGenesDatasetsNamesInDB.containsValue(datasetName);
    }

    public static HashMap<Integer, String> getValidGenesDatasetsNamesInDB() throws Exception
    {
        HashMap<Integer, String> result = new HashMap<>();

        ArrayList<String> genesDatasetsNamesFromR =
                Reader.readGeneDatasetNameList("./assets/GeneDatasetNameList_DB_V_3.txt");
        HashMap<Integer, String> datasetsNamesIDFromDB = getAllGenesDatasetsNamesFormDB();
        Set<String> validGenesDatasetsNamesFromDB = new TreeSet<>(datasetsNamesIDFromDB.values());
        validGenesDatasetsNamesFromDB.retainAll(genesDatasetsNamesFromR);

        for (Integer id : datasetsNamesIDFromDB.keySet())
        {
            if (validGenesDatasetsNamesFromDB.contains(datasetsNamesIDFromDB.get(id)))
            {
                result.put(id, datasetsNamesIDFromDB.get(id));
            }
        }

        if (result.size() != validGenesDatasetsNamesFromDB.size())
            throw new Exception("\n$ : we are duplication Gene Dataset name! \n" + result.toString() +
                                        "\n$ : #######################");

        return result;
    }

    public static int update(Gene gene)
    {
        Transaction tx = null;
        int updateCount = 0;

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaUpdate<Gene> update = builder.createCriteriaUpdate(Gene.class);
            Root<Gene> root = update.from(Gene.class);
            update.set(Gene_.GENE_NAME, gene.getGeneName());
            update.set(Gene_.lastUpdateTime, new Date());
            update.where(builder.equal(root.get(Gene_.geneStableID), gene.getGeneStableID()));
            updateCount = session.createQuery(update).executeUpdate();
            tx.commit();
        }
        catch (HibernateException he)
        {
            he.printStackTrace();
            System.out.println(gene.toString());
            if (tx != null)
                tx.rollback();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(gene.toString());
        }

        return updateCount;
    }
    //endregion


    public static ArrayList<Gene> getAllHomologyGene(Gene gene)
    {
        Transaction tx = null;
        ArrayList<Gene> results = new ArrayList<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Gene> query = builder.createQuery(Gene.class);
            Root<Gene> root = query.from(Gene.class);
            query.select(root).distinct(true)
                 .where(builder.equal(root.get(Gene_.geneName), gene.getGeneName()));
            List<Gene> allHomologyGene = session.createQuery(query).getResultList();
            tx.commit();
            results.addAll(allHomologyGene);
        }
        catch (HibernateException he)
        {
            he.printStackTrace();
            if (tx != null)
                tx.rollback();
        }

        return results;
    }

    public static ArrayList<String> getAllGeneStableIDsOfTranscriptList(ArrayList<String> transcriptStableIDs)
    {
        Transaction tx = null;
        ArrayList<String> results = new ArrayList<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            //noinspection rawtypes
            Query query = session.createQuery(
                    "select distinct t.geneStableID from Transcript t " +
                            "where t.transcriptStableID in (:transcriptStableIDs)", String.class)
                                 .setParameterList("transcriptStableIDs", transcriptStableIDs);
            //noinspection unchecked
            List<String> allGeneStableIDs = query.list();
            tx.commit();
            results.addAll(allGeneStableIDs);
        }
        catch (HibernateException he)
        {
            he.printStackTrace();
            if (tx != null)
                tx.rollback();
        }

        return results;
    }

    public static HashSet<String> getAllGenesName()
    {
        Transaction tx = null;
        HashSet<String> results = new HashSet<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<String> query = builder.createQuery(String.class);
            Root<Gene> root = query.from(Gene.class);
            query.select(root.get(Gene_.GENE_NAME)).distinct(true);
            List<String> allGenesName = session.createQuery(query).getResultList();
            tx.commit();
            results.addAll(allGenesName);
        }
        catch (HibernateException he)
        {
            he.printStackTrace();
            if (tx != null)
                tx.rollback();
        }

        return results;
    }

    public static ArrayList<Gene> getAllSTRProducerGenes(int speciesID, List<String> listOfSTRs)
    {
        Transaction tx = null;
        ArrayList<Gene> results = new ArrayList<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Gene> query = builder.createQuery(Gene.class);
            Root<Gene> root = query.from(Gene.class);
            Predicate speciesItem = builder.equal(
                    root.join(Gene_.species).get(Species_.ID), speciesID);
            Predicate strSet =
                    root.join(Gene_.transcriptsSet).join(Transcript_.strsSet).get(STR_.sequence).in(listOfSTRs);

            query.select(root).distinct(true).where(builder.and(speciesItem, strSet));
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
