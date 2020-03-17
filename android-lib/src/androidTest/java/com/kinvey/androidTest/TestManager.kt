package com.kinvey.androidTest

import android.content.Context
import android.os.Looper
import android.os.Message
import androidx.test.platform.app.InstrumentationRegistry
import com.google.api.client.json.GenericJson
import com.kinvey.android.Client
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore
import com.kinvey.android.store.UserStore.Companion.login
import com.kinvey.android.store.UserStore.Companion.logout
import com.kinvey.androidTest.callback.*
import com.kinvey.androidTest.model.Person
import com.kinvey.androidTest.store.datastore.PaginationTest
import com.kinvey.java.AbstractClient
import com.kinvey.java.Query
import com.kinvey.java.auth.KinveyAuthRequest.Builder
import com.kinvey.java.core.KinveyCachedAggregateCallback
import com.kinvey.java.core.KinveyClientCallback
import com.kinvey.java.core.KinveyClientRequestInitializer
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.model.AggregateType
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.BaseDataStore
import com.kinvey.java.store.StoreType
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import java.io.IOException
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by yuliya on 09/14/17.
 */

class TestManager<T : GenericJson> {

    @Throws(InterruptedException::class, IOException::class)
    fun login(userName: String?, password: String?, client: Client<*>?) {
        val latch = CountDownLatch(1)
        if (client?.isUserLoggedIn == false) {
            Thread(Runnable {
                Looper.prepare()
                try {
                    login<User>(userName ?: "", password ?: "", client as AbstractClient<User>, object : KinveyClientCallback<User> {
                        override fun onSuccess(result: User?) {
                            assertNotNull(result)
                            latch.countDown()
                        }

                        override fun onFailure(error: Throwable?) {
                            assertNull(error)
                            latch.countDown()
                        }
                    })
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                Looper.loop()
            }).start()
        } else {
            latch.countDown()
        }
        latch.await()
    }

    @Throws(InterruptedException::class)
    fun logout(client: Client<*>?) {
        if (client?.isUserLoggedIn == true) {
            val latch = CountDownLatch(1)
            val looperThread: LooperThread
            looperThread = LooperThread(Runnable {
                logout(client as AbstractClient<BaseUser>, object : KinveyClientCallback<Void> {
                    override fun onSuccess(result: Void?) {
                        latch.countDown()
                    }

                    override fun onFailure(error: Throwable?) {
                        assertNull(error)
                        latch.countDown()
                    }
                })
            })
            looperThread.start()
            latch.await()
            looperThread.mHandler?.sendMessage(Message())
        }
    }

    fun createBuilder(client: AbstractClient<*>?): Builder<*> {
        val appKey = (client?.kinveyRequestInitializer as KinveyClientRequestInitializer?)?.appKey
        val appSecret = (client?.kinveyRequestInitializer as KinveyClientRequestInitializer?)?.appSecret
        return Builder<User>(client?.requestFactory?.transport,
                client?.jsonFactory!!, client?.baseUrl, appKey, appSecret, null)
    }

    @Throws(InterruptedException::class)
    fun save(store: DataStore<Person>?, person: Person?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable { store?.save(person!!, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun delete(store: DataStore<Person>?, id: String?): DefaultKinveyDeleteCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyDeleteCallback(latch)
        val looperThread = LooperThread(Runnable { store?.delete(id, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun delete(store: DataStore<Person>?, query: Query?): DefaultKinveyDeleteCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyDeleteCallback(latch)
        val looperThread = LooperThread(Runnable { store?.delete(query!!, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun <T: GenericJson> deleteCustom(store: DataStore<T>?, query: Query?): DefaultKinveyDeleteCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyDeleteCallback(latch)
        val looperThread = LooperThread(Runnable { store?.delete(query!!, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun <T: GenericJson> deleteCustom(store: DataStore<T>?, id: String?): DefaultKinveyDeleteCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyDeleteCallback(latch)
        val looperThread = LooperThread(Runnable { store?.delete(id, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun <T: GenericJson> saveCustom(store: DataStore<T>?, person: T?): CustomKinveyClientCallback<T> {
        val latch = CountDownLatch(1)
        val callback = CustomKinveyClientCallback<T>(latch)
        val looperThread = LooperThread(Runnable { store?.save(person!!, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun <T: GenericJson> saveCustomInBackground(store: DataStore<T>?, person: T?) {
        val looperThread = LooperThread(Runnable {
            try {
                Thread.sleep(10000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            store?.save(person!!, object : KinveyClientCallback<T> {
                override fun onSuccess(result: T?) {
                    println("saved $result")
                }
                override fun onFailure(error: Throwable?) {
                    println(error?.message)
                }
            })
        })
        looperThread.start()
    }

    @Throws(InterruptedException::class)
    fun <T: GenericJson> saveCustomList(store: DataStore<T>?, persons: List<T>?): CustomKinveyListCallback<T> {
        val latch = CountDownLatch(1)
        val callback = CustomKinveyListCallback<T>(latch)
        val looperThread = LooperThread(Runnable { store?.save(persons!!, callback as KinveyClientCallback<List<T>>) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun find(store: DataStore<Person>?, query: Query?): DefaultKinveyReadCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyReadCallback(latch)
        val looperThread = LooperThread(Runnable { store?.find(query!!, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun find(store: DataStore<Person>?, id: String?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable { store?.find(id!!, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun <T: GenericJson> findCustom(store: DataStore<T>?, query: Query?): CustomKinveyReadCallback<T> {
        val latch = CountDownLatch(1)
        val callback = CustomKinveyReadCallback<T>(latch)
        val looperThread = LooperThread(Runnable { store?.find(query!!, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun <T: GenericJson> pullCustom(store: DataStore<T>?, query: Query?): CustomKinveyPullCallback {
        val latch = CountDownLatch(1)
        val callback = CustomKinveyPullCallback(latch)
        val looperThread = LooperThread(Runnable {
            if (query != null) {
                store?.pull(query, callback)
            } else {
                store?.pull(callback)
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun <T: GenericJson> pullCustom(store: DataStore<T>?, query: Query?, pageSize: Int): CustomKinveyPullCallback {
        val latch = CountDownLatch(1)
        val callback = CustomKinveyPullCallback(latch)
        val looperThread = LooperThread(Runnable {
            if (query != null) {
                store?.pull(query, pageSize, callback)
            } else {
                store?.pull(pageSize, callback)
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun <T: GenericJson> pullCustom(store: DataStore<T>?, query: Query?, isAutoPagination: Boolean): CustomKinveyPullCallback {
        val latch = CountDownLatch(1)
        val callback = CustomKinveyPullCallback(latch)
        val looperThread = LooperThread(Runnable {
            if (query != null) {
                store?.pull(query, isAutoPagination, callback)
            } else {
                store?.pull(isAutoPagination, callback)
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun push(store: DataStore<Person>?): DefaultKinveyPushCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyPushCallback(latch)
        val looperThread = LooperThread(Runnable { store?.push(callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun <T: GenericJson> calculation(store: DataStore<T>?, aggregateType: AggregateType?,
                    fields: ArrayList<String>?, sumField: String?, query: Query?,
                    cachedCallback: KinveyCachedAggregateCallback?): DefaultKinveyAggregateCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyAggregateCallback(latch)
        val looperThread = LooperThread(Runnable { store?.group(aggregateType!!, fields!!, sumField, query!!, callback, cachedCallback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun <T: GenericJson> sync(store: DataStore<T>?, query: Query?): CustomKinveySyncCallback {
        val latch = CountDownLatch(1)
        val callback = CustomKinveySyncCallback(latch)
        val looperThread = LooperThread(Runnable { store?.sync(query, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun <T: GenericJson> sync(store: DataStore<T>?, query: Query?, isAutoPagination: Boolean): CustomKinveySyncCallback {
        val latch = CountDownLatch(1)
        val callback = CustomKinveySyncCallback(latch)
        val looperThread = LooperThread(Runnable { store?.sync(query, isAutoPagination, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun <T: GenericJson> sync(store: DataStore<T>?, query: Query?, pageSize: Int): CustomKinveySyncCallback {
        val latch = CountDownLatch(1)
        val callback = CustomKinveySyncCallback(latch)
        val looperThread = LooperThread(Runnable {
            if (query != null) {
                store?.sync(query, pageSize, callback)
            } else {
                store?.sync(pageSize, callback)
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    //cleaning backend store (can be improved)
    @Deprecated("")
    @Throws(InterruptedException::class)
    fun cleanBackendDataStore(store: DataStore<Person>?) {
        val deleteCallback = delete(store, Query().notEqual("age", "100500"))
        assertNull(deleteCallback.error)
    }

    @Throws(InterruptedException::class)
    fun <T: GenericJson> cleanBackend(store: DataStore<T>?, storeType: StoreType) {
        if (storeType !== StoreType.NETWORK) {
            sync(store, store?.client?.query()!!)
        }
        val deleteCallback = deleteCustom(store, Query().notEqual("age", "100500"))
        assertNull(deleteCallback.error)
        if (storeType === StoreType.SYNC) {
            sync(store, store?.client?.query()!!)
        }
    }

    @Throws(IOException::class)
    fun createPersons(store: DataStore<Person>?, n: Int) {
        for (i in 0 until n) {
            val person = Person()
            person.username = TEST_USERNAME + i
            val savedPerson = store?.save(person)
            assertNotNull(savedPerson)
        }
    }

    @Throws(InterruptedException::class)
    fun <T: GenericJson> subscribe(store: DataStore<T>?, liveServiceCallback: CustomKinveyLiveServiceCallback<T>?): CustomKinveyClientCallback<Boolean> {
        val latch = CountDownLatch(1)
        val callback = CustomKinveyClientCallback<Boolean>(latch)
        val looperThread = LooperThread(Runnable { store?.subscribe(liveServiceCallback!!, callback) })
        looperThread.start()
        latch.await(60, TimeUnit.SECONDS)
//        looperThread.mHandler.sendMessage(new Message());


        return callback
    }

    @Throws(InterruptedException::class)
    fun <T: GenericJson> subscribeSync(store: DataStore<T>?): CustomKinveyLiveServiceCallback<*> {
        val latch = CountDownLatch(1)
        val callback = CustomKinveyLiveServiceCallback<T>(latch)
        val looperThread = LooperThread(Runnable {
            try {
                store?.subscribe(callback)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun <T: GenericJson> subscribeAsync(store: DataStore<T>?): CustomKinveyLiveServiceCallback<*> {
        val latch = CountDownLatch(1)
        val callback = CustomKinveyLiveServiceCallback<T>(latch)
        val looperThread = LooperThread(Runnable {
            try {
                store?.subscribe(callback)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class, NoSuchMethodException::class, ClassNotFoundException::class)
    fun <T: GenericJson> mockBaseDataStore(client: AbstractClient<*>?, collectionName: String?, className: Class<*>?, storeType: StoreType?, networkManager: NetworkManager<T>?): BaseDataStore<T> {
        val aClass = Class.forName("com.kinvey.java.store.BaseDataStore")
        val constructor = aClass.getDeclaredConstructor(AbstractClient::class.java, String::class.java, Class::class.java, StoreType::class.java, NetworkManager::class.java) as Constructor<BaseDataStore<T>>
        constructor.isAccessible = true
        return constructor.newInstance(client, collectionName, className, storeType, networkManager)
    }

    //use for PaginationTest.COLLECTION and for Person.class
    fun getCacheSize(storeType: StoreType, client: Client<*>?): Long {
        return client?.cacheManager?.getCache(PaginationTest.COLLECTION, Person::class.java, storeType.ttl)?.get()?.size?.toLong() ?: 0L
    }

    companion object {
        const val TEST_USERNAME = "Test_UserName"
        const val TEST_USERNAME_2 = "Test_UserName_2"
        const val USERNAME = "testSecond"
        const val USERNAME_USER = "test_user"
        const val USERNAME_FILE = "test_file"
        const val PASSWORD = "testSecond"
    }
}