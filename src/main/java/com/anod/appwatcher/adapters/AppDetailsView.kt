package com.anod.appwatcher.adapters

import android.support.annotation.ColorInt
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.anod.appwatcher.R
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata

/**
 * @author algavris
 * @date 14/05/2016.
 */
class AppDetailsView(view: View, private val mDataProvider: AppViewHolderBase.DataProvider) {
    private val mTextColor: Int
    @ColorInt private var mAccentColor: Int = 0

    val title: TextView = view.findViewById<TextView>(android.R.id.title)
    val details: TextView? = view.findViewById<TextView?>(R.id.details)
    val version: TextView? = view.findViewById<TextView?>(R.id.updated)
    val price: TextView? = view.findViewById<TextView?>(R.id.price)
    val updateDate: TextView? = view.findViewById<TextView?>(R.id.update_date)

    init {
        mAccentColor = mDataProvider.getColor(R.color.theme_accent)
        mTextColor = mDataProvider.getColor(R.color.primary_text)
    }

    fun fillDetails(app: AppInfo, isLocalApp: Boolean) {
        title.text = app.title
        details?.text = app.creator
        val uploadDate = app.uploadDate

        if (TextUtils.isEmpty(uploadDate)) {
            updateDate?.visibility = View.GONE
        } else {
            updateDate?.text = uploadDate
            updateDate?.visibility = View.VISIBLE
        }

        if (isLocalApp) {
            this.price?.visibility = View.GONE
            this.version?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stat_communication_stay_primary_portrait, 0, 0, 0)
            this.version?.text = mDataProvider.formatVersionText(app.versionName, app.versionNumber)
        } else {
            this.fillWatchAppView(app)
        }
    }


    private fun fillWatchAppView(app: AppInfo) {

        val isInstalled = mDataProvider.installedAppsProvider.getInfo(app.packageName).isInstalled
        version?.text = mDataProvider.formatVersionText(app.versionName, app.versionNumber)
        if (app.status == AppInfoMetadata.STATUS_UPDATED) {
            version?.setTextColor(mAccentColor)
        } else {
            version?.setTextColor(mTextColor)
        }

        price?.setTextColor(mAccentColor)
        if (isInstalled) {
            val installed = mDataProvider.installedAppsProvider.getInfo(app.packageName)
            price?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stat_communication_stay_primary_portrait, 0, 0, 0)
            if (TextUtils.isEmpty(installed.versionName)) {
                price?.text = mDataProvider.installedText
            } else {
                price?.text = mDataProvider.formatVersionText(installed.versionName, installed.versionCode)
            }
            if (app.versionNumber > installed.versionCode) {
                version?.setTextColor(mDataProvider.getColor(R.color.material_amber_800))
            } else {
                version?.setTextColor(mTextColor)
            }
        } else {
            price?.setCompoundDrawables(null, null, null, null)
            if (app.priceMicros == 0) {
                price?.setText(R.string.free)
            } else {
                price?.text = app.priceText
            }
        }
    }

    fun updateAccentColor(@ColorInt color: Int, app: AppInfo) {
        mAccentColor = color
        price!!.setTextColor(mAccentColor)
        if (app.status == AppInfoMetadata.STATUS_UPDATED) {
            version!!.setTextColor(mAccentColor)
        }
    }
}
