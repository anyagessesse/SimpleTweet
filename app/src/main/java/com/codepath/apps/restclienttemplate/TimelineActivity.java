package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetDao;
import com.codepath.apps.restclienttemplate.models.TweetWithUser;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = "TimelineActivity";
    private final int REQUEST_CODE = 20;

    TweetDao tweetDao;
    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        //lookup swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        client = TwitterApp.getRestClient(this);
        tweetDao = ((TwitterApp) getApplicationContext()).getMyDatabase().tweetDao();

        //find the recyclerview
        rvTweets = findViewById(R.id.rvTweets);  //TODO change to binding

        //init list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this,tweets);

        //recyclerview setup: layout manager and the adapter
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        rvTweets.setAdapter(adapter);

        //add dividing lines
        rvTweets.addItemDecoration(new DividerItemDecoration(rvTweets.getContext(), DividerItemDecoration.VERTICAL));

        //Query for existing tweets in the database
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"Showing data from database");
                List<TweetWithUser> tweetWithUsers = tweetDao.recentItems();
                List<Tweet> tweetsFromDB = TweetWithUser.getTweetList(tweetWithUsers);
                adapter.clear();
                adapter.addAll(tweetsFromDB);
            }
        });


        populateHomeTimeline();

        //setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateHomeTimeline();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu, adds items to action bar if present
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.compose){
            //compose icon has been selected
            //navigate to the compose activity
            Intent intent = new Intent(this,ComposeActivity.class);
            startActivityForResult(intent,REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            //get data from the intent (the tweet object)
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            //update the rv with the new tweet
            //modify data source of tweets
            tweets.add(0,tweet);
            //update the adapter
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG,"onSuccess!" +json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    final List<Tweet> tweetsFromNetwork = Tweet.fromJsonArray(jsonArray);
                    adapter.clear();
                    tweets.addAll(tweetsFromNetwork);
                    adapter.notifyDataSetChanged();

                    swipeContainer.setRefreshing(false);

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG,"Saving data into database");

                            //insert users first
                            List<User> usersFromNetwork = User.fromJsonTweetArray(tweetsFromNetwork);
                            tweetDao.insertModel(usersFromNetwork.toArray(new User[0]));

                            //insert tweets
                            tweetDao.insertModel(tweetsFromNetwork.toArray(new Tweet[0]));
                        }
                    });

                } catch (JSONException e) {
                    Log.e(TAG,"Json exception",e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG,"onFailure!" + response,throwable);

            }
        });
    }
}