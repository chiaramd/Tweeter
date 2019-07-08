package com.codepath.apps.restclienttemplate.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Parcel
public class Tweet {

    public String body;
    public long uid;//database ID for the tweet
    public User user;
    public String createdAt;
    public Integer retweets;
    public Integer faves;
    public String displayUrl;
    public boolean favorited;
    public boolean retweeted;
    public String replyId;
    public String dateCreated;
    public String timeCreated;

    //deserialize the JSON
    public static Tweet fromJSON(JSONObject jsonObject) throws JSONException, ParseException {
        Tweet tweet = new Tweet();

        //extract values from JSON
        if (jsonObject.has("full_text")) {
            tweet.body = jsonObject.getString("full_text");
        } else if (jsonObject.has("text")) {
            tweet.body = jsonObject.getString("text");
        }

        tweet.uid = jsonObject.getLong("id");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
        tweet.faves = jsonObject.getInt("favorite_count");
        tweet.retweets = jsonObject.getInt("retweet_count");
        tweet.favorited = jsonObject.getBoolean("favorited");
        tweet.retweeted = jsonObject.getBoolean("retweeted");

        if (jsonObject.getJSONObject("entities").has("media")) {
            tweet.displayUrl = jsonObject.getJSONObject("entities").getJSONArray("media").getJSONObject(0).getString("media_url_https");
        } else if (jsonObject.has("extended_entities") && jsonObject.getJSONObject("extended_entities").has("media")) {
            tweet.displayUrl = jsonObject.getJSONObject("extended_entities").getJSONArray("media").getJSONObject(0).getString("media_url_https");
        }

        if (jsonObject.getString("in_reply_to_user_id") != null) {
            tweet.replyId = jsonObject.getString("in_reply_to_screen_name");
        }

        SimpleDateFormat sf = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
        Date date = sf.parse(tweet.createdAt);
        tweet.dateCreated = String.format("%s/%s/%s",date.getMonth(),date.getDate(),date.getYear() + 1900);
        Integer hour;
        if (date.getHours() > 12) {
            hour = date.getHours() - 12;
        } else {
            hour = date.getHours();
        }
        tweet.timeCreated = String.format("%s:%s",hour,date.getMinutes());

        return tweet;
    }

    public Tweet() {

    }

    public String getBody() {
        return body;
    }

    public long getUid() {
        return uid;
    }

    public User getUser() {
        return user;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Integer getRetweets() {
        return retweets;
    }

    public Integer getFaves() {
        return faves;
    }

    public String getDisplayUrl() {
        return displayUrl;
    }
}
