package com.bitec.saafs.models;

import android.support.annotation.NonNull;

public class CommentId {

    String commentId;
    
    public String getCommentId()
    {
        return commentId;
    }

    public <T extends CommentId> T withId(@NonNull final String id) {
        this.commentId = id;
        return (T) this;
    }

}
