package com.anod.appwatcher.backup

import com.android.util.JsonReader
import info.anodsplace.android.log.AppLog
import java.io.IOException
import java.io.Reader

/**
 * @author algavris
 * *
 * @date 29/04/2017.
 */

class JsonArrayIterator<T>(reader: Reader, private val mObjectReader: JsonArrayIterator.ObjectJsonReader<T>) : Iterator<T> {
    private val mJsonReader: JsonReader = JsonReader(reader)
    private var mStarted: Boolean = false

    interface ObjectJsonReader<O> {
        @Throws(IOException::class)
        fun read(reader: JsonReader): O
    }

    @Throws(IOException::class)
    fun close() {
        mJsonReader.close()
    }

    override fun hasNext(): Boolean {
        var hasNext = false
        try {
            if (!mStarted) {
                mJsonReader.beginArray()
                mStarted = true
            }
            hasNext = mJsonReader.hasNext()

            if (!hasNext) {
                mJsonReader.endArray()
            }
        } catch (e: IOException) {
            AppLog.e(e)
        }

        return hasNext
    }

    override fun next(): T {
        return mObjectReader.read(mJsonReader)
    }
}
