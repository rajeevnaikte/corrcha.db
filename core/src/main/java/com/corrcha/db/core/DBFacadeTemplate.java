package com.corrcha.db.core;

import com.corrcha.db.core.annotations.ConsistencyLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Abstract class to be implemented by DB specific
 * adapters. It takes generic type M which will be
 * table metadata object of DB specific adapter.
 * @author Rajeev Naik
 * @since 2017-06-07
 * @param <M> table metadata type
 */
public abstract class DBFacadeTemplate<M> implements DBFacade
{
    private static Map<Class<?>, ParsedEntity> ENTITY_MAP = new HashMap<>();

    private static final String REV_TYPE = "REV_TYPE";
    private static final String REV_TIME = "REV_TIME";

    /**
     * This method will return already parsed entities
     * @return map of parsed entities
     */
    protected static Map<Class<?>, ParsedEntity> getEntityMap()
    {
        return ENTITY_MAP;
    }

    /**
     *
     * @return "REV_TYPE"
     */
    protected static String getRevTypeColumnName()
    {
        return REV_TYPE;
    }

    /**
     *
     * @return "REV_TIME"
     */
    protected static String getRevTimeColumnName()
    {
        return REV_TIME;
    }

    @Override public void parseEntity(Class<?>... entityClasses)
    {
        for (Class<?> entity : entityClasses)
        {
            if (getEntityMap().get(entity) != null)
                continue;

            ParsedEntity<M> parsedEntity = new ParsedEntity<>();

            parsedEntity.setEntityTableMetaData(getEntityTableMetaData(entity));

            if (AnnotationParser.isAudited(entity))
            {
                parsedEntity.setAuditPropertyList(getAnnotationParser().getAuditProperties(entity))
                        .setAuditTableMetaData(getAuditTableMetaData(entity, parsedEntity))
                        .setConsistencyLevel(AnnotationParser.getConsistencyLevel(entity));
            }

            getEntityMap().put(entity, parsedEntity);
        }
    }

    /**
     * This method will return DB specific table metadata
     * object for entity table.
     * @param entityClass class definition of entity
     * @param <T> type of entity
     * @return entity table metadata object
     */
    protected abstract <T> M getEntityTableMetaData(Class<T> entityClass);

    /**
     * This method will return DB specific table metadata
     * object for audit table.
     * @param entityClass class definition of entity
     * @param parsedEntity parsedEntity
     * @param <T> type of entity
     * @return audit table metadata object
     */
    protected abstract <T> M getAuditTableMetaData(Class<T> entityClass, ParsedEntity<M> parsedEntity);

    /**
     * This method will to return
     * DB adapter specific parser
     * @return annotation parser object
     */
    protected abstract AnnotationParser getAnnotationParser();

    private <T> void transactionStrategy(T entity, Consumer<ParsedEntity<M>> tx, Consumer<ParsedEntity<M>> txWithAudit)
    {
        ParsedEntity<M> parsedEntity = getEntityMap().get(entity.getClass());

        if (parsedEntity == null || parsedEntity.getAuditPropertyList() == null)
        {
            tx.accept(parsedEntity);
        }
        else
        {
            txWithAudit.accept(parsedEntity);
        }
    }

    /**
     * It will execute lambda single if
     * {@code ConsistencyLevel} is SINGLE
     * else will execute lambda both
     * @param parsedEntity parsedEntity
     * @param single lambda expressing
     * @param both lambda expressing
     * @return result of executed lambda
     */
    protected Object executionStrategy(ParsedEntity<M> parsedEntity, Supplier<Object> single, Supplier<Object> both)
    {
        if (ConsistencyLevel.SINGLE.equals(parsedEntity.getConsistencyLevel()))
            return single.get();
        return both.get();
    }

    @Override public <T> void save(T entity)
    {
        transactionStrategy(entity, (parsedEntity -> saveEntity(entity, parsedEntity)),
                (parsedEntity -> saveWithAudit(entity, parsedEntity)));
    }

    /**
     * This method will save entity in entity table.
     * @param entity entity object
     * @param parsedEntity parsedEntity of entity class
     * @param <T> type of entity
     */
    protected abstract <T> void saveEntity(T entity, ParsedEntity<M> parsedEntity);

    /**
     * This method will save entity in entity table
     * and insert audit entry into audit table with REV_TYPE=0.
     * @param entity entity object
     * @param parsedEntity parsedEntity of entity class
     * @param <T> type of entity
     */
    protected abstract <T> void saveWithAudit(T entity, ParsedEntity<M> parsedEntity);

    @Override public <T> void update(T entity)
    {
        transactionStrategy(entity, (parsedEntity -> updateEntity(entity, parsedEntity)),
                (parsedEntity -> updateWithAudit(entity, parsedEntity)));
    }

    /**
     * This method will update entity in entity table.
     * @param entity entity object
     * @param parsedEntity parsedEntity of entity class
     * @param <T> type of entity
     */
    protected abstract <T> void updateEntity(T entity, ParsedEntity<M> parsedEntity);

    /**
     * This method will update entity in entity table
     * and insert audit entry into audit table with REV_TYPE=1.
     * @param entity entity object
     * @param parsedEntity parsedEntity of entity class
     * @param <T> type of entity
     */
    protected abstract <T> void updateWithAudit(T entity, ParsedEntity<M> parsedEntity);

    @Override public <T> void saveOrUpdate(T entity)
    {
        if (get(entity) == null)
            save(entity);
        else
            update(entity);
    }

    @Override public <T> void delete(T entity)
    {
        transactionStrategy(entity, (parsedEntity -> deleteEntity(entity, parsedEntity)),
                (parsedEntity -> deleteWithAudit(entity, parsedEntity)));
    }

    /**
     * This method will delete entity in entity table.
     * @param entity entity object
     * @param parsedEntity parsedEntity of entity class
     * @param <T> type of entity
     */
    protected abstract <T> void deleteEntity(T entity, ParsedEntity<M> parsedEntity);

    /**
     * This method will delete entity in entity table
     * and insert audit entry into audit table with REV_TYPE=2.
     * @param entity entity object
     * @param parsedEntity parsedEntity of entity class
     * @param <T> type of entity
     */
    protected abstract <T> void deleteWithAudit(T entity, ParsedEntity<M> parsedEntity);

    /**
     * @param entity object
     * @param <T> type of object
     * @return class definition of object
     */
    protected <T> Class<T> getClass(T entity)
    {
        return (Class<T>) (entity.getClass());
    }

    /**
     * @see AnnotationParser#getAuditTableName(Class, String)
     */
    protected <T> String getAuditTableName(Class<T> entityClass, String entityTableName)
    {
        return AnnotationParser.getAuditTableName(entityClass, entityTableName);
    }

    /**
     * @see AnnotationParser#getAuditTableDBSpace(Class, String)
     */
    protected <T> String getAuditTableDBSpace(Class<T> entityClass, String entityTableDBSpace)
    {
        return AnnotationParser.getAuditTableDBSpace(entityClass, entityTableDBSpace);
    }

    /**
     * {@link #disconnect()} method will be called here.
     * @throws Throwable throwable
     */
    @Override protected void finalize() throws Throwable
    {
        disconnect();
        super.finalize();
    }
}
