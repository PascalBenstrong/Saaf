package com.bitec.saafs.ui.activities;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bitec.saafs.R;
import com.bitec.saafs.adapters.BaseRecyclerViewAdapter;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;

public class ActivityVideoPlayer extends AppCompatActivity{

    private Uri videoUri;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private long position;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_video_player );
        getWindow ().setFlags ( WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN );

        //videoUri = Uri.parse ( "https://firebasestorage.googleapis.com/v0/b/saaf-d1cf5.appspot.com/o/videos%2FAlcohol%20effects%20on%20brain%20and%20body.mp4?alt=media&token=07813dd7-2245-4bbe-915a-df25a918c0e0" );

        playerView = findViewById(R.id.exo_playerView);
        position = getIntent ().getLongExtra ( "position", 0);
        //ShowToast ( getIntent ().getStringExtra ( "url" ) );
        videoUri = Uri.parse ( getIntent ().getStringExtra ( "url" ) );

    }

    @Override
    protected void onStart() {
        super.onStart();

        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
        playerView.setPlayer(player);
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, null));

        ExtractorMediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(videoUri);
        player.prepare(mediaSource);
        player.seekTo(position);
        player.setPlayWhenReady(true);

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(player != null)
                if(player.getCurrentPosition() == player.getDuration())
                    finish();
                handler.postDelayed(this,1000L);
            }
        };
        handler.postDelayed(runnable,3000L);
    }
    
    @Override
    public void onBackPressed()
    {
        super.onBackPressed ();
        player.stop();
        finish();
    }
    
    @Override
    public void onStop () {
        super.onStop ();

        playerView.setPlayer(null);
        player.release();
        player = null;
    }
    
    @Override
    public void finish () {
    
        Intent intent = new Intent ();
        intent.putExtra ( "position", player.getCurrentPosition() );
        intent.putExtra ( "url", videoUri.toString () );
        
        setResult ( RESULT_OK, intent );
        
        super.finish ();
    }

    private void ShowToast(String  text)
    {
        Toast.makeText ( this, text, Toast.LENGTH_LONG ).show ();
    }
    
    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState ( outState );
        outState.putLong ( "position", player.getCurrentPosition() );
        outState.putString ( "url", videoUri.toString () );
        //mMediaPlayer.pause ();
    }

}
