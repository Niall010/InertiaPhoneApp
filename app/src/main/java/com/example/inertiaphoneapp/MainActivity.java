package com.example.inertiaphoneapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";

    private int frame;
    private long frameTime;
    private float millisPerFrame;
    long elapsedTimeMillis;
    private boolean isSendingData;

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

    private InertiaData inertiaData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frame = 0;
        millisPerFrame = 1000/60; //60fps initialised
        frameTime = System.currentTimeMillis();
        isSendingData = false;
        inertiaData = new InertiaData();

        //Get UI Elements
        accelerometerText = findViewById(R.id.accelerometerText);
        gravityText = findViewById(R.id.gravityText);
        linearAccelerationText = findViewById(R.id.linearAccelerationText);
        rotationVectorText = findViewById(R.id.rotationVectorText);
        gyroText = findViewById(R.id.gyroText);
        sendDataButton = findViewById(R.id.sendDataButton);
        setFrameRateButton = findViewById(R.id.setFrameRateButton);
        frameRateText = findViewById(R.id.frameRateText);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorLinearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        sensorManager.registerListener(MainActivity.this, sensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(MainActivity.this, sensorGravity, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(MainActivity.this, sensorGyro, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(MainActivity.this, sensorLinearAcceleration, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(MainActivity.this, sensorRotationVector, SensorManager.SENSOR_DELAY_FASTEST);


        sendDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              isSendingData = !isSendingData;
            }
        });

        setFrameRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                millisPerFrame = 1000/Long.parseLong(frameRateText.getText().toString());
            }
        });

    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        //Assign UI live data#
        Sensor sensor = event.sensor;
        String text = "";
        elapsedTimeMillis = System.currentTimeMillis() - frameTime;


        //Get most recent sensor update and assign to class object inertiaData + print
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            text = ("X: " + round(event.values[0], 2) + " Y: " + round(event.values[1], 2) + " Z: " + round(event.values[2], 2));
            inertiaData.setAccelerometerVector(event.values);
            accelerometerText.setText(text);
        } else if (sensor.getType() == Sensor.TYPE_GRAVITY) {
            text = ("X: " + round(event.values[0], 2) + " Y: " + round(event.values[1], 2) + " Z: " + round(event.values[2], 2));
            inertiaData.setGravityVector(event.values);
            gravityText.setText(text);
        } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            text = ("X: " + round(event.values[0], 2) + " Y: " + round(event.values[1], 2) + " Z: " + round(event.values[2], 2));
            inertiaData.setGyroVector(event.values);
            gyroText.setText(text);
        } else if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            inertiaData.setLinearAccelerationVector(event.values);
            text = ("X: " + round(event.values[0], 2) + " Y: " + round(event.values[1], 2) + " Z: " + round(event.values[2], 2));
            linearAccelerationText.setText(text);
        } else if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            inertiaData.setRotationVector(event.values);
            text = ("X: " + round(event.values[0], 2) + " Y: " + round(event.values[1], 2) + " Z: " + round(event.values[2], 2) + " Vector: " + round(event.values[3], 2));
            rotationVectorText.setText(text);
        }

        if(isSendingData == true) {
            //send data every 16 milliseconds, therefore 999 every second almost 60fps
            if (elapsedTimeMillis > frame) {
                frameTime = System.currentTimeMillis();
                BackgroundTask b1 = new BackgroundTask();
                b1.execute("frame " +  frame + " " + inertiaData.convertToString(inertiaData));
                frame++;
            }
        }
    }

    @Override
    public void onAccuracyChanged (Sensor sensor,int accuracy){

    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }



    class BackgroundTask extends AsyncTask<String,Void,Void>
    {
        Socket s;
        PrintWriter writer;

        @Override
        protected Void doInBackground(String... voids) {
            try {
                String message = voids[0];
                s = new Socket("192.168.1.78", 6000);
                writer = new PrintWriter(s.getOutputStream());
                writer.write(String.valueOf(message));
                writer.flush();
                writer.close();
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

        public String convertToString(InertiaData data)
        {
            String text = "";
            text = ("Accelerometer:" + Arrays.toString(data.getAccelerometerVector()) +
                    "Gravity:" + Arrays.toString(data.getGravityVector()) +
                    "Gyro:" + Arrays.toString(data.getGyroVector()) +
                    "LinearAcceleration:" + Arrays.toString(data.getLinearAccelerationVector()) +
                    "Rotation:" + Arrays.toString(data.getRotationVector())
            );

            return text;
        }
    }

}