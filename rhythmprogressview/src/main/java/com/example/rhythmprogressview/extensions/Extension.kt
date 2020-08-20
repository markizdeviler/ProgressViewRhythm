package com.example.rhythmprogressview.extensions

import android.view.View

fun View.isVisible() = visibility == View.VISIBLE

fun View.visible(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}