package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.EndlessRecyclerViewScrollListener;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetAdapter;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.text.ParseException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {

    private TwitterClient client;
    TweetAdapter tweetAdapter;
    ArrayList<Tweet> tweets;
    RecyclerView rvTweets;
    private SwipeRefreshLayout swipeContainer;
    long latestId = 1;
    FloatingActionButton fab;
    long earliestId;
    private EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setLogo(R.mipmap.ic_launcher_twittr);
        //getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        client = TwitterApp.getRestClient(this);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                composeTweet();
            }
        });

        //lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        //setup refresh listener
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //TODO un-comment this code
                fetchTimelineAsync();
                //TODO and comment out this code
                //swipeContainer.setRefreshing(false);
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //find RecyclerView
        rvTweets = (RecyclerView) findViewById(R.id.rvTweet);

        //init the arrayList (data source)
        tweets = new ArrayList<>();

        //construct adapter from this data src
        tweetAdapter = new TweetAdapter(tweets, this);
//        tweetAdapter.getRelativeTimeAgo("2019-06-03T17:53:29.621-0800");
//        1939-07-03T17:53:29.621-0800


        //RecyclerView setup (layout manager, use adapter)
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(linearLayoutManager);
        rvTweets.setAdapter(tweetAdapter);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                fetchMoreTweets();
            }
        };
        rvTweets.addOnScrollListener(scrollListener);

        populateTimeline();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateTimeline();
    }

    private void populateTimeline() {
        Log.d("TimelineActivity", "Populating Timeline");
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                //iterate through JSON array
                for (int i = 0; i < response.length(); i++) {
                    try {
                        //convert each object to a Tweet model
                        Tweet tweet = Tweet.fromJSON(response.getJSONObject(i));
                        //add that Tweet model to our data src
                        if (latestId == 0 || tweet.uid > latestId) {
                            latestId = tweet.uid;
                        }
                        if (tweet.uid < earliestId || earliestId == 0) {
                            earliestId = tweet.uid;
                        }
                        tweets.add(tweet);
                        //notify adapter that item has been added
                        tweetAdapter.notifyItemInserted(tweets.size() - 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("TwitterClient", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TwitterClient", responseString);
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                try {
                    JSONArray errorArray = errorResponse.getJSONArray("errors");
                    JSONObject error = errorArray.getJSONObject(0);
                    try {
                        Integer errorCode = error.getInt("code");
                        if (errorCode == 88) {
                            Toast.makeText(TimelineActivity.this, "You bitch, you exceeded your rate limit!!", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                throwable.printStackTrace();
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void composeTweet() {
        Intent i = new Intent(this, CompositionActivity.class);
        i.putExtra("id", "0");
        i.putExtra("username", "0");
        startActivityForResult(i, 20);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == 20) {
            Tweet newTweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));
            if (latestId == 0 || newTweet.uid > latestId) {
                latestId = newTweet.uid;
            }
            tweets.add(0, newTweet);
            tweetAdapter.notifyItemInserted(0);
            rvTweets.scrollToPosition(0);
        }
        if (resultCode == RESULT_OK && requestCode == 30) {
            Tweet newTweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));
            if (latestId == 0 || newTweet.uid > latestId) {
                latestId = newTweet.uid;
            }
            tweets.add(0, newTweet);
            tweetAdapter.notifyItemInserted(0);
            rvTweets.scrollToPosition(0);
        }
    }

    private void fetchTimelineAsync() {
        client.updateTimeline(new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                Log.d("TwitterClient", response.toString());
                Log.d("TwitterClient", Integer.toString(response.length()));
                Log.d("TwitterClient", "" + latestId);
                //iterate through JSON array
                for (int i = 0; i < response.length(); i++) {
                    try {
                        //convert each object to a Tweet model
                        Tweet tweet = Tweet.fromJSON(response.getJSONObject(i));
                        //add that Tweet model to our data src
                        tweets.add(tweet);
                        if (latestId == 0 || tweet.uid > latestId) {
                            latestId = tweet.uid;
                        }
                        //notify adapter that item has been added
                        tweetAdapter.notifyItemInserted(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                //signal that the refresh has completed
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("TwitterClient", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TwitterClient", responseString);
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                try {
                    JSONArray errorArray = errorResponse.getJSONArray("errors");
                    JSONObject error = errorArray.getJSONObject(0);
                    try {
                        Integer errorCode = error.getInt("code");
                        if (errorCode == 88) {
                            Toast.makeText(TimelineActivity.this, "You bitch, you exceeded your rate limit!!", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                throwable.printStackTrace();
                swipeContainer.setRefreshing(false);
            }
        }, latestId);
    }

    private void fetchMoreTweets() {
        client.getMoreTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("TwitterClient", response.toString());
                for (int i = 0; i < response.length(); i++) {
                    try {
                        Tweet tweet = Tweet.fromJSON(response.getJSONObject(i));
                        tweets.add(tweet);
                        if (tweet.uid < earliestId || earliestId == 0) {
                            earliestId = tweet.uid;
                        }
                        tweetAdapter.notifyItemInserted(tweets.size() - 1);
                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("TwitterClient", response.toString());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                super.onSuccess(statusCode, headers, responseString);
                Log.d("TwitterClient", responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TwitterClient", responseString);
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                try {
                    JSONArray errorArray = errorResponse.getJSONArray("errors");
                    JSONObject error = errorArray.getJSONObject(0);
                    try {
                        Integer errorCode = error.getInt("code");
                        if (errorCode == 88) {
                            Toast.makeText(TimelineActivity.this, "You bitch, you exceeded your rate limit!!", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                throwable.printStackTrace();
            }

        }, earliestId);
    }
}