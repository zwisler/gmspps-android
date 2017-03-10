package com.citaurus.gmspps;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProviderActivity extends AppCompatActivity {
    private String ProvidersJsonObjekt;
    ListView list;
    private Provider provider[] = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                ProvidersJsonObjekt= null;

            } else {
                ProvidersJsonObjekt= extras.getString("provider");

            }
        } else {
            ProvidersJsonObjekt= (String) savedInstanceState.getSerializable("provider");

        }
        try {
            JSONArray Obj = new JSONArray(ProvidersJsonObjekt);

            provider = new Provider[Obj.length()];
            for (int i = 0; i < Obj.length(); i++) {
                Provider item = new Provider();
                JSONObject row = Obj.getJSONObject(i);
                item.Url = row.getString("url");
                item.Name = row.getString("name");
                item.TypID = row.getInt("typeint");
                item.Typ = row.getString("type");
                item.IconUrl = row.getString("icon");
                item.ID = row.getString("id");

                provider[i] = item;

            }
            ProviderAdapter adapter = new ProviderAdapter(ProviderActivity.this, 1 , provider );
            list=(ListView)findViewById(R.id.list);
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Todo Provider wurde ausgewählt Suscrition senden
                   // Toast.makeText(ProviderActivity.this, "You Clicked at " + provider[+position].Name, Toast.LENGTH_SHORT).show();
                    Intent ProviderIntent = new Intent(ProviderActivity.this,  SuscribeActivity.class);
                    ProviderIntent.putExtra("url", provider[+position].Url);
                    ProviderIntent.putExtra("providerid", provider[+position].ID);// ProviderID = extras.getString("providerid");
                    //MissionIntent.putExtra("message", nhMessage);
                    startActivity(ProviderIntent);
                    ProviderActivity.this.finish();

                }
            });



        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        catch (Exception e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
    }

    // [START on_save_instance_state]
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //Provider Objekt im Bundle speichen
        outState.putString("provider",ProvidersJsonObjekt);

        super.onSaveInstanceState(outState);
    }
    // [END on_save_instance_state]



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_, menu);
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
}
