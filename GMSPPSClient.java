package com.citaurus.gmspps;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.io.InputStream;

public class GMSPPSClient {
    private static final String PREFS_NAME = "ANHSettings";
    private static final String REGID_SETTING_NAME = "ANHRegistrationId";
    //private String Register_Endpoint;
    private String State_Endpoint;
    private String Suscribe_Endpoint;
    private String Provider_Endpoint;
    private String Mission_State_Endpoint;
    private String Mission_Info_Endpoint;
    private String Version_Endpoint;


    SharedPreferences settings;
    protected HttpClient httpClient;
    private String authorizationHeader;

    public GMSPPSClient(Context context, String backendEnpoint) {
        super();
        this.settings = context.getSharedPreferences(PREFS_NAME, 0);
        httpClient =  new DefaultHttpClient();
        //Register_Endpoint = backendEnpoint + "/api/register";
        State_Endpoint = backendEnpoint + "/api/GPMS_Status";
        Suscribe_Endpoint = backendEnpoint + "/api/GPMS_Suscribe";
        Provider_Endpoint = backendEnpoint + "/api/GPMS_PROVIDER";
        Mission_State_Endpoint = backendEnpoint + "/api/MissionState";
        Mission_Info_Endpoint = backendEnpoint + "/api/GPMS_Mission";
        Version_Endpoint = backendEnpoint + "/api/Version?device=android";
    }

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }




    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    public String GetVersion() throws ClientProtocolException, IOException {
        String result = "";
        // make GET request to the given URL
        //HttpResponse httpResponse = httpClient.execute(new HttpGet(Suscribe_Endpoint));
        InputStream inputStream = null;
        HttpUriRequest request = new HttpGet(Version_Endpoint);
        HttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            inputStream = response.getEntity().getContent();
            // convert inputstream to string
            if(inputStream != null) {
                // String result = EntityUtils.toString(inputStream);
                result = convertInputStreamToString(inputStream);
            }
        }
        return result;
    }
    // Status an Server senden
    public void SetState( String Status , String LAT , String LON) throws ClientProtocolException, IOException, JSONException {
        int statusCode = SendStatus(Status, LAT, LON);

        if (statusCode == HttpStatus.SC_OK) {
            return;
        } else if (statusCode == HttpStatus.SC_GONE){
            //settings.edit().remove(REGID_SETTING_NAME).commit();
            //registrationId = retrieveRegistrationIdOrRequestNewOne(handle);
            //statusCode = upsertRegistration(registrationId, deviceInfo);
            statusCode = SendStatus(Status, LAT, LON);
            if (statusCode != HttpStatus.SC_OK) {
                Log.e("Client", "Error snd Status : " + statusCode);
                throw new RuntimeException("Error snd Status");
            }
        } else {
            Log.e("RegisterClient", "Error snd Status: " + statusCode);
            throw new RuntimeException("Error snd Status");
        }
    }
    // Mission Status (akk) an Server senden
    public void SetAkk( String Status , String missionID ) throws ClientProtocolException, IOException, JSONException {
        int statusCode = AkkMission(Status, missionID);

        if (statusCode == HttpStatus.SC_OK) {
            return;
        } else if (statusCode == HttpStatus.SC_GONE){
            //settings.edit().remove(REGID_SETTING_NAME).commit();
            //registrationId = retrieveRegistrationIdOrRequestNewOne(handle);
            //statusCode = upsertRegistration(registrationId, deviceInfo);
            statusCode = AkkMission(Status, missionID);
            if (statusCode != HttpStatus.SC_OK) {
                Log.e("Client", "Error akk Mission: " + statusCode);
                throw new RuntimeException("Error akk Mission");
            }
        } else {
            Log.e("RegisterClient", "Error akk Mission: " + statusCode);
            throw new RuntimeException("Error akk Mission");
        }
    }

    // Client beim Server registrieren Provider Suscribe
    public void register(String handle, Set<String> tags, String Password, String ProviderID ) throws ClientProtocolException, IOException, JSONException {
        String registrationId = retrieveRegistrationIdOrRequestNewOne(handle);
        JSONObject deviceInfo = new JSONObject();
        deviceInfo.put("Platform", "gcm");
        deviceInfo.put("Key", Password);
        deviceInfo.put("ProviderID", ProviderID);
        deviceInfo.put("Handle", handle);
        deviceInfo.put("Tags", new JSONArray(tags));

        int statusCode = upsertRegistration(registrationId, deviceInfo);

        if (statusCode == HttpStatus.SC_OK) {
            return;
        } else if (statusCode == HttpStatus.SC_GONE){
            settings.edit().remove(REGID_SETTING_NAME).commit();
            registrationId = retrieveRegistrationIdOrRequestNewOne(handle);
            statusCode = upsertRegistration(registrationId, deviceInfo);
            if (statusCode != HttpStatus.SC_OK) {
                Log.e("RegisterClient", "Error upserting registration: " + statusCode);
                throw new RuntimeException("Error upserting registration");
            }


        } else {
            if (statusCode == HttpStatus.SC_UNAUTHORIZED){
                Log.e("RegisterClient", "Passwort: " + statusCode);
                throw new RuntimeException("Passwort");


            }else {
                Log.e("RegisterClient", "Error upserting registration: " + statusCode);
                throw new RuntimeException("Error upserting registration");
            }
        }
    }
    private int AkkMission(String Status , String Mission)
            throws UnsupportedEncodingException, IOException,
            ClientProtocolException {
        HttpUriRequest request = new HttpPost(Mission_State_Endpoint+"?Mission="+Mission+"&state="+Status);
        request.addHeader("Authorization", "GMSPPSg " + authorizationHeader);
        HttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            Log.e("RegisterClient", "Error send Status: " + response.getStatusLine().getStatusCode());
            throw new RuntimeException("Error send Status: " + response.getStatusLine().getStatusCode());
        }
        int statusCode = response.getStatusLine().getStatusCode();
        return statusCode;
    }

    private int SendStatus(String Status , String LAT , String LON)
            throws UnsupportedEncodingException, IOException,
            ClientProtocolException {
        HttpUriRequest request = new HttpPost(State_Endpoint+"?State="+Status+"&LAT="+LAT+"&LON="+LON);
        request.addHeader("Authorization", "GMSPPSg " + authorizationHeader);
        HttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            Log.e("RegisterClient", "Error send Status: " + response.getStatusLine().getStatusCode());
            throw new RuntimeException("Error send Status");
        }
        int statusCode = response.getStatusLine().getStatusCode();
        return statusCode;
    }
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    private int upsertRegistration(String registrationId, JSONObject deviceInfo)
            throws UnsupportedEncodingException, IOException,
            ClientProtocolException {
        Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        Header header2 = new BasicHeader(HttpHeaders.AUTHORIZATION, "GMSPPSg "+authorizationHeader);
        HttpPut request = new HttpPut(Suscribe_Endpoint+"/"+registrationId);
        request.setEntity(new StringEntity(deviceInfo.toString()));
        request.addHeader(header2);
        request.addHeader(header);
       
        HttpResponse response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        return statusCode;
    }

    private String retrieveRegistrationIdOrRequestNewOne(String handle) throws ClientProtocolException, IOException {
        if (settings.contains(REGID_SETTING_NAME))
            return settings.getString(REGID_SETTING_NAME, null);

        HttpUriRequest request = new HttpPost(Suscribe_Endpoint+"?handle="+handle);
        request.addHeader("Authorization", "GMSPPSg "+authorizationHeader);
        HttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            Log.e("RegisterClient", "Error creating registrationId: " + response.getStatusLine().getStatusCode());
            throw new RuntimeException("Error creating Notification Hubs registrationId");
        }
        String registrationId = EntityUtils.toString(response.getEntity());
        registrationId = registrationId.substring(1, registrationId.length()-1);

        settings.edit().putString(REGID_SETTING_NAME, registrationId).commit();

        return registrationId;
    }
    public String GetSuscribetProvider(String handle) throws ClientProtocolException, IOException {
        String result = "";
        String registrationId = retrieveRegistrationIdOrRequestNewOne(handle);
        // make GET request to the given URL
        //HttpResponse httpResponse = httpClient.execute(new HttpGet(Suscribe_Endpoint));
        InputStream inputStream = null;
        //HttpUriRequest request = new HttpGet(Suscribe_Endpoint+"/"+ handle);
        HttpUriRequest request = new HttpGet(Suscribe_Endpoint+"?handle="+handle);
        request.addHeader("Authorization", "GMSPPSg "+authorizationHeader);
        HttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            inputStream = response.getEntity().getContent();
            // convert inputstream to string
            if(inputStream != null) {
                // String result = EntityUtils.toString(inputStream);
                result = convertInputStreamToString(inputStream);
            }
        }
        return result;
    }
    public String GetAllProvider() throws ClientProtocolException, IOException {
        String result = "";
        // make GET request to the given URL
        //HttpResponse httpResponse = httpClient.execute(new HttpGet(Suscribe_Endpoint));
        InputStream inputStream = null;
        HttpUriRequest request = new HttpGet(Provider_Endpoint);
        request.addHeader("Authorization", "GMSPPSg "+authorizationHeader);
        HttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            inputStream = response.getEntity().getContent();
            // convert inputstream to string
            if(inputStream != null) {
                // String result = EntityUtils.toString(inputStream);
                result = convertInputStreamToString(inputStream);
            }
        }
        return result;
    }
    public String GetAllProviderTags(String Id) throws ClientProtocolException, IOException {
        String result = "";
        // make GET request to the given URL
        //HttpResponse httpResponse = httpClient.execute(new HttpGet(Suscribe_Endpoint));
        InputStream inputStream = null;
        HttpUriRequest request = new HttpGet(Provider_Endpoint+"/"+Id);
        request.addHeader("Authorization", "GMSPPSg "+authorizationHeader);
        HttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            inputStream = response.getEntity().getContent();
            // convert inputstream to string
            if(inputStream != null) {
                // String result = EntityUtils.toString(inputStream);
                result = convertInputStreamToString(inputStream);
            }
        }
        return result;
    }
    public String GetMissionInfo(String Id) throws ClientProtocolException, IOException {
        String result = "";
        // make GET request to the given URL
        //HttpResponse httpResponse = httpClient.execute(new HttpGet(Suscribe_Endpoint));
        InputStream inputStream = null;
        HttpUriRequest request = new HttpGet(Mission_Info_Endpoint+"/"+Id);
        request.addHeader("Authorization", "GMSPPSg "+authorizationHeader);
        HttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            inputStream = response.getEntity().getContent();
            // convert inputstream to string
            if(inputStream != null) {
                // String result = EntityUtils.toString(inputStream);
                result = convertInputStreamToString(inputStream);
            }
        }
        return result;
    }
}
