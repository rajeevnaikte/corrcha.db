package com.corrcha.db.cassandra;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.corrcha.db.core.annotations.Audited;

import java.util.Map;
import java.util.UUID;

@Table(keyspace = "audit", name = "Passenger",
        readConsistency = "QUORUM",
        writeConsistency = "QUORUM",
        caseSensitiveKeyspace = false,
        caseSensitiveTable = true)
@Audited(table = "pax_audit", dbSpace = "demo")
public class Passenger2
{
    @PartitionKey
    private UUID id;
    @Column(name = "firstName", caseSensitive=true)
    private String firstName;
    @Column(name = "lastName", caseSensitive=true)
    private String lastName;
    private String title;

    private Gender gender;
    @Column(name = "travelDocuments", caseSensitive=true)
    private Map<DocumentType, TravelDocument> travelDocuments;

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Gender getGender()
    {
        return gender;
    }

    public void setGender(Gender gender)
    {
        this.gender = gender;
    }

    public Map<DocumentType, TravelDocument> getTravelDocuments()
    {
        return travelDocuments;
    }

    public void setTravelDocuments(Map<DocumentType, TravelDocument> travelDocuments)
    {
        this.travelDocuments = travelDocuments;
    }
}
