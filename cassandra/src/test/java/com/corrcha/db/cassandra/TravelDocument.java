package com.corrcha.db.cassandra;

import com.datastax.driver.core.LocalDate;
import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.UDT;

@UDT(keyspace = "audit", name = "travel_document")
public class TravelDocument
{
    @Field(name = "expDate", caseSensitive=true)
    private LocalDate expDate;
    @Field(name = "issueDate", caseSensitive=true)
    private LocalDate issueDate;
    private String number;
    private String nationality;

    private String city;
    private String line1;
    private String line2;
    private String zip;

    public LocalDate getExpDate()
    {
        return expDate;
    }

    public void setExpDate(LocalDate expDate)
    {
        this.expDate = expDate;
    }

    public LocalDate getIssueDate()
    {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate)
    {
        this.issueDate = issueDate;
    }

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

    public String getNationality()
    {
        return nationality;
    }

    public void setNationality(String nationality)
    {
        this.nationality = nationality;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public String getLine1()
    {
        return line1;
    }

    public void setLine1(String line1)
    {
        this.line1 = line1;
    }

    public String getLine2()
    {
        return line2;
    }

    public void setLine2(String line2)
    {
        this.line2 = line2;
    }

    public String getZip()
    {
        return zip;
    }

    public void setZip(String zip)
    {
        this.zip = zip;
    }
}
