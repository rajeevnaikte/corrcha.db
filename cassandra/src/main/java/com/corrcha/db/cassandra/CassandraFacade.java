package com.corrcha.db.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.corrcha.db.core.DBFacade;

/**
 * Main facade for users using cassandra DB
 * @author Rajeev Naik
 * @since 2017-06-07
 */
public interface CassandraFacade extends DBFacade
{
    /**
     * @return cluster object of connection
     */
    public Cluster getCluster();

    /**
     * @return session object of keySpace
     */
    public Session getSession();

    /**
     * @return session object of audit table keySpace if it is different from entity table keySpace
     */
    public Session getAuditSession();

    /**
     * Provides DBFacade implementation to connect
     * to Cassandra DB which uses DataStax mapping API in Java
     * @return new object of implementation of {@link DataStaxAdapter}
     */
    public static DBFacade getDataStaxAdapter()
    {
        return DataStaxAdapter.getAdapter();
    }
}
