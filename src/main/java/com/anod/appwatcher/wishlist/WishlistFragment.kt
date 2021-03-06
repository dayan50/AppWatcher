package com.anod.appwatcher.wishlist

import android.accounts.Account
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.android.volley.VolleyError
import com.anod.appwatcher.R
import com.anod.appwatcher.market.PlayStoreEndpoint
import com.anod.appwatcher.market.WishlistEndpoint
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.model.WatchAppList
import com.anod.appwatcher.tags.TagSnackbar
import kotterknife.bindView

/**
 * @author algavris
 * *
 * @date 16/12/2016.
 */

class WishlistFragment : Fragment(), WatchAppList.Listener, PlayStoreEndpoint.Listener {

    val loading: LinearLayout by bindView(R.id.loading)
    val listView: RecyclerView by bindView(android.R.id.list)
    val emptyView: TextView by bindView(android.R.id.empty)
    val retryView: LinearLayout by bindView(R.id.retry_box)
    val retryButton: Button by bindView(R.id.retry)

    private var endpoint: WishlistEndpoint? = null
    private var watchAppList: WatchAppList? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (endpoint == null) {
            endpoint = WishlistEndpoint(context!!, true)
        }

        if (watchAppList == null) {
            watchAppList = WatchAppList(this)
        }

        watchAppList!!.attach(context!!)
        endpoint!!.listener = this
    }

    override fun onDetach() {
        super.onDetach()
        endpoint!!.listener = null
        watchAppList!!.detach()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_wishlist, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView.layoutManager = LinearLayoutManager(context)
        retryButton.setOnClickListener { endpoint!!.startAsync() }

        listView.visibility = View.GONE
        emptyView.visibility = View.GONE
        loading.visibility = View.VISIBLE
        retryView.visibility = View.GONE

        activity.setTitle(R.string.wishlist)

        val account = arguments.getParcelable<Account>(EXTRA_ACCOUNT)
        val authToken = arguments.getString(EXTRA_AUTH_TOKEN)

        if (account == null || TextUtils.isEmpty(authToken)) {
            Toast.makeText(context, R.string.choose_an_account, Toast.LENGTH_SHORT).show()
            activity.finish()
        } else {
            startLoadingList(account, authToken!!)
        }
    }

    override fun onWatchListChangeSuccess(info: AppInfo, newStatus: Int) {
        if (newStatus == AppInfoMetadata.STATUS_NORMAL) {
            TagSnackbar.make(activity, info, false).show()
        }
        listView.adapter.notifyDataSetChanged()
    }

    override fun onWatchListChangeError(info: AppInfo, error: Int) {
        if (WatchAppList.ERROR_ALREADY_ADDED == error) {
            Toast.makeText(context, R.string.app_already_added, Toast.LENGTH_SHORT).show()
            listView.adapter.notifyDataSetChanged()
        } else if (error == WatchAppList.ERROR_INSERT) {
            Toast.makeText(context, R.string.error_insert_app, Toast.LENGTH_SHORT).show()
        }
    }


    private fun startLoadingList(account: Account, authSubToken: String) {
        endpoint!!.setAccount(account, authSubToken)

        val context = context

        val adapter = ResultsAdapterWishList(context, endpoint!!, watchAppList!!)
        listView.adapter = adapter

        endpoint!!.startAsync()
    }


    private fun showRetryButton() {
        listView.visibility = View.GONE
        emptyView.visibility = View.GONE
        loading.visibility = View.GONE
        retryView.visibility = View.VISIBLE
    }

    private fun showListView() {
        listView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        loading.visibility = View.GONE
        retryView.visibility = View.GONE
    }

    private fun showNoResults() {
        loading.visibility = View.GONE
        listView.visibility = View.GONE
        retryView.visibility = View.GONE
        emptyView.setText(R.string.no_result_found)
        emptyView.visibility = View.VISIBLE
    }

    override fun onDataChanged() {
        if (endpoint!!.count == 0) {
            showNoResults()
        } else {
            showListView()
            listView.adapter.notifyDataSetChanged()
        }
    }

    override fun onErrorResponse(error: VolleyError) {
        loading.visibility = View.GONE
        showRetryButton()
    }

    companion object {
        const val TAG = "wishlist"
        const val EXTRA_ACCOUNT = "extra_account"
        const val EXTRA_AUTH_TOKEN = "extra_auth_token"
    }
}
