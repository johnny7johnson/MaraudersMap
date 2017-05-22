package com.example.johanna.myapplication;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by Johanna on 22.11.2016.
 */

public class StartActivity extends AppCompatActivity {

    public static final String BASE_URL = "http://swlab-ws1617.iap.hs-heilbronn.de/api/v0.1/";

    Retrofit mRetrofit;
    RetrofitAPI mRetrofitAPI;
    CallContent initContent;
    Gson gson;

    private Button okButton;
    private Button logInButton;
    private TextView requestText;
    private EditText usernameInput;
    private EditText logInLdapInput;
    MapsActivity mapsActivity;
    private EditText ldapInput;
    private String username;
    private String ldap;
    private String filename;
    private String authToken;


    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gson = new GsonBuilder().setDateFormat("'username','ldap'").create();           //evtl?
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))                 //evtl?
                .build();
        mRetrofitAPI = mRetrofit.create(RetrofitAPI.class);

        setContentView(R.layout.activity_query);
        okButton = (Button) findViewById(R.id.okButton);
        logInButton = (Button) findViewById(R.id.loginButton);

        //okButton.setTextColor();
        requestText = (TextView) findViewById(R.id.requestText);
        usernameInput = (EditText) findViewById(R.id.usernameText);
        ldapInput = (EditText) findViewById(R.id.ldapText);
        logInLdapInput = (EditText) findViewById(R.id.logInLdapInput);

        registerButtonListener();
        logInButtonListener();
    }


    /**
     * button listener
     */
    private void registerButtonListener() {
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get username, ldap and position
                username = usernameInput.getText().toString();
                ldap = ldapInput.getText().toString();

                //send Username and ldap        "first communication"
                initContent = new CallContent(username, ldap, null);

                Call<CallContent> initCall = mRetrofitAPI.sendUserPOST(initContent);
                initCall.enqueue(new Callback<CallContent>() {
                    @Override
                    public void onResponse(Call<CallContent> call, Response<CallContent> response) {
                        Integer responseCode = response.code();
                        Toast.makeText(getApplicationContext(), responseCode.toString(), Toast.LENGTH_LONG).show();
                        if(response.code()== HttpURLConnection.HTTP_OK){
                            //weiss noch nicht, was zu tun
                            //evtl ldap dauerhaft merken? (in strings)
                            authToken = response.body().getAuthToken();
                            exportToCsvFile(authToken);


                            //switch to google maps activity
                            switchToMapsActivity();
                        }
                        else if(response.code() == 409){
                            Toast.makeText(getApplicationContext(), "Sorry, du bist wohl schon registriert >.<", Toast.LENGTH_LONG).show();


                        }
                        else {
                            Toast.makeText(getApplicationContext(), R.string.http_failure, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CallContent> call, Throwable t) {
                        // request was not possible, log throwable and notify user
                        Log.e("RETROFIT GET FAILURE", t.toString());
                        Toast.makeText(getApplicationContext(), R.string.retrofit_failure, Toast.LENGTH_LONG).show();
                    }
                });

                // switchToMapsActivity();
            }
        });
    }

    /**
     * logIn button
     */
    private void logInButtonListener(){
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ldap = logInLdapInput.getText().toString();
                tryToLogIn();
            }
        });


    }

    /**
     * logIn
     */
    public void tryToLogIn(){
        Call<CallContent> loginCall = mRetrofitAPI.sendUserGET(ldap);           //send userGet with path as parameter
        loginCall.enqueue(new Callback<CallContent>() {
            @Override
            public void onResponse(Call<CallContent> call, Response<CallContent> response) {
                Integer responseCode = response.code();
                Toast.makeText(getApplicationContext(), responseCode.toString(), Toast.LENGTH_LONG).show();
                if(response.code()==HttpURLConnection.HTTP_OK) {
                    ldap = response.body().ldap.toString();
                    username = response.body().name.toString();
                    authToken = response.body().authToken.toString();
                    switchToMapsActivity();
                }
            }

            @Override
            public void onFailure(Call<CallContent> call, Throwable t) {
                Log.e("RETROFIT GET FAILURE", t.toString());
                Toast.makeText(getApplicationContext(), R.string.retrofit_failure, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     *
     * @param authToken
     */
    private void exportToCsvFile(String authToken){

        filename = "authTokens.txt";
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(filename, Context.MODE_WORLD_READABLE);
            outputStream.write(authToken.getBytes());
            outputStream.write(",".getBytes());
            outputStream.write(ldap.getBytes());
            outputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    private boolean readAuthTokenFromFile(){
        //get authToken
        String readOutAuthToken;
        byte[] content = new byte[16384];                       //readBuffer
        String stringContent;                                   //readOutString
        filename = "authTokens.txt";
        FileInputStream inputStream;
        try {                                                       //read File
            inputStream = openFileInput(filename);
            inputStream.read(content);
            inputStream.close();

        }catch (IOException e){
            e.printStackTrace();
        }
            stringContent = content.toString();                     //get String from file
        List<String> stringList = new ArrayList<>();
        stringList = Arrays.asList(stringContent.split(","));       //split string an search for needed ldap
        for(String s : stringList){
            if(s.equals(ldap)){
                readOutAuthToken=stringList.listIterator().next().getBytes().toString();
            }
        }
        return true;
    }

    /**
     * switch to map
     */

    private void switchToMapsActivity(){
        //switch to maps layout
        mapsActivity=new MapsActivity();
        Intent toMaps = new Intent(StartActivity.this,MapsActivity.class);

        toMaps.putExtra("ldap", ldap);
        toMaps.putExtra("authToken", authToken);
        toMaps.putExtra("username", username);


    // toMaps.getExtras().putString("ldap", ldap);
    // toMaps.getExtras().putString("authToken", authToken);

        //StartActivity.this.startActivity(toMaps);
        startActivity(toMaps);
    }

}
