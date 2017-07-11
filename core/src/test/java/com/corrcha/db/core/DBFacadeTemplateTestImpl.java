package com.corrcha.db.core;

import java.util.Map;

/**
 * Created by Rajeev Naik on 6/11/2017.
 */
public class DBFacadeTemplateTestImpl extends DBFacadeTemplate<String>
{
    public Map<Class<?>, ParsedEntity> getEntitiesMap()
    {
        return getEntityMap();
    }

    @Override public void connect(String host, String port, String dbSpace, String user, String password)
    {

    }

    @Override public void disconnect()
    {

    }

    @Override public <T> boolean isFitFor(Class<T> entityClass)
    {
        return true;
    }

    public boolean entityPresent = true;
    @Override public <T> T get(T entity)
    {
        return (entityPresent ? entity : null);
    }

    @Override protected String getEntityTableMetaData(Class entity)
    {
        return entity.getSimpleName();
    }

    @Override protected String getAuditTableMetaData(Class entity, ParsedEntity parsedEntity)
    {
        return entity.getSimpleName() + "_AUD";
    }

    @Override protected AnnotationParser getAnnotationParser()
    {
        return new AnnotationParserTestImpl();
    }

    public boolean deletedWithAudit = false;
    @Override protected void deleteWithAudit(Object entity, ParsedEntity parsedEntity)
    {
        this.deletedWithAudit = true;
    }

    public boolean deletedEntity = false;
    @Override protected void deleteEntity(Object entity, ParsedEntity parsedEntity)
    {
        this.deletedEntity = true;
    }

    public boolean updatedWithAudit = false;
    @Override protected void updateWithAudit(Object entity, ParsedEntity parsedEntity)
    {
        this.updatedWithAudit = true;
    }

    public boolean updatedEntity = false;
    @Override protected void updateEntity(Object entity, ParsedEntity parsedEntity)
    {
        this.updatedEntity = true;
    }

    public boolean savedWithAudit = false;
    @Override protected void saveWithAudit(Object entity, ParsedEntity parsedEntity)
    {
        this.savedWithAudit = true;
    }

    public boolean savedEntity = false;
    @Override protected void saveEntity(Object entity, ParsedEntity parsedEntity)
    {
        this.savedEntity = true;
    }
}
