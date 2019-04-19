package com.bitec.saafs.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class BaseViewPager extends ViewPager {
    
    private final List<View> views = new ArrayList<> ();
    private View previousView = null;
    private int previous = 0;
    private final OnPageChangeListener listener = new SimpleOnPageChangeListener (){
        @Override
        public void onPageSelected (int position) {
            super.onPageSelected ( position );
            previousView = views.get ( previous);
            previous = position;
        }
    };
    
    public BaseViewPager (@NonNull Context context) {
        super ( context );
    }
    
    public BaseViewPager (@NonNull Context context, @Nullable AttributeSet attrs) {
        super ( context, attrs );
    }
    
    @Override
    public void addView (View child, int index, ViewGroup.LayoutParams params) {
        super.addView ( child, index, params );
        views.add ( index,child );
    }
    
    @Override
    public void addOnPageChangeListener (@NonNull OnPageChangeListener listener) {
        super.addOnPageChangeListener ( listener );
        removeOnPageChangeListener ( this.listener );
        super.addOnPageChangeListener ( this.listener );
    }
    
    public View getPreviousView()
    {
        return previousView;
    }
}
