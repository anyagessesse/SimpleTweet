package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.parceler.Parcels;

public class DetailViewActivity extends AppCompatActivity {

    ImageView ivProfileImage;
    TextView tvBody;
    TextView tvScreenName;
    TextView tvDate;
    ImageView ivMedia;
    Tweet tweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        ivProfileImage = findViewById(R.id.ivProfileImage2);
        tvBody = findViewById(R.id.tvBody2);
        tvScreenName = findViewById(R.id.tvScreenName2);
        tvDate = findViewById(R.id.tvDate2);
        ivMedia = findViewById(R.id.ivMedia2);

        tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra("tweet"));

        ivMedia.setVisibility(View.GONE);
        tvBody.setText(tweet.body);
        tvScreenName.setText(tweet.user.screenName);
        // TODO tvDate.setText(getRelativeTimeAgo(tweet.createdAt));
        Glide.with(this).load(tweet.user.profileImageUrl).into(ivProfileImage);

        //check that there is media and set media
        if(tweet.mediaUrl != null){
            ivMedia.setVisibility(View.VISIBLE);
            Glide.with(this).load(tweet.mediaUrl).apply(new RequestOptions().transform(new RoundedCorners(30))).into(ivMedia);
        }


    }
}