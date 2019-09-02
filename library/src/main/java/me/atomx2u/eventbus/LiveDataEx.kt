package me.atomx2u.eventbus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.skip(skipCount: Int = 1): LiveData<T> {
    val result = MediatorLiveData<T>()
    result.addSource(this, object: Observer<T> {
        var count = 0
        override fun onChanged(t: T?) {
            if (++count > skipCount) {
                result.value = t
            }
        }
    })
    return result
}