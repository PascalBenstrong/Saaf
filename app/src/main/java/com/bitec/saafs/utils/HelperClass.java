package com.bitec.saafs.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public final class HelperClass
{
    public static float DpToPixels( float dp, Context context)
    {
        Resources r = context.getResources ();
        return TypedValue.applyDimension ( TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics () );
    }
    
    public static String FormatVideoDuration(int duration)
    {
        int hours = duration/(1000 * 60 * 60 );
        int minutes = (duration % (1000 * 60 * 60 )) / (1000 * 60);
        int seconds = ((duration % (1000 * 60 * 60 )) % (1000 * 60) / 1000);
        
    
        return hours > 0 ?( hours + ":" + minutes + ":" + seconds ) :  minutes + ":" + seconds;
    }
}
