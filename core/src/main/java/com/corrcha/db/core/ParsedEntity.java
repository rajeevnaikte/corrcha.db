package com.corrcha.db.core;

import com.corrcha.db.core.annotations.ConsistencyLevel;

import java.util.List;

/**
 * This will hold details required to do transactions
 * on a entity.
 * @author Rajeev Naik
 * @since 2017-06-07
 */
public class ParsedEntity<M>
{
    //private List<EntityProperty> entityPropertyList;

    private List<EntityProperty> auditPropertyList;

    private M entityTableMetaData;

    private M auditTableMetaData;

    private ConsistencyLevel consistencyLevel;

    public ParsedEntity()
    {
    }

    public List<EntityProperty> getAuditPropertyList()
    {
        return auditPropertyList;
    }

    public M getEntityTableMetaData()
    {
        return entityTableMetaData;
    }

    public M getAuditTableMetaData()
    {
        return auditTableMetaData;
    }

    public ConsistencyLevel getConsistencyLevel()
    {
        return consistencyLevel;
    }

    /*public void setEntityPropertyList(List<EntityProperty> entityPropertyList)
    {
        this.entityPropertyList = entityPropertyList;
    }*/

    public ParsedEntity<M> setAuditPropertyList(List<EntityProperty> auditPropertyList)
    {
        this.auditPropertyList = auditPropertyList;
        return this;
    }

    public ParsedEntity<M> setEntityTableMetaData(M entityTableMetaData)
    {
        this.entityTableMetaData = entityTableMetaData;
        return this;
    }

    public ParsedEntity<M> setAuditTableMetaData(M auditTableMetaData)
    {
        this.auditTableMetaData = auditTableMetaData;
        return this;
    }

    public ParsedEntity<M> setConsistencyLevel(ConsistencyLevel consistencyLevel)
    {
        this.consistencyLevel = consistencyLevel;
        return this;
    }
}
