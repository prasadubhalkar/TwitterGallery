package mytwitter.android.pkubhalkar.com.twittertest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.io.Serializable;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Prasad on 3/15/2015.
 */
public class LoginFragment extends Fragment {
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "kpqicK6cbAN3qLgoVtxFGBXPs";
    private static final String TWITTER_SECRET = "iClI2B0V0Fjt7t2Oh4TBWZNKk39rXMHH0nUmcbFiUErBcjfba7";
    private TwitterLoginButton loginButton;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup parent,Bundle savedInstanceState){
        final TwitterFetcher fetcher = new TwitterFetcher(getActivity());

        View v = inflater.inflate(R.layout.login_fragment,parent,false);

        loginButton = (TwitterLoginButton)v.findViewById(R.id.login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
                fetcher.setSession(twitterSessionResult.data);

                //Intent i = new Intent(getActivity(),SuccessActivity.class);
                Intent i = new Intent(getActivity(),DrawerActivity.class);
                startActivity(i);
            }

            @Override
            public void failure(TwitterException e) {
                Log.d("FAILURE --------> ","Failed to Log in with twitter");
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        loginButton.onActivityResult(requestCode,resultCode,data);
    }
}
