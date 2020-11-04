package com.study.myplayer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
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

import static android.widget.RelativeLayout.ALIGN_PARENT_TOP;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private final int UPDATE_TIME = 1;
    private final int HIDE_CONTROLLER = 2;
    private final String[] playModes = new String[]{
            "播完暂停", "循环播放"
    };
    private final String[] playSpeeds = new String[]{
            "0.5", "1.0", "1.5", "2.0"
    };

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
    private String durationText = null;

    // volume controller
    private LinearLayout volumeController;
    private ImageView volumeIcon;
    private ProgressBar volumeBar;

    // menu
    private final int SET_VIDEO_PATH = 7;
    private final int SET_PLAY_MODE = 8;
    private final int SET_PLAY_SPEED = 9;
    private int currPlayMode = 0;
    private int currPlaySpeedIndex = 1;

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
        menu.add(1, SET_PLAY_MODE, 2, "播放模式");
        menu.add(1, SET_PLAY_SPEED, 3, "播放速度");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case SET_VIDEO_PATH:
                setVideoPathItemOnClick();
                break;
            case SET_PLAY_MODE:
                setPlayModeItemOnClick();
                break;
            case SET_PLAY_SPEED:
                setPlaySpeedItemOnClick();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setVideoPathItemOnClick(){
        verifyPermissions();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.menu_item_2, null,
                false);
        AlertDialog setVideoPathDialog = dialogBuilder
                .setTitle("输入视频地址")
                .setView(view)
                .setNegativeButton("取消", null)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText et = view.findViewById(R.id.et_video_path);
                        String path = et.getText().toString().trim();
                        setVideoPath(path);
                        releaseMediaPlayer();
                        mSurfaceView.setVisibility(View.GONE);
                        mSurfaceView.setVisibility(View.VISIBLE);
                    }
                }).create();
        setVideoPathDialog.show();
    }

    private void setVideoPath(String str){
        File file = new File(str);
        if(!file.exists()){
            Log.d(TAG, "setVideoPath: the file: " + videoPath + " does not exits!");
            Toast.makeText(getApplicationContext(), "the file: " + videoPath + " does not exits!",
                    Toast.LENGTH_LONG).show();
        }else{
            videoPath = str;
        }
    }

    private int selectPlayMode;
    private void setPlayModeItemOnClick(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        AlertDialog selectPlayModeDialog = dialogBuilder
                .setTitle("选择播放模式")
                .setSingleChoiceItems(playModes, currPlayMode, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectPlayMode = which;
                    }
                })
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currPlayMode = selectPlayMode;
                        if(currPlayMode == 0){
                            Toast.makeText(getApplicationContext(), "播完暂停", Toast.LENGTH_SHORT).show();
                            mMediaPlayer.setLooping(false);
                        }else if(currPlayMode == 1){
                            Toast.makeText(getApplicationContext(), "循环播放", Toast.LENGTH_SHORT).show();
                            mMediaPlayer.setLooping(true);
                        }
                    }
                }).create();
        selectPlayModeDialog.show();
    }

    private void setPlaySpeedItemOnClick(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        AlertDialog selectPlaySpeedDialog = dialogBuilder
                .setTitle("选择播放速度")
                .setItems(playSpeeds, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currPlaySpeedIndex = which;
                        Toast.makeText(getApplicationContext(), "设置播放倍速为" +
                                playSpeeds[currPlaySpeedIndex], Toast.LENGTH_SHORT).show();
                        Float playSpeed = Float.parseFloat(playSpeeds[currPlaySpeedIndex]);
                        setPlayerSpeed(playSpeed);
                    }
                }).create();
        selectPlaySpeedDialog.show();
    }

    private void setPlayerSpeed(Float speed){
        PlaybackParams playbackParams = mMediaPlayer.getPlaybackParams();
        playbackParams .setSpeed(speed);
        mMediaPlayer.setPlaybackParams(playbackParams);
    }

    private void addSurfaceView(){
        mSurfaceView = new SurfaceView(this);
        RelativeLayout.LayoutParams sf_lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        sf_lp.addRule(ALIGN_PARENT_TOP);
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
                    initParentPortLayout(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
                    play();
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(mMediaPlayer.isLooping()){
                        currPosition = 0;
                        play();
                    }else{
                        mMediaPlayer.seekTo(0);
                        pause();
                    }
                }
            });
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

    private ConstraintLayout.LayoutParams port_lp;
    private ConstraintLayout.LayoutParams lan_lp = new ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT);

    private void initParentPortLayout(int videoWidth, int videoHeight){
        port_lp = (ConstraintLayout.LayoutParams) parent.getLayoutParams();
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        int screenWidth = point.x;
        int screenHeight = point.y;
        // 如果视频宽高超过屏幕的宽高，则要进行缩放
        float ratio = Math.max((float)videoWidth / (float) screenWidth,
                (float)videoHeight / (float) screenHeight);
        port_lp.width = (int) Math.ceil((float)videoWidth / ratio);
        port_lp.height = (int) Math.ceil((float)videoHeight / ratio);
        parent.setLayoutParams(port_lp);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            showSystemUI();
            showController();
            mHandler.removeMessages(HIDE_CONTROLLER);
            parent.setLayoutParams(port_lp);
            isLandscape = false;
        }else if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            hideSystemUI();
            hideController();
            parent.setLayoutParams(lan_lp);
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

    private boolean isVerticalOperator = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(isLandscape){
                    hideSystemUI();
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

    private void verifyPermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    private void releaseMediaPlayer(){
        mMediaPlayer.seekTo(0);
        mMediaPlayer.pause();
        mMediaPlayer.release();
        currPosition = 0;
        initialized = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mMediaPlayer != null){
            pause();
        }
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}