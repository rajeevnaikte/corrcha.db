package com.corrcha.db.core;

import com.corrcha.db.core.annotations.ConsistencyLevel;

import java.lang.String;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Rajeev Naik on 6/11/2017.
 */
public class DBFacadeTemplateTest
{
    @Test
    public void testParseEntity()
    {
        DBFacadeTemplateTestImpl template = new DBFacadeTemplateTestImpl();
        template.parseEntity(Dummy.class, Dummy2.class);
        assertEquals(2, template.getEntitiesMap().size());
        template.parseEntity(String.class);
        assertEquals(3, template.getEntitiesMap().size());

        ParsedEntity<String> dummy = template.getEntitiesMap().get(Dummy.class);
        assertNotNull(dummy.getAuditPropertyList());
        assertEquals(2, dummy.getAuditPropertyList().size());
        assertEquals("Dummy", dummy.getEntityTableMetaData());
        assertEquals("Dummy_AUD", dummy.getAuditTableMetaData());
        Assert.assertEquals(ConsistencyLevel.SINGLE, dummy.getConsistencyLevel());

        ParsedEntity<String> dummy2 = template.getEntitiesMap().get(Dummy2.class);
        Assert.assertEquals(ConsistencyLevel.BOTH, dummy2.getConsistencyLevel());

        ParsedEntity<String> string = template.getEntitiesMap().get(String.class);
        assertNull(string.getAuditPropertyList());
        assertEquals("String", string.getEntityTableMetaData());
        assertNull(string.getAuditTableMetaData());
        assertNull(string.getConsistencyLevel());

        template.getEntitiesMap().clear();
    }

    @Test
    public void testParseEntityWithEmpty()
    {
        DBFacadeTemplateTestImpl template = new DBFacadeTemplateTestImpl();
        template.parseEntity();
        assertEquals(0, template.getEntitiesMap().size());
    }

    @Test
    public void testExecutionStrategy()
    {
        DBFacadeTemplateTestImpl template = new DBFacadeTemplateTestImpl();
        ParsedEntity<String> dummy = new ParsedEntity<>();
        dummy.setConsistencyLevel(ConsistencyLevel.BOTH);
        assertEquals("BOTH", template.executionStrategy(dummy, () -> "SINGLE", () -> "BOTH"));
        dummy.setConsistencyLevel(ConsistencyLevel.SINGLE);
        assertEquals("SINGLE", template.executionStrategy(dummy, () -> "SINGLE", () -> "BOTH"));
    }

    @Test
    public void testSaveEntity()
    {
        DBFacadeTemplateTestImpl template = new DBFacadeTemplateTestImpl();
        template.parseEntity(String.class);
        template.save(new String());
        assertTrue(template.savedEntity);
        assertFalse(template.savedWithAudit);
        template.getEntitiesMap().clear();
    }

    @Test
    public void testSaveWithAudit()
    {
        DBFacadeTemplateTestImpl template = new DBFacadeTemplateTestImpl();
        template.parseEntity(Dummy.class);
        template.save(new Dummy());
        assertFalse(template.savedEntity);
        assertTrue(template.savedWithAudit);
        template.getEntitiesMap().clear();
    }

    @Test
    public void testUpdateEntity()
    {
        DBFacadeTemplateTestImpl template = new DBFacadeTemplateTestImpl();
        template.parseEntity(String.class);
        template.update(new String());
        assertTrue(template.updatedEntity);
        assertFalse(template.updatedWithAudit);
        template.getEntitiesMap().clear();
    }

    @Test
    public void testUpdateWithAudit()
    {
        DBFacadeTemplateTestImpl template = new DBFacadeTemplateTestImpl();
        template.parseEntity(Dummy.class);
        template.update(new Dummy());
        assertFalse(template.updatedEntity);
        assertTrue(template.updatedWithAudit);
        template.getEntitiesMap().clear();
    }

    @Test
    public void testDeleteEntity()
    {
        DBFacadeTemplateTestImpl template = new DBFacadeTemplateTestImpl();
        template.parseEntity(String.class);
        template.delete(new String());
        assertTrue(template.deletedEntity);
        assertFalse(template.deletedWithAudit);
        template.getEntitiesMap().clear();
    }

    @Test
    public void testDeleteWithAudit()
    {
        DBFacadeTemplateTestImpl template = new DBFacadeTemplateTestImpl();
        template.parseEntity(Dummy.class);
        template.delete(new Dummy());
        assertFalse(template.deletedEntity);
        assertTrue(template.deletedWithAudit);
        template.getEntitiesMap().clear();
    }
}
