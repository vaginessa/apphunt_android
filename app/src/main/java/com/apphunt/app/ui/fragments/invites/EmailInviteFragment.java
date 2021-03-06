package com.apphunt.app.ui.fragments.invites;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.apphunt.app.R;
import com.apphunt.app.api.apphunt.models.users.User;
import com.apphunt.app.auth.LoginProviderFactory;
import com.apphunt.app.constants.Constants;
import com.apphunt.app.constants.TrackingEvents;
import com.apphunt.app.ui.fragments.base.BaseFragment;
import com.apphunt.app.utils.DeepLinkingUtils;
import com.apphunt.app.utils.FlurryWrapper;
import com.apphunt.app.utils.StringUtils;
import com.apphunt.app.utils.ui.NotificationsUtils;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * * Created by Seishin <atanas@naughtyspirit.co>
 * * on 8/24/15.
 * *
 * * NaughtySpirit 2015
 */
public class EmailInviteFragment extends BaseFragment {
    private static final String TAG = EmailInviteFragment.class.getSimpleName();
    private static final int EMAIL_REQUEST_CODE = 10;

    private AppCompatActivity activity;

    @InjectView(R.id.email)
    EditText email;

    public static EmailInviteFragment newInstance() {
        EmailInviteFragment fragment = new EmailInviteFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FlurryWrapper.logEvent(TrackingEvents.UserViewedEmailInvitation);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_email, container, false);
        ButterKnife.inject(this, view);

        email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;

                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMail(email.getText().toString());
                    handled = true;
                }

                return handled;
            }
        });

        return view;
    }

    @OnClick(R.id.send)
    public void onSendEmailClick() {
        sendMail(email.getText().toString());
    }

    private void sendMail(String emailStr){
        if (email.getText() == null || email.getText().length() == 0) {
            email.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake));
            email.setError("Empty field");
        } else if (email.getText() != null && !StringUtils.isEmailValid(email.getText().toString())) {
            email.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake));
            email.setError("Invalid email");
        } else {
            User user = LoginProviderFactory.get(activity).getUser();
            ArrayList<DeepLinkingUtils.DeepLinkingParam> params = new ArrayList<>();
            params.add(new DeepLinkingUtils.DeepLinkingParam(Constants.KEY_DL_TYPE, "welcome"));
            params.add(new DeepLinkingUtils.DeepLinkingParam(Constants.KEY_SENDER_ID, user.getId()));
            params.add(new DeepLinkingUtils.DeepLinkingParam(Constants.KEY_SENDER_NAME, user.getName()));
            params.add(new DeepLinkingUtils.DeepLinkingParam(Constants.KEY_SENDER_PROFILE_IMAGE_URL, user.getProfilePicture()));

            Intent email = new Intent(Intent.ACTION_SEND);
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{ emailStr});
            email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_invite_title));
            email.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.msg_email_invite), user.getName(), user.getName(), DeepLinkingUtils.getInstance(activity).generateShortUrl(params)));
            email.setType("message/rfc822");

            closeKeyboard(this.email);
            startActivityForResult(Intent.createChooser(email, "Send via..."), EMAIL_REQUEST_CODE);

            FlurryWrapper.logEvent(TrackingEvents.UserSentEmailInvite);
        }
    }

    private void closeKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public void onActivityResult(int requestCode, final int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EMAIL_REQUEST_CODE) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    activity.getSupportFragmentManager().popBackStack();
                    NotificationsUtils.showNotificationFragment(activity, getString(R.string.msg_successful_invite), false, false);
                }
            }, 100);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (AppCompatActivity) activity;
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        closeKeyboard(email);
    }
}
