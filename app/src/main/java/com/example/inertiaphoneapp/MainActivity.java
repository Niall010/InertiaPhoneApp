package com.example.inertiaphoneapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";

    private int frame;
    private long frameTime;
    private float millisPerFrame;
    long elapsedTimeMillis;
    private boolean isSendingData;
    private String ipAddress;
    private int port;
    private int sensorUpdateTime;
    private long refreshFreq;
    private long refreshRate;

    //Init up sensor manager
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorGravity;
    private Sensor sensorGyro;
    private Sensor sensorLinearAcceleration;
    private Sensor sensorRotationVector;

    //Init UI Elements
    private TextView accelerometerText;
    private TextView gravityText;
    private TextView linearAccelerationText;
    private TextView rotationVectorText;
    private TextView gyroText;
    private TextView frameRateText;
    private Button sendDataButton;
    private Button setFrameRateButton;
    private TextView ipAddressText;
    private TextView portText;
    private Button connectionButton;
    private TextView framesText;
    private TextView refreshText;

    private InertiaData inertiaData;

    //flash light signal
    private CameraManager cameraManager;
    private String cameraID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialise Variables
        frame = 0;
        millisPerFrame = 1000/30; //30fps initialised
        frameTime = System.currentTimeMillis();
        isSendingData = false;
        inertiaData = new InertiaData();
        ipAddress = "192.168.1.78";
        port = 6000;
        sensorUpdateTime = 1000; //10 milliseconds
        refreshFreq = 0; //Hz
        refreshRate = System.currentTimeMillis(); //elapsed time per frame

        //Get UI Elements
        accelerometerText = findViewById(R.id.accelerometerText);
        gravityText = findViewById(R.id.gravityText);
        linearAccelerationText = findViewById(R.id.linearAccelerationText);
        rotationVectorText = findViewById(R.id.rotationVectorText);
        gyroText = findViewById(R.id.gyroText);
        sendDataButton = findViewById(R.id.sendDataButton);
        setFrameRateButton = findViewById(R.id.setFrameRateButton);
        frameRateText = findViewById(R.id.frameRateText);
        ipAddressText = findViewById(R.id.ipAddressText);
        portText = findViewById(R.id.portText);
        connectionButton = findViewById(R.id.connectionButton);
        framesText = findViewById(R.id.framesText);
        refreshText = findViewById(R.id.refreshText);

        //Initialise Sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorLinearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        //Create listeners for change of sensors
        sensorManager.registerListener(MainActivity.this, sensorAccelerometer, sensorUpdateTime);
        sensorManager.registerListener(MainActivity.this, sensorGravity, sensorUpdateTime);
        sensorManager.registerListener(MainActivity.this, sensorGyro, sensorUpdateTime);
        sensorManager.registerListener(MainActivity.this, sensorLinearAcceleration, sensorUpdateTime);
        sensorManager.registerListener(MainActivity.this, sensorRotationVector,  sensorUpdateTime);

        //Initialise Camera Flash
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraID = cameraManager.getCameraIdList() [0]; //0 for back camera
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }



        sendDataButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
              isSendingData = !isSendingData;


                if(isSendingData)
              {
                  sendDataButton.setText("Stop Sending Data");

                  try {
                      cameraManager.setTorchMode(cameraID, true);
                  } catch (CameraAccessException e) {
                      e.printStackTrace();
                  }
              }
              else
              {
                  sendDataButton.setText("Send Data");
              }

            }
        });

        setFrameRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                millisPerFrame = 1000/Float.parseFloat(frameRateText.getText().toString());
            }
        });

        connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ipAddress = ipAddressText.getText().toString();
                port = Integer.parseInt(portText.getText().toString());
            }
        });
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onSensorChanged(SensorEvent event) {
        //Assign UI live data#
        Sensor sensor = event.sensor;
        String text = "";
        elapsedTimeMillis = System.currentTimeMillis() - frameTime;

        refreshFreq = (System.currentTimeMillis() - refreshRate);
        refreshRate = System.currentTimeMillis();
        refreshText.setText(String.valueOf(refreshFreq));


        //Get most recent sensor update and assign to class object inertiaData + print
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            text = ("X: " + HelperClass.round(event.values[0], 2) + " Y: " + HelperClass.round(event.values[1], 2) + " Z: " + HelperClass.round(event.values[2], 2));
            inertiaData.setAccelerometerVector(event.values);
            accelerometerText.setText(text);
        } else if (sensor.getType() == Sensor.TYPE_GRAVITY) {
            text = ("X: " + HelperClass.round(event.values[0], 2) + " Y: " + HelperClass.round(event.values[1], 2) + " Z: " + HelperClass.round(event.values[2], 2));
            inertiaData.setGravityVector(event.values);
            gravityText.setText(text);
        } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            text = ("X: " + HelperClass.round(event.values[0], 2) + " Y: " + HelperClass.round(event.values[1], 2) + " Z: " + HelperClass.round(event.values[2], 2));
            inertiaData.setGyroVector(event.values);
            gyroText.setText(text);
        } else if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            inertiaData.setLinearAccelerationVector(event.values);
            text = ("X: " + HelperClass.round(event.values[0], 2) + " Y: " + HelperClass.round(event.values[1], 2) + " Z: " + HelperClass.round(event.values[2], 2));
            linearAccelerationText.setText(text);
        } else if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            inertiaData.setRotationVector(event.values);
            text = ("X: " + HelperClass.round(event.values[0], 2) + " Y: " + HelperClass.round(event.values[1], 2) + " Z: " + HelperClass.round(event.values[2], 2) + " Vector: " + HelperClass.round(event.values[3], 2));
            rotationVectorText.setText(text);
        }


        if(isSendingData == true) {
            if (elapsedTimeMillis > millisPerFrame) {
                frameTime = System.currentTimeMillis();

                BackgroundTask b1 = new BackgroundTask();
                inertiaData.setDisplacement(getDisplacement(0, inertiaData.getLinearAccelerationVector(),millisPerFrame/1000));
                b1.execute("frame " +  frame + " " + inertiaData.convertToString(inertiaData));
                frame++;
                framesText.setText(String.valueOf(frame));
            }
            try {
                cameraManager.setTorchMode(cameraID, false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

    }

    public static float[] getDisplacement(float initialVelocity, float acceleration[], double time)
    {
        //calculated in meters, converted to millimeters
        String strDisplacement =" X: " + String.valueOf( ((initialVelocity*time) + 0.5*(acceleration[0]*(time*time)))*1000  ) +
                                " Y: " + String.valueOf( ((initialVelocity*time) + 0.5*(acceleration[1]*(time*time)))*1000  ) +
                                " Z: " +String.valueOf( ((initialVelocity*time) + 0.5*(acceleration[2]*(time*time)))*1000 );

        float[] displacement = new float[3];
        displacement[0] = (float) (((initialVelocity * time) + 0.5 * (acceleration[0] * (time * time))) * 1000);
        displacement[1] = (float) (((initialVelocity * time) + 0.5 * (acceleration[1] * (time * time))) * 1000);
        displacement[2] = (float) (((initialVelocity * time) + 0.5 * (acceleration[2] * (time * time))) * 1000);

        return displacement;
    }

    @Override
    public void onAccuracyChanged (Sensor sensor,int accuracy){

    }


    class BackgroundTask extends AsyncTask<String,Void,Void>
    {
        Socket s;
        OutputStream os;

        PrintWriter writer;

        @Override
        protected Void doInBackground(String... voids) {
            try {
                String message = voids[0];
                s = new Socket(ipAddress, port);
              //  os = s.getOutputStream();
                writer = new PrintWriter(s.getOutputStream());
                writer.write(String.valueOf(message));
                writer.flush();
                writer.close();

/*
                // Sending to c# sockets
                byte[] toSendBytes = message.getBytes();
                int toSendLen = toSendBytes.length;
                byte[] toSendLenBytes = new byte[4];
                toSendLenBytes[0] = (byte)(toSendLen & 0xff);
                toSendLenBytes[1] = (byte)((toSendLen >> 8) & 0xff);
                toSendLenBytes[2] = (byte)((toSendLen >> 16) & 0xff);
                toSendLenBytes[3] = (byte)((toSendLen >> 24) & 0xff);
                os.write(toSendLenBytes);
                os.write(toSendBytes);
*/

                s.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }





    }

    class InertiaData
    {
        //Vectors
        float accelerometerVector[];
        float gravityVector[];
        float gyroVector[];
        float linearAccelerationVector[];
        float rotationVector[];
        float displacement[];

        public void setAccelerometerVector(float[] accelerometerVector) { this.accelerometerVector = accelerometerVector; }

        public void setGravityVector(float[] gravityVector) { this.gravityVector = gravityVector; }

        public void setGyroVector(float[] gyroVector) { this.gyroVector = gyroVector; }

        public void setLinearAccelerationVector(float[] linearAccelerationVector) { this.linearAccelerationVector = linearAccelerationVector; }

        public void setRotationVector(float[] rotationVector) { this.rotationVector = rotationVector; }

        public float[] getAccelerometerVector() { return accelerometerVector; }

        public float[] getGravityVector() { return gravityVector; }

        public float[] getGyroVector() { return gyroVector; }

        public float[] getLinearAccelerationVector() { return linearAccelerationVector; }

        public float[] getRotationVector() { return rotationVector; }

        public float[] getDisplacement() { return displacement; }

        public void setDisplacement(float[] displacement) { this.displacement = displacement; }

        public String convertToString(InertiaData data)
        {
            String text = "";
            text = ("Accelerometer:" + Arrays.toString(data.getAccelerometerVector()) +
                    "Gravity:" + Arrays.toString(data.getGravityVector()) +
                    "Gyro:" + Arrays.toString(data.getGyroVector()) +
                    "LinearAcceleration:" + Arrays.toString(data.getLinearAccelerationVector()) +
                    "Rotation:" + Arrays.toString(data.getRotationVector()) +
                    "Linear Displacement: " + Arrays.toString(data.getDisplacement())
            );

            return text;
        }
    }

}