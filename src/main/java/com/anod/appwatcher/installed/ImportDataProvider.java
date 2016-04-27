package com.anod.appwatcher.installed;

import android.content.Context;
import android.support.v4.util.SimpleArrayMap;

import com.anod.appwatcher.adapters.AppViewHolderDataProvider;
import com.anod.appwatcher.utils.PackageManagerUtils;

class ImportDataProvider extends AppViewHolderDataProvider {

    public static final int STATUS_DEFAULT = 0;
    public static final int STATUS_IMPORTING = 1;
    public static final int STATUS_DONE = 2;
    public static final int STATUS_ERROR = 3;

    private SimpleArrayMap<String, Boolean> mSelectedPackages = new SimpleArrayMap<>();
    private boolean mDefaultSelected;
    private SimpleArrayMap<String, Integer> mProcessingPackages = new SimpleArrayMap<>();
    private boolean mImportStarted;

    public ImportDataProvider(Context context, PackageManagerUtils pmutils) {
        super(context, pmutils);
    }

    public void selectAllPackages(boolean select) {
        mSelectedPackages.clear();
        mDefaultSelected = select;
    }

    public void selectPackage(String packageName, boolean select) {
        mSelectedPackages.put(packageName, select);
    }

    public boolean isPackageSelected(String packageName) {
        if (mSelectedPackages.containsKey(packageName)) {
            return mSelectedPackages.get(packageName);
        }
        return mDefaultSelected;
    }

    public int getPackageStatus(String packageName) {
        if (mProcessingPackages.containsKey(packageName)) {
            return mProcessingPackages.get(packageName);
        }
        return STATUS_DEFAULT;
    }

    public void setPackageStatus(String packageName, int status) {
        mProcessingPackages.put(packageName, status);
    }

    public void setImportStarted(boolean started) {
        this.mImportStarted = started;
    }

    public boolean isImportStarted() {
        return mImportStarted;
    }
}