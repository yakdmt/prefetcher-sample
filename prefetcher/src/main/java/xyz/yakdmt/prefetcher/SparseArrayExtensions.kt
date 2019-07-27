@file:Suppress("NOTHING_TO_INLINE")
package xyz.yakdmt.prefetcher

import android.os.Build
import androidx.annotation.RequiresApi
import android.util.SparseArray
import android.util.SparseIntArray
import android.util.SparseLongArray

inline operator fun <E> SparseArray<E>.set(key: Int, value: E) = put(key, value)
inline operator fun SparseIntArray.set(key: Int, value: Int) = put(key, value)
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
inline operator fun SparseLongArray.set(key: Int, value: Long) = put(key, value)
