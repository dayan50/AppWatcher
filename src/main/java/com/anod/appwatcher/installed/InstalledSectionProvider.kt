package com.anod.appwatcher.installed

import android.content.Context
import android.database.Cursor
import android.support.v4.content.Loader
import com.anod.appwatcher.adapters.AppViewHolder
import com.anod.appwatcher.adapters.AppViewHolderDataProvider
import com.anod.appwatcher.fragments.AppWatcherListFragment
import com.anod.appwatcher.model.InstalledFilter
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.utils.InstalledAppsProvider
import info.anodsplace.android.widget.recyclerview.MergeRecyclerAdapter

/**
 * @author algavris
 * *
 * @date 01/04/2017.
 */
class InstalledSectionProvider : AppWatcherListFragment.DefaultSection() {

    override fun createLoader(context: Context, titleFilter: String, sortId: Int, filter: InstalledFilter?, tag: Tag?): Loader<Cursor> {
        return InstalledLoader(context, titleFilter, sortId, filter, tag, context.packageManager)
    }

    override fun fillAdapters(adapter: MergeRecyclerAdapter, context: Context, installedApps: InstalledAppsProvider, clickListener: AppViewHolder.OnClickListener) {
        val recentIndex = adapter.addAdapter(RecentlyInstalledAppsAdapter(context, context.packageManager, clickListener))
        adapterIndexMap.put(ADAPTER_RECENT, recentIndex)
        super.fillAdapters(adapter, context, installedApps, clickListener)
        val dataProvider = AppViewHolderDataProvider(context, installedApps)
        val index = adapter.addAdapter(InstalledAppsAdapter(context, context.packageManager, dataProvider, clickListener))
        adapterIndexMap.put(ADAPTER_INSTALLED, index)
    }

    override fun loadFinished(adapter: MergeRecyclerAdapter, loader: Loader<Cursor>, data: Cursor) {
        super.loadFinished(adapter, loader, data)
        val installedLoader = (loader as InstalledLoader)
        val downloadedAdapter = getAdapter<InstalledAppsAdapter>(ADAPTER_INSTALLED, adapter)
        downloadedAdapter.clear()
        downloadedAdapter.addAll(installedLoader.installedApps)

        val recentAdapter = getAdapter<RecentlyInstalledAppsAdapter>(ADAPTER_RECENT, adapter)
        recentAdapter.recentlyInstalled = installedLoader.recentlyInstalled
    }

    companion object {
        private const val ADAPTER_INSTALLED = 1
        private const val ADAPTER_RECENT = 2
    }
}
