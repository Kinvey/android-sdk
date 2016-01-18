package com.kinvey.java.errors;

import com.google.api.client.json.gson.GsonFactory;
import com.kinvey.java.core.KinveyErrorCodes;
import com.kinvey.java.core.KinveyJsonError;
import com.kinvey.java.core.KinveyMockUnitTest;

import java.io.IOException;

/**
 * Created by Prots on 1/19/16.
 */
public class KinveyErrorTest extends KinveyMockUnitTest {

    public void testSerializeError() throws IOException {
        KinveyJsonError test = new KinveyJsonError();
        test.setDebug("test");
        test.setError(KinveyErrorCodes.AppNotFound);
        test.setDescription("testDescription");

        GsonFactory factory = GsonFactory.getDefaultInstance();

        String result = factory.toString(test);

        assertTrue("error message is malformed", result.matches(".+\"error\":\"AppNotFound\".+"));
    }

    public void testParseError() throws IOException {
        String value = "{\"debug\":\"test\",\"description\":\"testDescription\",\"error\":\"AppNotFound\"}";

        GsonFactory factory = GsonFactory.getDefaultInstance();

        KinveyJsonError error = (KinveyJsonError)factory.createJsonParser(value).parse(KinveyJsonError.class,true);

        assertTrue("error message parse failed", error.getError() == KinveyErrorCodes.AppNotFound);
    }

    public void testParseUnknownError() throws IOException {
        String value = "{\"debug\":\"test\",\"description\":\"testDescription\",\"error\":\"Unknown\"}";

        GsonFactory factory = GsonFactory.getDefaultInstance();

        KinveyJsonError error = (KinveyJsonError)factory.createJsonParser(value).parse(KinveyJsonError.class,true);

        assertTrue("error message parse failed", error.getError() == KinveyErrorCodes.Unknown);
    }
}
