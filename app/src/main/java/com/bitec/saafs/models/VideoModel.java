package com.bitec.saafs.models;

import com.bitec.saafs.interfaces.IPropertyChangeListener;

import java.util.ArrayList;
import java.util.List;

public class VideoModel
{
    private String title, str_thumb;
    private boolean isSelected;
    private boolean isPlaying;
    
    private List<IPropertyChangeListener> propertyChangeListeners = new ArrayList<> ();
    
    public String getTitle () {
        return title;
    }
    
    public void setTitle (String str_path) {
        this.title = str_path;
    }
    
    public String getStr_thumb () {
        return str_thumb;
    }
    
    public void setStr_thumb (String str_thumb) {
        this.str_thumb = str_thumb;
    }
    
    public boolean isSelected () {
        return isSelected;
    }
    
    public void setSelected (boolean isSelected) {
        this.isSelected = isSelected;
    }
    
    public boolean isPlaying () {
        return isPlaying;
    }
    
    public void setPlaying (boolean playing) {
        isPlaying = playing;
        
        for( IPropertyChangeListener p : propertyChangeListeners)
        {
            p.onPropertyChanged ( "isPlaying",isPlaying );
        }
    }
    
    public void addPropertyChangedListener(IPropertyChangeListener propertyChangeListener)
    {
        propertyChangeListeners.add ( propertyChangeListener );
    }
}
