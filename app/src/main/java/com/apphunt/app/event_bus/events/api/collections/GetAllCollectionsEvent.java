package com.apphunt.app.event_bus.events.api.collections;

import com.apphunt.app.api.apphunt.models.collections.apps.AppsCollections;

public class GetAllCollectionsEvent {
    private AppsCollections appsCollection;

    public GetAllCollectionsEvent(AppsCollections appsCollection) {
        this.appsCollection = appsCollection;
    }

    public AppsCollections getAppsCollection() {
        return appsCollection;
    }

    public void setAppsCollection(AppsCollections appsCollection) {
        this.appsCollection = appsCollection;
    }
}
