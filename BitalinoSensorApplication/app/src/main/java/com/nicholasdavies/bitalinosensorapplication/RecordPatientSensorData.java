package com.nicholasdavies.bitalinosensorapplication;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.nicholasdavies.bitalinosensorapplication.BITalinoDevice;
import com.nicholasdavies.bitalinosensorapplication.BITalinoFrame;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import retrofit.RestAdapter;
import retrofit.client.Response;


public class RecordPatientSensorData extends Activity {

    private static final String TAG = "LiveInfo";
    private static final boolean UPLOAD = false;
    ArrayList<String> patientNames;
    ListView mainList;
    ArrayAdapter adapter;
    Button bCancel;
    Button bUpload;
    File root = android.os.Environment.getExternalStorageDirectory();
    File dir = new File(root.getAbsolutePath() + "/Temp");
    String outputDir = root.getAbsolutePath() + "/Temp/";
    String outputFile = "temp.txt";
    InputStream isr = null;


    private class GraphViewWrapper implements GraphViewDataInterface {

        private int mX = 0;
        private int mY = 0;

        public GraphViewWrapper(int x, int y) {
            System.out.println("X: " + x + "| Y: " + y);
            mX = x;
            mY = y;
        }

        @Override
        public double getX() {
            return mX;
        }

        @Override
        public double getY() {
            return mY;
        }
    }

    /*
     * http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html
     * #createRfcommSocketToServiceRecord(java.util.UUID)
     *
     * "Hint: If you are connecting to a Bluetooth serial board then try using the
     * well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB. However if you
     * are connecting to an Android peer then please generate your own unique
     * UUID."
     */
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean testInitiated = false;

    private int xVal = 0;

    private GraphViewSeries liveGraph;

    private ArrayList<GraphViewWrapper> graphViewWrapperList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_patient_sensor_data);
        StrictMode.enableDefaults(); //STRICT MODE ENABLED
        liveGraph = new GraphViewSeries(new GraphView.GraphViewData[]{});
        bCancel = (Button) findViewById(R.id.btnCancel);
        bUpload = (Button) findViewById(R.id.btnUpload);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        final int patientID = bundle.getInt("PatientID");
        final int sensorType = bundle.getInt("sensorType");
        Calendar c = Calendar.getInstance();
        final String Notes = "";
        Toast.makeText(getApplicationContext(), "Connecting to the Bitalino Device", Toast.LENGTH_LONG).show();

        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        final String dateFormatted = date.format(c.getTime());


        graphViewWrapperList = new ArrayList<GraphViewWrapper>();

        final TestAsyncTask MySyncTask = new TestAsyncTask();
        bCancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {

                MySyncTask.cancel(true);
                Intent openStartingPoint = new Intent("com.nicholasdavies.bitalinosensorapplication.MAIN");
                startActivity(openStartingPoint);

            }

        });

        bUpload.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {

                MySyncTask.cancel(true);


                String sensorData = readSensorFile();


                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);

                nameValuePairs.add(new BasicNameValuePair("patientID", Integer.toString(patientID)));
                nameValuePairs.add(new BasicNameValuePair("date", dateFormatted));
                nameValuePairs.add(new BasicNameValuePair("sensorfilename", sensorData));
                nameValuePairs.add(new BasicNameValuePair("sensortype", Integer.toString(sensorType)));
                nameValuePairs.add(new BasicNameValuePair("notes", Notes));

                //Actually connecting to the server
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost("http://178.62.115.123/scripts/createnewsensordata.php"); //YOUR PHP SCRIPT ADDRESS
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    isr = entity.getContent();

                    String msg = "Data Entered Successfully Returning to Main Menu";
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(3500); //This closes the activity once the toast has finished displaying
                                RecordPatientSensorData.this.finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    Intent openStartingPoint = new Intent("com.nicholasdavies.bitalinosensorapplication.MAIN");
                    startActivity(openStartingPoint);

                } catch (ClientProtocolException e) {
                    Log.e("ClientProtocal", "Log_tag");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e("Log_tag", "IOException");
                    e.printStackTrace();
                }


                //This is where the upload code will go
                //1. Change tempory file name to patientID+random integers
                //2. Upload file to database along with all relevant information.
                //3. Pass to New Screen
//                Intent openStartingPoint = new Intent("com.nicholasdavies.bitalinosensorapplication.MAIN");
//                startActivity(openStartingPoint);

            }

        });


        GraphView graphView = new LineGraphView(this, "Patient Data");
        graphView.setScrollable(true);
        graphView.addSeries(liveGraph);
        graphView.setDisableTouch(false);
        graphView.setManualMaxY(true);
        graphView.setManualMinY(true);
        graphView.setManualYMaxBound(1000);
        graphView.setManualYMinBound(0);

        LinearLayout layout = (LinearLayout) findViewById(R.id.live_graph);
        layout.addView(graphView);

        if (!testInitiated)
            MySyncTask.execute();
    }

    public String readSensorFile() {


        File file = new File(outputDir + outputFile);
        StringBuilder readData = new StringBuilder();
        String data = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                readData.append(line);
                readData.append('\n');
            }
            br.close();
        } catch (IOException e) {

        }

        //Con
        data = readData.toString();


        return data;

    }


    private class TestAsyncTask extends AsyncTask<Void, String, Void> {
        ArrayAdapter<String> adapter;
        private BluetoothDevice dev = null;
        private BluetoothSocket sock = null;
        private InputStream is = null;
        private OutputStream os = null;
        private BITalinoDevice bitalino;


        // ArrayAdapter<String> adapter = new ArrayAdapter<String>()

        @Override
        protected Void doInBackground(Void... paramses) {
            try {
                // Let's get the remote Bluetooth device
                final String remoteDevice = "98:D3:31:B1:83:A4";

                final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                dev = btAdapter.getRemoteDevice(remoteDevice);

                Log.d(TAG, "Stopping Bluetooth discovery.");
                btAdapter.cancelDiscovery();

                sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
                sock.connect();
                testInitiated = true;

                bitalino = new BITalinoDevice(10, new int[]{0});
                //  publishProgress("Connecting to BITalino [" + remoteDevice + "]..");
                bitalino.open(sock.getInputStream(), sock.getOutputStream());
                // publishProgress("Connected.");

                // get BITalino version
                // publishProgress("Version: " + bitalino.version());
                // publishProgress("Analogue Value" + );

                // start acquisition on predefined analog channels
                bitalino.start();

                // read until task is stopped
                int counter = 0;
                while (!isCancelled()) {
                    final int numberOfSamplesToRead = 1;

                    BITalinoFrame[] frames = bitalino.read(numberOfSamplesToRead);

                    if (UPLOAD) {
                        // prepare reading for upload
                        BITalinoReading reading = new BITalinoReading();
                        reading.setTimestamp(System.currentTimeMillis());
                        reading.setFrames(frames);
                        // instantiate reading service client
                        RestAdapter restAdapter = new RestAdapter.Builder()
                                .setEndpoint("http://server_ip:8080/bitalino")
                                .build();
                        ReadingService service = restAdapter.create(ReadingService.class);
                        // upload reading
                        Response response = service.uploadReading(reading);
                        assert response.getStatus() == 200;
                    }

                    // present data in screen
                    for (BITalinoFrame frame : frames)
                        //publishProgress(frame.toString());
                        publishProgress(Integer.toString(frame.getAnalog(0)));


                    counter++;
                }

                // trigger digital outputs
                // int[] digital = { 1, 1, 1, 1 };
                // device.trigger(digital);
            } catch (Exception e) {
                Log.e(TAG, "There was an error.", e);
            }

            return null;
        }

        @Override
        protected void onPreExecute() {


            if (!dir.isDirectory()) {
                dir.mkdir();
            }

            try {
                File tempFile = new File(outputDir + outputFile);
                //Deletes file first before creating a new one;
                tempFile.delete();
                tempFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }


        @Override
        protected void onProgressUpdate(String... values) {

            if (values.length > 0) {
                try {
                    try {
                        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputDir + outputFile, true)));
                        out.println(values[0]);
                        out.close();
                    } catch (IOException e) {

                    }

                    int yVal = Integer.parseInt(values[0]);

                    if (yVal < 1000 && yVal > 0) {
                        if (graphViewWrapperList.size() > 20)
                            graphViewWrapperList.remove(0);
                        graphViewWrapperList.add(new GraphViewWrapper(++xVal, yVal));
                        liveGraph.resetData(graphViewWrapperList.toArray(new GraphViewWrapper[0]));
                    }
                } catch (NumberFormatException e) {

                }
            }
        }

        @Override
        protected void onCancelled() {
            // stop acquisition and close bluetooth connection
            try {

                bitalino.stop();
                publishProgress("BITalino is stopped");

                sock.close();
                publishProgress("And we're done! :-)");
            } catch (Exception e) {
                Log.e(TAG, "There was an error.", e);
            }
        }

    }

}