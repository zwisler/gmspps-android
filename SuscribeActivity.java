package com.citaurus.gmspps;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

public class SuscribeActivity extends AppCompatActivity implements View.OnClickListener {
    private String nhUrl;
    private String ProviderID;
    public  WebView myWebView;
    private static final String defaultUrl = "http://beta.html5test.com/";
    private ProgressDialog dialog; //= new ProgressDialog(MissionActivity.this);
    private ListView lv;
    private GMSPPSClient Client;
    private GoogleCloudMessaging gcm;
    private EditText PasswordField;

    ArrayAdapter<String> adapter;
    ArrayList<String> Selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = new ProgressDialog(SuscribeActivity.this);
        Client =  new GMSPPSClient(this, MainActivity.BACKEND_ENDPOINT);
        Client.setAuthorizationHeader(MainActivity.AuthorizationHeader);
        gcm = GoogleCloudMessaging.getInstance(this);
        setContentView(R.layout.activity_suscribe);
        WebView  myWebView = (WebView) findViewById(R.id.webview);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                nhUrl= defaultUrl;
            } else {

                nhUrl= extras.getString("url");
                ProviderID = extras.getString("providerid");
            }
        } else {

            nhUrl= (String) savedInstanceState.getSerializable("url");
           // missionID = (int) savedInstanceState.getSerializable("missionid");
        }
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                view.setVisibility(View.VISIBLE);
            }
        });
        dialog.setMessage("Loading..Please wait.");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        myWebView.setVisibility(View.GONE);




        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        //myWebView.setWebChromeClient(new WebChromeClient());

       // webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        myWebView.loadUrl(nhUrl);

        // List View
        /*
        lv = (ListView) findViewById(R.id.listView1);
        //ToDO von service befüllen

        String[]  KeyWords = {"burger","pizza","olives","orange"};
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice,  KeyWords);

        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lv.setAdapter(adapter);
        */
        new GetProviderTagsAsyncTask().execute();


        findViewById(R.id.suscribe_button).setOnClickListener(this);
        findViewById(R.id.ok_suscribe_button).setOnClickListener(this);
        PasswordField = (EditText) findViewById(R.id.password);
        PasswordField.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (  actionId == EditorInfo.IME_ACTION_DONE ) {
                            String a = "ok";
                            ok();

                                return true; // consume.
                        }

                        return false;
                    }
                });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.suscribe_button:
                try {
                    findViewById(R.id.Subfield).setVisibility(View.GONE);
                    findViewById(R.id.Passfield).setVisibility(View.VISIBLE);
                    if (lv != null) {
                        findViewById(R.id.Webfield).setVisibility(View.GONE);
                        findViewById(R.id.Listfield).setVisibility(View.VISIBLE);
                    }
                }
                    catch (Exception e) {
                        Log.e("Fehler","Fehler beim Susribe Button " + e.toString());
                    }

                break;
            case R.id.ok_suscribe_button:
                ok();

                break;

        }
    }
    public void ok( ) {
        try {

            String pass = PasswordField.getText().toString();
            if (pass.length() <= 0) {

                DialogNotify(getString(R.string.password), getString(R.string.passwordiswrong));

            } else {
                if (lv != null) {
                    SparseBooleanArray checked = lv.getCheckedItemPositions();
                    ArrayList<String> selectedItems = new ArrayList<String>();
                    for (int i = 0; i < checked.size(); i++) {
                        // Item position in adapter
                        int position = checked.keyAt(i);
                        // Add sport if it is checked i.e.) == TRUE!
                        if (checked.valueAt(i))
                            selectedItems.add(adapter.getItem(position));
                    }
                    //Ergebniss
                    Selected = selectedItems;
                }
                new RegAsyncTask().execute(pass, ProviderID);
            }
        }
        catch (Exception e) {
            Log.e("Fehler","Fehler beim Susribe Button " + e.toString());
        }

    }


    public void DialogNotify(final String title,final String message )
    {
        final AlertDialog.Builder dlg;
        dlg = new AlertDialog.Builder(this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog dlgAlert = dlg.create();
                dlgAlert.setTitle(title);
                dlgAlert.setButton(DialogInterface.BUTTON_POSITIVE,
                        (CharSequence) "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dlgAlert.setMessage(message);
                dlgAlert.setCancelable(false);
                dlgAlert.show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_suscribe, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private class RegAsyncTask extends AsyncTask<String, Object, Object> {

        @Override
        protected Object doInBackground(String... params){
            try {
                String Password =  params[0];
                String ProviderID =  params[1];
                    String regid = gcm.register(MainActivity.SENDER_ID);
                if(Selected != null) {
                    Client.register(regid, new HashSet<String>(Selected), Password, ProviderID);
                }else {
                    Client.register(regid, new HashSet<String>(), Password, ProviderID);
                }
            } catch (Exception e) {
                Log.e("Fehler","Fehler bei Register " +  e.getMessage());
                if (e.getMessage() == "Passwort") {
                    DialogNotify(getString(R.string.password), getString(R.string.passwordiswrong));
                }
                return e;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Object result) {
            try {
                if (result == null)
                {
                    //ok
                    SuscribeActivity.this.finish();




                }

            } catch (Exception e) {
                Log.e("Fehler","Fehler bei Register " +  e.getMessage());
            }
           // RegisterClient_Ok = true;
            //sendPush.setEnabled(true);
            //Toast.makeText(context, "Logged in and registered.",
            //        Toast.LENGTH_LONG).show();

        }
    };
    private class GetProviderTagsAsyncTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params){
            try {
                return Client.GetAllProviderTags(ProviderID);

            } catch (Exception e) {
                Toast.makeText(SuscribeActivity.this, "SuscribeActivity - Failed to get Tags " , Toast.LENGTH_SHORT).show();

                return e.toString();
            }
            // null;
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                //JSONObject jObj = new JSONObject(result);
                JSONArray Obj = new JSONArray(result);
                if(Obj.length() > 0)
                {

                    String[]  KeyWords = new String[Obj.length()] ;
                    for (int i = 0; i < Obj.length(); i++) {
                        JSONObject row = Obj.getJSONObject(i);
                        KeyWords[i] = row.getString("name");
                        }
                    // Client hat sich bereits suscribt
                    //ToDo anzeigen
                    String test = "Ok";
                    // List View
                    lv = (ListView) findViewById(R.id.listView1);
                    //ToDO von service befüllen

                    //String[]  KeyWords = {"burger","pizza","olives","orange"};

                    adapter = new ArrayAdapter<String>(SuscribeActivity.this, android.R.layout.simple_list_item_multiple_choice,  KeyWords);

                    lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    lv.setAdapter(adapter);


                }
                else {
                    // Keinemöglichen Provider zurück geliefert

                    String test = "NOK";

                }

            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }
            catch (Exception e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }
        }
    };
}
