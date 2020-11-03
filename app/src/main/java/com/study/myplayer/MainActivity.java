package com.study.myplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Formatter;

import static android.view.Gravity.CENTER_VERTICAL;
import static android.view.View.generateViewId;
import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static android.widget.RelativeLayout.ALIGN_PARENT_LEFT;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;
import static android.widget.RelativeLayout.BELOW;
import static android.widget.RelativeLayout.CENTER_IN_PARENT;
import static android.widget.RelativeLayout.LEFT_OF;
import static android.widget.RelativeLayout.RIGHT_OF;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private final int UPDATE_TIME = 1;

    private String videoPath;

    private RelativeLayout parent;

    private MediaPlayer mMediaPlayer;
    private int currPosition = 0;
    private boolean initialized = false;

    private SurfaceView mSurfaceView;

    private RelativeLayout rlController;
    private ImageButton btnPlay;
    private ImageButton btnChangeOrientation;
    private SeekBar playerBar;
    private TextView tvShowTime;
    private boolean isControllerShow = false;
    private String durationText;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case UPDATE_TIME:
                    updateTime();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parent = findViewById(R.id.parent);
        parent.setBackgroundColor(Color.WHITE);

        addAllView();
        setViewListener();
        initMediaPlayer();
    }

    private void addAllView(){
        addSurfaceView();
        addControllerView();
    }

    private void setViewListener(){
        btnPlay.setOnClickListener(btnPlayOnClickListener);
        playerBar.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    private void initMediaPlayer(){
        try{
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(getResources().getAssets().openFd("test_1080_60.mp4"));
            mMediaPlayer.setDisplay(mSurfaceView.getHolder());
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.i(TAG, "onPrepared");
                    initialized = true;
                    mMediaPlayer.setDisplay(mSurfaceView.getHolder());
                    playerBar.setMax(mMediaPlayer.getDuration());
                    initTvShowText(mMediaPlayer.getDuration());
                    intSurfaceViewSize(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
                }
            });
            initialized = true;
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addSurfaceView(){
        mSurfaceView = new SurfaceView(this);
        RelativeLayout.LayoutParams sf_lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                600);
        sf_lp.addRule(CENTER_VERTICAL);
        mSurfaceView.setLayoutParams(sf_lp);
        mSurfaceView.setId(View.generateViewId());
        parent.addView(mSurfaceView);
        mSurfaceView.requestFocus();
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                Log.i(TAG, "surfaceCreated");
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                Log.i(TAG, "surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                Log.i(TAG, "surfaceDestroyed");
            }
        });
    }

    private void addControllerView(){
        rlController = new RelativeLayout(this);
        RelativeLayout.LayoutParams vd_lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                100);
        vd_lp.addRule(BELOW, mSurfaceView.getId());
        rlController.setLayoutParams(vd_lp);
        rlController.setBackgroundColor(Color.LTGRAY);
        parent.addView(rlController);

        addBtnPlay();
        addBtnChangeOrientation();
        addTvShowTime();
        addPlayerBar();
    }

    private void addBtnPlay(){
        btnPlay = new ImageButton(this);
        RelativeLayout.LayoutParams btp_lp = new RelativeLayout.LayoutParams(100, 100);
        btp_lp.addRule(ALIGN_PARENT_LEFT);
        btnPlay.setId(generateViewId());
        btnPlay.setLayoutParams(btp_lp);
        btnPlay.setPadding(0, 5, 0, 5);
        btnPlay.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btnPlay.setImageResource(android.R.drawable.ic_media_play);
        btnPlay.getBackground().setAlpha(0);
        rlController.addView(btnPlay);
    }

    private void addPlayerBar(){
        playerBar = new SeekBar(this);
        RelativeLayout.LayoutParams pb_lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        pb_lp.rightMargin = 10;
        pb_lp.addRule(RIGHT_OF, btnPlay.getId());
        pb_lp.addRule(LEFT_OF, tvShowTime.getId());
        pb_lp.addRule(RelativeLayout.CENTER_VERTICAL);
        playerBar.setId(generateViewId());
        playerBar.setLayoutParams(pb_lp);
        rlController.addView(playerBar);
    }

    private void addBtnChangeOrientation(){
        btnChangeOrientation = new ImageButton(this);
        RelativeLayout.LayoutParams btc_lp = new RelativeLayout.LayoutParams(100, 100);
        btc_lp.addRule(ALIGN_PARENT_RIGHT);
        btnChangeOrientation.setId(generateViewId());
        btnChangeOrientation.setLayoutParams(btc_lp);
        btnChangeOrientation.setImageResource(android.R.drawable.ic_menu_always_landscape_portrait);
        btnPlay.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btnPlay.getBackground().setAlpha(0);
        rlController.addView(btnChangeOrientation);
    }

    private void addTvShowTime(){
        tvShowTime = new TextView(this);
        RelativeLayout.LayoutParams tv_lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tv_lp.addRule(LEFT_OF, btnChangeOrientation.getId());
        tv_lp.addRule(RelativeLayout.CENTER_VERTICAL);
        tv_lp.rightMargin = 10;
        tvShowTime.setId(generateViewId());
        tvShowTime.setLayoutParams(tv_lp);
        tvShowTime.setTextSize(12);
        tvShowTime.setText(stringForTime(0) + "/" + stringForTime(0));
        rlController.addView(tvShowTime);
    }

    private View.OnClickListener btnPlayOnClickListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onClick(View v) {
            if(initialized) {
                if (mMediaPlayer.isPlaying()) {
                    Log.i(TAG, "onClick: pause");
                    Toast.makeText(getApplication(), "pause", Toast.LENGTH_SHORT).show();
                    mMediaPlayer.pause();
                    currPosition = mMediaPlayer.getCurrentPosition();
                    btnPlay.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    Log.i(TAG, "onClick: play");
                    Toast.makeText(getApplication(), "play", Toast.LENGTH_SHORT).show();
                    mMediaPlayer.seekTo(currPosition);
                    mMediaPlayer.start();
                    btnPlay.setImageResource(android.R.drawable.ic_media_pause);
                    mHandler.sendEmptyMessage(UPDATE_TIME);
                }
            }else{
                Toast.makeText(getApplication(), "video is preparing", Toast.LENGTH_SHORT).show();
                initMediaPlayer();
            }
        }
    };

    private View.OnClickListener btnOrientationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(orin)
        }
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                mMediaPlayer.seekTo(progress);
                setCurrTimeForTvShow();
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeMessages(UPDATE_TIME);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mHandler.sendEmptyMessage(UPDATE_TIME);
        }
    };

    private String stringForTime(int mills){
        int totalSeconds = mills / 1000;
        int seconds = totalSeconds % 60;
        int minutes = totalSeconds / 60 % 60;
        int hours = totalSeconds / 3600;

        Formatter fm = new Formatter();

        if (hours > 0){
            return fm.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        }else{
            return fm.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private void initTvShowText(int duration){
        durationText = stringForTime(duration);
        setCurrTimeForTvShow();
    }

    private RelativeLayout.LayoutParams sf_port_lp;
    private RelativeLayout.LayoutParams sf_lan_lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);

    private void intSurfaceViewSize(int videoWidth, int videoHeight){
        sf_port_lp = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
        // 如果视频宽高超过父容器的宽高，则要进行缩放
        float ratio = Math.max((float)videoWidth / (float) parent.getWidth(),
                (float)videoHeight / (float) parent.getHeight());
        sf_port_lp.width = (int) Math.ceil((float)videoWidth / ratio);
        sf_port_lp.height = (int) Math.ceil((float)videoHeight / ratio);
        mSurfaceView.setLayoutParams(sf_port_lp);
    }

    private void setCurrTimeForTvShow(){
        String currTime = stringForTime(mMediaPlayer.getCurrentPosition());
        tvShowTime.setText(currTime + "/" + durationText);
    }

    private void updateTime(){
        setCurrTimeForTvShow();
        playerBar.setProgress(mMediaPlayer.getCurrentPosition());

        if(mMediaPlayer.isPlaying()){
            mHandler.sendEmptyMessageDelayed(UPDATE_TIME, 500);
        }
    }



    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            mSurfaceView.setLayoutParams(sf_port_lp);
        }else if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            mSurfaceView.setLayoutParams(sf_lan_lp);
        }
    }
}