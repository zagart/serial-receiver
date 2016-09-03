package by.grodno.zagart.studies.serial_receiver.utils;


import by.grodno.zagart.studies.serial_receiver.database.entities.Module;
import by.grodno.zagart.studies.serial_receiver.database.entities.Stand;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

/**
 * Утилитный класс Hibernate.
 */
public class HibernateUtil {

    public static final int BATCH_SIZE = 20;

    private static final SessionFactory factory;
    private static Session currentSession;
    private static Transaction currentTransaction;

    private static int batch = 0;

    private HibernateUtil() {}

    static {
        ServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                .configure()
                .build();
        Metadata metadata = new MetadataSources(standardRegistry)
                .addAnnotatedClass(Stand.class)
                .addAnnotatedClass(Module.class)
                .buildMetadata();
        factory = metadata.buildSessionFactory();
    }

    public static Session getCurrentSession() {
        return currentSession;
    }

    public static Transaction getCurrentTransaction() {
        return currentTransaction;
    }

    public static Session openCurrentSession() {
        currentSession = factory.openSession();
        return currentSession;
    }

    public static Transaction openCurrentSessionWithTransaction() {
        currentSession = factory.openSession();
        currentTransaction = currentSession.beginTransaction();
        return currentTransaction;
    }

    public static void closeCurrentSession() {
        currentSession.close();
    }

    public static void closeCurrentSessionWithTransaction() {
        if (++batch % BATCH_SIZE == 0) {
            currentSession.flush();
            currentSession.clear();
            batch = 1;
        }
        currentTransaction.commit();
        currentSession.close();
    }

    public static SessionFactory getSessionFactory() {
        return factory;
    }

    public static void closeFactory() { factory.close(); }

}
