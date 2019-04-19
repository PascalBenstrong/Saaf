package com.bitec.saafs.models;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;

import com.bitec.saafs.interfaces.IPropertyChangeListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class Video extends PostId{

    long likes;
    String userId,name, description;
    String                        url;
    private List<Comment> comments = new ArrayList<>();
    private boolean                       isPlaying               = false;
    private List<IPropertyChangeListener> propertyChangeListeners = new ArrayList<> ();

    public Video (String userId, String name, String url, String description,int likes) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.url = url;
        this.likes = likes;
    }
    
    public Video() {
    }

    public String getUrl()
    {
        return url;
    }
    
    public String getUserId()
    {
        return userId;
    }

    public String getName() {
        return name;
    }

    public long getLikes(){return likes;}

    public void setName(String name) {
        this.name = name;
    
        for( IPropertyChangeListener p : propertyChangeListeners)
        {
            p.onPropertyChanged ( VideoProperty.name.name (),this.name );
        }
    }

    public void setLikes(long likes)
    {
        this.likes = likes;
    }

    public String getDescription() {
        return description;
    }
    
    public List<Comment> getComments()
    {
        return comments;
    }
    
    public void addComment(Comment comment)
    {
        comments.add ( comment );
        for( IPropertyChangeListener p : propertyChangeListeners)
            p.onPropertyChanged ( VideoProperty.comments.name () ,comments );
    }
    
    public void addCommentWithoutNotify(Comment comment)
    {
        comments.add ( comment );
        for( IPropertyChangeListener p : propertyChangeListeners)
            p.onPropertyChanged ( VideoProperty.gettingComments.name() ,comments );
    }

    public void setUpCommentListenerWithProgressBar(final ProgressBar progressBar)
    {
        if(getPostId() == null)
            return;
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        progressBar.setVisibility ( View.VISIBLE );
        mFirestore.collection ( "Streams" )
                .document (getPostId ())
                .collection ( "Comments" )
                .orderBy ( "timestamp", Query.Direction.ASCENDING )
                .addSnapshotListener ( (Activity) progressBar.getContext (), (querySnapshot, e) -> {
                    if ( e != null ){
                        progressBar.setVisibility ( View.GONE );
                        e.printStackTrace ();
                        return;
                    }

                    for (DocumentChange doc : querySnapshot.getDocumentChanges ()) {

                        if ( doc.getDocument ().exists () ){
                            if ( doc.getType () == DocumentChange.Type.ADDED ){

                                Comment comment = doc.getDocument ().toObject ( Comment.class
                                ).withId ( doc.getDocument ().getId () );

                                addCommentWithoutNotify ( comment );
                            }

                            if ( querySnapshot.getDocuments ().size () == comments.size() ){
                                progressBar.setVisibility ( View.GONE );
                            }
                        } else {
                            progressBar.setVisibility ( View.GONE );
                        }

                    }

                } );
    }
    
    public void setDescription (String description) {
        this.description = description;
    
        for( IPropertyChangeListener p : propertyChangeListeners)
        {
            p.onPropertyChanged ( VideoProperty.description.name (),this.description );
        }
    }
    
    public boolean isPlaying () {
        return isPlaying;
    }
    
    public void setPlaying (boolean playing) {
        isPlaying = playing;
        
        for( IPropertyChangeListener p : propertyChangeListeners)
        {
            p.onPropertyChanged ( VideoProperty.isPlaying.name (),isPlaying );
        }
    }
    
    public void addPropertyChangedListener(IPropertyChangeListener propertyChangeListener)
    {
        propertyChangeListeners.add ( propertyChangeListener );
    }
    
    public enum VideoProperty {
        userId,name,description,url, comments, isPlaying, gettingComments
    }
}
