package com.codepath.apps.restclienttemplate.models;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.restclienttemplate.CompositionActivity;
import com.codepath.apps.restclienttemplate.DetailActivity;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {

    private List<Tweet> mTweets;
    Context context;
    Activity activity;

    //pass in Tweets array in the constructor
    public TweetAdapter(List<Tweet> tweets, Activity activity1) {
        mTweets = tweets;
        activity = activity1;
    }

    //for each row, inflate the layout and cache references into ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);


//                "EEE MMM dd HH:mm:ss ZZZZZ yyyy"

        View tweetView = inflater.inflate(R.layout.item_tweet, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(tweetView);
        return viewHolder;
    }

    //bind values based on position of element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        //get data according to position
        Tweet tweet = mTweets.get(i);

        //populate views according to data
        viewHolder.tvUsername.setText(tweet.user.name);
        viewHolder.tvBody.setText(tweet.body);
        viewHolder.tvTimestamp.setText(getRelativeTimeAgo(tweet.createdAt));
        viewHolder.tvHandle.setText(String.format("@%s",tweet.user.screenName));
        viewHolder.tvRetweets.setText(Integer.toString(tweet.retweets));
        viewHolder.tvFaves.setText(Integer.toString(tweet.faves));
        if (tweet.favorited) {
            viewHolder.ivFave.setColorFilter(Color.argb(200,250,0,0));
        }
        if (tweet.retweeted) {
            viewHolder.ivRetweet.setColorFilter(Color.argb(200,0,120,0));
        }
        if (tweet.replyId != "null") {
            viewHolder.tvReply.setVisibility(View.VISIBLE);
            viewHolder.tvReply.setText(String.format("Replying to @%s", tweet.replyId));
        } else {
            viewHolder.tvReply.setVisibility(View.GONE);
        }

        String imageUrl = tweet.user.profileImageUrl;
        Glide.with(context)
                .load(imageUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(viewHolder.ivProfileImage);

        if (tweet.getDisplayUrl() != null) {
            viewHolder.ivTweetPic.setVisibility(View.VISIBLE);
//TODO add rounded corners
            Glide.with(context)
                    .load(tweet.displayUrl)
                    .into(viewHolder.ivTweetPic);

        } else {
            viewHolder.ivTweetPic.setVisibility(View.GONE);
        }
    }

    //common mistake is to leave this as return 0;
    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    //create ViewHolder class

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivProfileImage;
        public TextView tvUsername;
        public TextView tvBody;
        public TextView tvTimestamp;
        public TextView tvHandle;
        public TextView tvFaves;
        public TextView tvRetweets;
        public ImageView ivFave;
        public ImageView ivRetweet;
        public ImageView ivReply;
        public ImageView ivTweetPic;
        public TextView tvReply;
        private TwitterClient client;

        public ViewHolder(View itemView) {
            super(itemView);

            client = TwitterApp.getRestClient(context);

            ivProfileImage = (ImageView) itemView.findViewById(R.id.ivProfileImage);
            tvUsername = (TextView) itemView.findViewById(R.id.tvUserName);
            tvBody = (TextView) itemView.findViewById(R.id.tvBody);
            tvTimestamp = (TextView) itemView.findViewById(R.id.tvTime);
            tvHandle = (TextView) itemView.findViewById(R.id.tvHandle);
            tvFaves = (TextView) itemView.findViewById(R.id.tvFave);
            tvRetweets = (TextView) itemView.findViewById(R.id.tvRetweet);
            ivFave = (ImageView) itemView.findViewById(R.id.ivFave);
            ivRetweet = (ImageView) itemView.findViewById(R.id.ivRetweet);
            ivReply = (ImageView) itemView.findViewById(R.id.ivReply);
            ivTweetPic = (ImageView) itemView.findViewById(R.id.ivTweetPic);
            tvReply = itemView.findViewById(R.id.tvReplyTo);

            ivFave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        final Tweet tweet = mTweets.get(position);
                        long id = tweet.getUid();
                        client.faveTweet(id, tweet.favorited, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                Log.d("TwitterClient", response.toString());
                                String numFaves = tvFaves.getText().toString();
                                if (tweet.favorited) {
                                    ivFave.clearColorFilter();
                                    tvRetweets.setText(Integer.toString(Integer.parseInt(numFaves) - 1));
                                    tweet.favorited = false;
                                } else {
                                    ivFave.setColorFilter(Color.argb(200, 250, 0, 0));
                                    tvFaves.setText(Integer.toString(Integer.parseInt(numFaves) + 1));
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
                }
            });

            ivRetweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        final Tweet tweet = mTweets.get(position);
                        long id = tweet.getUid();
                        client.retweetTweet(id, tweet.retweeted, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                Log.d("TwitterClient", response.toString());
                                String numRetweets = tvRetweets.getText().toString();
                                if (tweet.retweeted) {
                                    ivRetweet.clearColorFilter();
                                    tvRetweets.setText(Integer.toString(Integer.parseInt(numRetweets) - 1));
                                    tweet.retweeted = false;
                                } else {
                                    ivRetweet.setColorFilter(Color.argb(200, 0, 120, 0));
                                    tvRetweets.setText(Integer.toString(Integer.parseInt(numRetweets) + 1));
                                    tweet.retweeted = true;
                                }                             }

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
                }
            });

            ivReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    Tweet tweet = mTweets.get(pos);
                    if (pos != RecyclerView.NO_POSITION) {
                        Intent intent = new Intent(activity, CompositionActivity.class);
                        intent.putExtra("id", Long.toString(tweet.uid));
                        intent.putExtra("username", tweet.user.screenName);
                        activity.startActivityForResult(intent, 30);
                    }
                }
            });

            itemView.setOnClickListener(this);

            tvBody.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                     Make the non-linked text clickable
                    if (tvBody.getSelectionStart() == -1 && tvBody.getSelectionEnd() == -1) {
                // on click stuff here
                        Log.d("TweetAdapter", "Item clicked");
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            Tweet tweet = mTweets.get(position);
                            Intent intent = new Intent(context, DetailActivity.class);
                            intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
//                            Pair<View, String> p1 = Pair.create((View)ivProfileImage,"profile");
//                            Pair<View, String> p2 = Pair.create((View)tvUsername,"username");
//                            Pair<View, String> p3 = Pair.create((View)tvBody,"body");
////                Pair<View, String> p4 = Pair.create((View)tvHandle,"handle");
////                Pair<View, String> p5 = Pair.create((View)ivRetweet,"retweetSymbol");
////                Pair<View, String> p6 = Pair.create((View)ivFave,"faveSymbol");
////                Pair<View, String> p7 = Pair.create((View)tvRetweets,"retweetText");
////                Pair<View, String> p8 = Pair.create((View)tvFaves,"faveText");
////                Pair<View, String> p9 = Pair.create((View)ivReply,"replyIcon");
//                            Pair<View, String> p10 = Pair.create((View)ivTweetPic,"tweetPic");
//                            ActivityOptionsCompat options = (ActivityOptionsCompat) ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, p1, p2, p3,  p10);

//                            context.startActivity(intent, options.toBundle());
                            context.startActivity(intent);
                        }
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            Log.d("TweetAdapter", "Item clicked");
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Tweet tweet = mTweets.get(position);
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                Pair<View, String> p1 = Pair.create((View)ivProfileImage,"profile");
                Pair<View, String> p2 = Pair.create((View)tvUsername,"username");
                Pair<View, String> p3 = Pair.create((View)tvBody,"body");
//                Pair<View, String> p4 = Pair.create((View)tvHandle,"handle");
//                Pair<View, String> p5 = Pair.create((View)ivRetweet,"retweetSymbol");
//                Pair<View, String> p6 = Pair.create((View)ivFave,"faveSymbol");
//                Pair<View, String> p7 = Pair.create((View)tvRetweets,"retweetText");
//                Pair<View, String> p8 = Pair.create((View)tvFaves,"faveText");
//                Pair<View, String> p9 = Pair.create((View)ivReply,"replyIcon");
                Pair<View, String> p10 = Pair.create((View)ivTweetPic,"tweetPic");
                ActivityOptionsCompat options = (ActivityOptionsCompat) ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, p1, p2, p3,  p10);

                context.startActivity(intent, options.toBundle());
            }
        }
    }

//    View.OnClickListener listener = (View v) -> {
//            // Make the non-linked text clickable
//            if (tvBody.getSelectionStart() == -1 && tvBody.getSelectionEnd() == -1) {
//                // on click stuff here
//            }
//        };

    //TODO figure out parsing day ago dates

    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        String[] splitDate = relativeDate.split(" ", 2);
        String reconstructedRelDate = String.format("%s%s", splitDate[0], splitDate[1].charAt(0));


        return reconstructedRelDate;
    }
}
