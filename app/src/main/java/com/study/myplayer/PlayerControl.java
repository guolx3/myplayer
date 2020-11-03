package com.study.myplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;

public class PlayerControl {
    private final int IDLE = 0;
    private final int PREPARED = 1;
    private final int PLAYING = 2;
    private final int STOP = 3;
    private final int PAUSE = 4;
    private final String PLAYER_TAG = "PlayerControl";

    private Context context;
    private MediaPlayer player;
    private SurfaceView surfaceView;
    private String video_path;
    private int state;

    PlayerControl(){

    }

    public void initPlayer(){

    }
}
