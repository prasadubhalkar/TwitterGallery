package mytwitter.android.pkubhalkar.com.twittertest;

import android.app.SearchManager;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;


public class TwitterActivity extends GenericActivity {
    @Override
    protected Fragment createFragment(){
        return new LoginFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        //Pass the activity result to the fragment, which will
        //then pass the result to the login button
        FragmentManager fm  = getSupportFragmentManager();
        Fragment fragment   = fm.findFragmentById(R.id.fragmentContainer);

        if(fragment != null){
            fragment.onActivityResult(requestCode,resultCode,data);
        }
    }
}
