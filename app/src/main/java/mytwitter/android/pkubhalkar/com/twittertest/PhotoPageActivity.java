package mytwitter.android.pkubhalkar.com.twittertest;

import android.support.v4.app.Fragment;

/**
 * Created by Prasad on 5/12/2015.
 */
public class PhotoPageActivity extends GenericActivity {
    @Override
    public Fragment createFragment(){
        return new PhotoPageFragment();
    }
}
