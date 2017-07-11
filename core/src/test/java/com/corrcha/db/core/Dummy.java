package com.corrcha.db.core;

import com.corrcha.db.core.annotations.ConsistencyLevel;
import com.corrcha.db.core.annotations.NotAudited;
import com.corrcha.db.core.annotations.Audited;

/**
 * Created by Rajeev Naik on 6/11/2017.
 */
@Audited(dbSpace = "audit", consistencyLevel = ConsistencyLevel.SINGLE)
public class Dummy
{
    @NotAudited
    public int id;
    public String name;

    private String profession;

    public void setProfession(String profession)
    {
        this.profession = profession;
    }

    @NotAudited
    public String getProfession()
    {
        return this.profession;
    }

    public int getId()
    {
        return this.id;
    }

    public int getAge()
    {
        return 29;
    }
}
