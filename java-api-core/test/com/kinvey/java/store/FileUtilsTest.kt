package com.kinvey.java.store

import com.kinvey.java.store.file.FileUtils
import junit.framework.TestCase
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class FileUtilsTest : TestCase() {

    private val STREAM_STR = "12345678901234567890"

    fun testCopyStreams() {
        val inStream = ByteArrayInputStream(STREAM_STR.toByteArray())
        val outStream = ByteArrayOutputStream()
        FileUtils.copyStreams(inStream, outStream)
        val resultStr = outStream.toString()
        assertEquals(STREAM_STR, resultStr)
    }
}