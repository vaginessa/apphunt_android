package com.apphunt.app.event_bus.events.api.collections;

/**
 * Created by nmp on 15-6-29.
 */
public class UnfavouriteCollectionApiEvent {
    private int statusCode;
    private String collectionId;

    public UnfavouriteCollectionApiEvent(String collectionId, int statusCode) {
        this.statusCode = statusCode;
        this.collectionId = collectionId;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getCollectionId() {
        return collectionId;
    }
}
