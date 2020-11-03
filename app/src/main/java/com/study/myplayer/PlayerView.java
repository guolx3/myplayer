package com.study.myplayer;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Formatter;

public class PlayerView extends RelativeLayout {
    private final String TAG = "PlayerView";

    private  Context mContext;
    private String videoPath;

    private MediaPlayer mMediaPlayer;
    private int currPosition = 0;
    private boolean initialized = false;

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    private RelativeLayout rlController;
    private ImageButton btnPlay;
    private ImageButton btnChangeOrientation;
    private SeekBar playerBar;
    private TextView tvShowTime;
    private boolean isControllerShow = false;

    public PlayerView(Context context) {
        super(context);
    }

    public PlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initThisView();
        addAllView();
        setViewListener();
        initMediaPlayer();
    }

    private void initThisView(){
        this.setBackgroundColor(Color.BLACK);
    }

    private void addAllView(){
        addSurfaceView();
        addControllerView();

    }

    private void setViewListener(){
        btnPlay.setOnClickListener(btnPlayOnClickListener);
    }

    private void initMediaPlayer(){
        if(!initialized){
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.prepareAsync();
        }
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.i(TAG, "onPrepared");
                initialized = true;
                try {
                    mMediaPlayer.setDataSource(videoPath);
                    mMediaPlayer.setDisplay(mSurfaceHolder);
                    initPlayerBarTime(mMediaPlayer.getDuration());
                    intSurfaceViewSize(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void addSurfaceView(){
        mSurfaceView = new SurfaceView(mContext);
        RelativeLayout.LayoutParams sf_lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                600);
        sf_lp.addRule(CENTER_VERTICAL);
        mSurfaceView.setLayoutParams(sf_lp);
        addView(mSurfaceView);
        mSurfaceView.requestFocus();
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                Log.i(TAG, "surfaceCreated");
                mSurfaceHolder = mSurfaceView.getHolder();
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
        rlController = new RelativeLayout(mContext);
        RelativeLayout.LayoutParams vd_lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                100);
        vd_lp.addRule(ALIGN_PARENT_BOTTOM);
        rlController.setLayoutParams(vd_lp);
        rlController.setBackgroundColor(Color.LTGRAY);
        addView(rlController);

        addBtnPlay();
        addPlayerBar();
        addBtnChangeOrientation();
        addTvShowTime();
    }

    private void addBtnPlay(){
        btnPlay = new ImageButton(mContext);
        RelativeLayout.LayoutParams btp_lp = new LayoutParams(100, 100);
        btp_lp.addRule(ALIGN_PARENT_LEFT);
        btnPlay.setId(View.generateViewId());
        btnPlay.setLayoutParams(btp_lp);
        btnPlay.setPadding(0, 5, 0, 5);
        btnPlay.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btnPlay.setImageResource(android.R.drawable.ic_media_play);
        btnPlay.getBackground().setAlpha(0);
        rlController.addView(btnPlay);
    }

    private void addPlayerBar(){
        playerBar = new SeekBar(mContext);
        RelativeLayout.LayoutParams pb_lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        pb_lp.rightMargin = 10;
        pb_lp.addRule(RIGHT_OF, btnPlay.getId());
        playerBar.setId(View.generateViewId());
        playerBar.setLayoutParams(pb_lp);
        rlController.addView(playerBar);
    }

    private void addBtnChangeOrientation(){
        btnChangeOrientation = new ImageButton(mContext);
        RelativeLayout.LayoutParams btc_lp = new LayoutParams(100, 100);
        btc_lp.addRule(ALIGN_PARENT_RIGHT);
        btnChangeOrientation.setId(View.generateViewId());
        btnChangeOrientation.setLayoutParams(btc_lp);
        btnChangeOrientation.setImageResource(android.R.drawable.ic_menu_always_landscape_portrait);
        btnPlay.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btnPlay.getBackground().setAlpha(0);
        rlController.addView(btnChangeOrientation);
    }

    private void addTvShowTime(){
        tvShowTime = new TextView(mContext);
        RelativeLayout.LayoutParams tv_lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tv_lp.addRule(LEFT_OF, btnChangeOrientation.getId());
        tv_lp.rightMargin = 10;
        tvShowTime.setId(generateViewId());
        tvShowTime.setLayoutParams(tv_lp);
        tvShowTime.setTextSize(12);
        tvShowTime.setText(stringForTime(0) + "/" + stringForTime(0));
        rlController.addView(tvShowTime);
    }

    private View.OnClickListener btnPlayOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mMediaPlayer != null && initialized) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    currPosition = mMediaPlayer.getCurrentPosition();
                    btnPlay.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    mMediaPlayer.seekTo(currPosition);
                    mMediaPlayer.start();
                    btnPlay.setImageResource(android.R.drawable.ic_media_pause);
                }
            }
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

    private void initPlayerBarTime(int mills){
        playerBar.setMax(mills);
        tvShowTime.setText(stringForTime(0) + "/" + stringForTime(mills));
    }

    private void intSurfaceViewSize(int videoWidth, int videoHeight){
        LayoutParams sf_lp = (LayoutParams) mSurfaceView.getLayoutParams();
        // 如果视频宽高超过父容器的宽高，则要进行缩放
        float ratio = Math.max((float)videoWidth / (float) getWidth(), (float)videoHeight / (float) getHeight());
        sf_lp.width = (int) Math.ceil((float)videoWidth / ratio);
        sf_lp.height = (int) Math.ceil((float)videoHeight / ratio);
        sf_lp.addRule(CENTER_VERTICAL);
        mSurfaceView.setLayoutParams(sf_lp);
    }
}
