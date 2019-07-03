package com.codepath.apps.restclienttemplate.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class Tweet {

    public String body;
    public long uid;//database ID for the tweet
    public User user;
    public String createdAt;
    public Integer retweets;
    public Integer faves;

    //deserialize the JSON
    public static Tweet fromJSON(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();

        //extract values from JSON

        try {
            tweet.body = jsonObject.getString("full_text");
        } catch (JSONException e) {
            tweet.body = jsonObject.getString("text");
        }

        tweet.uid = jsonObject.getLong("id");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
        tweet.faves = jsonObject.getInt("favorite_count");
        tweet.retweets = jsonObject.getInt("retweet_count");
        return tweet;
    }

    public Tweet() {

    }
}
