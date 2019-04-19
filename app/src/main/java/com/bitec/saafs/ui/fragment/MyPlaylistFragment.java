package com.bitec.saafs.ui.fragment;


import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bitec.saafs.R;
import com.bitec.saafs.adapters.BaseRecyclerViewAdapter;
import com.bitec.saafs.adapters.VideoRecyclerViewHolder;
import com.bitec.saafs.models.VideoModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyPlaylistFragment extends Fragment {


    private View mMainView;
    
    RecyclerView                   recyclerView;
    RecyclerView.LayoutManager recycleViewLayoutManager;
    
    private BaseRecyclerViewAdapter<VideoModel> mVideosAdapter;
    private RelativeLayout                      mPlayButton;
    
    
    
    public MyPlaylistFragment () {
        // Required empty public constructor
    }
    
    public View onCreateView (@NonNull LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        
        mMainView = inflater.inflate ( R.layout.fragment_my_playlist, container, false );
        
        init();
        
        // Inflate the layout for this fragment
        return mMainView;
    }
    
    private void init () {
        
        mVideosAdapter = new BaseRecyclerViewAdapter<VideoModel> () {};
        fetchVideosFromGallery ();
        mVideosAdapter.addCreateViewHolder ( (v,vt) -> new VideoRecyclerViewHolder ( LayoutInflater.from ( v.getContext () ).inflate ( R.layout.custom_video, v, false ) ) );
        
        mVideosAdapter.addViewHolderBinding ( (h,item,p) -> Glide.with ( h.itemView.getContext () ).load ( "file://" + ((VideoModel)item).getStr_thumb () )
                                                                    .apply ( RequestOptions.skipMemoryCacheOf ( false ) )
                                                                    .apply ( RequestOptions.diskCacheStrategyOf ( DiskCacheStrategy.AUTOMATIC ) )
                                                                    .into (((VideoRecyclerViewHolder)h).imageView ) );
    
        mVideosAdapter.addViewHolderBinding ( (viewHolder, item, position) -> {
            mPlayButton = viewHolder.itemView.findViewById ( R.id.videobutton );
            VideoRecyclerViewHolder holder = (VideoRecyclerViewHolder)viewHolder;
            
            holder.descriptionText.setText (((VideoModel)item).getTitle ());
            
            mPlayButton.setOnClickListener ( v -> {
                Testimonials.Testimony.getInstance ().setCurrentVideoUri(Uri.parse ( ((VideoModel)item).getStr_thumb () ) );
                Testimonials.Testimony.getInstance ().title.setText ( ((VideoModel)item).getTitle () );
                if(Testimonials.Testimony.getInstance ().isPlaying)
                {
                    Testimonials.Testimony.getInstance ().mVideoView.seekTo ( 0 );
                    Testimonials.Testimony.getInstance ().mVideoView.start ();
                    Testimonials.Testimony.getInstance ().isPlaying = true;
                
                    Testimonials.Testimony.getInstance ().mPlayButton.setImageResource ( R.drawable.ic_pause_button_24dp );
                }else
                {
                    Testimonials.Testimony.getInstance ().mVideoView.start ();
                    Testimonials.Testimony.getInstance ().isPlaying = true;
                
                    Testimonials.Testimony.getInstance ().mPlayButton.setImageResource ( R.drawable.ic_pause_button_24dp );
                }
                Toast.makeText ( v.getContext (), "video playing", Toast.LENGTH_SHORT ).show ();
            } );
        });
        
        recyclerView = mMainView.findViewById ( R.id.conv_list );
        recycleViewLayoutManager = new GridLayoutManager ( getContext (), 1 );
        recyclerView.setLayoutManager ( recycleViewLayoutManager );
        recyclerView.setAdapter ( mVideosAdapter );
    }
    
    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult ( requestCode, permissions, grantResults );
        
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED )
        {
            // if(requestCode == 1)
            // {
            fetchVideosFromGallery ();
            // }
        }
    }
    
    private void fetchVideosFromGallery () {
        Uri    uri;
        Cursor cursor;
        int    column_index_data, column_index_folder_name, column_id, thum;
        
        String absolutePathImage = null;
        
        if(ContextCompat.checkSelfPermission ( getContext (), Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions ( getActivity (), new String []{Manifest.permission.READ_EXTERNAL_STORAGE}, 1 );
            return;
        }
        
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        
        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Thumbnails.DATA};
        
        String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        
        if ( getContext () != null ){
            
            cursor = getContext ().getContentResolver ().query ( uri, projection, null, null,
                                                                 orderBy + " DESC" );
            
            column_index_data = cursor != null ? cursor.getColumnIndexOrThrow ( MediaStore.MediaColumns.DATA ) : 0;
            //column_index_folder_name = cursor.getColumnIndexOrThrow ( MediaStore.Video.Media
            //.BUCKET_DISPLAY_NAME );
            //column_id = cursor.getColumnIndexOrThrow ( MediaStore.Video.Media._ID );
            thum = cursor != null ? cursor.getColumnIndexOrThrow ( MediaStore.Video.Thumbnails.DATA ) : 0;
            
            while (cursor != null && cursor.moveToNext ()) {
                absolutePathImage = cursor.getString ( column_index_data );
                
                VideoModel videoModel = new VideoModel ();
                
                videoModel.setSelected ( false );
                videoModel.setTitle ( new File (absolutePathImage).getName () );
                videoModel.setStr_thumb ( cursor.getString ( thum ) );
                
                mVideosAdapter.addWithOutNotify ( videoModel );
            }
            
            mVideosAdapter.notifyDataSetChanged ();
            if ( cursor != null ){
                cursor.close ();
            }
        }
        
    }
}
