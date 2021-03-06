package com.nicholasdavies.bitalinosensorapplication;

import android.app.Activity;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Input Form For entering patient information
 *
 * @author Nick Davies
 */
public class CreatePatient extends Activity {

    EditText firstName, lastName, dateOfBirth, patientSymptoms;

    Button bCreate, bCancel;

    boolean firstNameCheck, lastNameCheck, dateCheck;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.create_patient);
        StrictMode.enableDefaults();

        firstName = (EditText) findViewById(R.id.firstName);

        lastName = (EditText) findViewById(R.id.lastName);

        dateOfBirth = (EditText) findViewById(R.id.dateOfBirth);

        patientSymptoms = (EditText) findViewById(R.id.symptoms);

        bCancel = (Button) findViewById(R.id.btnCancel);

        bCreate = (Button) findViewById(R.id.btnSubmit);

        bCreate.setOnClickListener(new View.OnClickListener() {
            InputStream isr = null;

            public void onClick(View arg0) {
                /** Extracts Patient Information from input form */
                String firstname = "" + firstName.getText().toString();
                String lastname = "" + lastName.getText().toString();
                String dateofbirth = "" + dateOfBirth.getText().toString();
                String symptoms = "" + patientSymptoms.getText().toString();


                /**  Input Validation */
                if (!firstname.equals("") && !lastname.equals("")) {


                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);


                    nameValuePairs.add(new BasicNameValuePair("firstname", firstname));
                    nameValuePairs.add(new BasicNameValuePair("lastname", lastname));
                    nameValuePairs.add(new BasicNameValuePair("dateofbirth", dateofbirth));
                    nameValuePairs.add(new BasicNameValuePair("symptoms", symptoms));

                    /** Connects to server */
                    try {
                        HttpClient httpclient = new DefaultHttpClient();
                        String baseURL = Utilities.getURL(getApplicationContext());
                        HttpPost httppost = new HttpPost(baseURL + "createnewpatient.php");
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        HttpResponse response = httpclient.execute(httppost);
                        HttpEntity entity = response.getEntity();
                        isr = entity.getContent();

                        String msg = "Data Entered Successfully";
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    } catch (ClientProtocolException e) {
                        Log.e("ClientProtocal", "Log_tag");
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.e("Log_tag", "IOException");
                        Toast.makeText(arg0.getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(arg0.getContext(), "Input is invalid", Toast.LENGTH_SHORT).show();
                }


            }
        });


        firstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                /**  First Name Validation */
                if (firstName.getText().toString().length() == 0) {
                    firstName.setError("First Name is required!");
                    firstNameCheck = true;
                } else
                    firstNameCheck = false;


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        lastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                /**  Second Name Validation */
                if (lastName.getText().toString().length() == 0) {
                    lastName.setError("Last Name is Required!");
                    lastNameCheck = true;
                } else
                    lastNameCheck = false;


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        dateOfBirth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                /**  Date Validation */
                if (dateOfBirth.getText().toString().length() == 0) {
                    dateOfBirth.setError("Date of Birth is required");
                    dateCheck = true;
                } else
                    dateCheck = false;


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

}



