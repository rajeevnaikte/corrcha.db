package com.corrcha.db.core;

import com.corrcha.db.core.annotations.Audited;
import com.corrcha.db.core.annotations.NotAudited;

/**
 * Created by Rajeev Naik on 6/11/2017.
 */
@Audited(table = "dummy2")
public class Dummy2
{
    @NotAudited
    public int id;
    public String name;

    private String profession;

    public void setProfession(String profession)
    {
        this.profession = profession;
    }

    public String getProfession()
    {
        return this.profession;
    }
}
