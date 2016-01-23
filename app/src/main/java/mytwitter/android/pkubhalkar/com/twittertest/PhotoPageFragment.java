package mytwitter.android.pkubhalkar.com.twittertest;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

/**
 * Created by Prasad on 5/12/2015.
 */
public class PhotoPageFragment extends Fragment {
    private WebView mWebView;
    private TweetData mData;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mData = (TweetData)getActivity().getIntent().getSerializableExtra("TweetData");
    }

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_photo_page,parent,false);
        TextView textTweet = (TextView)v.findViewById(R.id.tweetText);
        TextView textDateTime = (TextView)v.findViewById(R.id.tweetDateTime);
        TextView textRetweet = (TextView)v.findViewById(R.id.tweetRetweets);
        TextView textFav = (TextView)v.findViewById(R.id.tweetFav);

        mWebView = (WebView)v.findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlloading(WebView view, String url) {
                return false;
            }
        });

        mWebView.loadUrl(mData.getURL());
        textTweet.setText(mData.getTweet());
        textDateTime.setText(mData.getCreatedAt());
        textRetweet.setText(String.valueOf(mData.getRetweetCount()));
        textFav.setText(String.valueOf(mData.getFavCount()));

        return v;
    }
}
