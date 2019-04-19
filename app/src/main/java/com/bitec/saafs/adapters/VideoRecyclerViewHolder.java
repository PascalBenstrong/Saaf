package com.bitec.saafs.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitec.saafs.R;

public final class VideoRecyclerViewHolder extends RecyclerView.ViewHolder
{
    public final ImageView imageView;
    public final TextView  nameText;
    public final TextView  descriptionText;
    
    public VideoRecyclerViewHolder (View itemView)
    {
        super(itemView);
        imageView = itemView.findViewById ( R.id.iv_image );
        nameText = itemView.findViewById (R.id.authorname);
        descriptionText = itemView.findViewById (R.id.videotitle);
        
    }
    
}