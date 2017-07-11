package com.corrcha.db.cassandra;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.schemabuilder.Create;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.annotations.Table;
import com.corrcha.db.core.DBFacadeTemplate;
import com.corrcha.db.core.EntityProperty;
import com.corrcha.db.core.ParsedEntity;
import com.corrcha.db.core.RevisionType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.datastax.driver.core.querybuilder.QueryBuilder.bindMarker;

/**
 * @author Rajeev Naik
 * @since 2017-06-07
 */
class DataStaxAdapterImpl extends DBFacadeTemplate<TableMetadata> implements DataStaxAdapter
{
    private Cluster cluster;
    private Session session;
    private Session auditSession;
    private MappingManager manager;

    @Override public void connect(String host, String port, String dbSpace, String user, String password)
    {
        Cluster.Builder builder = Cluster.builder().addContactPoint(host);
        if (user != null && password != null)
            builder.withCredentials(user, password);
        cluster = builder.build();

        session = cluster.connect(dbSpace);

        manager = new MappingManager(session);
    }

    @Override public void disconnect()
    {
        session.close();
        cluster.close();
    }

    @Override public Cluster getCluster()
    {
        return cluster;
    }

    @Override public Session getSession()
    {
        return session;
    }

    @Override public Session getAuditSession()
    {
        if (auditSession == null)
            return session;
        return auditSession;
    }

    @Override protected DataStaxAnnotationParser getAnnotationParser()
    {
        return DataStaxAnnotationParser.INSTANCE;
    }

    @Override public <T> boolean isFitFor(Class<T> entityClass)
    {
        return entityClass.isAnnotationPresent(Table.class);
    }

    public <T> Mapper<T> getMapper(T entity)
    {
        return manager.mapper(getClass(entity));
    }

    @Override protected <T> void saveEntity(T entity, ParsedEntity<TableMetadata> parsedEntity)
    {
        getMapper(entity).save(entity);
    }

    @Override protected <T> void saveWithAudit(T entity, ParsedEntity<TableMetadata> parsedEntity)
    {
        BoundStatement statement = (BoundStatement) getMapper(entity).saveQuery(entity);

        BoundStatement auditStatement = getInsertAuditEntryStatement(parsedEntity, entity, RevisionType.INSERT);

        executeStatements(parsedEntity, statement, auditStatement);
    }

    @Override protected <T> void updateEntity(T entity, ParsedEntity<TableMetadata> parsedEntity)
    {
        saveEntity(entity, parsedEntity);
    }

    @Override protected <T> void updateWithAudit(T entity, ParsedEntity<TableMetadata> parsedEntity)
    {
        BoundStatement statement = (BoundStatement) getMapper(entity).saveQuery(entity);

        BoundStatement auditStatement = getInsertAuditEntryStatement(parsedEntity, entity, RevisionType.UPDATE);

        executeStatements(parsedEntity, statement, auditStatement);
    }

    @Override protected <T> void deleteEntity(T entity, ParsedEntity<TableMetadata> parsedEntity)
    {
        getMapper(entity).delete(entity);
    }

    @Override protected <T> void deleteWithAudit(T entity, ParsedEntity<TableMetadata> parsedEntity)
    {
        entity = get(entity);

        if (entity == null)
            return;

        BoundStatement statement = (BoundStatement) getMapper(entity).deleteQuery(entity);

        BoundStatement auditStatement = getInsertAuditEntryStatement(parsedEntity, entity, RevisionType.DELETE);

        executeStatements(parsedEntity, statement, auditStatement);
    }

    @Override public <T> T get(T entity)
    {
        ParsedEntity<TableMetadata> parsedEntity = getEntityMap().get(entity.getClass());
        return getMapper(entity).get(parsedEntity.getEntityTableMetaData().getPrimaryKey());
    }

    protected Object executeStatements(ParsedEntity<TableMetadata> parsedEntity, BoundStatement statement, BoundStatement auditStatement)
    {
        return super.executionStrategy(parsedEntity,
                () ->
                {
                    execute(statement);
                    return executeAudit(auditStatement);
                },
                () -> executeInBatch(statement, auditStatement));
    }

    private ResultSet execute(BoundStatement statement)
    {
        return getSession().execute(statement);
    }

    private ResultSet executeAudit(BoundStatement statement)
    {
        return getAuditSession().execute(statement);
    }

    private ResultSet executeInBatch(BoundStatement entityStatement, BoundStatement auditStatement)
    {
        if (auditSession == null)
        {
            BatchStatement batchStatement = new BatchStatement()
                    .add(entityStatement)
                    .add(auditStatement);
            return getSession().execute(batchStatement);
        }
        execute(entityStatement);
        return executeAudit(auditStatement);
    }

    private void createAuditTable(String tableName, String keySpace, TableMetadata entityTableMetatdata)
    {
        final Create create = SchemaBuilder.createTable(keySpace, tableName);

        entityTableMetatdata.getPartitionKey()
                .forEach(col -> create.addPartitionKey(col.getName(), col.getType()));

        create.addClusteringColumn(getRevTimeColumnName(), DataType.timeuuid())
                .addColumn(getRevTypeColumnName(), DataType.cint());

        IntStream.range(entityTableMetatdata.getPartitionKey().size(), entityTableMetatdata.getColumns().size())
                .forEach(i ->
                {
                    ColumnMetadata col = entityTableMetatdata.getColumns().get(i);
                    create.addColumn(Metadata.quote(col.getName()), createUserTypeIfNotExists(col.getType()));
                });

        getAuditSession().execute(create.buildInternal());
    }

    private DataType createUserTypeIfNotExists(DataType dataType)
    {
        if (dataType instanceof UserType)
        {
            UserType userType = (UserType) dataType;
            UserType requiredUserType = getUserType(userType.getTypeName());
            if (requiredUserType == null)
            {
                String createUserType = userType.asCQLQuery()
                        .replaceFirst(userType.getKeyspace(), getAuditSession().getLoggedKeyspace());
                getAuditSession().execute(createUserType);
                requiredUserType = getUserType(userType.getTypeName());
            }
            return requiredUserType;
        }
        else if (dataType.isCollection())
        {
            List<DataType> dataTypes = dataType.getTypeArguments().stream()
                    .map(type -> createUserTypeIfNotExists(type))
                    .collect(Collectors.toList());
            switch (dataType.getName())
            {
                case MAP:
                    return DataType.map(dataTypes.get(0), dataTypes.get(1), dataType.isFrozen());
                case LIST:
                    return DataType.list(dataTypes.get(0), dataType.isFrozen());
                case SET:
                    return DataType.set(dataTypes.get(0), dataType.isFrozen());
            }
        }
        return dataType;
    }

    private UserType getUserType(String typeName)
    {
        return cluster.getMetadata().getKeyspace(getAuditSession().getLoggedKeyspace()).getUserType(typeName);
    }

    private <T> BoundStatement getInsertAuditEntryStatement(ParsedEntity<TableMetadata> parsedEntity, T entity, RevisionType revisionType)
    {
        Insert insertAudit = QueryBuilder.insertInto(parsedEntity.getAuditTableMetaData());

        insertAudit.value(getRevTimeColumnName(), bindMarker());

        insertAudit.value(getRevTypeColumnName(), bindMarker());

        parsedEntity.getAuditPropertyList().stream()
                .filter(col -> !((DataStaxEntityProperty) col).isComputed())
                .forEach(col -> insertAudit.value(col.getColumnName(), bindMarker()));

        SimpleStatement s = new SimpleStatement(insertAudit.toString());

        s.setIdempotent(true);

        PreparedStatement ps = getSession().prepare(s);

        if (ps == null)
            throw new RuntimeException("Unable to prepare statement: " + insertAudit.toString());

        return getBoundedStatement(ps, parsedEntity, entity, revisionType);
    }

    private <T> BoundStatement getBoundedStatement(PreparedStatement ps, ParsedEntity<TableMetadata> parsedEntity, T entity, RevisionType revisionType)
    {
        BoundStatement bs = ps.bind();

        int i = 0;

        bs.set(i++, UUIDs.timeBased(), TypeCodec.timeUUID());

        bs.set(i++, revisionType.ordinal(), TypeCodec.cint());

        for (EntityProperty col : parsedEntity.getAuditPropertyList())
        {
            DataStaxEntityProperty cep = (DataStaxEntityProperty) col;

            if (cep.isComputed())
                continue;

            if (cep.getCustomCodec() != null)
                bs.set(i++, cep.getValue(entity), cep.getCustomCodec());
            else
                bs.set(i++, cep.getValue(entity), cep.getJavaType());
        }

        return bs;
    }

    @Override protected <T> TableMetadata getEntityTableMetaData(Class<T> entity)
    {
        manager.mapper(entity);

        String keySpace = null, tableName = null;

        Table table = entity.getAnnotation(Table.class);

        if (table != null)
        {
            tableName = table.name() == null ? entity.getName() : table.name();
            keySpace = table.keyspace() == null ? getSession().getLoggedKeyspace() : table.keyspace();
        }

        KeyspaceMetadata keyspaceMetadata = getSession().getCluster().getMetadata().getKeyspace(keySpace);

        TableMetadata tableMetadata = keyspaceMetadata.getTable("\"" + tableName + "\"");

        if (tableMetadata == null)
        {
            tableMetadata = keyspaceMetadata.getMaterializedView("\"" + tableName + "\"").getBaseTable();
        }

        return tableMetadata;
    }

    @Override protected <T> TableMetadata getAuditTableMetaData(Class<T> entity, ParsedEntity<TableMetadata> parsedEntity)
    {
        TableMetadata entityTableMetaData = parsedEntity.getEntityTableMetaData();

        String auditTableName = getAuditTableName(entity, entityTableMetaData.getName());

        String auditKeySpace = getAuditTableDBSpace(entity, entityTableMetaData.getKeyspace().getName());

        if (!auditKeySpace.equals(entityTableMetaData.getKeyspace().getName()))
            auditSession = cluster.connect(auditKeySpace);

        TableMetadata auditTableMetadata = this.getAuditSession()
                .getCluster().getMetadata().getKeyspace(auditKeySpace).getTable(auditTableName);

        if (auditTableMetadata == null)
        {
            createAuditTable(auditTableName, auditKeySpace, entityTableMetaData);

            auditTableMetadata = this.getAuditSession()
                    .getCluster().getMetadata().getKeyspace(auditKeySpace).getTable(auditTableName);
        }

        parsedEntity.getAuditPropertyList().forEach(property ->
                getAnnotationParser().parseAndRegisterUDTCodec(property.getJavaType().getType(),
                        this.getAuditSession().getCluster().getMetadata().getKeyspace(auditKeySpace)));

        return auditTableMetadata;
    }

    @Override public MappingManager getManager()
    {
        return this.manager;
    }
}
