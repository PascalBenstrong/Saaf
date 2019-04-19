package com.bitec.saafs.ui.fragment;


import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bitec.saafs.R;
import com.bitec.saafs.adapters.BaseRecyclerViewAdapter;
import com.bitec.saafs.adapters.VideoRecyclerViewHolder;
import com.bitec.saafs.models.Video;
import com.bitec.saafs.ui.activities.MainActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tylersuehr.esr.EmptyStateRecyclerView;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class VideosFragment extends Fragment {
    private View mMainView;
    
    private static final String                         TAG = "FireLog";
    private              FirebaseFirestore              mFirestore;
    private              EmptyStateRecyclerView         mRecyclerView;
    private              BaseRecyclerViewAdapter<Video> mRecyclerAdapter;
    private              ProgressBar                    pbar;
    
    public View onCreateView (@NonNull LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        mMainView = inflater.inflate ( R.layout.fragment_videos, container, false );
        return mMainView;
    }
    
    @Override
    public void onViewCreated (View view, @Nullable Bundle savedInstanceState) {
        
        pbar = view.findViewById ( R.id.pbar );
        
        mRecyclerView = mMainView.findViewById ( R.id.video_recyclerview );
        mRecyclerView.setHasFixedSize ( true );
        mRecyclerView.setLayoutManager ( new LinearLayoutManager ( mMainView.getContext () ) );
        
        init ();
        mRecyclerView.setAdapter ( mRecyclerAdapter );
        
        mFirestore = FirebaseFirestore.getInstance ();
        
        pbar.setVisibility ( View.VISIBLE );
        new Handler ( ).post ( this::getAllVideos );
        
    }
    
    private void init () {
        mRecyclerAdapter = new BaseRecyclerViewAdapter<Video> () {
        };
        
        mRecyclerAdapter.addCreateViewHolder ( (viewHolder, vt) -> (new VideoRecyclerViewHolder (
                LayoutInflater.from ( viewHolder.getContext () ).inflate ( R.layout.custom_video,
                                                                           viewHolder, false ) )) );
        mRecyclerAdapter.addViewHolderBinding ( (viewHolder, item, p) -> (
                (VideoRecyclerViewHolder) viewHolder).nameText.setText ( ((Video) (item)).getName () ) );

        mRecyclerAdapter.addViewHolderBinding ( (viewHolder, item, p) -> (
                (VideoRecyclerViewHolder) viewHolder).descriptionText.setText ( ((Video) (item)).getDescription () ) );

        mRecyclerAdapter.addViewHolderBinding ( (viewHolder, item, p) -> Glide.with ( viewHolder
                                                                                              .itemView.getContext () )
                                                                                 .setDefaultRequestOptions ( new RequestOptions ().placeholder ( R.drawable.gradient ) )
                                                                                 .load ( ((Video) (item)).getUrl () )
                                                                                 .into ( (
                                                                                         (VideoRecyclerViewHolder) viewHolder).imageView ) );
        
        mRecyclerAdapter.addViewHolderBinding ( (viewHolder, item, position) -> {

            RelativeLayout mPlayButton = viewHolder.itemView.findViewById ( R.id.videobutton );
            
            mPlayButton.setOnClickListener ( v -> {
                
                Testimonials.Testimony.getInstance ().setCurrentVideoUri ( Uri.parse ( (
                        (Video) item).getUrl () ) );

                Video video = (Video)item;
                String title = String.format(Locale.getDefault(),"%s-%s",video.getDescription(),video.getName() );
                Testimonials.Testimony.getInstance ().title.setText (title);
                
                if ( Testimonials.Testimony.getInstance ().isPlaying ){
                    Testimonials.Testimony.getInstance ().mVideoView.seekTo ( 0 );
                    Testimonials.Testimony.getInstance ().mVideoView.start ();
                    Testimonials.Testimony.getInstance ().isPlaying = true;
                    
                    
                    Testimonials.Testimony.getInstance ().mPlayButton.setImageResource ( R.drawable.ic_pause_button_24dp );
                } else {
                    Testimonials.Testimony.getInstance ().mVideoView.start ();
                    Testimonials.Testimony.getInstance ().isPlaying = true;
                    
                    Testimonials.Testimony.getInstance ().mPlayButton.setImageResource ( R.drawable.ic_pause_button_24dp );
                }
            } );
        } );
    }
    
    private void getAllVideos () {
        
        mFirestore.collection ( "Testimonial" ).addSnapshotListener ( MainActivity.activity.getActivity (), (queryDocumentSnapshots, e) ->
        {
            if ( e != null ){
                pbar.setVisibility ( View.GONE );
                mRecyclerView.invokeState ( EmptyStateRecyclerView.STATE_ERROR );
                Log.w ( "Error", "listen:error", e );
                return;
            }
            
            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges ()) {
                if ( doc.getType () == DocumentChange.Type.ADDED ){
                    
                    Video videos = doc.getDocument ().toObject ( Video.class );
                    mRecyclerAdapter.add ( videos );
                    
                    pbar.setVisibility ( View.GONE );
                    
                    mRecyclerAdapter.notifyDataSetChanged ();
                }
            }
            
            
        } );
    }
}
