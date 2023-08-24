package io.github.peacefulprogram.nivod_tv.ext

import kotlinx.coroutines.CancellationException

inline fun <T> runCoroutineCompatibleCatching(block: () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (ex: Throwable) {
        if (ex is CancellationException) {
            throw ex
        }
        Result.failure(ex)
    }
}