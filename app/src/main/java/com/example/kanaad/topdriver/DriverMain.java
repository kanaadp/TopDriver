package com.example.kanaad.topdriver;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;


public class DriverMain extends ActionBarActivity implements SensorEventListener{
    private long lastUpdate = 0;

    float last_x = 0;
    float last_y = 0;
    float last_z = 0;

    float start_x, start_y, start_z;

    float target_x = 0;
    float target_y = 0;
    float target_z = 0;

    private static final String TAG = "MainActivity";

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private int motion_target = 0;

    boolean driving = false;

    private final String[] MOTION_TARGET_COMMANDS = {"", "Turn left!", "Turn right!", "Increase your speed!", "Decrease your speed!"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);
        Log.w(TAG, "Creating main activity.");
        int newUiOptions = getWindow().getDecorView().getSystemUiVisibility();


        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        }
        else{
            Log.e(TAG, "You cannot play this game. No Accelerometers on the device.");
        }




        ((Button) findViewById(R.id.button2)).setClickable(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_driver_main, menu);
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];


            last_x = x;
            last_y = y;
            last_z = z;

            if (curTime - lastUpdate > 400) {
                lastUpdate = curTime;


                if (Math.abs(target_x - x) < 1.5 && Math.abs(target_y - y) < 1.5 && Math.abs(target_z - z) < 1.5) {
                    reachedMotionTarget();
                }
                else if( (motion_target  == 1  || motion_target == 2 )&& Math.abs(target_y - y) < 1.5){
                    reachedMotionTarget();
                } else {
                    Log.d(TAG, "Not at target");
                    Log.d(TAG, "Y = " + y);
                }
            }
            }

    }

    public void reachedMotionTarget(){
        if(driving) {
            Random rand = new Random();

            Log.d(TAG, "At target");
            if (motion_target != 0) {
                //Turn green
                motion_target = 0;

            } else {
                motion_target = rand.nextInt(4) + 1;
            }
            generateMotionThresholds(motion_target);
            ((TextView) findViewById(R.id.directions)).setText(MOTION_TARGET_COMMANDS[motion_target]);

        }
    }

    public void generateMotionThresholds(int motion_target){
        final float TURN_VALUE = 6;
        final float INCREASE_VALUE = 9;
        final float DECREASE_VALUE = 9;




        switch (motion_target){
            case 0:
                target_x = start_x;
                target_y = start_y;
                target_z = start_z;
                break;
            case 1:
                // Turn Left
                target_y = TURN_VALUE;
                if(start_x > 0)
                    target_y *= -1;
                target_x = start_x;
                target_z = start_z;
            break;
            case 2:
                target_y = TURN_VALUE;
                if(start_x < 0)
                    target_y *= -1;
                target_x = start_x;
                target_z = start_z;
            break;
            case 3:
                target_x = 0;
                target_y = 0;
                target_z = INCREASE_VALUE;

            break;
            case 4:
                target_x = DECREASE_VALUE;
                target_y = 0;
                target_z = 0;
                if(start_y > 0)
                    target_y *= -1;
            break;


        }
        Log.d(TAG, "X = " + target_x);
        Log.d(TAG, "Y = " + target_y);
        Log.d(TAG, "Z = " + target_z);
    }

    public void start(View view){

        Log.d(TAG, "Starting");
        ((Button) view).setClickable(false);
        ((Button) findViewById(R.id.button2)).setClickable(true);

        start_x = last_x;
        start_y = last_y;
        start_z = last_z;
        generateMotionThresholds(0);
        driving = true;

    }
    public void stop(View view){
        Log.d(TAG, "Stopping");
        ((Button) view).setClickable(false);
        ((Button) findViewById(R.id.button)).setClickable(true);
        mSensorManager.unregisterListener(this, mSensor);

        driving = false;
    }




    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }
}
