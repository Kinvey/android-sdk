package com.kinvey.java.linkedResources

import junit.framework.TestCase

class LinkedFileTest : TestCase() {

    private val ID = "123456789"
    private val FILENAME = "filename.test"

    private val ID_EXTRA = "_id"
    private val FILENAME_EXTRA = "_filename"

    fun testConstructor() {
        val file = LinkedFile(ID, FILENAME)
        assertEquals(ID, file.id)
        assertEquals(FILENAME, file.fileName)
    }

    fun testExtras() {
        val file = LinkedFile()
        file.addExtra(ID_EXTRA, ID)
        file.addExtra(FILENAME_EXTRA, FILENAME)

        assertTrue(file.hasExtras())
        assertEquals(file.getExtra(ID_EXTRA), ID)
        assertEquals(file.getExtra(FILENAME_EXTRA), FILENAME)
    }
}