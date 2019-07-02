package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class CompositionActivity extends AppCompatActivity {
    EditText etItemText;
    private TwitterClient client;
    TextView tvChars;
    Button btnSendTweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_composition);
        etItemText = (EditText) findViewById(R.id.etCompose);
        tvChars = (TextView) findViewById(R.id.tvChars);
        etItemText.addTextChangedListener(textEditorWatcher);
        btnSendTweet = (Button) findViewById(R.id.button);
        getSupportActionBar().setTitle("Compose a Tweet");
        client = TwitterApp.getRestClient(this);

    }

    public void onClick(View v) {
        String message = etItemText.getText().toString();
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    public void cancel(View view) {
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
                btnSendTweet.setClickable(true);
            } else if (charsLeft == -1) {
                btnSendTweet.setClickable(false);
            }

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
