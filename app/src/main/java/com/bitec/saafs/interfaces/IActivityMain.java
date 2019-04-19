package com.bitec.saafs.interfaces;

import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.bitec.saafs.ui.activities.MainActivity;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public interface IActivityMain {
    
    Toolbar getToolbar();
    TextView getUsername();
    MainActivity getActivity();
    FirebaseUser getCurrentuser ();
    Fragment getmCurrentFragment ();
    String getUserId();
    CircleImageView getImageView();
    
    void setmCurrentFragment(Fragment fragment);
}
