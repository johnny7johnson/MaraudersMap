package com.example.johanna.myapplication;



/**
 * Created by Johanna on 28.11.2016.
 */

public class CallContent {

    //Location mLocation;
    String name;
    String ldap;
    String description;
    String authToken;





    /**
     *
     * @param name
     * @param ldap
     * @param pDescription
     */
    //constructor for init post request
    public CallContent(String name, String ldap, String pDescription){
        this.name = name;
        this.ldap = ldap;
        if(pDescription==null){
            this.description = "test";
        }
        else{
            this.description = pDescription;
        }

    }



    public String getAuthToken(){
        return authToken;
    }






}
