package com.apphunt.app.api.apphunt.requests.collections;

import com.android.volley.Response;
import com.apphunt.app.api.apphunt.models.collections.apps.AppsCollections;
import com.apphunt.app.api.apphunt.requests.base.BaseGetRequest;
import com.apphunt.app.event_bus.BusProvider;
import com.apphunt.app.event_bus.events.api.collections.GetAllCollectionsApiEvent;

/**
 * Created by nmp on 15-6-26.
 */
public class GetAllCollectionsRequest extends BaseGetRequest<AppsCollections> {
    public GetAllCollectionsRequest(String sortBy, int page, int pageSize, Response.ErrorListener listener) {
        super(BASE_URL + "/app-collections?page=" + page +"&pageSize=" + pageSize +
                "&status=public&sortBy=" + sortBy, listener);
    }

    public GetAllCollectionsRequest(String userId, String sortBy, int page, int pageSize, Response.ErrorListener listener) {
        super(BASE_URL + "/app-collections?page=" + page +"&pageSize=" + pageSize + "&userId=" +
                userId + "&status=public&sortBy=" + sortBy, listener);
    }

    @Override
    public Class<AppsCollections> getParsedClass() {
        return AppsCollections.class;
    }

    @Override
    public void deliverResponse(AppsCollections response) {
        BusProvider.getInstance().post(new GetAllCollectionsApiEvent(response));
    }
}
