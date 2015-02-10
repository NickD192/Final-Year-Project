package com.nicholasdavies.bitalinosensorapplication;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PatientNames extends Activity implements Serializable {
    /**
     * Called when the activity is first created.
     */

    private static int lastArbitraryData = 0;

    private class ListObject  {
        private ListObject(Integer arbitraryDataA, String arbitraryDataB) {
            this.arbitraryDataA = arbitraryDataA;
            this.arbitraryDataB = arbitraryDataB;
        }

        private Integer arbitraryDataA = 0;
        private String arbitraryDataB = "";

        @Override
        public String toString() {
            return arbitraryDataB;
        }

        public int getArbitraryDataA() {
            return arbitraryDataA;
        }

        public String getArbitraryDataB() {
            return arbitraryDataB;
        }

        public ListObject() {
            arbitraryDataA = ++PatientNames.lastArbitraryData;
        }
    }

    List<ListObject> patientnames;
    TextView error;
    ListView resultView;

    Button bBack;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_names);
        StrictMode.enableDefaults(); //STRICT MODE ENABLED
        patientnames = new ArrayList<ListObject>();
        ListView resultView = (ListView) findViewById(R.id.patient_Names);

        Button bBack = (Button) findViewById(R.id.btnBack);

        getData();
        onClickList();

        ArrayAdapter<ListObject> myadapter = new ArrayAdapter<ListObject>(this, R.layout.patient_list_item, R.id.label, patientnames);
        resultView.setAdapter(myadapter);

        bBack.setOnClickListener(new View.OnClickListener(){

            public void onClick(View arg0) {

                Intent openStartingPoint = new Intent(getApplicationContext(),Main.class);
                startActivity(openStartingPoint);

            }
        });

    }

    public void getData() {
        String result = "";
        InputStream isr = null;
        //Actually connecting to the server
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://178.62.115.123/scripts/getpatientnames.php"); //YOUR PHP SCRIPT ADDRESS
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            isr = entity.getContent();
        } catch (Exception e) {
            Log.e("log_tag", "Error in http connection " + e.toString());
            error.setText("Couldn't connect to database");
        }
        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(isr, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            isr.close();

            result = sb.toString();
        } catch (Exception e) {
            Log.e("log_tag", "Error  converting result " + e.toString());
        }

        //parse json data
        try {
            String s = "";
            String sid = "";
            Integer id = 0;
            JSONArray jArray = new JSONArray(result);

            for (int i = 0; i < jArray.length(); i++) {
                JSONObject json = jArray.getJSONObject(i);
                s = "" + json.getString("First Names") + " " + json.getString("Last Names") + "\n";
                sid = "" + json.getString("PatientID");
                id = Integer.parseInt(sid);

                patientnames.add(new ListObject(id, s));

            }

            error.setText(s);

        } catch (Exception e) {
            // TODO: handle exception
            Log.e("log_tag", "Error Parsing Data " + e.toString());
        }

    }


    private void onClickList() {
        final ListView resultView = (ListView) findViewById(R.id.patient_Names);
        resultView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent PatientNamesIntent = new Intent(getApplicationContext(), PatientInfo.class);

                ListObject item = (ListObject) resultView.getItemAtPosition(i);

                PatientNamesIntent.putExtra("PatientID",item.getArbitraryDataA());





                startActivity(PatientNamesIntent);



            }
        });


    }
}
