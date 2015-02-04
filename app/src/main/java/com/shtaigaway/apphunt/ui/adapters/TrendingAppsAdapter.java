package com.shtaigaway.apphunt.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.shtaigaway.apphunt.R;
import com.shtaigaway.apphunt.api.AppHuntApiClient;
import com.shtaigaway.apphunt.api.Callback;
import com.shtaigaway.apphunt.api.models.App;
import com.shtaigaway.apphunt.api.models.AppsList;
import com.shtaigaway.apphunt.api.models.Vote;
import com.shtaigaway.apphunt.app.AppItem;
import com.shtaigaway.apphunt.app.Item;
import com.shtaigaway.apphunt.app.MoreAppsItem;
import com.shtaigaway.apphunt.app.SeparatorItem;
import com.shtaigaway.apphunt.utils.Constants;
import com.shtaigaway.apphunt.utils.FacebookUtils;
import com.shtaigaway.apphunt.utils.SharedPreferencesHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class TrendingAppsAdapter extends BaseAdapter {

    private static final String TAG = TrendingAppsAdapter.class.getName();

    private Context ctx;
    private ListView listView;
    private ArrayList<Item> items = new ArrayList<>();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d");
    private Calendar calendar = Calendar.getInstance();

    private int resultsCounter = 0;
    private ArrayList<AppsList> tempList = new ArrayList<>();

    private ViewHolderItem viewHolderItem = null;
    private ViewHolderSeparator viewHolderSeparator = null;
    private ViewHolderMoreApps viewHolderMoreApps = null;

    public TrendingAppsAdapter(Context ctx, ListView listView) {
        this.ctx = ctx;
        this.listView = listView;

//        getApps();
        getAppsForNextDate();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (getItemViewType(position) == Constants.ItemType.ITEM.getValue()) {
                viewHolderItem = new ViewHolderItem();

                view = inflater.inflate(R.layout.layout_app_item, parent, false);
                viewHolderItem.layout = (RelativeLayout) view.findViewById(R.id.item);
                viewHolderItem.icon = (ImageView) view.findViewById(R.id.icon);
                viewHolderItem.title = (TextView) view.findViewById(R.id.name);
                viewHolderItem.description = (TextView) view.findViewById(R.id.description);
                viewHolderItem.vote = (Button) view.findViewById(R.id.vote);

                view.setTag(viewHolderItem);
            } else if (getItemViewType(position) == Constants.ItemType.SEPARATOR.getValue()) {
                viewHolderSeparator = new ViewHolderSeparator();

                view = inflater.inflate(R.layout.layout_app_list_header, parent, false);
                viewHolderSeparator.header = (TextView) view.findViewById(R.id.header);

                view.setTag(viewHolderSeparator);
            } else if (getItemViewType(position) == Constants.ItemType.MORE_APPS.getValue()) {
                viewHolderMoreApps = new ViewHolderMoreApps();

                view = inflater.inflate(R.layout.layout_more_apps, parent, false);
                viewHolderMoreApps.moreApps = (ImageButton) view.findViewById(R.id.more_apps);

                view.setTag(viewHolderMoreApps);
            }
        } else {
            if (getItemViewType(position) == Constants.ItemType.ITEM.getValue()) {
                viewHolderItem = (ViewHolderItem) view.getTag();
            } else if (getItemViewType(position) == Constants.ItemType.SEPARATOR.getValue()) {
                viewHolderSeparator = (ViewHolderSeparator) view.getTag();
            } else if (getItemViewType(position) == Constants.ItemType.MORE_APPS.getValue()) {
                viewHolderMoreApps = (ViewHolderMoreApps) view.getTag();
            }
        }

        if (getItemViewType(position) == Constants.ItemType.ITEM.getValue() && viewHolderItem != null) {
            final App app = ((AppItem) getItem(position)).getData();

            Ion.with(viewHolderItem.icon)
                    .load(app.getIcon());

            viewHolderItem.title.setText(app.getName());
            viewHolderItem.description.setText(app.getDescription());
            viewHolderItem.vote.setText(app.getVotesCount());

            viewHolderItem.vote.setOnClickListener(null);
            viewHolderItem.vote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Log.e("btn", "bla");
                    if (FacebookUtils.isSessionOpen()) {
                        if (app.isHasVoted()) {
                            AppHuntApiClient.getClient().downVote(app.getId(), SharedPreferencesHelper.getStringPreference(ctx, Constants.KEY_USER_ID), new Callback<Vote>() {
                                @Override
                                public void success(Vote vote, Response response) {
                                    app.setVotesCount(vote.getVotes());
                                    app.setHasVoted(false);
                                    ((Button) v).setText(vote.getVotes());
                                    v.setBackgroundResource(R.drawable.btn_vote);
                                }
                            });
                        } else {
                            AppHuntApiClient.getClient().vote(app.getId(), SharedPreferencesHelper.getStringPreference(ctx, Constants.KEY_USER_ID), new Callback<Vote>() {
                                @Override
                                public void success(Vote vote, Response response) {
                                    app.setVotesCount(vote.getVotes());
                                    app.setHasVoted(true);
                                    ((Button) v).setText(vote.getVotes());
                                    v.setBackgroundResource(R.drawable.btn_down_vote);
                                }
                            });
                        }
                    } else {
                        FacebookUtils.showLoginFragment(ctx);
                    }
                }
            });
            viewHolderItem.vote.setBackgroundResource((app.isHasVoted() ? R.drawable.btn_down_vote : R.drawable.btn_vote));

            viewHolderItem.layout.setOnClickListener(null);
            viewHolderItem.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(((AppItem) getItem(position)).getData().getShortUrl()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ctx.startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Couldn't get the shortUrl");
                    }

                }
            });
        } else if (getItemViewType(position) == Constants.ItemType.SEPARATOR.getValue() && viewHolderSeparator != null) {
            viewHolderSeparator.header.setText(((SeparatorItem) getItem(position)).getData());
        } else if (getItemViewType(position) == Constants.ItemType.MORE_APPS.getValue() && viewHolderMoreApps != null) {
            viewHolderMoreApps.moreApps.setOnClickListener(null);
            viewHolderMoreApps.moreApps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadMoreApps(position);
                }
            });
        }

        return view;
    }

    private void getApps() {
        calendar = Calendar.getInstance();

//        resultsCounter = 0;
        String userId = SharedPreferencesHelper.getStringPreference(ctx, Constants.KEY_USER_ID);

        for (int i = 0; i < 5; i++) {

            AppHuntApiClient.getClient().getApps(userId, dateFormat.format(calendar.getTime()), 1, 5, Constants.PLATFORM, new Callback<AppsList>() {
                @Override
                public void success(AppsList appsList, Response response) {
                    resultsCounter += 1;

                    if (appsList.getTotalCount() > 0 && !tempList.contains(appsList)) {
                        tempList.add(appsList);

                        Log.e("date", appsList.getDate());
                        Log.e("success", "success");
                    }

                    if (resultsCounter == 5) {
                        notifyAdapter();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e("failure", "failure");
                }
            });

            calendar.add(Calendar.DATE, -1);
        }
    }

    private void notifyAdapter() {
        Collections.sort(tempList, new ComparatorApps());

        Calendar today = Calendar.getInstance();

//        Log.e("resultCounter", "" + resultsCounter);
//        Log.e("listSize", "" + tempList.size());

        for (AppsList appsList : tempList) {
//            Log.e("list_date", appsList.getDate());
            if (dateFormat.format(today.getTime()).equals(appsList.getDate())) {
                items.add(new SeparatorItem("Today"));
            } else {
                items.add(new SeparatorItem(appsList.getDate()));
            }

            for (App app : appsList.getApps()) {
                items.add(new AppItem(app));
            }

            if (appsList.haveMoreApps())
                items.add(new MoreAppsItem(1, 5, appsList.getDate()));
        }

        notifyDataSetChanged();
    }

    private class ComparatorApps implements Comparator<AppsList> {
        @Override
        public int compare(AppsList first, AppsList second) {
            if (first.getDateAsCalendar() != null && second.getDateAsCalendar() != null) {
                return (second.getDateAsCalendar().compareTo(first.getDateAsCalendar()));
            }

            return -1;
        }
    }

    public void getAppsForNextDate() {
//        calendar.add(Calendar.DATE, -1);

        String date = dateFormat.format(calendar.getTime());

        AppHuntApiClient.getClient().getApps(SharedPreferencesHelper.getStringPreference(ctx, Constants.KEY_USER_ID),
                date, 1, 5, Constants.PLATFORM, new Callback<AppsList>() {
            @Override
            public void success(AppsList appsList, Response response) {
                ArrayList<Item> items = new ArrayList<>();

                if (appsList.getTotalCount() > 0) {
                    items.add(new SeparatorItem(appsList.getDate()));

                    for (App app : appsList.getApps()) {
                        items.add(new AppItem(app));
                    }

                    if (appsList.haveMoreApps())
                        items.add(new MoreAppsItem(1, 5, appsList.getDate()));

                    addItems(items);
                }
            }
        });
    }

    public void addItems(ArrayList<Item> items) {
        this.items.addAll(items);
        notifyDataSetChanged();

        listView.smoothScrollToPosition(this.items.size() - 3);
    }

    private void loadMoreApps(final int position) {
        final MoreAppsItem item = (MoreAppsItem) getItem(position);
        AppHuntApiClient.getClient().getApps(SharedPreferencesHelper.getStringPreference(ctx, Constants.KEY_USER_ID),
                item.getDate(), item.getNextPage(), item.getItems(), Constants.PLATFORM, new Callback<AppsList>() {
            @Override
            public void success(AppsList appsList, Response response) {
                if (!appsList.haveMoreApps()) {
                    items.remove(position);
                } else {
                   item.setPage(item.getNextPage());
                }

                ArrayList<AppItem> newItems = new ArrayList<>();

                for (App app : appsList.getApps()) {
                    newItems.add(new AppItem(app));
                }

                items.addAll(position, newItems);

                notifyDataSetChanged();
                listView.smoothScrollToPosition(position + newItems.size());
            }
        });
    }

    public void resetAdapter() {
        items.clear();
        tempList.clear();
        resultsCounter = 0;
        notifyDataSetInvalidated();

        calendar = Calendar.getInstance();

//        Log.e("temp", "" + tempList.size());
//        Log.e("items", "" + items.size());
//        Log.e("date", dateFormat.format(calendar.getTime()));
//        Log.e("resultCounter", "" + resultsCounter);

//        getApps();
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType().getValue();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolderItem {
        RelativeLayout layout;
        ImageView icon;
        TextView title;
        TextView description;
        Button vote;
    }

    private static class ViewHolderSeparator {
        TextView header;
    }

    private static class ViewHolderMoreApps {
        ImageButton moreApps;
    }
}