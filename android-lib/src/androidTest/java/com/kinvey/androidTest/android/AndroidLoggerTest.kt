package com.kinvey.androidTest.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.kinvey.android.AndroidLogger
import io.mockk.spyk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class AndroidLoggerTest {

    private val loggerMsg: String = "test msg"

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testInfo() {
        val logger: AndroidLogger = spyk(AndroidLogger())
        logger.info(loggerMsg)
        verify { logger.info(loggerMsg) }
    }

    @Test
    fun testDebug() {
        val logger: AndroidLogger = spyk(AndroidLogger())
        logger.info(loggerMsg)
        verify { logger.info(loggerMsg) }
    }

    @Test
    fun testTrace() {
        val logger: AndroidLogger = spyk(AndroidLogger())
        logger.trace(loggerMsg)
        verify { logger.trace(loggerMsg) }
    }

    @Test
    fun testWarning() {
        val logger: AndroidLogger = spyk(AndroidLogger())
        logger.warning(loggerMsg)
        verify { logger.info(loggerMsg) }
    }

    @Test
    fun testError() {
        val logger: AndroidLogger = spyk(AndroidLogger())
        logger.error(loggerMsg)
        verify { logger.info(loggerMsg) }
    }
}