package com.corrcha.db.core;

import com.google.common.reflect.TypeToken;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * This will hold necessary data of a data element in
 * entity to be operated.
 * @author Rajeev Naik
 * @since 2017-06-07
 */
public class EntityProperty
{
    private final String propertyName;
    private final Field field;
    private final Method getter;
    private final Method setter;
    private final TypeToken<Object> javaType;
    private final String columnName;

    public EntityProperty(String name, Field field, PropertyDescriptor property, String columnName)
    {
        this.propertyName = name;
        this.field = field;
        this.getter = property == null ? null : property.getReadMethod();
        this.setter = property == null ? null : property.getWriteMethod();
        this.javaType = inferJavaType();
        this.columnName = columnName;
    }

    public TypeToken<Object> getJavaType()
    {
        return this.javaType;
    }

    private TypeToken<Object> inferJavaType()
    {
        return (TypeToken<Object>) (this.getter == null ? TypeToken.of(field.getGenericType()) : TypeToken.of(getter.getGenericReturnType()));
    }

    public Object getValue(Object entity)
    {
        try
        {
            if (getter != null && getter.isAccessible())
                return getter.invoke(entity);
            else
                return field.get(entity);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Unable to read property '" + propertyName + "' of " + entity.getClass(), e);
        }
    }

    public void setValue(Object entity, Object value)
    {
        try
        {
            if (setter != null && setter.isAccessible())
                setter.invoke(entity, value);
            else
                field.set(entity, value);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Unable to set '" + propertyName + "' of " + entity.getClass(), e);
        }
    }

    public String getColumnName()
    {
        return columnName;
    }

    public String getPropertyName()
    {
        return this.propertyName;
    }
}
