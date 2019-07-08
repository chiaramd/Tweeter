package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class DetailActivity extends AppCompatActivity {

    Tweet tweet;

    public ImageView ivProfileImage;
    public TextView tvUsername;
    public TextView tvBody;
    public TextView tvTime;
    public TextView tvHandle;
    public TextView tvNumFaves;
    public TextView tvNumRetweets;
    public ImageView ivFaveImage;
    public ImageView ivRetweetImage;
    public ImageView ivReplyImage;
    public TextView tvRetweetText;
    public TextView tvFaveText;
    public TextView tvDate;
    public ImageView ivTweetImage;

    private TwitterClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tweet = Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));

        client = TwitterApp.getRestClient(this);

        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvUsername = findViewById(R.id.tvUserName);
        tvBody = findViewById(R.id.tvBody);
        tvTime = findViewById(R.id.tvTime);
        tvHandle = findViewById(R.id.tvHandle);
        tvNumFaves = findViewById(R.id.tvFave);
        tvNumRetweets = findViewById(R.id.tvRetweet);
        ivFaveImage = findViewById(R.id.ivFave);
        ivRetweetImage = findViewById(R.id.ivRetweet);
        ivReplyImage = findViewById(R.id.ivReply);
        tvRetweetText = findViewById(R.id.tvRetweetText);
        tvFaveText = findViewById(R.id.tvFaveText);
        tvDate = findViewById(R.id.tvDate);
        ivTweetImage = findViewById(R.id.ivTweetPic);

        tvUsername.setText(tweet.user.name);
        tvBody.setText(tweet.body);
        tvDate.setText(tweet.dateCreated);
        tvTime.setText(tweet.timeCreated);
        tvHandle.setText(String.format("@%s",tweet.user.screenName));
        tvNumRetweets.setText(String.format("%s", tweet.retweets));
        tvNumFaves.setText(String.format("%s", tweet.faves));
        if (tweet.retweeted) {
            ivRetweetImage.setColorFilter(Color.argb(200,0,120,0));
        }
        if (tweet.favorited) {
            ivFaveImage.setColorFilter(Color.argb(200,200,0,0));
        }

        String imageUrl = tweet.user.profileImageUrl;
        Glide.with(this)
                .load(imageUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(ivProfileImage);

        if (tweet.getDisplayUrl() != null) {
            ivTweetImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(tweet.displayUrl)
                    .into(ivTweetImage);
        } else {
            ivTweetImage.setVisibility(View.GONE);
        }
    }

    public void fave(View v) {
        long id = tweet.getUid();
        client.faveTweet(id, tweet.favorited, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("TwitterClient", response.toString());
                String numFaves = tvNumFaves.getText().toString();
                if (tweet.favorited) {
                    ivFaveImage.clearColorFilter();
                    tvNumFaves.setText(String.format("%s", Integer.parseInt(numFaves) - 1));
                    tweet.favorited = false;
                } else {
                    ivFaveImage.setColorFilter(Color.argb(200, 250, 0, 0));
                    tvNumFaves.setText(String.format("%s", Integer.parseInt(numFaves) + 1));
                    tweet.favorited = true;
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("TwitterClient", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TwitterClient", throwable.toString());
            }
        });
    }

    public void retweet(View v) {
        long id = tweet.getUid();
        client.retweetTweet(id, tweet.retweeted, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("TwitterClient", response.toString());
                String numRetweets = tvNumRetweets.getText().toString();
                if (tweet.retweeted) {
                    ivRetweetImage.clearColorFilter();
                    tvNumRetweets.setText(String.format("%s", Integer.parseInt(numRetweets) - 1));
                    tweet.retweeted = false;
                } else {
                    ivRetweetImage.setColorFilter(Color.argb(200, 0, 120, 0));
                    tvNumRetweets.setText(String.format("%s", Integer.parseInt(numRetweets) + 1));
                    tweet.retweeted = true;
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("TwitterClient", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TwitterClient", throwable.toString());
            }
        });

    }

    public void reply(View v) {
        Intent intent = new Intent(this, CompositionActivity.class);
        intent.putExtra("id", Long.toString(tweet.uid));
        intent.putExtra("username", tweet.user.screenName);
        startActivityForResult(intent, 30);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == 30) {
            Tweet newTweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));
            Intent i = new Intent(this, TimelineActivity.class);
            startActivity(i);
        }
    }
}
