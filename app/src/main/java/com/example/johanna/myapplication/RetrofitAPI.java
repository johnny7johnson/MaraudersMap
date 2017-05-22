package com.example.johanna.myapplication;

/**
 * Created by Johanna on 29.11.2016.
 */


import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Interface used to define the REST API  * of the demo used by Retrofit  *  * @author retwet  * created on 15.11.2016
 */
public interface RetrofitAPI {

    /**      * Performs POST request to send a date      *
     * *Response is the same date which was sent if HTTP response code is 200      *
     * * @param pDemoPOJO stores the date which should be sent
     * * @return Call object which can be enqueued as request
     * param: name, ldap, description
     * returns: Call with authToken*/
    //Registration request
    @POST("user") // path added to the BASE_URL
    Call<CallContent> sendUserPOST(@Body CallContent content);

    /**
     * Performs GET request to receive the current date      *
     * * @return Call object which can be enqueued as request
     * param: -
     * returns: Call with name, ldap, description, authtoken
     */
    //Login Request
    @GET("user/{ldap}")  // path added to the BASE_URL
    Call<CallContent> sendUserGET(@Path("ldap")String userLdap);


    /*
    * @param  name ldap, description
    * @return -
    * */
    //User Update
    @POST("user/{token}")
    Call<ResponseBody> sendUserUpdatePOST(@Path("token") String authToken, @Body CallContent content);


    /*
     * @param latitude, longitude
     * @return -
     * */
    //Location Update
    @POST("location/{token}")
    Call<ResponseBody> sendLocationUpdatePost(@Path("token") String token, @Body LocationContent content);


    /*
     * gets all users nearby
     * @param -
     * @return {name, description, locations{latitude, longitude}}
     * */
    //users nearby
    @GET("location/{token}/{radius}/{lat}/{lon}")
    Call<ArrayList<NeighbourLocations>> getUsersLocations(@Path("token") String token, @Path("radius") double radius, @Path("lat") double latitude, @Path("lon") double longitude);



 }