package com.corrcha.db.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;

/**
 * Created by Rajeev Naik on 6/10/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({DBFactory.class})
public class DBFactoryTest
{
    @Test
    public void testGetDataStaxMapper()
    {
        assertNull(DBFactory.getDataStaxAdapter());
    }

    @Test
    public void testGetWithoutEntity()
    {
        try
        {
            DBFactory.get("", "", "");
            assertTrue(false);
        }
        catch (RuntimeException e)
        {
            assertTrue(true);
        }
        try
        {
            DBFactory.get("", "", "", new Class[]{});
            assertTrue(false);
        }
        catch (RuntimeException e)
        {
            assertTrue(true);
        }
    }

    @Test
    public void testGet()
    {
        assertNull(DBFactory.get("","","",Object.class));
        assertNull(DBFactory.get("","","",Object.class, Math.class));
        assertNull(DBFactory.get("","","",new Class[]{Object.class, Math.class}));
    }
}
