package com.apphunt.app.event_bus.events.votes;


public class UserVotedForAppEvent {
    private boolean isVote = true;

    public UserVotedForAppEvent(boolean isVote) {
        this.isVote = isVote;
    }

    public boolean isVote() {
        return isVote;
    }

    public void setIsVote(boolean isVote) {
        this.isVote = isVote;
    }
}
