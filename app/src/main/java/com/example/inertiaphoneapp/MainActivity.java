package com.example.inertiaphoneapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";

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
    private Button sendDataButton;

    //Vectors
    float accelerometerVector[];
    float gravityVector[];
    float gyroVector[];
    float linearAccelerationVector[];
    float rotationVector[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get UI Elements
        accelerometerText = findViewById(R.id.accelerometerText);
        gravityText = findViewById(R.id.gravityText);
        linearAccelerationText = findViewById(R.id.linearAccelerationText);
        rotationVectorText = findViewById(R.id.rotationVectorText);
        gyroText = findViewById(R.id.gyroText);
        sendDataButton = findViewById(R.id.sendDataButton);

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
                sendData(v);
            }
        });


    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        //Assign UI live data#
        Sensor sensor = event.sensor;
        String text;

        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            text = ("X: " + round(event.values[0],2) + " Y: " + round( event.values[1],2) + " Z: " + round(event.values[2],2));
            accelerometerText.setText(text);
        } else if (sensor.getType() == Sensor.TYPE_GRAVITY) {
            text = ("X: " + round(event.values[0],2) + " Y: " + round( event.values[1],2) + " Z: " + round(event.values[2],2));
            gravityText.setText(text);
        } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            text = ("X: " + round(event.values[0],2) + " Y: " + round( event.values[1],2) + " Z: " + round(event.values[2],2));
            gyroText.setText(text);
        } else if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            text = ("X: " + round(event.values[0],2) + " Y: " + round( event.values[1],2) + " Z: " + round(event.values[2],2));
            linearAccelerationText.setText(text);
        } else if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            text = ("X: " + round(event.values[0],2) + " Y: " + round( event.values[1],2) + " Z: " + round(event.values[2],2) + " Vector: " + round(event.values[3],2));
            rotationVectorText.setText(text);
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




    public void sendData(View v)
    {
        String message = accelerometerText.getText().toString();

        BackgroundTask b1 = new BackgroundTask();
        b1.execute(message);
    }

    class BackgroundTask extends AsyncTask<String,Void,Void>
    {

        Socket s;
        PrintWriter writer;

        @Override
        protected Void doInBackground(String... voids) {
            try {
                String message = voids[0];
                s = new Socket(" 192.168.1.78", 6000);
                writer = new PrintWriter(s.getOutputStream());
                writer.write(message);
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

}