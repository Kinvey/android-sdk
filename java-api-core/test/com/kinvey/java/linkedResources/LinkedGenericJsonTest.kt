package com.kinvey.java.linkedResources

import junit.framework.TestCase

class LinkedGenericJsonTest: TestCase() {

    private val ID_USER = "123456789"
    private val USER_NAME = "filename1.test"

    private val ID1 = "123456789"
    private val ID2 = "987654321"

    private val FILENAME1 = "filename1.test"
    private val FILENAME2 = "filename2.test"

    fun testConstructor() {

        val linkedObj = LinkedPerson()

        linkedObj.id = ID_USER
        linkedObj.username = USER_NAME

        assertEquals(ID_USER, linkedObj.id)
        assertEquals(USER_NAME, linkedObj.username)
    }

    fun testLinkedFilesStoring() {

        val linkedObj = LinkedPerson()

        linkedObj.putFile(FILENAME1, LinkedFile(ID1, FILENAME1))
        linkedObj.putFile(FILENAME2, LinkedFile(ID2, FILENAME2))

        val file1 = linkedObj.getFile(FILENAME1)
        val file2 = linkedObj.getFile(FILENAME2)

        assertNotNull(file1)
        assertEquals(ID1, file1?.id)

        assertNotNull(file2)
        assertEquals(ID2, file2?.id)
    }
}
