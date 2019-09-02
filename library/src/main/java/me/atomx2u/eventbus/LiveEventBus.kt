package me.atomx2u.eventbus

import android.annotation.SuppressLint
import android.os.Looper
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.lifecycle.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass


/**
 * 基于 [androidx.lifecycle.LiveData] 实现的 event bus。与生命周期绑定，保证了线程安全、空安全和类型安全。
 * 一般用于 UI 刷新事件。
 * <br/>
 * 已知限制：
 *  相比 RxEventBus ，不能确保收到全部的消息(比如ABA事件)；因为 LiveData 的只关注最终的数据，
 *  这也影响了此类的实现。因此，如果需要接受全部的消息，而不出现丢失，请使用传统的 EventBus，RxEventBus。
 * */
object LiveEventBus {
    private val map = ConcurrentHashMap<Class<*>, LiveEventImpl<*>>()

    /**
     * 得到 [LiveEvent] 对象。
     *
     * @param eventType 事件类型。
     * @return [LiveEvent] 对象是发出和观察事件的入口。
     * */
    @Suppress("UNCHECKED_CAST")
    @AnyThread
    fun <T : Event> with(eventType: Class<T>): LiveEvent<T> {
        var liveEvent = map[eventType]
        if (liveEvent == null) {
            liveEvent = LiveEventImpl<T>()
            val newLiveEvent = map.putIfAbsent(eventType, liveEvent) as LiveEventImpl<T>?
            if (newLiveEvent != null)
                liveEvent = newLiveEvent
        }
        return liveEvent as LiveEvent<T>
    }

    fun <T : Event> with(eventType: KClass<T>): LiveEvent<T> {
        return with(eventType.java)
    }

    interface Event

    abstract class EventObserver<T : Event> : Observer<T> {
        final override fun onChanged(t: T?) {
            if (t != null)
                onEvent(t)
        }

        /**
         * 当 lifecycleOwner 处于活跃状态，并收到新的事件时，触发此方法。
         * */
        @MainThread
        abstract fun onEvent(event: T)
    }

    interface LiveEvent<T : Event> {
        /**
         * 发出事件。
         *
         * @param event 事件对象。
         * */
        @AnyThread
        fun emit(event: T)

        /**
         * 观察事件。
         *
         * @param lifecycleOwner 生命周期持有者。
         * @param sticky 是否是粘性消息。
         * @param observe 观察者。
         * */
        @MainThread
        fun observe(lifecycleOwner: LifecycleOwner, sticky: Boolean = false, observe: EventObserver<T>)

        /**
         * 观察事件。
         *
         * @param lifecycleOwner 生命周期持有者。
         * @param sticky 是否是粘性消息。
         * @param observe 观察者。
         * */
        @MainThread
        fun observe(lifecycleOwner: LifecycleOwner, sticky: Boolean = false, @MainThread observe: (T) -> Unit) {
            observe(lifecycleOwner, sticky, object : EventObserver<T>() {
                override fun onEvent(event: T) {
                    observe(event)
                }
            })
        }

        /**
         * 观察事件。
         *
         * @param lifecycleOwner 生命周期持有者。
         * @param observe 观察者。
         * */
        @MainThread
        fun observe(lifecycleOwner: LifecycleOwner, @MainThread observe: (T) -> Unit) {
            observe(lifecycleOwner, false, object : EventObserver<T>() {
                override fun onEvent(event: T) {
                    observe(event)
                }
            })
        }

        /**
         * 取消观察。
         *
         * @param observer 观察者。
         * */
        @MainThread
        fun removeObserver(observer: EventObserver<T>)

        /**
         * 取消[lifecycleOwner]下所有的观察。
         *
         * @param lifecycleOwner 生命周期持有者。
         * */
        @MainThread
        fun removeObserver(lifecycleOwner: LifecycleOwner)
    }

    private class LiveEventImpl<T : Event> : LiveEvent<T> {

        private val liveData = MutableLiveData<T>()

        @SuppressLint("WrongThread")
        override fun emit(event: T) {
            if (Looper.getMainLooper().thread == Thread.currentThread())
                liveData.setValue(event)
            else
                liveData.postValue(event)
        }

        override fun observe(
                lifecycleOwner: LifecycleOwner,
                sticky: Boolean,
                observe: EventObserver<T>
        ) {
            if (!sticky && liveData.value != null)
                (liveData.skip(1) as MutableLiveData<T>).observe(lifecycleOwner, observe)
            else
                liveData.observe(lifecycleOwner, observe)
        }

        override fun removeObserver(observer: EventObserver<T>) {
            liveData.removeObserver(observer)
        }

        override fun removeObserver(lifecycleOwner: LifecycleOwner) {
            liveData.removeObservers(lifecycleOwner)
        }
    }
}
