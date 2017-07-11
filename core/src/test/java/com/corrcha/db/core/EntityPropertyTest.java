package com.corrcha.db.core;

import org.junit.Before;
import org.junit.Test;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by Rajeev Naik on 6/11/2017.
 */
public class EntityPropertyTest
{
    Dummy dummy;

    @Before
    public void setUp()
    {
        dummy = new Dummy();
        dummy.id = 1;
        dummy.name = "dummy";
        dummy.setProfession("Engineer");
    }

    @Test
    public void testWithPublicFileds()
    {
        try
        {
            Field idFiled = Dummy.class.getDeclaredField("id");
            EntityProperty id = new EntityProperty(idFiled.getName(), idFiled, null, "id");
            assertTrue(id.getJavaType().isPrimitive());
            Field nameFiled = Dummy.class.getDeclaredField("name");
            EntityProperty name = new EntityProperty(nameFiled.getName(), nameFiled, null, "name");
            assertTrue(name.getJavaType().getType() == String.class);
            assertEquals(1, id.getValue(dummy));
            assertEquals("dummy", name.getValue(dummy));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testWithPrivateFields()
    {
        try
        {
            Field profFiled = Dummy.class.getDeclaredField("profession");
            profFiled.setAccessible(true);
            EntityProperty prof = new EntityProperty(profFiled.getName(), profFiled, null, "profession");
            assertTrue(prof.getJavaType().getType() == String.class);
            assertEquals("Engineer", prof.getValue(dummy));

            profFiled.setAccessible(false);
            PropertyDescriptor propDesc = Arrays.stream(Introspector.getBeanInfo(Dummy.class).getPropertyDescriptors())
                    .filter(prop -> "profession".equals(prop.getName())).findFirst().get();
            propDesc.getReadMethod().setAccessible(true);
            prof = new EntityProperty(profFiled.getName(), profFiled, propDesc, "profession");
            assertEquals("Engineer", prof.getValue(dummy));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
