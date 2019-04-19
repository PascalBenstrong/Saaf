package com.bitec.saafs.adapters;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class BasePageAdapter<T> extends PagerAdapter {
    protected final List<T> mItemsList = new ArrayList<> ();
    
    public BasePageAdapter () {
    }
    
    @Override
    public int getCount () {
        return mItemsList.size ();
    }
    
    public boolean isEmpty()
    {
        return mItemsList.isEmpty ();
    }
    
    @Override
    public boolean isViewFromObject (@NonNull View view, @NonNull Object o) {
        return view.equals ( o );
    }
    
    @NonNull
    @Override
    public Object instantiateItem (@NonNull ViewGroup container, int position) {
        
        return createItem ( container, position, mItemsList.get ( position ) );
    }
    
    public abstract Object createItem (@NonNull ViewGroup container, int position, T item);
    
    public void addWithOutNotify (T item) {
        mItemsList.add ( item );
    }
    
    public T getItem (int position) {
        return mItemsList.get ( position );
    }
    
    public void add (T item) {
        mItemsList.add ( item );
        notifyDataSetChanged ();
    }
    
    public void addRange (List<T> items) {
        mItemsList.addAll ( items );
    }
    
    @Override
    public void destroyItem (@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView ( (View) object );
    }
}
