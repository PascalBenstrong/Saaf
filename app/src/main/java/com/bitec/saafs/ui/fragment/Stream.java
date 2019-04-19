package com.bitec.saafs.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.FaceDetector;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bitec.saafs.R;
import com.bitec.saafs.adapters.BasePageAdapter;
import com.bitec.saafs.adapters.StreamsCommentsAdapter;
import com.bitec.saafs.models.Comment;
import com.bitec.saafs.models.Users;
import com.bitec.saafs.models.Video;
import com.bitec.saafs.ui.activities.ActivityVideoPlayer;
import com.bitec.saafs.utils.AnimationUtil;
import com.bitec.saafs.utils.HelperClass;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.client.Firebase;
import com.firebase.client.GenericTypeIndicator;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class Stream extends Fragment {
    
    private View           mView;
    private ViewPager      mViewPager;
    private RelativeLayout mAddCommentRelativeLayout;
    private EditText       mCommentText;
    private TextView mNumberOfComments;

    private BasePageAdapter<Video>          mVideoModelPageAdapter;

    private StreamsCommentsAdapter mCommentAdapter;
    String user_id, post_id;
    private FirebaseFirestore mFirestore;
    private FirebaseUser      mCurrentUser;
    private boolean           owner;
    private CircleImageView   user_image;
    private TextView          likes;
    private ProgressBar       mProgress;
    private MaterialFavoriteButton mLike;

    private static Users mUser;

    public Stream() {
    }

    public static void startActivity (@NonNull Context context, @NonNull List<Video> video,
                                      @NonNull String desc, int pos, boolean owner) {
        Intent intent = new Intent ( context, Stream.class );
        intent.putExtra ( "user_id", video.get ( pos ).getUserId () );
        intent.putExtra ( "post_desc", desc );
        intent.putExtra ( "post_id", video.get ( pos ).getPostId ());
        intent.putExtra ( "owner", owner );
        context.startActivity ( intent );
    }


    @Nullable
    @Override
    public View onCreateView (@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate ( R.layout.fragment_streams, container, false );
        return mView;
    }

    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {

        mViewPager = view.findViewById ( R.id.viewPager );

        CalligraphyConfig.initDefault ( new CalligraphyConfig.Builder ()
                .setDefaultFontPath ( "fonts/bold.ttf" )
                .setFontAttrId ( R.attr.fontPath )
                .build ()
        );

        view.findViewById ( R.id.add_comment ).setOnClickListener ( this::showOrHideAddCommentViewWithKeyboard);
        view.findViewById ( R.id.post_comment ).setOnClickListener ( this::onPostComment );
        mProgress = view.findViewById ( R.id.progressBar );
        mNumberOfComments = view.findViewById(R.id.commented);
        mCommentText = view.findViewById ( R.id.comment_text );
        mLike = view.findViewById(R.id.like_button);
        likes = view.findViewById(R.id.likes);

        mNumberOfComments.setText("0");
        mCommentText.setHint ( "Add a comment.." );
        mLike.setOnClickListener( vw -> onLike());

        mFirestore = FirebaseFirestore.getInstance ();
        mCurrentUser = FirebaseAuth.getInstance ().getCurrentUser ();

        mFirestore.collection ( "Users" )
                .document ( mCurrentUser.getUid () )
                .get ()
                .addOnSuccessListener ( documentSnapshot -> {
                    String username = documentSnapshot.getString("username");
                    String image = documentSnapshot.getString("image");
                    mUser = new Users(username,image);
                });

        mAddCommentRelativeLayout = view.findViewById ( R.id.relLayout3 );
        mAddCommentRelativeLayout.post ( () -> mAddCommentRelativeLayout.setTranslationY (
                mAddCommentRelativeLayout.getTranslationY () + mAddCommentRelativeLayout
                                                                       .getHeight () ) );

        user_id = getActivity ().getIntent ().getStringExtra ( "user_id" );
        post_id = getActivity ().getIntent ().getStringExtra ( "post_id" );

        mCommentAdapter = new StreamsCommentsAdapter (view.getContext (), owner );
        mCommentAdapter.addPropertyChangedListener((propertyName, property) -> {

            if(propertyName.equals(StreamsCommentsAdapter.CommentsPropety.removed.name()))
            {
                if(CurrentVideo != null)
                {
                    CurrentVideo.getComments().clear();
                    CurrentVideo.getComments().addAll(mCommentAdapter.getItems());
                    mNumberOfComments.setText(String.valueOf(mCommentAdapter.getItemCount()));
                }
            }
        });
        RecyclerView CommentsRecyclerView = view.findViewById ( R.id.comments_recycler_view );

        CommentsRecyclerView.setItemAnimator ( new DefaultItemAnimator () );
        CommentsRecyclerView.addItemDecoration (new DividerItemDecoration ( getActivity (), DividerItemDecoration.VERTICAL) );
        CommentsRecyclerView.setLayoutManager ( new LinearLayoutManager ( view.getContext () ));
        CommentsRecyclerView.setAdapter ( mCommentAdapter );

        mIntentPlayVideo = new Intent ( view.getContext (), ActivityVideoPlayer.class );

        initVideosView ();
    }

    private static Video CurrentVideo = null;
    private void initVideosView () {

        mVideoModelPageAdapter = new BasePageAdapter<Video> () {
            @Override
            public Object createItem (@NonNull ViewGroup container, int position, Video item) {

                View view = LayoutInflater.from ( container.getContext () ).inflate ( R.layout.slide_item, container, false );

                RelativeLayout _descriptionContainer = view.findViewById ( R.id.description_container );
                RelativeLayout _mediaConatiner       = view.findViewById ( R.id.media_container );

                ImageView            thumbnail       = view.findViewById ( R.id.iv_image );
                FloatingActionButton playButtonFloat = view.findViewById ( R.id.button_play_float );
                ImageView            playButton      = view.findViewById ( R.id.button_play );
                VideoView            videoView       = view.findViewById ( R.id.videoView );
                TextView             title           = view.findViewById ( R.id.videotitle );
                TextView             author          = view.findViewById ( R.id.authorname );
                ProgressBar          mBufferProgress = view.findViewById ( R.id.bufferProgress );
                SeekBar              seekBar         = view.findViewById ( R.id.seekBar );
                TextView             mcurrentTimer   = view.findViewById ( R.id.currentTimer );
                TextView             mDurationTimer  = view.findViewById ( R.id.durationTimer );
                view.findViewById ( R.id.button_fullscreen ).setOnClickListener ( vw -> new Handler().post(() -> onFullscreen ( videoView, playButton, Uri.parse ( item.getUrl () ), videoView.getCurrentPosition () )) );

                view.findViewById ( R.id.button_restart ).setOnClickListener ( vw -> onRestart (videoView ) );
                view.findViewById ( R.id.button_skip_back ).setOnClickListener ( vw -> onSkipBack
                                                                                               ( videoView, videoView.getCurrentPosition () - 15 * 1000 ) );
                view.findViewById ( R.id.button_skip_forward ).setOnClickListener ( vw -> onSkipForward ( videoView, videoView.getCurrentPosition () + 15 * 1000 ) );

                title.setText (item.getDescription ());
                author.setText (item.getName () );

                Glide.with ( container.getContext () ).load ( item.getUrl () )
                        .apply ( RequestOptions.skipMemoryCacheOf ( false ) )
                        .apply ( RequestOptions.diskCacheStrategyOf ( DiskCacheStrategy.AUTOMATIC
                                                                    ) )
                        .into ( thumbnail );

                item.addPropertyChangedListener ( (propertyName, property) -> {

                    if ( propertyName.equals ( Video.VideoProperty.isPlaying.name () ) ){
                        boolean v = (boolean) property;

                        if ( !v ){
                            videoView.pause ();
                            playButton.setImageResource ( R.drawable.ic_play_arrow_button );

                        } else {
                            videoView.start ();
                            playButton.setImageResource ( R.drawable.ic_pause_button_24dp );
                        }
                    }else if(propertyName.equals ( Video.VideoProperty.comments.name () ))
                    {
                        sendComment ();
                    }else if(propertyName.equals ( Video.VideoProperty.gettingComments.name() ))
                    {
                        if(CurrentVideo.getUrl().equals(item.getUrl()))
                        {
                            mCommentAdapter.refresh(CurrentVideo.getComments());
                            mNumberOfComments.setText(String.valueOf(mCommentAdapter.getItemCount()));
                        }
                    }

                } );

                mBufferProgress.setVisibility ( View.INVISIBLE );

                playButtonFloat.setOnClickListener ( vw -> {

                    videoView.setVisibility ( View.VISIBLE );
                    //Toast.makeText ( getContext (),item.getUserId ()+": "+item.getUrl (),Toast.LENGTH_SHORT ).show ();
                    videoView.setVideoURI ( Uri.parse ( item.getUrl () ) );

                    videoView.requestFocus ();
                    _mediaConatiner.setVisibility ( View.VISIBLE );
                    _descriptionContainer.setVisibility ( View.INVISIBLE );

                    thumbnail.setVisibility ( View.INVISIBLE );
                    playButtonFloat.hide ();

                    videoView.setOnPreparedListener ( mp -> {
                        videoView.setOnInfoListener ( (mediaPlayer, i, i1) -> {
                            if ( i == MediaPlayer.MEDIA_INFO_BUFFERING_START ){
                                mBufferProgress.setVisibility ( View.VISIBLE );
                            } else if ( i == MediaPlayer.MEDIA_INFO_BUFFERING_END ){
                                mBufferProgress.setVisibility ( View.INVISIBLE );

                            }

                            return false;
                        } );
                        videoView.seekTo ( 0 );
                        videoView.start ();
                        item.setPlaying ( true );
                        playButton.setImageResource ( R.drawable.ic_pause_button_24dp );

                        initializeSeekBar ( seekBar, videoView );
                        setHandler ( seekBar, videoView );

                        seekBar.setMax ( mp.getDuration () );

                        mDurationTimer.setText ( HelperClass.FormatVideoDuration ( mp.getDuration () ) );

                        videoProgress ( seekBar, videoView, mcurrentTimer, item );
                    } );

                } );

                playButton.setOnClickListener ( vw -> item.setPlaying ( !item.isPlaying () ));

                container.addView ( view, 0 );
                return view;
            }
        };


        mViewPager.setAdapter ( mVideoModelPageAdapter );
        mViewPager.addOnPageChangeListener ( new ViewPager.SimpleOnPageChangeListener () {

            private int previousItem = 0;
            @Override
            public void onPageSelected (int position) {
                super.onPageSelected ( position );

                if(previousItem != position)
                {
                    ((BasePageAdapter<Video>)mViewPager.getAdapter ()).getItem ( previousItem ).setPlaying ( false );
                    previousItem = position;

                    Video video = ((BasePageAdapter<Video>)mViewPager.getAdapter ()).getItem ( position );

                   if(video != null)
                   {
                       CurrentVideo = video;
                       mCommentAdapter.refresh(CurrentVideo.getComments());
                       mNumberOfComments.setText(String.valueOf(mCommentAdapter.getItemCount()));
                       SetLiked();
                   }
                }
            }
        } );

        new Handler().post(this::getAllVideos);
    }

    private void getAllVideos () {

        mFirestore.collection("Streams")
                .addSnapshotListener(getActivity(),
                        (queryDocumentSnapshots, e) -> {

                            if (e != null) {
                                Log.w("Error", "listen:error", e);
                                return;
                            }

                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    Video video = doc.getDocument().toObject(Video.class);

                                    if (mVideoModelPageAdapter.getCount() == 0)
                                    {
                                        CurrentVideo = video;
                                        SetLiked();
                                        mCommentAdapter.refresh(CurrentVideo.getComments());
                                        mNumberOfComments.setText(String.valueOf(mCommentAdapter.getItemCount()));
                                    }

                                    mVideoModelPageAdapter.add(video);

                                    mVideoModelPageAdapter.notifyDataSetChanged();

                                    video.setUpCommentListenerWithProgressBar(mProgress);
                                }
                            }
                        });
    }

    private boolean            hidden = true;
    private InputMethodManager inputMethodManager;

    //Online Likes

    OnCompleteListener<DocumentSnapshot> onCompleteListener = null;
    OnFailureListener onFailedListener = null;
    private void onLike()
    {
        if(CurrentVideo != null) {
            final Map<String, String> data = new HashMap<>();
            final Map<String, Object> liked_videos = new HashMap<>();
            data.put(CurrentVideo.getPostId(), CurrentVideo.getDescription());
            liked_videos.put("liked_videos", data);

            onCompleteListener = task ->
            {
                boolean exists = task.getResult().contains("liked_videos");
                if (exists) {
                    if (mLike.isFavorite())
                    {
                        Map<String,Object> r_vids = new HashMap<>();
                        r_vids.put(String.valueOf("liked_videos."+CurrentVideo.getPostId()), FieldValue.delete());

                        mFirestore.collection("Users").document(mCurrentUser.getUid()).update(r_vids );

                        mFirestore.collection("Streams").document(CurrentVideo.getPostId()).update("likes",FieldValue.increment(-1)).addOnCompleteListener(getActivity(),task1 ->
                        {

                            if(task1.isSuccessful())
                            {
                                mFirestore.collection("Streams").document(CurrentVideo.getPostId()).get().addOnCompleteListener(getActivity(),task2 -> {
                                if(task2.getResult() != null)
                                {

                                    Map<String,Object> _data = task2.getResult().getData();
                                    if(_data != null)
                                        FormatLikes(Long.valueOf(String.valueOf(_data.get("likes"))));
                                    else
                                        FormatLikes(0);
                                    //Toast.makeText(getContext(),String.valueOf(),Toast.LENGTH_SHORT).show();
                                }
                            });
                            }
                        });

                    }
                    else {
                        mFirestore.collection("Users").document(mCurrentUser.getUid()).set(liked_videos , SetOptions.merge()).addOnCompleteListener(getActivity(), task1 ->
                                {
                                    mFirestore.collection("Streams").document(CurrentVideo.getPostId()).update("likes",FieldValue.increment(1)).addOnCompleteListener(getActivity(), task2 ->
                                    {
                                        if(task2.isSuccessful())
                                        {
                                           mFirestore.collection("Streams").document(CurrentVideo.getPostId()).get().addOnCompleteListener(getActivity(), task3 ->
                                           {
                                              long _likes = task3.getResult().getLong("likes") ;

                                               FormatLikes(_likes);
                                           });
                                        }
                                    });
                                });

                    }
                    mLike.setFavorite(!mLike.isFavorite(),true);
                }
            };

            onFailedListener = e -> Toast.makeText(getContext(), CurrentVideo.getDescription() + " : False \n"+e.getMessage() , Toast.LENGTH_SHORT).show();

            mFirestore.collection("Users").document(mCurrentUser.getUid()).get(Source.CACHE).addOnCompleteListener(getActivity(), onCompleteListener).addOnFailureListener(getActivity(),onFailedListener);

            //mFirestore.collection("Users").document(mCurrentUser.getUid()).set(liked_videos , SetOptions.merge());

        }
    }
    final char[] suf = {' ','K','M','B'};
    private void FormatLikes(long likes)
    {
        int n = 0;

        long f = likes/(long)(Math.pow(1000,n));

        while (f >= 1000 && n < suf.length-1)
        {
            n++;
            f = likes/(long)(Math.pow(1000,n));
        }
        this.likes.setText(String.valueOf(f+String.valueOf(suf[n])));

    }

    OnCompleteListener<DocumentSnapshot> onLikedCompleteListener = null;
    //OnFailureListener onLikedFailedListener = null;

    private void SetLiked()
    {
        onLikedCompleteListener = task ->
        {
            boolean exists = task.getResult().contains("liked_videos");

            if (exists) {
                Map<String,Object> liked_videos = (Map<String, Object>) task.getResult().get("liked_videos");

                if(liked_videos != null && liked_videos.containsKey(CurrentVideo.getPostId()))
                {
                    //Toast.makeText(getContext(),String.valueOf("key: "+CurrentVideo.getPostId()+" value: "+liked_videos.get(CurrentVideo.getPostId())),Toast.LENGTH_SHORT).show();
                    mLike.setFavorite(true,true);
                }else
                {
                    mLike.setFavorite(false,true);
                }
            }
            else {
                mLike.setFavorite(false,true);
            }
        };

        //onLikedFailedListener = e -> Toast.makeText(getContext(), CurrentVideo.getDescription() + " : False \n"+e.getMessage() , Toast.LENGTH_SHORT).show();
        mFirestore.collection("Users").document(mCurrentUser.getUid()).get(Source.CACHE).addOnCompleteListener(getActivity(), onLikedCompleteListener);
       if(CurrentVideo != null)
        {
            mFirestore.collection("Streams").document(CurrentVideo.getPostId()).get().addOnCompleteListener(getActivity(),task1 -> {
                if(task1.getResult() != null)
                {

                    Map<String,Object> data = task1.getResult().getData();
                    if(data != null && data.containsKey("likes"))
                        FormatLikes(Long.valueOf(String.valueOf(data.get("likes"))));
                    else
                        FormatLikes(0);
                    //Toast.makeText(getContext(),String.valueOf(),Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    //Online comments
    private void sendNotification () {

        Map<String, Object> commentNotification = new HashMap<> ();
        //commentNotification.put ( "post_desc", post_desc.getText ().toString () );
        commentNotification.put ( "owner", owner );
        commentNotification.put ( "post_id", post_id );
        commentNotification.put ( "admin_id", user_id );
        commentNotification.put ( "notification_id", String.valueOf ( System.currentTimeMillis ()
                                                                    ) );
        commentNotification.put ( "timestamp", String.valueOf ( System.currentTimeMillis () ) );

        mFirestore.collection ( "Notifications" )
                .document ( mCurrentUser.getUid () )
                .collection ( "Comment" )
                .add ( commentNotification )
                .addOnSuccessListener ( documentReference -> Log.i ( "Comment Message", "success"
                                                                   ) )
                .addOnFailureListener ( e -> Log.w ( "Comment Message", "failure", e ) );

    }

    private void onPostComment(View view) {

            String comment = mCommentText.getText ().toString ();
            if ( !TextUtils.isEmpty ( comment ) ){
                showOrHideAddCommentViewWithKeyboard( view );

                String id = mCurrentUser.getUid();
                Comment c = new Comment ( id,mUser.getImage(),mUser.getName(),CurrentVideo.getPostId (),comment,String.valueOf ( System.currentTimeMillis () ) );
                CurrentVideo.addComment ( c );

                mCommentText.setText ( "" );
            } else {
                AnimationUtil.shakeView ( mCommentText, view.getContext () );
            }
            mCommentText.getText ().clear ();


    }

    private void sendComment () {

        mProgress.setVisibility(View.VISIBLE);

        Comment comment = CurrentVideo.getComments().get(CurrentVideo.getComments().size() - 1);

        CurrentVideo.getComments().remove(CurrentVideo.getComments().size()-1);

        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("id", comment.getId());
        commentMap.put("username", comment.getUsername());
        commentMap.put("image", comment.getImage());
        commentMap.put("post_id", CurrentVideo.getPostId());
        commentMap.put("comment", comment.getComment());
        commentMap.put("timestamp", comment.getTimestamp());

        mFirestore.collection("Streams")
                .document(CurrentVideo.getPostId())
                .collection("Comments")
                .add(commentMap)
                .addOnSuccessListener(documentReference -> {
                    mProgress.setVisibility(View.GONE);
                    sendNotification();
                    mCommentText.setHint("Add a comment..");
                    mCommentText.getText().clear();
                })
                .addOnFailureListener(e -> {
                    mProgress.setVisibility(View.GONE);
                    Toast.makeText(mView.getContext(), "Error sending " +
                            "comment: "
                            + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Error send comment", e.getMessage());
                });

    }

    private void showOrHideAddCommentViewWithKeyboard(View view) {
        if ( mAddCommentRelativeLayout == null ){
            return;
        }

        if ( inputMethodManager == null ){
            inputMethodManager = (InputMethodManager) view.getContext ().getSystemService (
                    Context.INPUT_METHOD_SERVICE );
        }

        SpringAnimation mAddCommentRelativeLayoutAnimation = new SpringAnimation (
                mAddCommentRelativeLayout, DynamicAnimation.TRANSLATION_Y, hidden ? 0 : (int) (mAddCommentRelativeLayout.getHeight () * 1.5) );
        mAddCommentRelativeLayoutAnimation.addEndListener ( (da, c, va, vel) -> {
            if ( !hidden ){
                View v = getActivity ().getCurrentFocus ();
                if ( v != null ){
                    inputMethodManager.hideSoftInputFromWindow ( v.getWindowToken (),
                            InputMethodManager
                                    .HIDE_NOT_ALWAYS );
                }
            } else {
                inputMethodManager.toggleSoftInput ( InputMethodManager.SHOW_IMPLICIT,
                        InputMethodManager.HIDE_NOT_ALWAYS );
                mCommentText.requestFocus();
            }
            hidden = !hidden;
        } );

        new Handler ().post ( mAddCommentRelativeLayoutAnimation::start );
    }

    //Video Playback controller

    private void onRestart (VideoView videoView) {
        videoView.seekTo ( 0 );
    }

    private void onSkipBack (VideoView videoView, int position) {
        videoView.seekTo ( Math.max ( 0, position - 15 * 1000 ) );
    }

    private void onSkipForward (VideoView videoView, int position) {
        videoView.seekTo ( Math.min ( videoView.getDuration (), position + 15 * 1000 ) );
    }

    Intent mIntentPlayVideo;

    private void onFullscreen (@NonNull VideoView videoView, @NonNull ImageView mPlayButton, Uri videoUri, long position) {
        mIntentPlayVideo.putExtra ( "position", position );
        mIntentPlayVideo.putExtra ( "url", videoUri.toString () );

        videoView.pause ();
        mPlayButton.setImageResource ( R.drawable.ic_play_arrow_button );
        startActivityForResult ( mIntentPlayVideo, 1 );
    }

    private void setHandler (SeekBar seekBar, VideoView videoView) {
        Handler mVideoHandler = new Handler ();
        Runnable mVideoRunnable = new Runnable () {
            @Override
            public void run () {
                if ( videoView.getDuration () > 0 ){
                    seekBar.setProgress ( videoView.getCurrentPosition () );
                }
                mVideoHandler.postDelayed ( this, 0 );
            }
        };

        mVideoHandler.postDelayed ( mVideoRunnable, 0 );
    }

    private void initializeSeekBar (SeekBar seekBar, VideoView videoView) {
        seekBar.setProgress ( 0 );
        seekBar.setOnSeekBarChangeListener ( new SeekBar.OnSeekBarChangeListener () {
            @Override
            public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser) {
                if ( fromUser ){
                    videoView.seekTo ( progress );
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

    private void videoProgress (SeekBar seekBar, VideoView videoView, TextView currentTimer,
                                Video video) {
        Handler videoProgressHandler = new Handler ();
        Runnable videoProgressRunnable = new Runnable () {

            @Override
            public void run () {
                if ( video.isPlaying () ){

                    seekBar.setProgress ( videoView.getCurrentPosition () );
                    currentTimer.setText ( HelperClass.FormatVideoDuration ( videoView
                            .getCurrentPosition () ) );
                }

                videoProgressHandler.postDelayed ( this, 0 );
            }
        };

        videoProgressHandler.postDelayed ( videoProgressRunnable, 0 );
    }
}
