/*
 * Copyright (c) 2014, Kinvey, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kinvey.java.query;

/**
 * Created by Prots on 1/19/16.
 */
public enum  KinveyClientErrorCode {
    /**
     * Already logged in error.
     */
    AlreadyLoggedIn("Attempting to login when a user is already logged in",
            "call `myClient.user().logout().execute() first -or- check `myClient.user().isUserLoggedIn()` before attempting to login again",
            "Only one user can be active at a time, and logging in a new user will replace the current user which might not be intended"),
    /**
     * Database error.
     */
    DatabaseError(
            "The database used for local persistence encountered an error.",
            "",
            ""
    ),
    /**
     * Missing app credentials.
     */
    MissingAppCredentials("Missing credentials: `appKey` and/or `appSecret`.",
            "Did you forget to call `KinveyClient.init` with your `appKey` and/or `appSecret`?", ""),
    /**
     * Missing master credentials.
     */
    MissingMasterCredentials("Missing credentials: `appKey` and/or `masterSecret`.",
            "Did you forget to call `KinveyClient.init` with your `appKey` and/or `masterSecret`?", ""),
    /**
     * No active user.
     */
    NoActiveUser("No user is currently logged in.",
            "call myClient.User().login(...) first to login",
            "Registering for Push Notifications needs a logged in user"),
    /**
     * No Google play services on Device
     */
    NoGooglePlay(
            "Google Play Services is not available on the current device",
            "The device needs Google Play Services",
            "GCM for push notifications requires Google Play Services"
    ),

    /**
     * Request abort error.
     */
    RequestAbortError("The request was aborted.","", ""),
    /**
     * Request error.
     */
    RequestError("The request failed.","", ""),
    /**
     * Request timeout error.
     */
    RequestTimeoutError("The request timed out.","", ""),
    /**
     * Social error.
     */
    SocialError("The social identity cannot be obtained.","", ""),

    /**
     * Sync error.
     */
    SyncError("The synchronization operation cannot be completed.","", ""),
    /**
     * MIC error.
     */
    MICError("Unable to authorize using Mobile Identity Connect.","", ""),
    /**
     * Unuble to parse json that client recieve from server
     */
    CantParseJson("Unable to parse the JSON in the response", "examine BL or DLC to ensure data format is correct. If the exception is caused by `key <somkey>`, then <somekey> might be a different type than is expected (int instead of of string)","" ),
    /**
     * download file does not exist on remote server
     */
    BlobNotFound("BlobNotFound", "This blob not found for this app backend", "The file being downloaded does not exist."),

    /**
     * Internal error means that server returns json without _downloadUrl for file object
     */
    DownloadUrlMissing("_downloadURL is null!","do not remove _downloadURL in collection hooks for File!","The library cannot download a file without this url"),

    /**
     * Internal error means that server returns json without _uploadUrl for file object
     */
    UploadUrlMissing("_uploadURL is null!","do not remove _uploadURL in collection hooks for File!","The library cannot upload a file without this url");


    private String explain;
    private String fix;
    private String reason;

    KinveyClientErrorCode(String reason, String fix, String explanation){
        this.reason = reason;
        this.explain = explanation;
        this.fix = fix;
    }

    public String getExplain() {
        return explain;
    }

    public String getFix() {
        return fix;
    }

    public String getReason() {
        return reason;
    }
}
