package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.text.ParseException;

import cz.msebera.android.httpclient.Header;

public class CompositionActivity extends AppCompatActivity {
    EditText etItemText;
    private TwitterClient client;
    TextView tvChars;
    TextView tvReply;
    boolean reply;
    String replyId;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_composition);



        Toolbar toolbar = findViewById(R.id.tbCompose);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        etItemText = findViewById(R.id.etCompose);
        tvReply = findViewById(R.id.tvReplyTo);
        tvChars = findViewById(R.id.tvChars);

        Intent i = getIntent();
        replyId = i.getStringExtra("id"); //problematic
        username = i.getStringExtra("username");
        if (username.equals("0")) {
            Log.d("Composition Activity", "Composing new tweet");
            tvReply.setVisibility(View.GONE);
            reply = false;
        } else {
            Log.d("Composition Activity", "Replying to user " + username);
            tvReply.setVisibility(View.VISIBLE);
            tvReply.setText(String.format("Replying to @%s", username));
            reply = true;
        }

        etItemText.addTextChangedListener(textEditorWatcher);
        getSupportActionBar().setTitle("Compose a Tweet");
        client = TwitterApp.getRestClient(this);

    }

    public void tweet() {
        String message = etItemText.getText().toString();
        if (reply) {
            message = String.format("@%s %s", username, message);
            client.reply(message, Long.parseLong(replyId), new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        //convert each object to a Tweet model
                        Tweet tweet = Tweet.fromJSON(response);
                        Intent i = new Intent();
                        i.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                        setResult(RESULT_OK, i);
                        finish();
                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    Log.d("TwitterClient", errorResponse.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d("TwitterClient", throwable.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("TwitterClient", errorResponse.toString());
                }
            });
        } else {
            client.sendTweet(message, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        //convert each object to a Tweet model
                        Tweet tweet = Tweet.fromJSON(response);
                        Intent i = new Intent();
                        i.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                        setResult(RESULT_OK, i);
                        finish();
                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_compose, menu);

        final MenuItem cancelItem = menu.findItem(R.id.miCancel);
        final MenuItem tweetItem = menu.findItem(R.id.miTweet);

        cancelItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                cancel();
                return true;
            }
        });

        tweetItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                tweet();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public void cancel() {
        Intent i = new Intent();
        setResult(RESULT_CANCELED, i);
        finish();
    }

    private final TextWatcher textEditorWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Integer charsLeft = 180 - Integer.valueOf(s.length());
            tvChars.setText(String.format("%s chars left", charsLeft));
            if (charsLeft == 0) {
//                btnSendTweet.setClickable(true);
            } else if (charsLeft == -1) {
//                btnSendTweet.setClickable(false);
            }

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
