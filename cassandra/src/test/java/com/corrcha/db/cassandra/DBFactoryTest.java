package com.corrcha.db.cassandra;

import com.corrcha.db.core.DBFacade;
import com.corrcha.db.core.DBFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.mockito.PowerMockito.*;
import static org.junit.Assert.*;

/**
 * Created by Rajeev Naik on 6/10/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({DBFactory.class})
public class DBFactoryTest
{
    @Mock DBFacade dbFacade;

    @Before
    public void beforeTest()
    {
        PowerMockito.spy(DBFactory.class);
        doNothing().when(dbFacade).parseEntity(Passenger.class);
    }

    @Test
    public void testGetDataStaxMapper()
    {
        assertTrue(DBFactory.getDataStaxAdapter() instanceof DataStaxAdapterImpl);
    }

    @Test
    public void testGetWithFitFor()
    {
        when(DBFactory.getDataStaxAdapter()).thenReturn(dbFacade);
        doNothing().when(dbFacade).connect("","","", null, null);
        when(dbFacade.isFitFor(Passenger.class)).thenReturn(true);
        assertNotNull(DBFactory.get("","","", Passenger.class));
    }

    @Test
    public void testGetWithNotFitFor()
    {
        when(DBFactory.getDataStaxAdapter()).thenReturn(dbFacade);
        when(dbFacade.isFitFor(Passenger.class)).thenReturn(false);
        assertNull(DBFactory.get("","","", Passenger.class));
    }
}
