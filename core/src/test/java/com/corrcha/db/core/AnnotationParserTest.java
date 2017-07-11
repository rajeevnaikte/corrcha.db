package com.corrcha.db.core;

import com.corrcha.db.core.annotations.ConsistencyLevel;
import com.corrcha.db.core.annotations.NotAudited;
import org.junit.Test;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Rajeev Naik on 6/11/2017.
 */
public class AnnotationParserTest
{
    AnnotationParser parser = new AnnotationParserTestImpl();

    @Test
    public void testGetAuditTableName()
    {
        assertEquals("entity_AUD", AnnotationParser.getAuditTableName(Dummy.class, "entity"));
        assertEquals("dummy2", AnnotationParser.getAuditTableName(Dummy2.class, "entity"));
    }

    @Test
    public void testGetAuditTableDBSpace()
    {
        assertEquals("audit", AnnotationParser.getAuditTableDBSpace(Dummy.class, "entity"));
        assertEquals("entity", AnnotationParser.getAuditTableDBSpace(Dummy2.class, "entity"));
    }

    @Test
    public void testIsAudited()
    {
        assertTrue(AnnotationParser.isAudited(Dummy.class));
        assertFalse(AnnotationParser.isAudited(String.class));
    }

    @Test
    public void testGetConsistencyLevel()
    {
        assertEquals(ConsistencyLevel.SINGLE, AnnotationParser.getConsistencyLevel(Dummy.class));
        assertEquals(ConsistencyLevel.BOTH, AnnotationParser.getConsistencyLevel(Dummy2.class));
    }

    @Test
    public void testGetInstanceOfClass()
    {
        assertTrue(parser.getInstance(String.class) instanceof String);
    }

    @Test
    public void testGetAnnotation()
    {
        try
        {
            Field idFiled = Dummy.class.getDeclaredField("id");
            idFiled.setAccessible(true);
            assertTrue(parser.getAnnotation(idFiled, null, NotAudited.class) instanceof NotAudited);

            Field nameFiled = Dummy.class.getDeclaredField("name");
            nameFiled.setAccessible(true);
            assertNull(parser.getAnnotation(nameFiled, null, NotAudited.class));

            PropertyDescriptor idDesc = Arrays.stream(Introspector.getBeanInfo(Dummy.class).getPropertyDescriptors())
                    .filter(prop -> "id".equals(prop.getName())).findFirst().get();
            idDesc.getReadMethod().setAccessible(true);
            assertTrue(parser.getAnnotation(idFiled, idDesc, NotAudited.class) instanceof NotAudited);

            Field profField = Dummy.class.getDeclaredField("profession");
            profField.setAccessible(true);
            PropertyDescriptor profDesc = Arrays.stream(Introspector.getBeanInfo(Dummy.class).getPropertyDescriptors())
                    .filter(prop -> "profession".equals(prop.getName())).findFirst().get();
            profDesc.getReadMethod().setAccessible(true);
            assertTrue(parser.getAnnotation(profField, profDesc, NotAudited.class) instanceof NotAudited);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetAuditProperties()
    {
        List<EntityProperty> properties = parser.getAuditProperties(Dummy.class);
        assertEquals(2, properties.size());
    }
}
