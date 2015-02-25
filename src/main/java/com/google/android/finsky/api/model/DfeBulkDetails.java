package com.google.android.finsky.api.model;

import com.android.volley.Response;
import com.android.volley.toolbox.RequestFuture;
import com.anod.appwatcher.BuildConfig;
import com.anod.appwatcher.utils.AppLog;
import com.google.android.finsky.api.DfeApi;
import com.google.android.finsky.protos.Details;
import com.google.android.finsky.protos.DocumentV2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DfeBulkDetails extends DfeBaseModel<Details.BulkDetailsResponse>
{
    private Details.BulkDetailsResponse mBulkDetailsResponse;
    private final DfeApi mDfeApi;
    private final List<String> mDocIds;
    
    public DfeBulkDetails(final DfeApi mDfeApi, final List<String> mDocIds) {
        super();
        this.mDocIds = mDocIds;
        this.mDfeApi = mDfeApi;

    }

    @Override
    protected void execute(Response.Listener<Details.BulkDetailsResponse> responseListener, Response.ErrorListener errorListener) {
        mDfeApi.getDetails(mDocIds, false, responseListener, errorListener);
    }


    public List<Document> getDocuments() {
        List<Document> list;
        if (this.mBulkDetailsResponse == null) {
            list = null;
        }
        else {
            list = new ArrayList<Document>();
            for (int i = 0; i < this.mBulkDetailsResponse.entry.length; ++i) {
                final DocumentV2.DocV2 doc = this.mBulkDetailsResponse.entry[i].doc;
                if (doc == null) {
                    if (BuildConfig.DEBUG) {
                        AppLog.d("Null document for requested docId: %s ", this.mDocIds.get(i));
                    }
                }
                else {
                    list.add(new Document(doc));
                }
            }
        }
        return list;
    }
    
    @Override
    public boolean isReady() {
        return this.mBulkDetailsResponse != null;
    }

    @Override
    public void onResponse(final Details.BulkDetailsResponse mBulkDetailsResponse) {
        this.mBulkDetailsResponse = mBulkDetailsResponse;
        this.notifyDataSetChanged();
    }
}