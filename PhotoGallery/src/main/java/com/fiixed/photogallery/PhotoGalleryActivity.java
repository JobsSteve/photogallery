package com.fiixed.photogallery;

import android.support.v4.app.Fragment;


/*
Activity returns an instance of PhotoGalleryFragment
 */
public class PhotoGalleryActivity extends SingleFragmentActivity {


    @Override
    protected Fragment createFragment() {
        return new PhotoGalleryFragment();
    }
}
