package mytwitter.android.pkubhalkar.com.twittertest;

import java.io.Serializable;

/**
 * Created by Prasad on 5/16/2015.
 */
public class TweetData implements Serializable {
    private String URL;
    private String Tweet;
    private String CreatedAt;
    private int FavCount;
    private int RetweetCount;

    public String getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        CreatedAt = createdAt;
    }

    public int getFavCount() {
        return FavCount;
    }

    public void setFavCount(int favCount) {
        FavCount = favCount;
    }

    public int getRetweetCount() {
        return RetweetCount;
    }

    public void setRetweetCount(int retweetCount) {
        RetweetCount = retweetCount;
    }

    public String getTweet() {
        return Tweet;
    }

    public void setTweet(String tweet) {
        Tweet = tweet;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }
}
