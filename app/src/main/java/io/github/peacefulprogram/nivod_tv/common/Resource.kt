package io.github.peacefulprogram.nivod_tv.common

sealed class Resource<in T> {
    data object Loading : Resource<Any>()

    class Success<T>(val data: T) : Resource<T>()

    class Error(val msg: String, error: Throwable? = null) : Resource<Any>()
}
