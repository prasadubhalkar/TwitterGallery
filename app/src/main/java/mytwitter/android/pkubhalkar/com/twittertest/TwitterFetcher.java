package mytwitter.android.pkubhalkar.com.twittertest;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.internal.oauth.OAuth2Token;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.SearchService;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.TweetViewAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.ResponseContent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import io.fabric.sdk.android.Fabric;

/**
 * Created by Prasad on 3/19/2015.
 */
public class TwitterFetcher {
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY             = "<<Your key>>";
    private static final String TWITTER_SECRET          = "<<Your secret key>>";
    public static  final String SEARCH_QUERY            = "SEARCHQUERY";
    public static  final String OPERATION_SEARCH        = "SEARCH";
    public static  final String OPERATION_TIMELINE      = "TIMELINE";
    public static  final String OPERATION_LOCAL_TRENDS  = "LOCAL_TRENDS";
    public static  final String OPERATION_WORLD_TRENDS  = "WORLDWIDETRENDS";
    private String CURRENT_TASK_TODO                    = "";

    private static final String SEARCH_RESULT_TYPE      = "recent";
    private static final int    SEARCH_COUNT            = 1000;
    private static final String AvailTrendsURL          = "https://api.twitter.com/1.1/trends/place.json";
    private static final String TwitterTokenURL         = "https://api.twitter.com/oauth2/token";
    private static final String ClosestTrendURL         = "https://api.twitter.com/1.1/trends/closest.json";

    private long maxId;
    private TwitterAuthConfig authConfig;

    private Context mContext;

    public static String    USERNAME;
    public static long      USERID;
    public static String    TOKEN;
    public static String    SECRET;

    private TwitterSession session;
    private LocationManager locationManager;
    private String authToken;

    public void setSession(TwitterSession session) {
        this.setSessionDetails();
    }

    public void setSessionDetails(){
        this.USERNAME   = this.session.getUserName();
        this.USERID     = this.session.getUserId();
        this.TOKEN      = this.session.getAuthToken().token;
        this.SECRET     = this.session.getAuthToken().secret;
    }

    public void setCURRENT_TASK_TODO(String CURRENT_TASK_TODO) {
        this.CURRENT_TASK_TODO = CURRENT_TASK_TODO;
    }

    public TwitterFetcher(Context context){
        try {
            mContext = context;
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
            Fabric.with(context, new Twitter(authConfig));
            this.session = Twitter.getSessionManager().getActiveSession();
        } catch(Exception e) {
            Log.d("EXCEPTION",e.getMessage());
        }
    }

    public List<Tweet> filterMediaOnly(List<Tweet> listResult){
        List<Tweet> mediaTweets = new ArrayList<Tweet>();
        for(Tweet t : listResult ){
            if(t.entities.media != null) {
                if(t.entities.media.iterator().hasNext()){
                    if(t.entities.media.iterator().next().mediaUrl != null) {
                        mediaTweets.add(t);
                    }
                }
            }
        }
        return mediaTweets;
    }

    public void loadHomeTimelinesTweets(){
        new FetchFabricTweetTasks().execute();
    }

    public void loadNonHomeTimlineTweets(){
        new FetchNonFabricTweetTasks().execute();
    }

    public HttpPost getEncodedOAuthReq()throws UnsupportedEncodingException{
        HttpPost httpPost       = new HttpPost(TwitterTokenURL);
        try{
            String urlApiKey        = URLEncoder.encode(TWITTER_KEY, "UTF-8");
            String urlApiSecret     = URLEncoder.encode(TWITTER_SECRET, "UTF-8");

            String combined         = urlApiKey + ":" + urlApiSecret;
            String base64Encoded    = Base64.encodeToString(combined.getBytes(), Base64.NO_WRAP);

            httpPost.setHeader("Authorization", "Basic " + base64Encoded);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            httpPost.setEntity(new StringEntity("grant_type=client_credentials"));

        } catch (UnsupportedEncodingException e){
            Log.d("PREPARING_POST-URL",e.getMessage());
        }
        return httpPost;
    }

    public HttpGet getEncodedClosestTrendsReq(String OAuthToken) {
        String url      = null;
        HttpGet httpGet = null;
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            String latitude = Double.toString(location.getLatitude());
            String longitude = Double.toString(location.getLongitude());
            url = Uri.parse(ClosestTrendURL).buildUpon()
                    .appendQueryParameter("lat", latitude)
                    .appendQueryParameter("long", longitude)
                    .build().toString();
            httpGet = new HttpGet(url);
            httpGet.setHeader("Authorization", "Bearer " + OAuthToken);
            httpGet.setHeader("Content-Type", "application/json");
        }
        return httpGet;
    }

    public HttpGet getEncodedAvaliableTrends(String OAuthToken,String id){
        String url      = null;
        HttpGet httpGet = null;
        url = Uri.parse(AvailTrendsURL).buildUpon()
                .appendQueryParameter("id", id)
                .build().toString();

        httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Bearer " + OAuthToken);
        httpGet.setHeader("Content-Type", "application/json");
        return httpGet;
    }

    public void getOAuthorized(){
        try {
            HttpPost authPostReq        = getEncodedOAuthReq();         //Get Encoded Post Request for User Authentication Key
            HttpClient clientConnection = new DefaultHttpClient();      //GetDefault HTTP Client to execute HTTP Request
            ByteArrayOutputStream out   = new ByteArrayOutputStream();  //Get the stream data in Byte Buffer
            InputStream inputStream     = null;                         //Input Stream to get the buffer data

            try {
                HttpResponse isAuthenticated = clientConnection.execute(authPostReq);

                if(isAuthenticated.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
                    return;
                }
                inputStream = isAuthenticated.getEntity().getContent();

                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while ((bytesRead = inputStream.read(buffer)) > 0){
                    out.write(buffer,0,bytesRead);
                }
                out.close();

                String output = new String(out.toByteArray());

                JSONObject authJSON = new JSONObject(output);
                authToken = authJSON.getString("access_token");

            } catch(Exception e){
                Log.d("Exception",e.getMessage());
            } finally {
                if(inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e){
            Log.d("IO Exception",e.getMessage());
        }
    }

    public String executeHTTPGetReq(HttpGet getReq){
        String JSONString                   = "";
        InputStream inputStream             = null;
        ByteArrayOutputStream outputStream  = new ByteArrayOutputStream();

        try {
            HttpClient client = new DefaultHttpClient();
            HttpResponse getAvailResponse = client.execute(getReq);
            if (getAvailResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                return null;
            }

            inputStream = getAvailResponse.getEntity().getContent();
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();

            JSONString = new String(outputStream.toByteArray());

        } catch (Exception e){
            Log.d("HTTPGETREQERROR","Error getting the HTTP Get Request");
        }
        return JSONString;
    }

    public List<String> getNearestTrends() throws IOException{
        try {
            if (authToken != null) {
                HttpGet getAvailTrendsReq = getEncodedClosestTrendsReq(authToken);
                String locationsJSON = executeHTTPGetReq(getAvailTrendsReq);

                JSONArray locationJSONArray = new JSONArray(locationsJSON);
                JSONObject closestLocation = locationJSONArray.getJSONObject(0);
                String WOIED = closestLocation.get("woeid").toString();
                return getAvailableTrends(WOIED);
            }
        }
        catch (Exception e) {
            Log.d("TRENDS_EXCEPTION", e.getMessage());
        }
        return null;
    }

    public List<String> getAvailableTrends(String Id) throws IOException{

        List<String> Trends = new ArrayList<String>();

        try{
            if(authToken != null){
                HttpGet getTrendsReq = getEncodedAvaliableTrends(authToken, Id);
                String locationsJSON = executeHTTPGetReq(getTrendsReq);

                JSONArray replyArray  = new JSONArray(locationsJSON);
                JSONObject jsonObject = replyArray.getJSONObject(0);
                JSONArray trendsArray = jsonObject.getJSONArray("trends");
                JSONObject looperJSON;

                if(trendsArray.length() > 0){
                    for(int i = 0; i < trendsArray.length(); i++){
                        looperJSON = trendsArray.getJSONObject(i);
                        Trends.add(looperJSON.getString("name"));
                    }
                }
                return Trends;
            }
        } catch (Exception e) {
            Log.d("ERROR-GETTING-LOCATIONS",e.getMessage());
        }
        return null;
    }

    private class FetchNonFabricTweetTasks  extends AsyncTask<Void,Void,List<String>>{
        private class ResponseContainer{
            public List<String> result;
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            final ResponseContainer responseContainer = new ResponseContainer();

            if (mContext == null)
                return new ArrayList<String>();

            switch (CURRENT_TASK_TODO) {
                case OPERATION_LOCAL_TRENDS:
                    try{
                        responseContainer.result = getNearestTrends();
                    } catch (IOException e){
                        Log.d("NEAREASTTRENDS","Error getting nearest trends");
                    }
                    break;
                case OPERATION_WORLD_TRENDS:
                    try{
                        responseContainer.result = getAvailableTrends("1");
                    } catch (IOException e){
                        Log.d("WORLDWIDETRENDS","Error getting World Wide Trends");
                    }
                    break;
                default:
                    getOAuthorized();
                    break;
            }
            return responseContainer.result;
        }

        @Override
        protected void onPostExecute(List<String> result){
            if(result != null){
                ((DrawerActivity)mContext).updateFragment(result);
            }
        }
    }

    private class FetchFabricTweetTasks extends AsyncTask<Void,Void,List<Tweet>> {
        private class ResponseContainer {
            public List<Tweet> result;
        }
        @Override
        protected List<Tweet> doInBackground(Void... params){

            final ResponseContainer responseContainer = new ResponseContainer();

            if (mContext == null)
                return new ArrayList<Tweet>();

            switch (CURRENT_TASK_TODO){
                case OPERATION_SEARCH:
                    String query = PreferenceManager.getDefaultSharedPreferences(mContext)
                            .getString(TwitterFetcher.SEARCH_QUERY,null);
                    SearchService service = Twitter.getApiClient().getSearchService();
                    if(query != null) {
                        service.tweets(query, null, null, null, SEARCH_RESULT_TYPE, SEARCH_COUNT, null, null, maxId, true
                                , new Callback<Search>() {
                            @Override
                            public void success(Result<Search> searchResult) {
                                ((DrawerActivity)mContext).updateAdapter(filterMediaOnly(searchResult.data.tweets));
                            }

                            @Override
                            public void failure(TwitterException e) {
                                responseContainer.result = null;
                            }
                        });
                    }
                    break;
                case OPERATION_TIMELINE:
                    StatusesService status = Twitter.getApiClient().getStatusesService();
                    status.homeTimeline(SEARCH_COUNT,null,null,true,true,true,true,new Callback<List<Tweet>>() {
                        @Override
                        public void success(Result<List<Tweet>> listResult) {
                            ((DrawerActivity)mContext).updateAdapter(filterMediaOnly(listResult.data));
                        }
                        @Override
                        public void failure(TwitterException e) {
                            Log.d("Exception : ",e.toString());
                        }
                    });
                    break;
                default:
                    break;
            }
            return responseContainer.result;
        }
    }

    byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = in.read(buffer)) > 0){
                out.write(buffer,0,bytesRead);
            }
            out.close();
            return out.toByteArray();

        } finally {
            connection.disconnect();
        }
    }
}
