package org.adhash.sdk.adhashask.ext

import android.content.Context

fun Int.toDp(context: Context?) = this / (context?.resources?.displayMetrics?.density ?: 1f)

fun Int.toPx(context: Context?) = this * (context?.resources?.displayMetrics?.density ?: 1f)