package com.citaurus.gmspps;

/**
 * Created by rzwisler on 02.09.2015.
 */
public class Provider {
    public int TypID;
    public String Typ;
    public String Name;
    public String Url;
    public String IconUrl;
    public String ID;
    public Provider(){
        super();
    }
    public Provider(int typID, String Typ, String name, String url, String IconUrl, String id ) {
        super();
        this.TypID = typID;
        this.Name = name;
        this.Url = url;
        this.IconUrl = IconUrl;
        this.Typ =  Typ;
        this.ID = id;
    }

}
