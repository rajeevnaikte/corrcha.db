package com.corrcha.db.core;

import java.lang.reflect.InvocationTargetException;

/**
 * This factory class will provide required
 * DB Facade implementation.
 * @author Rajeev Naik
 * @since 2017-06-07
 */
public class DBFactory
{
    /**
     * Provides DBFacade implementation to connect
     * to Cassandra DB which uses DataStax mapping API in Java
     * @return DataStaxAdapter
     */
    public static DBFacade getDataStaxAdapter()
    {
        try
        {
            return (DBFacade) Class.forName("com.corrcha.db.cassandra.DataStaxAdapter").getMethod("getAdapter").invoke(null);
        }
        catch (IllegalAccessException|ClassNotFoundException|NoSuchMethodException|InvocationTargetException e)
        {
        }
        return null;
    }

    /**
     * This method will find appropriate DBFacade implementation
     * by parsing one of the provided entityClass and it will
     * do {@link DBFacade#connect(String, String, String, String, String)} and
     * for all the given entityClasses it will do
     * {@link DBFacade#parseEntity(Class[])}
     * @param host host address of DB
     * @param port port of DB
     * @param dbSpace DB name/space/schema
     * @param entityClasses entity classes
     * @return DBFacade object
     */
    public static DBFacade get(String host, String port, String dbSpace, Class<?>... entityClasses)
    {
        if (entityClasses == null || entityClasses.length == 0)
            throw new RuntimeException("Entity is needed to find DB adapter");
        Class<?> entityClass = entityClasses[0];
        DBFacade dbFacade = DBFactory.getDataStaxAdapter();
        if (dbFacade != null && dbFacade.isFitFor(entityClass))
        {
            dbFacade.connect(host, port, dbSpace, null, null);
            dbFacade.parseEntity(entityClasses);
            return dbFacade;
        }
        return null;
    }
}
