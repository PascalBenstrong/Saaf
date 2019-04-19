package com.bitec.saafs.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.bitec.saafs.interfaces.IPropertyChangeListener;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements IPropertyChangeListener
{
    protected final List<T> mItemsList = new ArrayList<> ();
    protected RequestViewHolderCreation mRequestViewHolderCreation;
    protected List<ViewHolderBinding> mViewHolderBinding = new ArrayList<> (  );
    
    @NonNull
    @Override
    public  RecyclerView.ViewHolder onCreateViewHolder (@NonNull ViewGroup viewGroup, int i) {
    
        assert mRequestViewHolderCreation != null;
        return mRequestViewHolderCreation.CreateViewHolder ( viewGroup,i );
    }
    
    @Override
    public void onBindViewHolder (@NonNull RecyclerView.ViewHolder viewHolder, int i)
    {
        for ( ViewHolderBinding binding:mViewHolderBinding) {
         
            binding.BindViewHolder ( viewHolder ,getItem ( i ), i );
        }
    }
    
    public void addCreateViewHolder( @NonNull RequestViewHolderCreation holderCreation)
    {
        mRequestViewHolderCreation = holderCreation;
        onPropertyChanged ( BaseRecyclerViewAdapterProperty.requestViewHolderCreation.name (),mRequestViewHolderCreation );
    }
    
    public void addViewHolderBinding(@NonNull ViewHolderBinding viewHolderBinding)
    {
        mViewHolderBinding.add ( viewHolderBinding);
        onPropertyChanged ( BaseRecyclerViewAdapterProperty.viewHolderBinding.name (),mViewHolderBinding );
    }
    
    @Override
    public int getItemCount () {
        return mItemsList.size ();
    }
    
    public void refresh(List<T> items)
    {
        mItemsList.clear ();
        mItemsList.addAll ( items );
        notifyDataSetChanged ();
        
        onPropertyChanged ( BaseRecyclerViewAdapterProperty.itemsList.name (), mItemsList );
    }
    
    public void clear()
    {
        mItemsList.clear ();
        notifyDataSetChanged();
    }
    
    public void add(T item)
    {
        mItemsList.add ( item );
        notifyItemInserted ( getItemCount () -1 );

        onPropertyChanged ( BaseRecyclerViewAdapterProperty.itemsList.name (), mItemsList );

    }
    
    public void addWithOutNotify(T item)
    {
        mItemsList.add ( item );
        onPropertyChanged ( BaseRecyclerViewAdapterProperty.itemsList.name (),mItemsList );
    }
    
    public T getItem(int position)
    {
        return mItemsList.get (position);
    }

    public List<T> getItems(){return mItemsList;}
    
    public boolean isEmpty()
    {
        return mItemsList.isEmpty ();
    }
    
    @Override
    public void onPropertyChanged (String propertyName, Object property) {
    
    }
    
    public interface RequestViewHolderCreation
    {
        RecyclerView.ViewHolder CreateViewHolder (@NonNull ViewGroup parent, int viewType);
    }
    
    public interface ViewHolderBinding
    {
        void BindViewHolder (@NonNull RecyclerView.ViewHolder viewHolder,@NonNull Object item, int position);
    }
    
    public enum BaseRecyclerViewAdapterProperty
    {
        itemsList, requestViewHolderCreation, viewHolderBinding
    }
}

