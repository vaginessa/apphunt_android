package com.apphunt.app.event_bus.events.api.users;


import com.apphunt.app.api.apphunt.models.users.User;

public class UserUpdatedApiEvent extends UserCreatedApiEvent{
    public UserUpdatedApiEvent(User user) {
        super(user);
    }
}
