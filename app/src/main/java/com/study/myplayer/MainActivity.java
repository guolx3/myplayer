package com.study.myplayer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Formatter;

import static android.widget.RelativeLayout.CENTER_IN_PARENT;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private final int UPDATE_TIME = 1;
    private final int HIDE_CONTROLLER = 2;

    private String videoPath = null;

    private RelativeLayout parent;

    // video player
    private MediaPlayer mMediaPlayer;
    private int currPosition = 0;
    private boolean initialized = false;
    private SurfaceView mSurfaceView;

    // video controller
    VideoControllerView videoControllerView;
    private ImageButton btnPlay;
    private ImageButton btnChangeOrientation;
    private SeekBar playerBar;
    private TextView tvShowTime;
    private boolean isControllerShowing = false;
    private String durationText;

    // volume controller
    private LinearLayout volumeController;
    private ImageView volumeIcon;
    private ProgressBar volumeBar;

    // menu
    private final int SET_VIDEO_PATH = 7;
    private final int PLAY_MODE = 8;
    private final int PLAY_SPEED= 9;

    private boolean isSystemUIShowing = true;
    private boolean isLandscape = false;

    private final Handler mHandler = new Handler(new Handler.Callback() {
        //?? Callback interface you can use when instantiating a Handler to avoid having to implement your own subclass of Handler

        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case UPDATE_TIME:
                    updateTime();
                    break;
                case HIDE_CONTROLLER:
                    hideController();
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        initParentView();
        addAllView();
        setViewListener();
    }

    private void initParentView(){
        parent = findViewById(R.id.parent);
        parent.setBackgroundColor(Color.WHITE);
    }

    private void addAllView(){
        addSurfaceView();
        addVideoControllerView();
        setViewListener();
    }

    private void addVideoControllerView(){
        videoControllerView = new VideoControllerView(this);
        parent.addView(videoControllerView.getRootView());
        btnPlay = videoControllerView.getBtnPlay();
        btnChangeOrientation = videoControllerView.getBtnChangeOrientation();
        playerBar = videoControllerView.getPlayerBar();
        tvShowTime = videoControllerView.getTvShowTime();
    }

    private void setViewListener(){
        videoControllerView.setBtnPlayOnClickListener(btnPlayOnClickListener);
        videoControllerView.setBtnChangeOrientationOnClickListener(btnOrientationClickListener);
        videoControllerView.setPlayerBarChangeListener(seekBarChangeListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, SET_VIDEO_PATH, 1, "输入视频地址");
        menu.add(1, PLAY_MODE, 2, "播放模式");
        menu.add(1, PLAY_SPEED, 3, "播放速度");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case SET_VIDEO_PATH:
                break;
            case PLAY_MODE:
                break;
            case PLAY_SPEED:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addSurfaceView(){
        mSurfaceView = new SurfaceView(this);
        RelativeLayout.LayoutParams sf_lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                600);
        sf_lp.addRule(CENTER_IN_PARENT);
        mSurfaceView.setLayoutParams(sf_lp);
        mSurfaceView.setId(View.generateViewId());
        parent.addView(mSurfaceView);
        mSurfaceView.requestFocus();
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                Log.i(TAG, "surfaceCreated");
                if(!initialized){
                    initMediaPlayer(holder);
                }else{
                    mMediaPlayer.setDisplay(holder);
                }
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

    private View.OnClickListener btnPlayOnClickListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onClick(View v) {
            if(initialized) {
                if (mMediaPlayer.isPlaying()) {
                    Log.i(TAG, "onClick: pause");
                    Toast.makeText(getApplication(), "pause", Toast.LENGTH_SHORT).show();
                    pause();
                } else {
                    Log.i(TAG, "onClick: play");
                    Toast.makeText(getApplication(), "play", Toast.LENGTH_SHORT).show();
                    play();
                }
            }else{
                Toast.makeText(getApplication(), "video is preparing", Toast.LENGTH_SHORT).show();
                initMediaPlayer(mSurfaceView.getHolder());
            }
        }
    };

    private View.OnClickListener btnOrientationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isLandscape){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }else{
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                setCurrTimeForTvShow();
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            pause();
            mHandler.removeMessages(UPDATE_TIME);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            currPosition = seekBar.getProgress();
            play();
            mHandler.sendEmptyMessage(UPDATE_TIME);
        }
    };

    private void pause(){
        mMediaPlayer.pause();
        currPosition = mMediaPlayer.getCurrentPosition();
        btnPlay.setImageResource(android.R.drawable.ic_media_play);
    }

    private void play(){
        mMediaPlayer.seekTo(currPosition);
        mMediaPlayer.start();
        btnPlay.setImageResource(android.R.drawable.ic_media_pause);
        mHandler.sendEmptyMessage(UPDATE_TIME);
    }

    private void initMediaPlayer(SurfaceHolder holder){
        try{
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            if(videoPath == null){
                mMediaPlayer.setDataSource(getResources().getAssets().openFd("test_1080_60.mp4"));
            }else{
                mMediaPlayer.setDataSource(videoPath);
            }
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
                    play();
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mMediaPlayer.seekTo(0);
                    pause();
                }
            });
            initialized = true;
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

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
            showSystemUI();
            showController();
            mHandler.removeMessages(HIDE_CONTROLLER);
            mSurfaceView.setLayoutParams(sf_port_lp);
            isLandscape = false;
        }else if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            hideSystemUI();

            hideController();
            mSurfaceView.setLayoutParams(sf_lan_lp);
            isLandscape = true;
        }
    }

    private void hideController(){
        videoControllerView.setVisibility(View.GONE);
        isControllerShowing = false;
    }

    private void showController(){
        videoControllerView.setVisibility(View.VISIBLE);
        isControllerShowing = true;
    }

    private void setVideoPath(String str){
        File file = new File(str);
        if(!file.exists()){
            Log.d(TAG, "initMediaPlayer: the file: " + videoPath + " does not exits!");
            Toast.makeText(getApplicationContext(), "the file: " + videoPath + " does not exits!",
                    Toast.LENGTH_LONG).show();
        }else{
            videoPath = str;
        }
    }

    private boolean isVerticalOperator = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(isSystemUIShowing){
                    hideSystemUI();
                    isSystemUIShowing = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if(isControllerShowing){
                    hideController();
                    mHandler.removeMessages(HIDE_CONTROLLER);
                }else{
                    if(!isVerticalOperator){
                        showController();
                        mHandler.sendEmptyMessageDelayed(HIDE_CONTROLLER, 5000);
                    }
                }
                break;
        }
        return true;
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onStop() {
        super.onStop();
        pause();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}