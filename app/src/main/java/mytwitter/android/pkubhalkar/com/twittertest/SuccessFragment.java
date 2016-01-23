package mytwitter.android.pkubhalkar.com.twittertest;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.tweetui.TweetViewAdapter;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Prasad on 3/16/2015.
 */
@TargetApi(11)
public class SuccessFragment extends Fragment {
    private static final String TAG = "SuccessFragment";
    private TwitterFetcher                  fetcher;
    private TweetViewAdapter                adapter;
    private ArrayList<Tweet>                emptyTweets;
    private GridView                        mGridView;
    private ThumnailDownloader<ImageView>   mThumbnailThread;
    List<String> mediaURLs;
    List<Tweet> mediaTweets;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mThumbnailThread = new ThumnailDownloader<ImageView>(new Handler());
        mThumbnailThread.setListener(new ThumnailDownloader.Listener<ImageView>(){
            public void onThumbnailDownloaded(ImageView imageView,Bitmap thumbnail){
                if(isVisible()){
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        mThumbnailThread.setContext(getActivity());
        mThumbnailThread.start();
        mThumbnailThread.getLooper();
        Log.i(TAG,"Background thread started");
    }

    @TargetApi(11)
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup parent,Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_photo_gallery,parent,false);
        fetcher = new TwitterFetcher(getActivity());
        fetcher.loadNonHomeTimlineTweets();
        executeAction(0);

        mGridView = (GridView)v.findViewById(R.id.gridView);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String photoPageUri = "";
                Tweet t = mediaTweets.get(position);
                Intent i = new Intent(getActivity(),PhotoPageActivity.class);

                if(t.entities.media != null) {
                    if(t.entities.media.iterator().hasNext()){
                        if(t.entities.media.iterator().next().mediaUrl != null) {
                            photoPageUri = t.entities.media.iterator().next().mediaUrl;
                        }
                    }
                }
                TweetData mdata = new TweetData();
                mdata.setTweet(t.text);
                mdata.setURL(photoPageUri);
                i.putExtra("TweetData",mdata);
                startActivity(i);
            }
        });

        return v;
    }

    @Override
    @TargetApi(11)
    public void onCreateOptionsMenu(Menu menu, MenuInflater mInflater){
        super.onCreateOptionsMenu(menu, mInflater);
        mInflater.inflate(R.menu.menu_twitter, menu);
    }

    public void setupTrends(List<String> localTrends){
        AlertDialog.Builder listBuilder = new AlertDialog.Builder(getActivity());
        listBuilder.setTitle("Local Trends");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(localTrends);
        listBuilder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                        .putString(TwitterFetcher.SEARCH_QUERY, strName)
                        .commit();
                fetcher.setCURRENT_TASK_TODO(TwitterFetcher.OPERATION_SEARCH);
                updateTweets();
            }
        });
        listBuilder.show();
    }

    public void executeAction(int action){
        clearPreferences();
        switch (action){
            case 0 :
                fetcher.setCURRENT_TASK_TODO(TwitterFetcher.OPERATION_TIMELINE);
                updateTweets();
                break;
            case 1:
                showSearchDialog();
                break;
            case 2:
                fetcher.setCURRENT_TASK_TODO(TwitterFetcher.OPERATION_LOCAL_TRENDS);
                fetcher.loadNonHomeTimlineTweets();
                break;
            case 3:
                fetcher.setCURRENT_TASK_TODO(TwitterFetcher.OPERATION_WORLD_TRENDS);
                fetcher.loadNonHomeTimlineTweets();
                break;
            default:
                break;
        }
    }

    public void showSearchDialog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Search");
        final EditText input = new EditText(getActivity());
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String result = input.getText().toString();
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                        .putString(TwitterFetcher.SEARCH_QUERY,result)
                        .commit();
                fetcher.setCURRENT_TASK_TODO(TwitterFetcher.OPERATION_SEARCH);
                updateTweets();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        return super.onOptionsItemSelected(menuItem);
    }

    public void updateTweets() {
        fetcher.loadHomeTimelinesTweets();
    }

    public void updateAdapter(List<Tweet> tweets){
        /*mediaURLs = new ArrayList<String>();
        for(Tweet t : tweets ){
            if(t.entities.media != null) {
                if(t.entities.media.iterator().hasNext()){
                    if(t.entities.media.iterator().next().mediaUrl != null) {
                        mediaURLs.add(t.entities.media.iterator().next().mediaUrl);
                    }
                }
            }
        }
        if(getActivity() == null || mGridView == null) return;

        if(mediaURLs != null){
            mGridView.setAdapter(new GalleryItemAdapter(mediaURLs));
        } else {
            mGridView.setAdapter(null);
        }*/
        mediaTweets = tweets;
        mGridView.setAdapter(new GalleryItemAdapter(tweets));
    }

    private class GalleryItemAdapter extends ArrayAdapter<Tweet>{
        public GalleryItemAdapter(List<Tweet> items){
            super(getActivity(), 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            if(convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item,parent,false);
            }
            ImageView imageView = (ImageView)convertView.findViewById(R.id.gallery_item_ImageView);
            imageView.setImageResource(R.drawable.loader);
            Tweet t = getItem(position);
            String Url = "";
            if(t.entities.media != null) {
                if(t.entities.media.iterator().hasNext()){
                    if(t.entities.media.iterator().next().mediaUrl != null) {
                        Url = t.entities.media.iterator().next().mediaUrl;
                        //mediaURLs.add(t.entities.media.iterator().next().mediaUrl);
                    }
                }
            }

            String url = Url;
            mThumbnailThread.queueThumbnail(imageView,url);

            return convertView;
        }
    }

    public void clearPreferences(){
        PreferenceManager.getDefaultSharedPreferences(getActivity())
            .edit()
            .putString(TwitterFetcher.SEARCH_QUERY,null)
            .commit();
    }

    @Override
    public void onDestroyView() {
        clearPreferences();
        super.onDestroyView();
        mThumbnailThread.quit();
        mThumbnailThread.clearQueue();
        Log.i(TAG,"Background thread destroyed");
    }
}
