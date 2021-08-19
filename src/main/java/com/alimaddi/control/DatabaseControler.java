package com.alimaddi.control;

import com.alimaddi.Utility.HibernateUtility;
import com.alimaddi.Utility.Reader;
import com.alimaddi.Utility.Utilities;
import com.alimaddi.control.converter.JSON2POJO;
import com.alimaddi.control.downloader.Downloader;
import com.alimaddi.model.*;
import org.hibernate.*;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.util.*;

public class DatabaseControler
{
    private static final SessionFactory sessionFactory = HibernateUtility.getSessionFactory();

    //region Checked DB 3
    public static HashSet<Species> getAllSpeciesFromDB()
    {
        Transaction tx = null;
        HashSet<Species> results = new HashSet<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Species> query = builder.createQuery(Species.class);
            Root<Species> root = query.from(Species.class);
            query.select(root);
            List<Species> allSpecies = session.createQuery(query).getResultList();
            tx.commit();
            results.addAll(allSpecies);
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }
        return results;
    }

    // TODO : remove this method and use bellow methods (list argument)!!
    public static Species getSpeciesFromDB(int speciesID)
    {
        Transaction tx = null;
        Species Species = null;

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Species> query = builder.createQuery(Species.class);
            Root<Species> root = query.from(Species.class);
            query.select(root).where(builder.equal(root.get(Species_.ID), speciesID));
            Species = session.createQuery(query).getSingleResult();
            tx.commit();
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }
        return Species;
    }

    public static ArrayList<Species> getSpeciesFromDB(Collection<Integer> speciesIDs)
    {
        Transaction tx = null;
        ArrayList<Species> result = new ArrayList<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Species> query = builder.createQuery(Species.class);
            Root<Species> root = query.from(Species.class);
            query.select(root).distinct(true)
                 .where(root.get(Species_.ID).in(speciesIDs));
            List<Species> allSpecies = session.createQuery(query).getResultList();
            tx.commit();
            result.addAll(allSpecies);
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }
        return result;
    }

    public static void printAllSpecies()
    {
        System.out.println("The list of all species : ");
        getAllSpeciesFromDB().forEach(System.out::println);
    }

    public static int[] updateSpecies()
    {
        int duplicateNumber = 0;
        int uniqueNumber = 0;

        Downloader downloader = new Downloader();
        JSON2POJO converter = new JSON2POJO();

        ArrayList<Species> speciesInCloud;
        speciesInCloud = converter.convertAllSpecies(downloader.downloadAllSpecies());

        HashSet<Species> speciesInDB = getAllSpeciesFromDB();

        for (int i = 0 ; i < 5 ; i++)
            System.out.print("\n");
        System.out.println("The Program is updating the database for different type of species.");

        int counter = 0;
        Utilities.showProgress(speciesInCloud.size(), counter, 50, "" + "\t\t\t");
        for (Species sp : speciesInCloud)
        {
            if (!speciesInDB.contains(sp))
            {
                insertOrUpdate(sp);
                uniqueNumber++;
            }
            else
            {
                duplicateNumber++;
            }
            counter ++;

            Utilities.showProgress(speciesInCloud.size(), counter, 50, sp.getName() + "\t\t\t");
            try
            {
                Thread.sleep(20);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        return new int[]{uniqueNumber, duplicateNumber};
    }

    public static void insertOrUpdate(Object object)
    {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            session.saveOrUpdate(object);
            tx.commit();
        }
        catch (HibernateException he)
        {
            if (object instanceof Gene)//ENSTRUG00000013195  id={158,159}
            {
//                Gene gene = (Gene) object;
                System.err.println("$ : Duplication id!!!");
//                System.out.println(gene.toString());
                he.printStackTrace();
            }
            if (tx != null)
                tx.rollback();
        }
        catch (Exception e)
        {
            if (object instanceof Gene)//ENSTRUG00000013195  id={158,159}
            {
//                Gene gene = (Gene) object;
                System.err.println("$ : Duplication id!!!");
//                System.out.println(gene.toString());
            }
            e.printStackTrace();
        }
    }

    public static ArrayList<Integer> getFilteredSpeciesIDs(String fileName)
    {
        return Reader.readAppropriateSpeciesID(fileName);
    }

    public static ArrayList<Integer> getAllSpeciesIDsOfGenesList(ArrayList<String> genesStableIDs)
    {
        Transaction tx = null;
        ArrayList<Integer> results = new ArrayList<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Integer> query = builder.createQuery(Integer.class);
            Root<Gene> root = query.from(Gene.class);
            query.select(root.join(Gene_.species).get(Species_.ID)).distinct(true)
                 .where(root.get(Gene_.geneStableID).in(genesStableIDs));
            List<Integer> allGenesOfID = session.createQuery(query).getResultList();
            tx.commit();
            results.addAll(allGenesOfID);
        }
        catch (HibernateException he)
        {
            he.printStackTrace();
            if (tx != null)
                tx.rollback();
        }

        return results;
    }

    public static ArrayList<Integer> getAllSpeciesIDsOfGenesNameList(ArrayList<String> genesNames)
    {
        Transaction tx = null;
        ArrayList<Integer> results = new ArrayList<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Integer> query = builder.createQuery(Integer.class);
            Root<Gene> root = query.from(Gene.class);
            query.select(root.join(Gene_.species).get(Species_.ID)).distinct(true)
                 .where(root.get(Gene_.geneName).in(genesNames));
            List<Integer> allSpeciesID = session.createQuery(query).getResultList();
            tx.commit();
            results.addAll(allSpeciesID);
        }
        catch (HibernateException he)
        {
            he.printStackTrace();
            if (tx != null)
                tx.rollback();
        }

        return results;
    }
    //endregion









    public static ArrayList<Integer> getAllSpeciesIDsOfTranscriptList(ArrayList<String> transcriptStableIDs)
    {
        Transaction tx = null;
        ArrayList<Integer> results = new ArrayList<>();

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
            //noinspection rawtypes
            Query query = session.createQuery(
                    "select distinct g.speciesId from Transcript t inner join Gene g " +
                            "on t.geneStableID=g.geneStableID " +
                            "where t.transcriptStableID in (:transcriptStableIDs)", Integer.class)
                                 .setParameterList("transcriptStableIDs", transcriptStableIDs);
            //noinspection unchecked
            List<Integer> allSpeciesIDs = query.list();
            tx.commit();
            results.addAll(allSpeciesIDs);
        }
        catch (HibernateException he)
        {
            he.printStackTrace();
            if (tx != null)
                tx.rollback();
        }

        return results;
    }


    public static HashSet<Transcript> getAllTranscriptsOfGeneStableIDFromDB(String geneStableID)
    {
        HashSet<Transcript> results = null;
        List<Transcript> listOfTranscript = null;
        Session session = sessionFactory.openSession();
        Transaction tx = null;

        try
        {
            tx = session.beginTransaction();
            listOfTranscript = session
                    .createQuery("select e from Transcript as e where e.geneStableID = :geneStableID", Transcript.class)
                    .setParameter("geneStableID", geneStableID).list();
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
        if (listOfTranscript == null)
            results = new HashSet<>();
        else
            results = new HashSet<>(listOfTranscript);

        return results;
    }
}
