package com.example.soundtime;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 42;
    private static  final long START_TIME_IN_MILLIS = 60000;
    MediaPlayer player;
    private static final String TAG = "MainActivity";
    private static long startTime=60000;
    float volume = 0;
    private TextView mtextCount;
    private CountDownTimer mcountDownTimer;
    private boolean mTimerRunning;
    private long timeLeft;
    private Button mStart;
    private Button mPause;
    private Button mStop;
    private Button mSet;
    private Button mSetTime;
    Uri uri=null;
    AudioManager am ;
    int volume_level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mtextCount= (TextView)findViewById(R.id.textCount);
        mStart=findViewById(R.id.button3);
        mPause=findViewById(R.id.button);
        mStop=findViewById(R.id.button4);
        mSet=findViewById(R.id.button2);
        uri=null;

        getvolume();
        resetTimer();




        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTimerRunning){

                    pause();
                }
                else{

                    play();
                }
            }
        });
        System.out.println("made it here");

        mPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause();
            }
        });

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });

        mSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFile(view);
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        updateCountDownText();
    }

    public void getvolume(){
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        volume_level= am.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public void show()
    {

        final Dialog d = new Dialog(MainActivity.this);
        d.setTitle("NumberPicker");
        d.setContentView(R.layout.dialog);
        Button b1 = (Button) d.findViewById(R.id.button1);
        Button b2 = (Button) d.findViewById(R.id.button2);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPickermin);
        np.setMaxValue(60);
        np.setMinValue(0);
        np.setWrapSelectorWheel(true);
        final NumberPicker np2 = (NumberPicker) d.findViewById(R.id.numberPickersec);
        np2.setMaxValue(60);
        np2.setMinValue(0);
        np2.setWrapSelectorWheel(true);

        //np.setOnValueChangedListener();
        b1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                startTime=(np.getValue()*60000)+(np2.getValue()*1000);
                stop();
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();


    }

    public void play(){
        if(player==null&&!mTimerRunning){
            if(uri==null) {
                player = MediaPlayer.create(this, R.raw.song);
            }
            else{
                player=MediaPlayer.create(this,uri);
            }
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlayer();
                }
            });
            startTimer();


        }
        else{
            startTimer();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlayer();
                }
            });
        }
        volume=0;
        startFadeIn();

    }

    public void openFile(View v){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        startActivityForResult(intent,READ_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData){
        if(requestCode==READ_REQUEST_CODE&&resultCode== Activity.RESULT_OK){

            if(resultData!=null){
                uri=resultData.getData();
                Log.i(TAG,"Uri: "+uri.toString());
                player= MediaPlayer.create(this,uri);
            }
        }
    }

    public void pause(){
        startFadeOut(true);
        pauseTimer();


    }

    public void stop(){
        startFadeOut(false);

        resetTimer();
    }

    private void stopPlayer(){
        if(player!=null){
            player.release();
            player=null;
            Toast.makeText(this,"song finished",Toast.LENGTH_SHORT);
        }
    }



    @Override
    protected void onStop(){
        super.onStop();
        stopPlayer();
    }

    private void startTimer(){

        mcountDownTimer = new CountDownTimer(timeLeft,1000){

            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft=millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mTimerRunning=false;
                timeLeft=0;
                player.stop();
                updateCountDownText();


            }
        }.start();
        mTimerRunning=true;

    }
    private void pauseTimer(){
        mcountDownTimer.cancel();
        mTimerRunning=false;

    }

    private void resetTimer(){

        timeLeft = startTime;

        mTimerRunning = false;
        updateCountDownText();

        if(mcountDownTimer!=null) {
            mcountDownTimer.cancel();
        }

    }

    private void updateCountDownText(){
        int minutes = (int) (timeLeft/1000)/60;
        int seconds = (int) (timeLeft/1000)% 60;
        String timeLeftFormateed = String.format("%02d:%02d", minutes, seconds);

        mtextCount.setText(timeLeftFormateed);
    }

    private void startFadeIn(){


        Log.d(TAG, "startFadeIn: music volume is"+volume_level);

        final int FADE_DURATION = 3000; //The duration of the fade
        //The amount of time between volume changes. The smaller this is, the smoother the fade
        final int FADE_INTERVAL = 250;
        final float MAX_VOLUME = .1f * volume_level; //The volume will increase from 0 to 1
        int numberOfSteps = FADE_DURATION/FADE_INTERVAL; //Calculate the number of fade steps
        //Calculate by how much the volume changes each step
        final float deltaVolume = MAX_VOLUME / (float)numberOfSteps;

        //Create a new Timer and Timer task to run the fading outside the main UI thread
        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                fadeInStep(deltaVolume); //Do a fade step
                //Cancel and Purge the Timer if the desired volume has been reached
                if(volume>=MAX_VOLUME){
                    timer.cancel();
                    timer.purge();
                    Log.d(TAG, "run: cancelled");
                }
            }
        };

        timer.schedule(timerTask,FADE_INTERVAL,FADE_INTERVAL);
        player.setVolume(0,0);
        player.start();
    }
    private void fadeInStep(float deltaVolume){
        if(player!=null) {
            player.setVolume(volume, volume);
            volume += deltaVolume;
        }

    }
    private void startFadeOut(final boolean isPause){

        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        int volume_level= am.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d(TAG, "startFadeIn: music volume is"+volume_level);




        final int FADE_DURATION = 3000; //The duration of the fade
        //The amount of time between volume changes. The smaller this is, the smoother the fade
        final int FADE_INTERVAL = 250;
        final float MAX_VOLUME = .1f * volume_level; //The volume will increase from 0 to 1
        final int numberOfSteps = FADE_DURATION/FADE_INTERVAL; //Calculate the number of fade steps
        //Calculate by how much the volume changes each step
        final float deltaVolume = MAX_VOLUME / (float)numberOfSteps;
        final float volume2=volume;

        if(mTimerRunning==false&&isPause==false){
            runOnUiThread(new Runnable() {
                public void run() {
                    stopPlayer();
                }
            });
            volume=MAX_VOLUME;
            return;
        }

        //Create a new Timer and Timer task to run the fading outside the main UI thread
        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                fadeOutStep(deltaVolume); //Do a fade step
                //Cancel and Purge the Timer if the desired volume has been reached
                Log.d(TAG, "run: data"+volume+" "+ (volume2-(deltaVolume*numberOfSteps)));
                if(volume<0){

                    if(isPause==true){
                        Log.d(TAG, "run: choice");
                        if(player!=null){
                            player.pause();

                        }
                    }
                    else{
                        runOnUiThread(new Runnable() {
                            public void run() {
                                stopPlayer();
                            }
                        });

                        Log.d(TAG, "run: choice 2");

                    }
                    volume=MAX_VOLUME;
                    timer.cancel();
                    timer.purge();
                    Log.d(TAG, "run: cancelled");
                }
            }
        };

        timer.schedule(timerTask,FADE_INTERVAL,FADE_INTERVAL);

    }
    private void fadeOutStep(float deltaVolume){

        try {
            player.setVolume(volume, volume);
            volume -= deltaVolume;
        }catch (Exception e){

        }
    }



}
