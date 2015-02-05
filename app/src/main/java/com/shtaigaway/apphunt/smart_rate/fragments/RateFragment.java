package com.shtaigaway.apphunt.smart_rate.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shtaigaway.apphunt.R;
import com.shtaigaway.apphunt.smart_rate.SmartRateConstants;

public class RateFragment extends BaseRateFragment implements OnClickListener {

    private ActionBarActivity activity;
    private View view;
    private RelativeLayout smartRateLayout;
    private TextView message;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.smartrate_rate_dialog_title);
        setFragmentTag(SmartRateConstants.TAG_RATE_FRAGMENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_smartrate_rate, container, false);

        initUI();

        return view;
    }

    private void initUI() {
        smartRateLayout = (RelativeLayout) view.findViewById(R.id.smartrate);

        message = (TextView) view.findViewById(R.id.smartrate_text);
        message.setText(String.format(getString(R.string.smartrate_rate_dialog_message), getString(R.string.app_name)));

        Button yes = (Button) view.findViewById(R.id.yes);
        yes.setOnClickListener(this);

        Button no = (Button) view.findViewById(R.id.no);
        no.setOnClickListener(this);
    }

    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (enter) {
            Animation enterAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.alpha_in);
            enterAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Animation notificationEnterAnim = AnimationUtils.loadAnimation(activity,
                            R.anim.slide_in_top_notification);
                    notificationEnterAnim.setFillAfter(true);
                    smartRateLayout.startAnimation(notificationEnterAnim);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            return enterAnim;
        } else {
            Animation outAnim = AnimationUtils.loadAnimation(activity, R.anim.alpha_out);;

            smartRateLayout.startAnimation(AnimationUtils.loadAnimation(activity,
                    R.anim.slide_out_top));

            return outAnim;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.yes:
                getOnYesClickListener().onYesClick(RateFragment.this, v);
                break;

            case R.id.no:
                getOnNoClickListener().onNoClick(RateFragment.this, v);
                break;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.activity = (ActionBarActivity) activity;
    }
}
