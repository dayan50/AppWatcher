package com.google.android.finsky.api.model

import com.android.volley.Response
import com.google.android.finsky.api.DfeApi
import com.google.android.finsky.protos.nano.Messages
import com.google.android.finsky.protos.nano.Messages.Details

class DfeDetails(private val api: DfeApi) : DfeBaseModel() {
    private var detailsResponse: Details.DetailsResponse? = null
    var detailsUrl: String? = null

    override fun execute(responseListener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener) {
        api.details(detailsUrl, false, false, responseListener, errorListener)
    }

    val document: Document?
        get() = if (this.detailsResponse == null || this.detailsResponse!!.docV2 == null) {
            null
        } else Document(this.detailsResponse!!.docV2)

    override val isReady: Boolean
        get() = this.detailsResponse != null

    override fun onResponse(responseWrapper: Messages.Response.ResponseWrapper) {
        this.detailsResponse = responseWrapper.payload.detailsResponse
        this.notifyDataSetChanged()
    }

}
