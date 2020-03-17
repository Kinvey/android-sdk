package com.kinvey.androidTest.store.datastore

import android.content.Context
import android.util.Log
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore
import com.kinvey.android.store.DataStore.Companion.collection
import com.kinvey.androidTest.TestManager
import com.kinvey.androidTest.TestManager.Companion.PASSWORD
import com.kinvey.androidTest.TestManager.Companion.TEST_USERNAME
import com.kinvey.androidTest.TestManager.Companion.USERNAME
import com.kinvey.androidTest.model.LiveModel
import com.kinvey.java.AbstractClient
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.store.BaseUserStore.logout
import com.kinvey.java.store.BaseUserStore.registerLiveService
import com.kinvey.java.store.BaseUserStore.unRegisterLiveService
import com.kinvey.java.store.KinveyDataStoreLiveServiceCallback
import com.kinvey.java.store.KinveyLiveServiceStatus
import com.kinvey.java.store.LiveServiceRouter.Companion.instance
import com.kinvey.java.store.StoreType
import junit.framework.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException

/**
 * Created by yuliya on 2/19/17.
 */

@RunWith(AndroidJUnit4::class)
@SmallTest
class LiveServiceTest {

    private var client: Client<*>? = null
    private var testManager: TestManager<LiveModel>? = null
    private var store: DataStore<LiveModel>? = null

    @Before
    @Throws(InterruptedException::class, IOException::class)
    fun setUp() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        client?.enableDebugLogging()
        testManager = TestManager()
        testManager?.login(USERNAME, PASSWORD, client)
    }

    @After
    fun tearDown() {
        client?.performLockDown()
        if (Client.kinveyHandlerThread != null) {
            try {
                client?.stopKinveyHandlerThread()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testRegisterUnregisterSync() {
        assertTrue(client?.isUserLoggedIn == true)
        registerLiveService<BaseUser>()
        assertTrue(instance?.isInitialized == true)
        unRegisterLiveService<BaseUser>()
        assertFalse(instance?.isInitialized == true)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSubscribeUnsubscribeSync() {
        assertTrue(client?.isUserLoggedIn == true)
        registerLiveService<BaseUser>()
        assertTrue(instance?.isInitialized == true)
        store = collection(LiveModel.COLLECTION, LiveModel::class.java, StoreType.SYNC, client)
        val model = LiveModel()
        model.username = "Live model name"
        store?.save(model)
        store?.pushBlocking()
        store?.subscribe(object : KinveyDataStoreLiveServiceCallback<LiveModel> {
            override fun onNext(next: LiveModel) {
                Log.d(TAG, next.toString())
            }
            override fun onError(e: Exception) {
                Log.d(TAG, e.toString())
            }
            override fun onStatus(status: KinveyLiveServiceStatus) {
                Log.d(TAG, status.toString())
            }
        })
        store?.unsubscribe()
        unRegisterLiveService<BaseUser>()
        assertFalse(instance?.isInitialized == true)
    }

    @Test
    @Throws(ClassNotFoundException::class, NoSuchMethodException::class, IllegalAccessException::class,
            InvocationTargetException::class, InstantiationException::class)
    fun testDeviceUuid() {
        val aClass = Class.forName("com.kinvey.android.UuidFactory")
        val constructor = aClass.getDeclaredConstructor(Context::class.java)
        constructor.isAccessible = true
        val uuidFactory: Any = constructor.newInstance(client?.context)
        val theFirstUiId = uuidFactory.javaClass.getDeclaredMethod("getDeviceUuid").invoke(uuidFactory).toString()
        val uuidFactorySecond: Any = constructor.newInstance(client?.context)
        val theSecondUiId = uuidFactorySecond.javaClass.getDeclaredMethod("getDeviceUuid").invoke(uuidFactorySecond).toString()
        assertEquals(theFirstUiId, theSecondUiId)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testLogout() {
        assertTrue(client?.isUserLoggedIn == true)
        registerLiveService<BaseUser>()
        assertTrue(instance?.isInitialized == true)
        store = collection(LiveModel.COLLECTION, LiveModel::class.java, StoreType.SYNC, client)
        store?.subscribe(object : KinveyDataStoreLiveServiceCallback<LiveModel> {
            override fun onNext(next: LiveModel) {
                Log.d(TAG, next.toString())
            }
            override fun onError(e: Exception) {
                Log.d(TAG, e.toString())
            }
            override fun onStatus(status: KinveyLiveServiceStatus) {
                Log.d(TAG, status.toString())
            }
        })
        testManager = TestManager()
        logout<BaseUser>(client as AbstractClient<BaseUser>)
        assertFalse(instance?.isInitialized == true)
    }

    companion object {
        private const val DEFAULT_TIMEOUT = 60 * 1000
        private const val TAG = "LiveServiceTest"
    }
}