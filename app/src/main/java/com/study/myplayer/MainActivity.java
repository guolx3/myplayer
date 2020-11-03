package com.study.myplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "mainActivity";
    enum STATE {
        IDLE, INITIALIZED, PREPARED, PLAYING, PAUSE
    }
    private STATE state;
    private EditText et;
    private SurfaceView sv;
    private MediaPlayer player;
    private SeekBar sb;
    private TextView tv_curr_time;
    private TextView tv_video_time;
    private boolean isPlaying;
    private int video_width;
    private int video_height;
    private float ratio;
    private boolean isSeekBarChanging;
    private Button btn_play, btn_pause, btn_stop, btn_orientation;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            int curr_pos = msg.arg1;
            sb.setProgress(curr_pos);
            tv_curr_time.setText(String.valueOf(curr_pos));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        bindView();
        state = STATE.IDLE;
    }

    private void bindView(){
        sb = findViewById(R.id.sb);
        tv_curr_time = findViewById(R.id.curr_time);
        tv_video_time = findViewById(R.id.video_time);
        sv = findViewById(R.id.sv);
        et = findViewById(R.id.et);
        et.setText("");
        btn_play = findViewById(R.id.btn_play);
        btn_pause = findViewById(R.id.btn_pause);
        btn_stop = findViewById(R.id.btn_stop);
        btn_orientation = findViewById(R.id.btn_orientation);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void play(final View view){
        if(state == STATE.PLAYING) return;
        if(state == STATE.PAUSE || state == STATE.PREPARED){
            player.start();
            btn_play.setEnabled(false);
            btn_pause.setEnabled(true);
            btn_stop.setEnabled(true);
            state = STATE.PLAYING;
            return;
        }

        try {
            if(state == STATE.IDLE) {
                player = new MediaPlayer();
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);

                String video_path = et.getText().toString().trim();
                File file = new File(video_path);
                if (file.exists()) {
                    player.setDataSource(file.getAbsolutePath());
                } else {
                    player.setDataSource(getAssets().openFd("test_1080_60.mp4"));
//                Log.e(TAG, "initPlayer: can't find video_path: " + video_path +
//                            " please check");
                }
                player.setDisplay(sv.getHolder());
                state = STATE.INITIALIZED;
                Log.i(TAG, "play: start loading");
            }

            player.prepareAsync();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.i(TAG, "onPrepared: finish load");
                    state = STATE.PREPARED;
                    video_width = player.getVideoWidth();
                    video_height = player.getVideoHeight();
                    LinearLayout ll = findViewById(R.id.ll);
                    ratio = Math.max((float)video_width / (float)ll.getWidth(),
                            (float)video_height / (float)ll.getHeight());
                    ViewGroup.LayoutParams lp = sv.getLayoutParams();
                    lp.width = (int) Math.ceil((float)video_width / ratio);
                    lp.height = (int) Math.ceil((float)video_height / ratio);
                    sv.setLayoutParams(lp);

                    player.start();
                    state = STATE.PLAYING;
                    player.seekTo(0);
                    int duration = player.getDuration();
                    sb.setMax(duration);
                    tv_video_time.setText(String.valueOf(duration));
                    tv_curr_time.setText(String.valueOf(0));
                    sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            tv_curr_time.setText(String.valueOf(progress));
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                            isSeekBarChanging = true;
                            player.pause();
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            isSeekBarChanging = false;
                            player.seekTo(seekBar.getProgress());
                            player.start();
                            tv_curr_time.setText(String.valueOf(player.getCurrentPosition()));
                        }
                    });

                    sendCurrPosThread thread = new sendCurrPosThread();
                    thread.start();

                    btn_play.setEnabled(false);
                    btn_pause.setEnabled(true);
                    btn_stop.setEnabled(true);
                }
            });

            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    btn_play.setEnabled(true);
                }
            });

            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    return false;
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public void pause(View view){
        if(state == STATE.PLAYING){
            player.pause();
            btn_pause.setEnabled(false);
            btn_play.setEnabled(true);
            state = STATE.PAUSE;
        }
    }

    public void stop(View view){
        player.seekTo(0);
        sb.setProgress(0);
        player.stop();
        player.release();
        state = STATE.IDLE;
        btn_stop.setEnabled(false);
        btn_pause.setEnabled(true);
        btn_play.setEnabled(true);
    }

    public void orientationChange(View view){
        if(getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
            MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }else {
            MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            ViewGroup.LayoutParams lp = sv.getLayoutParams();
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            sv.setLayoutParams(lp);
        }else {
            ViewGroup.LayoutParams lp = sv.getLayoutParams();
            lp.width = (int) Math.ceil((float)video_width / ratio);
            lp.height = (int) Math.ceil((float)video_height / ratio);
            sv.setLayoutParams(lp);
        }
    }

    private String timeParser(int milli){
        String result = "";
        double second = Math.ceil((float) milli / (float) 1000);
        double tmp[] = new double[3];
        int i = 0;
        while (second >= 60){
            tmp[i++] = second % 60;
            second = second / 60;
        }

        return result;
    }

    class sendCurrPosThread extends Thread{
        @Override
        public void run() {
            try{
                while(state == STATE.PLAYING || state == STATE.PAUSE){
                    int curr_pos = player.getCurrentPosition();
                    if(!isSeekBarChanging){
                        Message msg = new Message();
                        msg.arg1 = curr_pos;
                        handler.sendMessage(msg);
                    }
                    Thread.sleep(500);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}