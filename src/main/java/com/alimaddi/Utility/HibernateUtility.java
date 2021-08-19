package com.alimaddi.Utility;

import com.alimaddi.model.Gene;
import com.alimaddi.model.STR;
import com.alimaddi.model.Species;
import com.alimaddi.model.Transcript;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class HibernateUtility
{
    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;

//    static
//    {
//        try
//        {
//            Configuration configuration = new Configuration();
//            configuration.addAnnotatedClass(Species.class);
//            configuration.addAnnotatedClass(Gene.class);
//            configuration.addAnnotatedClass(Transcript.class);
//            configuration.addAnnotatedClass(STR.class);
//
//
//            //driver
//            configuration.setProperty("connection.driver_class", "com.mysql.jdbc.Driver");
//
//            //url
//            configuration.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/STRsDataBase2");
//            //TODO correct database and other dependencies!!!
//
//            //user name
//            configuration.setProperty("hibernate.connection.username", "amaddi");
//
//            //password
//            configuration.setProperty("hibernate.connection.password", "uajLWiNAnOSWoGZN@$123");
//
//            //dialect
////            configuration.setProperty("dialect", "org.hibernate.dialect.MySQL8Dialect");//comment for build on fenjooon server
////            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLInnoDBDialect");//comment for build on fenjooon server
//
//            //schema auto update
//            configuration.setProperty("hibernate.hbm2ddl.auto", "update");
//
//            StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
//                    .applySettings(configuration.getProperties());
//
//            sessionFactory = configuration.buildSessionFactory(builder.build());
//
//        }
//        catch (Throwable exception)
//        {
//            exception.printStackTrace();
//            throw new ExceptionInInitializerError(exception);
//        }
//    }
//
//    public static SessionFactory getSessionFactory()
//    {
//        return sessionFactory;
//    }
    public static SessionFactory getSessionFactory()
    {
        if (sessionFactory == null)
        {
            try
            {
                // Create registry
                registry = new StandardServiceRegistryBuilder()
                        .configure()
                        .build();

                // Create MetadataSources
                MetadataSources sources = new MetadataSources(registry);

                // Create Metadata
                Metadata metadata = sources.getMetadataBuilder().build();

                // Create SessionFactory
                sessionFactory = metadata.getSessionFactoryBuilder().build();

            }
            catch (Exception e)
            {
                e.printStackTrace();
                if (registry != null)
                {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
            }
        }
        return sessionFactory;
    }

    public static void shutdown()
    {
        if (registry != null)
        {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
