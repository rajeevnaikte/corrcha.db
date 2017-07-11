package com.corrcha.db.core;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * Created by Rajeev Naik on 6/11/2017.
 */
public class AnnotationParserTestImpl extends AnnotationParser
{
    @Override protected String getColumnName(Field field, PropertyDescriptor property)
    {
        return (field == null ? property.getName() : field.getName());
    }
}
