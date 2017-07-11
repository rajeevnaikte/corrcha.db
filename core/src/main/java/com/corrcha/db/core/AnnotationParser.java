package com.corrcha.db.core;

import com.corrcha.db.core.annotations.ConsistencyLevel;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.corrcha.db.core.annotations.Audited;
import com.corrcha.db.core.annotations.NotAudited;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * Utility to parse annotations of entity.
 * @author Rajeev Naik
 * @since 2017-06-07
 */
public abstract class AnnotationParser
{
    private static final String AUDIT_EXTN = "_AUD";
    private static final Set<String> EXCLUDED_PROPERTIES = ImmutableSet.of(
            "class",
            "metaClass"
    );

    /**
     * This mehtod will parse the entity for annotation and
     * return list of data elements to be audited.
     * @param entityClass class definition of entity
     * @param <T> type of entity
     * @return list of {@link EntityProperty} to be audited
     */
    public <T> List<EntityProperty> getAuditProperties(Class<T> entityClass)
    {
        Map<String, Field> fields = scanFields(entityClass);
        Map<String, PropertyDescriptor> properties = scanProperties(entityClass);
        List<EntityProperty> entityProperties = new ArrayList<>(fields.size());
        for (Map.Entry<String, Field> entry : fields.entrySet())
        {
            PropertyDescriptor property = properties.remove(entry.getKey());
            if (getAnnotation(entry.getValue(), property, NotAudited.class) != null)
                continue;
            entityProperties.add(getEntityProperty(entry.getKey(), entry.getValue(), property));
        }
        for (Map.Entry<String, PropertyDescriptor> entry : properties.entrySet())
        {
            if (getAnnotation(null, entry.getValue(), NotAudited.class) != null)
                continue;
            entityProperties.add(getEntityProperty(entry.getKey(), null, entry.getValue()));
        }
        return entityProperties;
    }

    protected static <T> Map<String, Field> scanFields(Class<T> entityClass)
    {
        HashMap<String, Field> fields = new HashMap<String, Field>();
        Class<?> clasz = entityClass;
        for (; !clasz.equals(Object.class); clasz = clasz.getSuperclass())
        {
            for (Field field : clasz.getDeclaredFields())
            {
                if (field.isSynthetic() || Modifier.isTransient(field.getModifiers())
                        || Modifier.isStatic(field.getModifiers())
                        || EXCLUDED_PROPERTIES.contains(field.getName()))
                    continue;
                if (!fields.containsKey(field.getName()))
                {
                    tryMakeAccessible(field);
                    fields.put(field.getName(), field);
                }
            }
        }
        return fields;
    }

    protected static <T> Map<String, PropertyDescriptor> scanProperties(Class<T> entityClass)
    {
        BeanInfo beanInfo;
        try
        {
            beanInfo = Introspector.getBeanInfo(entityClass);
        }
        catch (IntrospectionException e)
        {
            throw Throwables.propagate(e);
        }
        Map<String, PropertyDescriptor> properties = new HashMap<String, PropertyDescriptor>();
        for (PropertyDescriptor property : beanInfo.getPropertyDescriptors())
        {
            if (EXCLUDED_PROPERTIES.contains(property.getName()))
                continue;
            tryMakeAccessible(property.getReadMethod());
            tryMakeAccessible(property.getWriteMethod());
            properties.put(property.getName(), property);
        }
        return properties;
    }

    protected static void tryMakeAccessible(AccessibleObject object)
    {
        if (object == null)
            return;
        if (!object.isAccessible())
        {
            try
            {
                object.setAccessible(true);
            }
            catch (SecurityException e)
            {

            }
        }
    }

    /**
     * This method returns new object of class {@link EntityProperty}
     * @param name name of data element
     * @param field filed object of entity data element
     * @param property getter/setter of entity data element
     * @return new object of {@link EntityProperty}
     */
    protected EntityProperty getEntityProperty(String name, Field field, PropertyDescriptor property)
    {
        return new EntityProperty(name, field, property, getColumnName(field, property));
    }

    /**
     * This method will return the appropriate column name
     * in DB for the data element.
     * @param field filed object of entity data element
     * @param property getter/setter of entity data element
     * @return column name for the data element
     */
    protected abstract String getColumnName(Field field, PropertyDescriptor property);

    /**
     * This method will look for given annotation presence
     * in getter of data element. If not exists then it will look
     * for the annotation in field object of the data element.
     * @param field filed object of entity data element
     * @param property getter of entity data element
     * @param annotation annotation class
     * @param <A> type of annotation
     * @return annotation object or null
     */
    protected <A extends Annotation> A getAnnotation(Field field, PropertyDescriptor property, Class<A> annotation)
    {
        A ann = null;
        if (property != null && property.getReadMethod() != null)
            ann = property.getReadMethod().getAnnotation(annotation);
        if (ann == null && field != null)
            ann = field.getAnnotation(annotation);
        return ann;
    }

    /**
     * This method will return a new instance of type given class.
     * @param clazz class definition
     * @param <T> type of instance needed
     * @return instance of given class
     */
    public static <T> T getInstance(Class<T> clazz)
    {
        if (clazz == null)
            return null;
        Constructor<T> publicConstructor;
        try {
            publicConstructor = clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            try {
                Constructor<T> privateConstructor = clazz.getDeclaredConstructor();
                privateConstructor.setAccessible(true);
                return privateConstructor.newInstance();
            } catch (Exception e1) {
                throw new IllegalArgumentException("Can't create an instance of " + clazz, e);
            }
        }
        try {
            return publicConstructor.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't create an instance of " + clazz, e);
        }
    }

    /**
     * This will look for table name from {@link Audited#table()}
     * if not exists then it will return the param entityTableName+"_AUD"
     * @param entityClass class definition of entity
     * @param entityTableName entity table name
     * @param <T> type of entity
     * @return audit table name
     */
    protected static <T> String getAuditTableName(Class<T> entityClass, String entityTableName)
    {
        Audited audited = entityClass.getAnnotation(Audited.class);
        if (audited == null)
            return null;
        return ("".equals(audited.table()) ? entityTableName + AUDIT_EXTN : audited.table());
    }

    /**
     * This will look for dbSpace name from {@link Audited#dbSpace()}
     * if not exists then it will return the param entityTableDBSpace
     * @param entityClass class definition of entity
     * @param entityTableDBSpace entity DBSpace name
     * @param <T> type of entity
     * @return DBSpace for audit table
     */
    protected static <T> String getAuditTableDBSpace(Class<T> entityClass, String entityTableDBSpace)
    {
        Audited audited = entityClass.getAnnotation(Audited.class);
        if (audited == null)
            return null;
        return ("".equals(audited.dbSpace()) ? entityTableDBSpace : audited.dbSpace());
    }

    /**
     * This method will indicate whether the given class
     * has {@link Audited} annotation
     * @param entityClass class definition of entity
     * @param <T> type of entity
     * @return true or false
     */
    protected static <T> boolean isAudited(Class<T> entityClass)
    {
        return (entityClass.getAnnotation(Audited.class) != null);
    }

    /**
     * Returns audit {@link ConsistencyLevel} for the entity class
     * @param entityClass class definition of entity
     * @param <T> type of entity
     * @return {@link ConsistencyLevel}
     */
    protected static <T> ConsistencyLevel getConsistencyLevel(Class<T> entityClass)
    {
        return entityClass.getAnnotation(Audited.class).consistencyLevel();
    }
}
