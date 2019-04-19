package com.bitec.saafs.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.bitec.saafs.ui.fragment.MyPlaylistFragment;
import com.bitec.saafs.ui.fragment.TrainingFragment;
import com.bitec.saafs.ui.fragment.VideosFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {


    public SectionsPagerAdapter (FragmentManager fm) {
        super(fm);
    }
    
    @Override
    public Fragment getItem (int position) {

        switch(position) {
            case 0:
                MyPlaylistFragment myPlaylistFragment = new MyPlaylistFragment();
                return myPlaylistFragment;

            case 1:
                VideosFragment videosFragment = new VideosFragment();
                return  videosFragment;

            case 2:
                TrainingFragment trainingFragment = new TrainingFragment();
                return trainingFragment;

            default:
                return  null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle (int position){

        switch (position) {
            case 0:
                return "MY PLAYLISTS";

            case 1:
                return "VIDEOS";

            case 2:
                return "TRAININGS";

            default:
                return null;
        }

    }
    
    
}
