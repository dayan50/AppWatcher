package com.google.android.finsky.api.model

import com.android.volley.Request
import com.anod.appwatcher.utils.CollectionsUtils
import com.google.android.finsky.api.DfeApi
import com.google.android.finsky.protos.nano.Messages.Search

class DfeSearch(
        private val dfeApi: DfeApi,
        val query: String, initialUrl: String,
        autoLoadNextPage: Boolean,
        responseFilter: CollectionsUtils.Predicate<Document>)
    : ContainerList<Search.SearchResponse>(initialUrl, autoLoadNextPage, responseFilter) {

    override fun makeRequest(url: String): Request<*> {
        return this.dfeApi.search(url, this, this)
    }
}
