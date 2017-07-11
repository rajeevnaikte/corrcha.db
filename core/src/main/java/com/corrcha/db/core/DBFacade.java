package com.corrcha.db.core;

/**
 * Main facade for users
 * @author Rajeev Naik
 * @since 2017-06-07
 */
public interface DBFacade
{
    /**
     * This will make connection to the DB of
     * specified connection details
     * @param host host address of DB
     * @param port port of DB
     * @param dbSpace name/space/schema of DB
     */
    public void connect(String host, String port, String dbSpace, String user, String password);

    /**
     * Provided list of classes, this method will recognize annotations
     * and prepare for CRUD operations.
     * @param entityClasses list of classes
     */
    public void parseEntity(Class<?>... entityClasses);

    /**
     * Inserts entity into DB table.
     * @param entity entity object
     * @param <T> entity type
     */
    public <T> void save(T entity);

    /**
     * Updates entity into DB table.
     * @param entity entity object
     * @param <T> entity type
     */
    public <T> void update(T entity);

    /**
     * Inserts entity if not present else updates into DB table.
     * @param entity entity object
     * @param <T> entity type
     */
    public <T> void saveOrUpdate(T entity);

    /**
     * deletes entity into DB table.
     * @param entity entity object
     * @param <T> entity type
     */
    public <T> void delete(T entity);

    /**
     * Retrieves entity from DB table with primaryKeys from entity.
     * @param entity entity object
     * @param <T> entity type
     * @return entity object if present in DB
     */
    public <T> T get(T entity);

    /**
     * Disconnect from DB connection which is
     * established in {@link #connect(String, String, String, String, String)}
     */
    public void disconnect();

    /**
     * Indicates whether the facade object can parse
     * and do CRUD for the given entityClass.
     * @param entityClass class definition of entity
     * @param <T> entity type
     * @return is the adapter fit for the entityClass
     */
    public <T> boolean isFitFor(Class<T> entityClass);
}
