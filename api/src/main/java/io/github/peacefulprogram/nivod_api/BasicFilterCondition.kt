package io.github.peacefulprogram.nivod_api

interface BasicFilterCondition<T> {
    val conditionName: String
    val conditionValue: T

    fun toPair(): Pair<String, T> = Pair(conditionName, conditionValue)

}