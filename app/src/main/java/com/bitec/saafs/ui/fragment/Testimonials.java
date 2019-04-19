package com.bitec.saafs.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bitec.saafs.R;
import com.bitec.saafs.adapters.BaseRecyclerViewAdapter;
import com.bitec.saafs.adapters.SectionsPagerAdapter;
import com.bitec.saafs.interfaces.ITestimony;
import com.bitec.saafs.models.Video;
import com.bitec.saafs.models.VideoModel;
import com.bitec.saafs.ui.activities.ActivityVideoPlayer;
import com.bitec.saafs.ui.activities.MainActivity;
import com.bitec.saafs.utils.HelperClass;

public class Testimonials extends Fragment implements ITestimony {
    
    public static ITestimony Testimony;
    public TextView title;
    
    private TabLayout mTabLayout;
    
    public  VideoView        mVideoView;
    public  ImageView        mPlayButton;
    private TextView         mcurrentTimer;
    private TextView         mDurationTimer;
    private SeekBar      mCurrentProgress;
    private ProgressBar      mBufferProgress;
    private RelativeLayout mSoundController;
    
    public boolean isPlaying;
    private int current = 0;
    private int duration = 0;

    private static Uri CurrentVideoUri = Uri.EMPTY;
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        
        View mView = inflater.inflate ( R.layout.fragment_testimonials, container, false );
        
        Testimony = this;
    
        //Tabs
        ViewPager mViewPager = mView.findViewById ( R.id.main_tabPager );
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter ( MainActivity.activity.getActivity ().getSupportFragmentManager () );
    
        mViewPager.setAdapter ( mSectionsPagerAdapter );
    
        mTabLayout = mView.findViewById ( R.id.main_tabs );
        mTabLayout.setupWithViewPager ( mViewPager );
        
        return mView;
    }
    
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    
        isPlaying = false;
    
        title = view.findViewById ( R.id.testimonial_title );
        mVideoView = view.findViewById(R.id.videoView);
        mPlayButton = view.findViewById(R.id.button_play);
        mcurrentTimer = view.findViewById(R.id.currentTimer);
        mDurationTimer = view.findViewById(R.id.durationTimer);
        mCurrentProgress = view.findViewById(R.id.seekBar);
        mSoundController = view.findViewById(R.id.soundController);
        mBufferProgress = view.findViewById(R.id.bufferProgress);
        mCurrentProgress.setMax ( 100 );

        mPlayButton.setOnClickListener ( this::onPlayPause );
        view.findViewById ( R.id.button_fullscreen ).setOnClickListener ( vw-> new Handler().post(this::onFullscreen ));
    
        view.findViewById ( R.id.button_restart ).setOnClickListener ( this::onRestart );
        view.findViewById ( R.id.button_skip_back ).setOnClickListener ( this::onSkipBack );
        view.findViewById ( R.id.button_skip_forward ).setOnClickListener ( this::onSkipForward );

        mVideoView.setOnClickListener(this::hideOrShowSoundController);

        mSoundController.setVisibility(View.GONE);
        //demoVideo = Uri.parse ("android.resource://" +getPackageName()+"/"+ R.raw.demo);
        position = 0;
        initializeSeekBar ();
        setHandler ();
        mVideoView.setVideoURI(CurrentVideoUri);
    
        mVideoView.setOnInfoListener ( (mediaPlayer, i, i1) -> {
            if(i == MediaPlayer.MEDIA_INFO_BUFFERING_START )
            {
                mBufferProgress.setVisibility ( View.VISIBLE );
            }
            else if(i == MediaPlayer.MEDIA_INFO_BUFFERING_END )
            {
                mBufferProgress.setVisibility ( View.INVISIBLE );
            
            }
        
            return false;
        } );
    
        mVideoView.setOnPreparedListener ( mediaPlayer -> {
            mCurrentProgress.setMax ( mediaPlayer.getDuration () );
        
            mDurationTimer.setText (HelperClass.FormatVideoDuration (mediaPlayer.getDuration ()));
            mVideoView.start ();
            isPlaying = true;
        
            videoProgress ();
        } );
        
        mPlayButton.setImageResource ( R.drawable.ic_play_arrow_button );

        isPlaying = true;
        hideOrShowSoundController(mPlayButton);
        isPlaying = false;

    }
    
    @Override
    public void onStop () {
        super.onStop ();
        
        isPlaying = false;
    }
    
    private Handler mVideoProgressHandler;
    
    private void videoProgress()
    {
        mVideoProgressHandler = new Handler (  );
        Runnable mVideoProgressRunnable = new Runnable () {
            
            @Override
            public void run () {
                if ( isPlaying ){
                    
                    mCurrentProgress.setProgress ( mVideoView.getCurrentPosition () );
                    mcurrentTimer.setText ( HelperClass.FormatVideoDuration ( mVideoView.getCurrentPosition () ) );
                }
                
                mVideoProgressHandler.postDelayed ( this, 0 );
            }
        };
        
        mVideoProgressHandler.postDelayed ( mVideoProgressRunnable, 0 );
    }
    
    private long position;
    
    private void onPlayPause(View view)
    {
        if(isPlaying)
        {
            mVideoView.pause ();
            isPlaying = false;
            mPlayButton.setImageResource ( R.drawable.ic_play_arrow_button );
        }else
        {
            mVideoView.start ();
            isPlaying = true;
            mPlayButton.setImageResource ( R.drawable.ic_pause_button_24dp );
        }
    }
    public void onRestart(View v)
    {
        mVideoView.seekTo ( 0 );
    }
    
    private void onSkipBack(View v)
    {
        mVideoView.seekTo ( (int)(position = Math.max ( 0, position - 15 * 1000 )));
    }
    
    private void onSkipForward(View v)
    {
        mVideoView.seekTo ( (int)(position = Math.min ( mVideoView.getDuration (), position + 15 * 1000 )));
    }
    
    private void onFullscreen()
    {
        Intent mIntentPlayVideo = new Intent (getContext (), ActivityVideoPlayer.class );
        mIntentPlayVideo.putExtra ( "position", position );
        mIntentPlayVideo.putExtra ( "url", CurrentVideoUri.toString());
        
        mVideoView.pause ();
        mPlayButton.setImageResource ( R.drawable.ic_play_arrow_button );
        startActivityForResult ( mIntentPlayVideo, 1 );
    }
    
    private Handler mVideoHandler;
    
    private void setHandler()
    {
        mVideoHandler = new Handler ();
        Runnable mVideoRunnable = new Runnable () {
            @Override
            public void run () {
                if ( mVideoView.getDuration () > 0 ){
                    mCurrentProgress.setProgress ( (int)(position = mVideoView.getCurrentPosition ()) );
                }
                mVideoHandler.postDelayed ( this, 0 );
            }
        };
        
        mVideoHandler.postDelayed ( mVideoRunnable, 0 );
    }
    
    private void initializeSeekBar()
    {
        mCurrentProgress.setProgress ( 0 );
        mCurrentProgress.setOnSeekBarChangeListener ( new SeekBar.OnSeekBarChangeListener () {
            @Override
            public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser)
            {
                if(fromUser)
                {
                    mVideoView.seekTo ( progress );
                }
            }
            
            @Override
            public void onStartTrackingTouch (SeekBar seekBar) {
            
            }
            
            @Override
            public void onStopTrackingTouch (SeekBar seekBar) {
            
            }
        } );
    }

    boolean hide = true;
    private void hideOrShowSoundController(View view)
    {
        if(isPlaying)
        {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mSoundController,View.ALPHA,mSoundController.getAlpha(),hide?0:1);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(hide?1000:500);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation, boolean isReverse) {
                    if(!hide)
                        mSoundController.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation, boolean isReverse) {
                    if(hide)
                        mSoundController.setVisibility(View.GONE);
                    else new Handler().postDelayed(animator::start,2000);
                    hide = !hide;
                }
            });
            animator.start();
        }
    }
    
    
    @Override
    public Testimonials getInstance () {
        return this;
    }
    public void setCurrentVideoUri(@NonNull Uri uri)
    {
        CurrentVideoUri = uri;
        mVideoView.setVideoURI(CurrentVideoUri);
    }
    public Uri getCurrentVideoUri(){return CurrentVideoUri;}
}
