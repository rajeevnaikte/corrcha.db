package com.corrcha.db.cassandra;

import com.datastax.driver.mapping.MappingManager;

/**
 * @author Rajeev Naik
 * @since 2017-06-07
 */
public interface DataStaxAdapter extends CassandraFacade
{
    /**
     * Provides DBFacade implementation to connect
     * to Cassandra DB which uses DataStax mapping API in Java
     * @return new object of implementation of {@link DataStaxAdapter}
     */
    public static CassandraFacade getAdapter()
    {
        return new DataStaxAdapterImpl();
    }

    /**
     * This method will return {@link MappingManager}
     * object used for the session
     * @return new object of {@link MappingManager}
     */
    public MappingManager getManager();
}
