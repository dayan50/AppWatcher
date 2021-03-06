package com.anod.appwatcher.installed

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v4.util.SimpleArrayMap
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.anod.appwatcher.App
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AccountChooser
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.model.WatchAppList
import com.anod.appwatcher.ui.ToolbarActivity
import com.anod.appwatcher.utils.InstalledAppsProvider
import com.anod.appwatcher.utils.PackageManagerUtils
import info.anodsplace.android.log.AppLog
import kotterknife.bindView
import java.util.*


/**
 * @author algavris
 * *
 * @date 19/04/2016.
 */
class ImportInstalledActivity : ToolbarActivity(), LoaderManager.LoaderCallbacks<List<String>>, AccountChooser.OnAccountSelectionListener, ImportBulkManager.Listener {
    val listView: RecyclerView by bindView(android.R.id.list)
    val progressBar: ProgressBar by bindView(android.R.id.progress)

    private var allSelected: Boolean = false
    private lateinit var dataProvider: ImportDataProvider
    private lateinit var importManager: ImportBulkManager
    private var accountChooser: AccountChooser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_installed)
        setupToolbar()

        importManager = ImportBulkManager(this, this)
        dataProvider = ImportDataProvider(this, InstalledAppsProvider.MemoryCache(InstalledAppsProvider.PackageManager(packageManager)))

        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = ImportAdapter(this, packageManager, dataProvider)
        listView.itemAnimator = ImportItemAnimator()

        findViewById<View>(android.R.id.button3).setOnClickListener {
            val importAdapter = listView.adapter as ImportAdapter
            allSelected = !allSelected
            dataProvider.selectAllPackages(allSelected)
            importAdapter.notifyDataSetChanged()
        }

        findViewById<View>(android.R.id.button2).setOnClickListener {
            if (dataProvider.isImportStarted) {
                importManager.stop()
            }
            finish()
        }

        findViewById<View>(android.R.id.button1).setOnClickListener {
            findViewById<View>(android.R.id.button3).visibility = View.GONE
            findViewById<View>(android.R.id.button1).visibility = View.GONE
            val adapter = listView.adapter as ImportAdapter

            importManager.init()
            adapter.clearPackageIndex()
            for (idx in 0..adapter.itemCount - 1) {
                val packageName = adapter.getItem(idx)
                if (dataProvider.isPackageSelected(packageName)) {
                    importManager.addPackage(packageName)
                    adapter.storePackageIndex(packageName, idx)
                }
            }
            if (importManager.isEmpty) {
                finish()
            } else {
                importManager.start()
            }
        }

        supportLoaderManager.initLoader(0, null, this).forceLoad()
    }

    override fun onResume() {
        super.onResume()

        accountChooser = AccountChooser(this, App.provide(this).prefs, this)
        accountChooser!!.init()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<String>> {
        return LocalPackageLoader(this)
    }

    override fun onLoadFinished(loader: Loader<List<String>>, data: List<String>) {
        progressBar.visibility = View.GONE
        val downloadedAdapter = listView.adapter as ImportAdapter
        downloadedAdapter.clear()
        downloadedAdapter.addAll(data)
    }

    override fun onLoaderReset(loader: Loader<List<String>>) {
        val downloadedAdapter = listView.adapter as ImportAdapter
        downloadedAdapter.clear()
    }

    override fun onAccountSelected(account: Account, authSubToken: String?) {
        if (authSubToken == null) {
            if (App.with(this).isNetworkAvailable) {
                Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show()
            }
            finish()
            return
        }
        importManager.setAccount(account, authSubToken)
    }

    override fun onAccountNotFound(errorMessage: String) {
        if (errorMessage.isNotBlank()) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
        }
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        accountChooser?.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        accountChooser?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onImportProgress(docIds: List<String>, result: SimpleArrayMap<String, Int>) {
        val adapter = listView.adapter as ImportAdapter
        for (packageName in docIds) {
            val resultCode = result.get(packageName)
            val status: Int
            if (resultCode == null) {
                status = ImportDataProvider.STATUS_ERROR
            } else {
                status = if (resultCode == WatchAppList.RESULT_OK) ImportDataProvider.STATUS_DONE else ImportDataProvider.STATUS_ERROR
            }
            dataProvider.setPackageStatus(packageName, status)
            adapter.notifyPackageStatusChanged(packageName)
        }
    }

    override fun onImportFinish() {
        findViewById<Button>(android.R.id.button2).setText(android.R.string.ok)
    }

    override fun onImportStart(docIds: List<String>) {
        val adapter = listView.adapter as ImportAdapter
        dataProvider.isImportStarted = true
        for (packageName in docIds) {
            AppLog.d(packageName)
            dataProvider.setPackageStatus(packageName, ImportDataProvider.STATUS_IMPORTING)
            adapter.notifyPackageStatusChanged(packageName)
        }
    }

    private class LocalPackageLoader internal constructor(context: Context) : AsyncTaskLoader<List<String>>(context) {
        override fun loadInBackground(): List<String> {
            val cr = DbContentProviderClient(context)
            val watchingPackages = cr.queryPackagesMap(false)
            cr.close()

            val pm = context.packageManager
            val list = PackageManagerUtils.getInstalledPackages(pm).filter { !watchingPackages.containsKey(it) }
            Collections.sort(list) { lPackageName, rPackageName -> PackageManagerUtils.getAppTitle(lPackageName, pm).compareTo(PackageManagerUtils.getAppTitle(rPackageName, pm)) }

            return list
        }
    }

}
