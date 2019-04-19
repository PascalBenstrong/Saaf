package com.bitec.saafs.models;

import android.support.annotation.NonNull;

public class PostId {

    String postId;
    
    public String getPostId()
    {
        return postId;
    }

    public <T extends PostId> T withId(@NonNull final String id) {
        this.postId = id;
        return (T) this;
    }

}
