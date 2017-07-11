package com.corrcha.db.cassandra;

import com.datastax.driver.core.LocalDate;
import com.corrcha.db.core.DBFacade;
import com.corrcha.db.core.DBFactory;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Rajeev Naik on 6/5/2017.
 */
public class WithAudit
{
    @Test
    public void testWithAudit()
    {
        AuditCodecRegistry.init();

        DBFacade dbFacade = DBFactory.getDataStaxAdapter();
        dbFacade.connect("127.0.0.1", null, "audit", null, null);
        dbFacade.parseEntity(Passenger.class);

        Passenger passenger = new Passenger();
        passenger.setId(UUID.randomUUID());
        //passenger.setId(UUID.fromString("35782767-a3ec-49ef-bc72-b60cbbe53999"));
        passenger.setLastName("Rajamani");
        passenger.setFirstName("Ganesan");
        passenger.setGender(Gender.MALE);
        passenger.setTitle("Mr.");
        Map<DocumentType, TravelDocument> documentMap = new HashMap<>(2);
        TravelDocument document = new TravelDocument();
        document.setNumber("G24749203");
        document.setNationality("IN");
        document.setExpDate(LocalDate.fromYearMonthDay(2020,10,10));
        document.setIssueDate(LocalDate.fromYearMonthDay(2010,10,10));
        documentMap.put(DocumentType.PASSPORT, document);
        document = new TravelDocument();
        document.setCity("Bengaluru");
        document.setLine1("Creator");
        document.setLine2("ITPL");
        document.setZip("560066");
        documentMap.put(DocumentType.RESIDENCE_ADDRESS, document);
        passenger.setTravelDocuments(documentMap);

        dbFacade.save(passenger);
        dbFacade.disconnect();
    }

    @Test
    public void testWithAuditDBSpace()
    {
        AuditCodecRegistry.init();

        DBFacade dbFacade = DBFactory.getDataStaxAdapter();
        dbFacade.connect("127.0.0.1", null, "audit", null, null);
        dbFacade.parseEntity(Passenger.class);

        Passenger2 passenger = new Passenger2();
        passenger.setId(UUID.randomUUID());
        //passenger.setId(UUID.fromString("35782767-a3ec-49ef-bc72-b60cbbe53999"));
        passenger.setLastName("Rajamani");
        passenger.setFirstName("Ganesan");
        passenger.setGender(Gender.MALE);
        passenger.setTitle("Mr.");
        Map<DocumentType, TravelDocument> documentMap = new HashMap<>(2);
        TravelDocument document = new TravelDocument();
        document.setNumber("G24749203");
        document.setNationality("IN");
        document.setExpDate(LocalDate.fromYearMonthDay(2020,10,10));
        document.setIssueDate(LocalDate.fromYearMonthDay(2010,10,10));
        documentMap.put(DocumentType.PASSPORT, document);
        document = new TravelDocument();
        document.setCity("Bengaluru");
        document.setLine1("Creator");
        document.setLine2("ITPL");
        document.setZip("560066");
        documentMap.put(DocumentType.RESIDENCE_ADDRESS, document);
        passenger.setTravelDocuments(documentMap);

        dbFacade.save(passenger);
        dbFacade.disconnect();
    }
}
