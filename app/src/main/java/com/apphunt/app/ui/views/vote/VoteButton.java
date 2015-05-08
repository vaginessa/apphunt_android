package com.apphunt.app.ui.views.vote;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.apphunt.app.R;
import com.apphunt.app.api.apphunt.AppHuntApiClient;
import com.apphunt.app.api.apphunt.Callback;
import com.apphunt.app.api.apphunt.models.App;
import com.apphunt.app.api.apphunt.models.Vote;
import com.apphunt.app.auth.LoginProviderFactory;
import com.apphunt.app.event_bus.BusProvider;
import com.apphunt.app.event_bus.events.votes.UserVotedForAppEvent;
import com.apphunt.app.utils.Constants;
import com.apphunt.app.utils.LoginUtils;
import com.apphunt.app.utils.SharedPreferencesHelper;
import com.apphunt.app.utils.TrackingEvents;
import com.flurry.android.FlurryAgent;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.client.Response;

/**
 * Created by nmp on 15-5-8.
 */
public class VoteButton extends LinearLayout {
    private App app;
    private LayoutInflater inflater;

    @InjectView(R.id.vote)
    Button voteButton;

    public VoteButton(Context context) {
        super(context);
        if (!isInEditMode()) {
            init(context);
        }
    }

    public VoteButton(Context context, App app) {
        super(context);
        this.app = app;
        if (!isInEditMode()) {
            init(context);
        }
    }

    public VoteButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context);
        }
    }

    public VoteButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            init(context);
        }
    }

    protected void init(Context context) {
        if(inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        View view = inflater.inflate(R.layout.view_vote_button, this, true);
        ButterKnife.inject(this, view);

        if(app == null) {
            voteButton.setText("0");
            return;
        }

        updateVoteButton();
    }

    private void updateVoteButton() {
        if (app.isHasVoted()) {
            voteButton.setTextColor(getResources().getColor(R.color.bg_secondary));
            voteButton.setBackgroundResource(R.drawable.btn_voted_v2);
        } else {
            voteButton.setTextColor(getResources().getColor(R.color.bg_primary));
            voteButton.setBackgroundResource(R.drawable.btn_vote);
        }
        voteButton.setText(app.getVotesCount());
    }

    public void setApp(App app) {
        this.app = app;
        updateVoteButton();
    }

    @OnClick(R.id.vote)
    public void vote(View view) {
        if(!LoginProviderFactory.get((Activity)getContext()).isUserLoggedIn()) {
            LoginUtils.showLoginFragment(getContext());
            return;
        }

        if(app == null)
            return;

        if(app.isHasVoted()) {
            downVote();
        } else {
            vote();
        }
    }

    protected void downVote() {
        AppHuntApiClient.getClient().downVote(app.getId(), SharedPreferencesHelper.getStringPreference(Constants.KEY_USER_ID), new Callback<Vote>() {
            @Override
            public void success(Vote voteResult, Response response) {
                FlurryAgent.logEvent(TrackingEvents.UserDownVotedAppFromDetails);
                app.setVotesCount(voteResult.getVotes());
                app.setHasVoted(false);
                voteButton.setText(voteResult.getVotes());
                voteButton.setTextColor(getResources().getColor(R.color.bg_primary));
                voteButton.setBackgroundResource(R.drawable.btn_vote);
                postUserVotedEvent(false);
            }
        });
    }

    protected void vote() {
        AppHuntApiClient.getClient().vote(app.getId(), SharedPreferencesHelper.getStringPreference(Constants.KEY_USER_ID), new Callback<Vote>() {
            @Override
            public void success(Vote voteResult, Response response) {
                FlurryAgent.logEvent(TrackingEvents.UserVotedAppFromDetails);
                app.setVotesCount(voteResult.getVotes());
                app.setHasVoted(true);
                voteButton.setText(voteResult.getVotes());
                voteButton.setBackgroundResource(R.drawable.btn_voted_v2);
                voteButton.setTextColor(getResources().getColor(R.color.bg_secondary));
                postUserVotedEvent(true);
            }
        });
    }

    protected void postUserVotedEvent(boolean hasVoted) {
        BusProvider.getInstance().post(new UserVotedForAppEvent(hasVoted));
    }


}
