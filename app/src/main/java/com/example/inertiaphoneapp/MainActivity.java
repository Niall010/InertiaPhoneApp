package com.example.inertiaphoneapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import java.net.Socket;
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
    float[] previousDisplacement = new float[3];
    float[] initialVelocity = new float[3];

    //Init up sensor manager
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorGravity;
    private Sensor sensorGyro;
    private Sensor sensorLinearAcceleration;
    private Sensor sensorRotationVector;
    private Sensor sensorMagneticField;

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
    private TextView magnetometerText;
    private Button arTrackButton;

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
        ipAddress = "192.168.1.101";
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
        magnetometerText = findViewById(R.id.magnetometerText);
        arTrackButton = findViewById(R.id.arTrackButton);

        //Initialise Sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorLinearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //Create listeners for change of sensors
        sensorManager.registerListener(MainActivity.this, sensorAccelerometer, sensorUpdateTime);
        sensorManager.registerListener(MainActivity.this, sensorGravity, sensorUpdateTime);
        sensorManager.registerListener(MainActivity.this, sensorGyro, sensorUpdateTime);
        sensorManager.registerListener(MainActivity.this, sensorLinearAcceleration, sensorUpdateTime);
        sensorManager.registerListener(MainActivity.this, sensorRotationVector,  sensorUpdateTime);
        sensorManager.registerListener(MainActivity.this, sensorMagneticField,  sensorUpdateTime);

/*
        //Initialise Camera Flash
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraID = cameraManager.getCameraIdList() [0]; //0 for back camera
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
 */

        arTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ARTrackActivity.class);
                startActivity(intent);

            }
        });



        sendDataButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
              isSendingData = !isSendingData;


                if(isSendingData)
              {
                  sendDataButton.setText("Stop Sending Data");
/*
                  try {
                      cameraManager.setTorchMode(cameraID, true);
                  } catch (CameraAccessException e) {
                      e.printStackTrace();
                  }

 */
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
        if(refreshFreq  != 0)
        {
            refreshFreq = 1/refreshFreq;
            refreshText.setText(String.valueOf(refreshFreq) + "Hz");
        }
        else
        {
            refreshText.setText("null");
        }
        refreshRate = System.currentTimeMillis();


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
        } else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {   inertiaData.setMagnetometerVector(event.values);
            text = ("X: " + HelperClass.round(event.values[0], 2) + " Y: " + HelperClass.round(event.values[1], 2) + " Z: " + HelperClass.round(event.values[2], 2));
            magnetometerText.setText(text);
        }


        if(isSendingData == true) {
            if (elapsedTimeMillis > millisPerFrame) {

                // Rotation matrix based on current readings from accelerometer and magnetometer.
                final float[] rotationMatrix = new float[9];
                SensorManager.getRotationMatrix(rotationMatrix, null,
                        inertiaData.accelerometerVector, inertiaData.magnetometerVector);

                // Express the updated rotation matrix as three orientation angles.
                final float[] orientationAngles = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientationAngles);
                                                                        //Blender Script Format
                String orientationString =("( 0, 0, 0, "                //Position First    // GOAL - ( 0.56,0.13,-0.83, -4.20,-2.58,-3.48 ),
                        + String.valueOf(orientationAngles[0]) + ", "   // Orientation Second
                        + String.valueOf(orientationAngles[1]) + ", "
                        + String.valueOf(orientationAngles[2]) + " ),");


                frameTime = System.currentTimeMillis();
               // initialVelocity = getVelocity(initialVelocity,inertiaData.getLinearAccelerationVector(),millisPerFrame);
               // inertiaData.setDisplacement(HelperClass.addVectors(previousDisplacement, getDisplacement(initialVelocity, inertiaData.getLinearAccelerationVector(),millisPerFrame)));

                //Send data to windows java exe application
                BackgroundTask b1 = new BackgroundTask();
                b1.execute(orientationString);
                // b1.execute("frame " +  frame + " " + inertiaData.convertToString(inertiaData));
                //b1.execute(Arrays.toString(getDisplacement(initialVelocity, inertiaData.getLinearAccelerationVector(),millisPerFrame)));
                //b1.execute(inertiaData.convertToBlenderString(inertiaData));

                //Set variables for next pass
                previousDisplacement = inertiaData.getDisplacement();
                frame++;
                framesText.setText(String.valueOf(frame));
            }
/*
            try {
                cameraManager.setTorchMode(cameraID, false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
*/
        }

    }

    public static float[] getDisplacement(float initialVelocity[], float acceleration[], double time)
    {
        //calculated in meters
        //convert time to seconds from milliseconds

        time = time/1000;

        String strDisplacement =" X: " + String.valueOf( ((initialVelocity[0]*time) + 0.5*(acceleration[0]*(time*time)))  ) +
                                " Y: " + String.valueOf( ((initialVelocity[1]*time) + 0.5*(acceleration[1]*(time*time)))  ) +
                                " Z: " +String.valueOf( ((initialVelocity[2]*time) + 0.5*(acceleration[2]*(time*time))) );

        float[] displacement = new float[3];
        displacement[0] = (float) (((initialVelocity[0] * time) + 0.5 * (acceleration[0] * (time * time))));
        displacement[1] = (float) (((initialVelocity[1] * time) + 0.5 * (acceleration[1] * (time * time))));
        displacement[2] = (float) (((initialVelocity[2] * time) + 0.5 * (acceleration[2] * (time * time))));

        return displacement;
    }

    public static float[] getVelocity(float initialVelocity[], float acceleration[], double time)
    {
        //calculated in meters
        //convert time to seconds from milliseconds
        time = time/1000;

        float[] finalVelocity = new float[3];
        finalVelocity[0] = (float) (initialVelocity[0] + acceleration[0]*time);
        finalVelocity[1] = (float) (initialVelocity[1] + acceleration[1]*time);
        finalVelocity[2] = (float) (initialVelocity[2] + acceleration[2]*time);

        return finalVelocity;
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
        float[] accelerometerVector;
        float[] gravityVector;
        float[] gyroVector;
        float[] linearAccelerationVector;
        float[] rotationVector;
        float[] displacement;
        float[] magnetometerVector;

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

        public float[] getMagnetometerVector() { return magnetometerVector; }

        public void setMagnetometerVector(float[] magnetometerVector) { this.magnetometerVector = magnetometerVector; }

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

        public String convertToBlenderString(InertiaData data)
        {
            String text = "";
            //text = (Arrays.toString(data.getDisplacement()) + Arrays.toString(data.getRotationVector()));
            text = "( " + Arrays.toString(data.getDisplacement()) + " , " + Arrays.toString(data.getRotationVector()) + " ),";
            return text;
        }
    }

}

//  desired xyz transforms and rotations format for blender //
//      ( 0.56,0.13,-0.83, -4.20,-2.58,-3.48 ),
//      ( 0.74,0.19,-0.63, -4.40,-1.95,-3.67 ),
//      ( 0.01,0.03,-0.57, 0,-1.61,-3.64 )
