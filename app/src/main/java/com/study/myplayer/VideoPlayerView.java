package com.study.myplayer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Formatter;

public class VideoPlayerView extends RelativeLayout {
    private Context mContext;

    private LinearLayout llVideo;
    private SurfaceView mSurfaceView;

    // player controller
    private RelativeLayout llPlayerController;
    private ImageButton btnPlay;
    private ImageButton btnChangeOrientation;
    private SeekBar playerBar;
    private TextView tvShowTime;

    public VideoPlayerView(Context context) {
        super(context);
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        addAllView();
    }

    private void addAllView(){
        addVideoView();
        addPlayerControllerView();
    }

    private void addVideoView(){
        llVideo = new LinearLayout(mContext);
        RelativeLayout.LayoutParams ll_lp = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        llVideo.setLayoutParams(ll_lp);
        llVideo.setBackgroundColor(Color.BLACK);
        llVideo.setId(View.generateViewId());
        addView(llVideo);
    }

    private void addPlayerControllerView(){
        llPlayerController = new RelativeLayout(mContext);
        RelativeLayout.LayoutParams ll_lp = new LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        ll_lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        llPlayerController.setLayoutParams(ll_lp);
        llPlayerController.setBackgroundColor(Color.LTGRAY);
        llPlayerController.getBackground().setAlpha(0);
//        llPlayerController.setVisibility(GONE);
        addView(llPlayerController);

        btnPlay = new ImageButton(mContext);
        RelativeLayout.LayoutParams bt_lp = new RelativeLayout.LayoutParams(100, 100);
        bt_lp.addRule(ALIGN_PARENT_LEFT);
        btnPlay.setId(View.generateViewId());
        btnPlay.setLayoutParams(bt_lp);
        btnPlay.setPadding(0, 5, 0, 5);
        btnPlay.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btnPlay.setImageResource(android.R.drawable.ic_media_play);
        btnPlay.getBackground().setAlpha(0);
        llPlayerController.addView(btnPlay, 0);

        playerBar = new SeekBar(mContext);
        RelativeLayout.LayoutParams pb_lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        pb_lp.addRule(RIGHT_OF, btnPlay.getId());
        pb_lp.addRule(LEFT_OF, tvShowTime.getId());
        playerBar.setLayoutParams(pb_lp);
        llPlayerController.addView(playerBar, 1);

        tvShowTime = new TextView(mContext);
        RelativeLayout.LayoutParams tv_lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        tv_lp.addRule(ALIGN_PARENT_RIGHT);
        tvShowTime.setId(View.generateViewId());
        tvShowTime.setLayoutParams(tv_lp);
        tvShowTime.setGravity(Gravity.CENTER_HORIZONTAL);

        tvShowTime.setBackgroundColor(Color.RED);
        tvShowTime.setText(stringForTime(0) + "/" + stringForTime(0));
        llPlayerController.addView(tvShowTime, 2);
    }

    private String stringForTime(int mills){
        int totalSeconds = mills / 1000;
        int seconds = mills % 60;
        int minutes = mills / 60 % 60;
        int hours = mills / 3600;

        Formatter fm = new Formatter();

        if (hours > 0){
            return fm.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        }else{
            return fm.format("%02d:%02d", minutes, seconds).toString();
        }
    }
}
