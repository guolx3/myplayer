package com.study.myplayer;

import android.content.Context;
import android.graphics.Color;
import android.opengl.Visibility;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import static android.view.View.generateViewId;
import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static android.widget.RelativeLayout.ALIGN_PARENT_LEFT;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;
import static android.widget.RelativeLayout.LEFT_OF;
import static android.widget.RelativeLayout.RIGHT_OF;

public class VideoControllerView {
    Context mContext;

    // video controller
    private RelativeLayout videoController;
    private ImageButton btnPlay;
    private ImageButton btnChangeOrientation;
    private SeekBar playerBar;
    private TextView tvShowTime;

    public VideoControllerView(Context context){
        this.mContext = context;

        videoController = new RelativeLayout(mContext);
        RelativeLayout.LayoutParams vd_lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                100);
        vd_lp.addRule(ALIGN_PARENT_BOTTOM);
        videoController.setLayoutParams(vd_lp);
        videoController.setBackgroundColor(Color.LTGRAY);

        addBtnPlay();
        addBtnChangeOrientation();
        addTvShowTime();
        addPlayerBar();
    }

    private void addBtnPlay(){
        btnPlay = new ImageButton(mContext);
        RelativeLayout.LayoutParams btp_lp = new RelativeLayout.LayoutParams(100, 100);
        btp_lp.addRule(ALIGN_PARENT_LEFT);
        btnPlay.setId(generateViewId());
        btnPlay.setLayoutParams(btp_lp);
        btnPlay.setPadding(0, 5, 0, 5);
        btnPlay.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btnPlay.setImageResource(android.R.drawable.ic_media_play);
        btnPlay.getBackground().setAlpha(0);
        videoController.addView(btnPlay);
    }

    private void addPlayerBar(){
        playerBar = new SeekBar(mContext);
        RelativeLayout.LayoutParams pb_lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        pb_lp.rightMargin = 10;
        pb_lp.addRule(RIGHT_OF, btnPlay.getId());
        pb_lp.addRule(LEFT_OF, tvShowTime.getId());
        pb_lp.addRule(RelativeLayout.CENTER_VERTICAL);
        playerBar.setId(generateViewId());
        playerBar.setLayoutParams(pb_lp);
        videoController.addView(playerBar);
    }

    private void addBtnChangeOrientation(){
        btnChangeOrientation = new ImageButton(mContext);
        RelativeLayout.LayoutParams btc_lp = new RelativeLayout.LayoutParams(100, 100);
        btc_lp.addRule(ALIGN_PARENT_RIGHT);
        btnChangeOrientation.setId(generateViewId());
        btnChangeOrientation.setLayoutParams(btc_lp);
        btnChangeOrientation.setImageResource(android.R.drawable.ic_menu_always_landscape_portrait);
        btnPlay.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btnPlay.getBackground().setAlpha(0);
        videoController.addView(btnChangeOrientation);
    }

    private void addTvShowTime(){
        tvShowTime = new TextView(mContext);
        RelativeLayout.LayoutParams tv_lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tv_lp.addRule(LEFT_OF, btnChangeOrientation.getId());
        tv_lp.addRule(RelativeLayout.CENTER_VERTICAL);
        tv_lp.rightMargin = 10;
        tvShowTime.setId(generateViewId());
        tvShowTime.setLayoutParams(tv_lp);
        tvShowTime.setTextSize(12);
        tvShowTime.setText("00:00/00:00");
        videoController.addView(tvShowTime);
    }

    public void setVisibility(int flag){
        videoController.setVisibility(flag);
    }

    public RelativeLayout getRootView(){
        return videoController;
    }

    public ImageButton getBtnPlay() {
        return btnPlay;
    }

    public ImageButton getBtnChangeOrientation() {
        return btnChangeOrientation;
    }

    public SeekBar getPlayerBar() {
        return playerBar;
    }

    public TextView getTvShowTime() {
        return tvShowTime;
    }

    public void setBtnPlayOnClickListener(View.OnClickListener listener){
        btnPlay.setOnClickListener(listener);
    }

    public void setBtnChangeOrientationOnClickListener(View.OnClickListener listener){
        btnChangeOrientation.setOnClickListener(listener);
    }

    public void setPlayerBarChangeListener(SeekBar.OnSeekBarChangeListener listener){
        playerBar.setOnSeekBarChangeListener(listener);
    }
}
