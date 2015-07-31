package com.apphunt.app.ui.views.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apphunt.app.R;
import com.apphunt.app.constants.TrackingEvents;
import com.apphunt.app.utils.PackagesUtils;
import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.Map;

public class DownloadButton extends LinearLayout {
    private TextView textView;

    private String appPackage;
    public DownloadButton(Context context) {
        super(context);
        if (!isInEditMode()) {
            init(context);
        }
    }

    public DownloadButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context);
        }
    }

    public DownloadButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init(context);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DownloadButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (!isInEditMode()) {
            init(context);
        }
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_download, this, true);
        textView = (TextView) view.findViewById(R.id.tv_download);

        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(appPackage)) {
                    return;
                }

                Map<String, String> params = new HashMap<>();
                params.put("appPackage", appPackage);

                if (PackagesUtils.isPackageInstalled(appPackage, getContext())) {
                    FlurryAgent.logEvent(TrackingEvents.UserOpenedInstalledApp, params);
                    Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(appPackage);
                    getContext().startActivity(intent);
                } else {
                    FlurryAgent.logEvent(TrackingEvents.UserOpenedAppInMarket, params);
                    PackagesUtils.openInMarket(getContext(), appPackage);
                    //TODO save to the db

                }
            }
        });
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
        if(PackagesUtils.isPackageInstalled(appPackage, getContext())) {
            textView.setText(R.string.open);
        } else {
            textView.setText(R.string.install);
        }

    }
}
