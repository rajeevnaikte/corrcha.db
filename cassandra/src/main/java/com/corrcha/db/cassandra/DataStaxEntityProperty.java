package com.corrcha.db.cassandra;

import com.datastax.driver.core.TypeCodec;
import com.corrcha.db.core.EntityProperty;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * @author Rajeev Naik
 * @since 2017-06-07
 */
class DataStaxEntityProperty extends EntityProperty
{
    private final TypeCodec<Object> customCodec;
    private final boolean isComputed;

    public DataStaxEntityProperty(String name, Field field, PropertyDescriptor property, String coulmnName, TypeCodec<Object> customCodec, boolean isComputed)
    {
        super(name, field, property, coulmnName);
        this.customCodec = customCodec;
        this.isComputed = isComputed;
    }

    public TypeCodec<Object> getCustomCodec()
    {
        return customCodec;
    }

    public boolean isComputed()
    {
        return isComputed;
    }
}
