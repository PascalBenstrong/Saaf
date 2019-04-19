package com.bitec.saafs.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bitec.saafs.R;

public class CommentRecyclerViewHolder extends RecyclerView.ViewHolder
{
    public final TextView comment;
    
    public CommentRecyclerViewHolder (@NonNull View itemView) {
        super ( itemView );
        
        comment = itemView.findViewById ( R.id.comment );
    }
    
}
