package com.corrcha.db.cassandra;

import com.datastax.driver.core.*;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Computed;
import com.datastax.driver.mapping.annotations.Defaults;
import com.datastax.driver.mapping.annotations.UDT;
import com.corrcha.db.core.AnnotationParser;
import com.corrcha.db.core.EntityProperty;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rajeev Naik
 * @since 2017-06-07
 */
class DataStaxAnnotationParser extends AnnotationParser
{
    public static final DataStaxAnnotationParser INSTANCE = new DataStaxAnnotationParser();

    private DataStaxAnnotationParser()
    {

    }

    @Override protected EntityProperty getEntityProperty(String name, Field field, PropertyDescriptor property)
    {
        return new DataStaxEntityProperty(name, field, property,
                getColumnName(field, property), getCustomCodec(field, property),
                getAnnotation(field, property, Computed.class) != null);
    }

    @Override protected String getColumnName(Field field, PropertyDescriptor property)
    {
        Annotation annotation = getAnnotation(field, property, Computed.class);
        if (annotation != null)
        {
            return ((Computed) annotation).value();
        }
        boolean caseSensitive = false;
        String columnName = (field == null ? property.getName() : field.getName());
        annotation = getAnnotation(field, property, Column.class);
        if (annotation != null)
        {
            Column column = (Column) annotation;
            caseSensitive = column.caseSensitive();
            if (!column.name().isEmpty())
                columnName = column.name();
        }
        else if ((annotation = getAnnotation(field, property, com.datastax.driver.mapping.annotations.Field.class)) != null)
        {
            com.datastax.driver.mapping.annotations.Field udtField = (com.datastax.driver.mapping.annotations.Field) annotation;
            caseSensitive = udtField.caseSensitive();
            if (!udtField.name().isEmpty())
                columnName = udtField.name();
        }
        return caseSensitive ? Metadata.quote(columnName) : columnName.toLowerCase();
    }

    private TypeCodec<Object> getCustomCodec(Field field, PropertyDescriptor property)
    {
        Class<? extends TypeCodec<?>> codecClass = null;
        Annotation annotation;
        if ((annotation = getAnnotation(field, property, Column.class)) != null)
            codecClass = (((Column) annotation).codec().equals(Defaults.NoCodec.class) ? null : ((Column) annotation).codec());
        else if ((annotation = getAnnotation(field, property, com.datastax.driver.mapping.annotations.Field.class)) != null)
            codecClass = (((com.datastax.driver.mapping.annotations.Field) annotation).codec().equals(Defaults.NoCodec.class) ? null : ((com.datastax.driver.mapping.annotations.Field) annotation).codec());
        return (TypeCodec<Object>) getInstance(codecClass);
    }

    void parseAndRegisterUDTCodec(Type type, KeyspaceMetadata keyspaceMetadata)
    {
        if (type instanceof ParameterizedType)
        {
            if (((ParameterizedType) type).getRawType() instanceof Class)
            {
                Class<?> raw = (Class<?>) ((ParameterizedType) type).getRawType();
                if (List.class.isAssignableFrom(raw))
                    parseAndRegisterUDTCodec(((ParameterizedType) type).getActualTypeArguments()[0], keyspaceMetadata);
                else if (Set.class.isAssignableFrom(raw))
                    parseAndRegisterUDTCodec(((ParameterizedType) type).getActualTypeArguments()[0], keyspaceMetadata);
                else if (Map.class.isAssignableFrom(raw))
                {
                    parseAndRegisterUDTCodec(((ParameterizedType) type).getActualTypeArguments()[0], keyspaceMetadata);
                    parseAndRegisterUDTCodec(((ParameterizedType) type).getActualTypeArguments()[1], keyspaceMetadata);
                }
            }
        }
        else if (type instanceof Class && ((Class) type).isAnnotationPresent(UDT.class))
        {
            Class<?> udtClass = (Class) type;
            Map<String, Field> fields = scanFields(udtClass);
            Map<String, PropertyDescriptor> properties = scanProperties(udtClass);
            List<DataStaxEntityProperty> entityProperties = new ArrayList<>(fields.size());
            for (Map.Entry<String, Field> entry : fields.entrySet())
            {
                PropertyDescriptor property = properties.remove(entry.getKey());
                entityProperties.add((DataStaxEntityProperty) getEntityProperty(entry.getKey(), entry.getValue(), property));
            }
            for (Map.Entry<String, PropertyDescriptor> entry : properties.entrySet())
            {
                entityProperties.add((DataStaxEntityProperty) getEntityProperty(entry.getKey(), null, entry.getValue()));
            }
            UDT udt = udtClass.getAnnotation(UDT.class);
            String userTypeName = (udt.caseSensitiveType() ? Metadata.quote(udt.name()) : udt.name());
            DataStaxUDTCodec<?> udtCodec = new DataStaxUDTCodec<>(keyspaceMetadata.getUserType(userTypeName),
                    udtClass,
                    entityProperties.stream().collect(Collectors.toMap(prop -> prop.getColumnName(), prop -> prop)));
            CodecRegistry.DEFAULT_INSTANCE.register(udtCodec);

            entityProperties.forEach(property -> parseAndRegisterUDTCodec(property.getJavaType().getType(), keyspaceMetadata));
        }
    }
}
