package com.google.android.finsky.api.model

import android.util.SparseArray

import com.google.android.finsky.protos.nano.Messages.AppDetails
import com.google.android.finsky.protos.nano.Messages.Common
import com.google.android.finsky.protos.nano.Messages.DocV2

import java.util.ArrayList

class Document(private val doc: DocV2) {

    val appDetails: AppDetails
        get() = this.doc.details?.appDetails ?: AppDetails()

    val title: String
        get() = this.doc.title

    val docId: String
        get() = if (this.doc.docid == null) "" else this.doc.docid

    val backend: Int
        get() = this.doc.backendId

    val detailsUrl: String
        get() = if (this.doc.detailsUrl == null) "" else this.doc.detailsUrl

    val creator: String
        get() = if (this.doc.creator == null) "" else this.doc.creator

    // Type 1 ?
    val offer: Common.Offer
        get() {
            var offer = this.getOffer(Common.Offer.TYPE_1)
            if (offer == null) {
                offer = Common.Offer()
            }
            return offer
        }

    fun getOffer(type: Int): Common.Offer? {
        return this.doc.offer.firstOrNull { it.offerType == type }
    }

    val iconUrl: String?
        get() {
            val images = this.imageTypeMap.get(4) ?: return null
            return if (images.isNotEmpty()) {
                images[0].imageUrl
            } else null
        }

    private val imageTypeMap: SparseArray<MutableList<Common.Image>> by lazy {
            val typeMap = SparseArray<MutableList<Common.Image>>()
            for (image2 in this.doc.image) {
                val imageType = image2.imageType
                if (typeMap.get(imageType) == null) {
                    typeMap.put(imageType, mutableListOf())
                }
                typeMap.get(imageType).add(image2)
            }
            typeMap
        }
}
