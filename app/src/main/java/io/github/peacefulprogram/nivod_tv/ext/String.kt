package io.github.peacefulprogram.nivod_tv.ext

import android.widget.Toast
import io.github.peacefulprogram.nivod_tv.NivodApp

fun String.showToast(duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(NivodApp.context, this, duration).show()
}