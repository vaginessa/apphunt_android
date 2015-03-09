package com.apphunt.app.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.apphunt.app.R;
import com.apphunt.app.api.AppHuntApiClient;
import com.apphunt.app.api.Callback;
import com.apphunt.app.api.models.Comment;
import com.apphunt.app.api.models.CommentVote;
import com.apphunt.app.api.models.Comments;
import com.apphunt.app.auth.LoginProviderFactory;
import com.apphunt.app.ui.listview_items.Item;
import com.apphunt.app.ui.listview_items.comments.CommentItem;
import com.apphunt.app.ui.listview_items.comments.SubCommentItem;
import com.apphunt.app.ui.widgets.AvatarImageView;
import com.apphunt.app.utils.Constants;
import com.apphunt.app.utils.FacebookUtils;
import com.apphunt.app.utils.SharedPreferencesHelper;
import com.apphunt.app.utils.TrackingEvents;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

import it.appspice.android.AppSpice;
import retrofit.client.Response;

public class CommentsAdapter extends BaseAdapter {

    private static final String TAG = CommentsAdapter.class.getName();
    private final ListView listView;

    private Context ctx;
    private ArrayList<Item> items = new ArrayList<>();
    private CommentViewHolder commentViewHolder = null;
    private SubCommentViewHolder subCommentViewHolder = null;
    private LayoutInflater inflater;
    private int page;
    private int totalPages;

    public CommentsAdapter(Context ctx, Comments comments, ListView listView) {
        this.ctx = ctx;
        this.listView = listView;

        addItems(comments.getComments());
        totalPages = comments.getTotalPages();
        page = comments.getPage();

        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        
        if (view == null) {
            if (getItemViewType(position) == 0) {
                commentViewHolder = new CommentViewHolder();
                view = inflater.inflate(R.layout.layout_comment, parent, false);

                commentViewHolder.layout = (RelativeLayout) view.findViewById(R.id.layout);
                commentViewHolder.avatar = (AvatarImageView) view.findViewById(R.id.avatar);
                commentViewHolder.name = (TextView) view.findViewById(R.id.name);
                commentViewHolder.comment = (TextView) view.findViewById(R.id.text);
                commentViewHolder.vote = (Button) view.findViewById(R.id.vote);
                
                view.setTag(commentViewHolder);
            }
            else if (getItemViewType(position) == 1) {
                view = inflater.inflate(R.layout.layout_subcomment, parent, false);
                subCommentViewHolder = new SubCommentViewHolder();

                subCommentViewHolder.layout = (RelativeLayout) view.findViewById(R.id.layout);
                subCommentViewHolder.avatar = (AvatarImageView) view.findViewById(R.id.avatar);
                subCommentViewHolder.name = (TextView) view.findViewById(R.id.name);
                subCommentViewHolder.comment = (TextView) view.findViewById(R.id.text);
                subCommentViewHolder.vote = (Button) view.findViewById(R.id.vote);

                view.setTag(subCommentViewHolder);
            }
        } else {
            if (getItemViewType(position) == 0) {
                commentViewHolder = (CommentViewHolder) view.getTag();
            } else if (getItemViewType(position) == 1) {
                subCommentViewHolder = (SubCommentViewHolder) view.getTag();
            }
        }
        
        if (getItemViewType(position) == 0 && commentViewHolder != null) {
            final Comment comment = ((CommentItem) getItem(position)).getData();

            Picasso.with(ctx)
                    .load(comment.getUser().getProfilePicture())
                    .into(commentViewHolder.avatar);

            commentViewHolder.name.setText(comment.getUser().getName());
            commentViewHolder.comment.setText(comment.getText());
            commentViewHolder.vote.setText(String.valueOf(comment.getVotesCount()));
            
            commentViewHolder.vote.setOnClickListener(null);
            commentViewHolder.vote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (userHasPermissions()) {
                        if (comment.isHasVoted()) {
                            AppSpice.createEvent(TrackingEvents.UserDownVotedComment).track();
                            AppHuntApiClient.getClient().downVoteComment(SharedPreferencesHelper.getStringPreference(ctx, Constants.KEY_USER_ID), comment.getId(), new Callback<CommentVote>() {
                                @Override
                                public void success(CommentVote vote, Response response) {
                                    comment.setHasVoted(false);
                                    comment.setVotesCount(vote.getVotesCount());
                                    ((Button) v).setTextColor(ctx.getResources().getColor(R.color.bg_primary));
                                    ((Button) v).setText(String.valueOf(vote.getVotesCount()));
                                    v.setBackgroundResource(R.drawable.btn_vote);
                                }
                            });
                        } else {
                            AppSpice.createEvent(TrackingEvents.UserVotedComment).track();
                            AppHuntApiClient.getClient().voteComment(SharedPreferencesHelper.getStringPreference(ctx, Constants.KEY_USER_ID), comment.getId(), new Callback<CommentVote>() {
                                @Override
                                public void success(CommentVote vote, Response response) {
                                    comment.setHasVoted(true);
                                    comment.setVotesCount(vote.getVotesCount());
                                    ((Button) v).setTextColor(ctx.getResources().getColor(R.color.bg_secondary));
                                    ((Button) v).setText(String.valueOf(vote.getVotesCount()));
                                    v.setBackgroundResource(R.drawable.btn_voted);
                                }
                            });
                        }
                    } else {
                        FacebookUtils.showLoginFragment(ctx);
                    }
                }
            });

            if (comment.isHasVoted()) {
                commentViewHolder.vote.setTextColor(ctx.getResources().getColor(R.color.bg_secondary));
                commentViewHolder.vote.setBackgroundResource(R.drawable.btn_voted);
            } else {
                commentViewHolder.vote.setTextColor(ctx.getResources().getColor(R.color.bg_primary));
                commentViewHolder.vote.setBackgroundResource(R.drawable.btn_vote);
            }
        } else if (getItemViewType(position) == 1 && subCommentViewHolder != null) {
            final Comment comment = ((SubCommentItem) getItem(position)).getData();

            Picasso.with(ctx)
                    .load(comment.getUser().getProfilePicture())
                    .into(subCommentViewHolder.avatar);

            subCommentViewHolder.name.setText(comment.getUser().getName());
            subCommentViewHolder.comment.setText(comment.getText());
            subCommentViewHolder.vote.setText(String.valueOf(comment.getVotesCount()));

            if (comment.isHasVoted()) {
                subCommentViewHolder.vote.setTextColor(ctx.getResources().getColor(R.color.bg_secondary));
                subCommentViewHolder.vote.setBackgroundResource(R.drawable.btn_voted);
            } else {
                subCommentViewHolder.vote.setTextColor(ctx.getResources().getColor(R.color.bg_primary));
                subCommentViewHolder.vote.setBackgroundResource(R.drawable.btn_vote);
            }
            
            subCommentViewHolder.vote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (userHasPermissions()) {
                        if (comment.isHasVoted()) {
                            AppSpice.createEvent(TrackingEvents.UserDownVotedReplyComment).track();
                            AppHuntApiClient.getClient().downVoteComment(SharedPreferencesHelper.getStringPreference(ctx, Constants.KEY_USER_ID), comment.getId(), new Callback<CommentVote>() {
                                @Override
                                public void success(CommentVote vote, Response response) {
                                    comment.setHasVoted(false);
                                    comment.setVotesCount(vote.getVotesCount());
                                    ((Button) v).setTextColor(ctx.getResources().getColor(R.color.bg_primary));
                                    ((Button) v).setText(String.valueOf(vote.getVotesCount()));
                                    v.setBackgroundResource(R.drawable.btn_vote);
                                }
                            });
                        } else {
                            AppSpice.createEvent(TrackingEvents.UserVotedReplyComment).track();
                            AppHuntApiClient.getClient().voteComment(SharedPreferencesHelper.getStringPreference(ctx, Constants.KEY_USER_ID), comment.getId(), new Callback<CommentVote>() {
                                @Override
                                public void success(CommentVote vote, Response response) {
                                    comment.setHasVoted(true);
                                    comment.setVotesCount(vote.getVotesCount());
                                    ((Button) v).setTextColor(ctx.getResources().getColor(R.color.bg_secondary));
                                    ((Button) v).setText(String.valueOf(vote.getVotesCount()));
                                    v.setBackgroundResource(R.drawable.btn_voted);
                                }
                            });
                        }
                    } else {
                        FacebookUtils.showLoginFragment(ctx);
                    }
                    
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                }
            });
        }
        
        return view;
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

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position).getType().getValue() == Constants.ItemType.COMMENT.getValue()) ? 0 : 1;
    }
    
    public Comment getComment(int position) {
        Item item = items.get(position);
        return (item instanceof CommentItem) ? ((CommentItem) item).getData() : ((SubCommentItem) item).getData();
    }
    
    public void addItems(ArrayList<Comment> comments) {
        for (Comment c : comments) {
            items.add(new CommentItem(c));

            if (c.getChildren().size() > 0) {
                for (Comment subC: c.getChildren()) {
                    items.add(new SubCommentItem(subC));
                }
            }
        }

        page = 2;
        notifyDataSetChanged();
    }
    
    public void loadMore(String appId, String userId) {
        if (page < totalPages) {
            AppHuntApiClient.getClient().getAppComments(appId, userId, page + 1, 3, new Callback<Comments>() {
                @Override
                public void success(Comments comments, Response response) {
                    addItems(comments.getComments());
                    listView.smoothScrollToPosition(items.size() - 3);
                    page = comments.getPage();
                    totalPages = comments.getTotalPages();
                }
            });
        }
    }

    public void resetAdapter(ArrayList<Comment> comments) {
        items.clear();
        addItems(comments);
    }

    private boolean userHasPermissions() {
        return LoginProviderFactory.get((Activity) ctx).isUserLoggedIn();
    }
    
    static class CommentViewHolder {
        RelativeLayout layout;
        Target avatar;
        TextView name;
        TextView comment;
        Button vote;
    }

    static class SubCommentViewHolder {
        RelativeLayout layout;
        Target avatar;
        TextView name;
        TextView comment;
        Button vote;
    }
}
