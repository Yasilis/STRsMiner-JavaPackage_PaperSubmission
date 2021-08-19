package com.alimaddi.control;

import com.alimaddi.Utility.HibernateUtility;
import com.alimaddi.Utility.Utilities;
import org.hibernate.*;

import java.util.ArrayList;
import java.util.List;

class DatabaseControllerReport
{
    private static SessionFactory sessionFactory = HibernateUtility.getSessionFactory();


    public static ArrayList<ArrayList<String>> getAllOccurrenceOf(String str)
    {
        Transaction tx = null;
        ArrayList<ArrayList<String>> results = new ArrayList<>();
        List<Object[]> listOfOccurrence = null;
        String core;
        int repeat;
        String[] output;

        output = Utilities.decomposeSTR(str);
        core = output[0];
        repeat = Integer.parseInt(output[1]);

        try (Session session = sessionFactory.openSession())
        {
            tx = session.beginTransaction();
//            Criteria strCriteria = session.createC
            listOfOccurrence = session
                    .createQuery("select sp.commonName, sp.id, g.geneName, g.geneStableID, t.transcriptStableID, s.sequence, " +
                                         "s.abundance from STR as s inner join Transcript as t on " +
                                         "s.transcriptStableId = t.transcriptStableID inner join Gene as g on " +
                                         "t.geneStableID = g.geneStableID inner  join Species as sp on " +
                                         "sp.id = g.speciesId where s.sequence like :pattern", Object[].class)
                    .setParameter("pattern", "(" + core + ")%").list();
            tx.commit();
        }
        catch (HibernateException he)
        {
            if (tx != null)
                tx.rollback();
        }
        if (listOfOccurrence != null)
        {
            for (Object[] objects : listOfOccurrence)
            {
                ArrayList<String> primaryOutput = new ArrayList<>();
                for (Object object : objects)
                {
//                    if (object instanceof String)
                        primaryOutput.add("" + object);
//                    else if (object instanceof Integer)
//                        primaryOutput.add("" + object);
                }
                results.add(primaryOutput);
            }
        }

        if (listOfOccurrence != null && !listOfOccurrence.isEmpty())
            listOfOccurrence.clear();

        return results;
    }
}
