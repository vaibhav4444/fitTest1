package com.vvorld.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataTypeCreateRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataTypeResult;
import com.google.android.gms.fitness.result.SessionReadResult;

import java.util.concurrent.TimeUnit;


//http://stackoverflow.com/questions/28234525/fetching-google-fit-data-into-android-application
//http://stackoverflow.com/questions/34088223/google-fit-custom-data-type?rq=1
//http://stackoverflow.com/questions/34088223/google-fit-custom-data-type?rq=1\
//https://www.programmableweb.com/news/how-to-develop-your-first-google-fit-app/how-to/2015/02/19

public class MainActivity extends AppCompatActivity {
    DataTypeCreateRequest request;
    String TAG = "main";
    PendingResult<DataTypeResult> pendingResult;
    DataType dataType;
    private GoogleApiClient mClient = null;
    DataSource climbDataSource;
    DataSet climbDataSet;
    Session session;
    private long timestamp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        request = new DataTypeCreateRequest.Builder()
                // The prefix of your data type name must match your app's package name
                .setName("com.vvorld.myapplication.data1")
                // Add some custom fields, both int and float
                .addField("field1", Field.FORMAT_INT32)
                .addField("field2", Field.FORMAT_FLOAT)
                // Add some common fields
                .addField(Field.FIELD_ACTIVITY)
                .build();
        timestamp = System.currentTimeMillis();
        session = new Session.Builder()
                .setName("SESSION_NAME")
                .setIdentifier(getString(R.string.app_name) + " " + System.currentTimeMillis())
                .setDescription("Running in Segments")
                .setStartTime(timestamp, TimeUnit.MILLISECONDS)
                .setEndTime((timestamp + (DateUtils.MINUTE_IN_MILLIS * 50)), TimeUnit.MILLISECONDS)
                .setActivity(FitnessActivities.RUNNING)
                .build();
    }
    public void onClick(View v){
        int id = v.getId();
        if(id == R.id.connect){
            buildFitnessClient();
        }
        else if(id == R.id.put){
             pendingResult =
                    Fitness.ConfigApi.createCustomDataType(mClient, request);
            pendingResult.setResultCallback(
                    new ResultCallback<DataTypeResult>() {
                        @Override
                        public void onResult(DataTypeResult dataTypeResult) {
                             dataType = dataTypeResult.getDataType();

                            Status status =dataTypeResult.getStatus();
                            if(status.isSuccess()){
                                Log.i("tag", "tag");
                            }
                            else{
                                Log.i("tag", "tag");
                            }
                            // Retrieve the created data type
                            DataType customType = dataTypeResult.getDataType();
                            // Use this custom data type to insert data in your app
                            // (see other examples)
                            Log.i("tag", "tag");
                        }
                    }
            );
        }
        else if(id == R.id.get){
            pendingResult =
                    Fitness.ConfigApi.readDataType(mClient, "com.vvorld.myapplication.data1");
            pendingResult.setResultCallback(
                    new ResultCallback<DataTypeResult>() {
                        @Override
                        public void onResult(DataTypeResult dataTypeResult) {
                            dataTypeResult.getStatus();
                            // Retrieve the created data type
                            dataType = dataTypeResult.getDataType();
                            Status status =dataTypeResult.getStatus();
                            if(status.isSuccess()){
                                Log.i("tag", "tag");
                            }
                            else{
                                Log.i("tag", "tag");
                            }
                            // Use this custom data type to insert data in your app
                            // (see other examples)
                            Log.i("tag", "tag");
                        }
                    }
            );

        }
        else if(id == R.id.saveDataCloud){
            if(climbDataSource == null) {
                buildDataSet(dataType);
            }
            else{
                saveSession();
            }
        }
        else if(id == R.id.retreiveData){
            readSession();
        }

    }
    private void buildFitnessClient() {
        if (mClient == null ) {
            mClient = new GoogleApiClient.Builder(this)
                    .addApi(Fitness.SENSORS_API)
                    .addApi(Fitness.CONFIG_API)
                    .addApi(Fitness.SESSIONS_API)
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                    .addConnectionCallbacks(
                            new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(Bundle bundle) {
                                    Log.i(TAG, "Connected!!!");
                                    // Now you can make calls to the Fitness APIs.
                                    //findFitnessDataSources();
                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                    // If your connection to the sensor gets lost at some point,
                                    // you'll be able to determine the reason and react to it here.
                                    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                        Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                    } else if (i
                                            == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                        Log.i(TAG,
                                                "Connection lost.  Reason: Service Disconnected");
                                    }
                                }
                            }
                    )
                    .enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {
                            Log.i(TAG, "Google Play services connection failed. Cause: " +
                                    result.toString());
                            Toast.makeText(
                                    MainActivity.this,
                                    "Exception while connecting to Google Play services: " +
                                            result.getErrorMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    })
                    .build();
        }
    }
    public void buildDataSet(DataType dataType){
        climbDataSource = new DataSource.Builder()
                .setAppPackageName(MainActivity.this.getPackageName())
                .setDataType(dataType)
                .setName("SAMPLE_SESSION_NAME")
                .setType(DataSource.TYPE_RAW)
                .build();
        // Create a data point for a data source that provides
        DataPoint dataPoint = DataPoint.create(climbDataSource);
        // Set values for the data point
        // This data type has one custom fields (int) and a common field
        //tricky way to set single int
        dataPoint.getValue(dataType.getFields().get(0)).setInt(8);
        dataPoint.getValue(dataType.getFields().get(1)).setFloat(8.99f);
        dataPoint.setTimestamp(timestamp, TimeUnit.MILLISECONDS);

        //dataPoint.setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        //tricky way to set activity, not at all how the non-working google sample code is set up
        //FitnessActivities.setValue(dataPoint, FitnessActivities.STAIR_CLIMBING);
        climbDataSet = DataSet.create(climbDataSource);
        climbDataSet.add(dataPoint);
        //Fitness.SensorsApi.add()

        saveSession();


    }
    private void saveSession(){
        SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                .setSession(session)
                .addDataSet(climbDataSet)
                .build();

        PendingResult<Status> pendingResult =
                Fitness.SessionsApi.insertSession(mClient, insertRequest);

        pendingResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if( status.isSuccess() ) {
                    Log.i("Tuts+", "successfully inserted running session");
                } else {
                    Log.i("Tuts+", "Failed to insert running session: " + status.getStatusMessage());
                }
            }
        });
    }
    private void readSession(){
        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .read(dataType)
                .setTimeInterval(timestamp, (System.currentTimeMillis() + (DateUtils.MINUTE_IN_MILLIS * 50)), TimeUnit.MILLISECONDS)
                .setSessionName("SESSION_NAME")
                .build();

        PendingResult<SessionReadResult> sessionReadResult =
                Fitness.SessionsApi.readSession(mClient, readRequest);
        sessionReadResult.setResultCallback(new ResultCallback<SessionReadResult>() {
            @Override
            public void onResult(SessionReadResult sessionReadResult) {
                if (sessionReadResult.getStatus().isSuccess()) {
                    Log.i("Tuts+", "Successfully read session data");
                    for (Session session : sessionReadResult.getSessions()) {
                        Log.i("Tuts+", "Session name: " + session.getName());
                        for (DataSet dataSet : sessionReadResult.getDataSet(session)) {
                            for (DataPoint dataPoint : dataSet.getDataPoints()) {
                                Value value = dataPoint.getValue(dataType.getFields().get(0));
                               int i = value.asInt();

                                Log.i("Tuts+", "Speed: " + "tag");
                            }
                        }
                    }
                } else {
                    Log.i("Tuts+", "Failed to read session data");
                }
            }
        });
    }
}
