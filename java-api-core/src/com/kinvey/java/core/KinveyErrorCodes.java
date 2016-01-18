package com.kinvey.java.core;


import com.google.api.client.util.Value;
import com.google.gson.annotations.SerializedName;

public enum KinveyErrorCodes {

    @Value("ParameterValueOutOfRange")
    ParameterValueOutOfRange("The value specified for one of the request parameters is out of range"),

    @Value("InvalidQuerySyntax")
    InvalidQuerySyntax("The query string in the request has an invalid syntax"),

    @Value("MissingQuery")
    MissingQuery("The request is missing a query string"),

    @Value("JSONParseError")
    JSONParseError("Unable to parse the JSON in the request"),

    @Value("MissingRequestHeader")
    MissingRequestHeader("The request is missing a required header"),

    @Value("IncompleteRequestBody")
    IncompleteRequestBody("The request body is either missing or incomplete"),

    @Value("MissingRequestParameter")
    MissingRequestParameter("A required parameter is missing from the request"),

    @Value("InvalidIdentifier")
    InvalidIdentifier("One of more identifier names in the request has an invalid format"),

    @Value("FeatureUnavailable")
    FeatureUnavailable("Requested functionality is unavailable in this API version"),

    @Value("CORSDisabled")
    CORSDisabled("Cross Origin Support is disabled for this application"),

    @Value("BadRequest")
    BadRequest("Unable to understand request"),

    @Value("BLRuntimeError")
    BLRuntimeError("The Business Logic script has a runtime error. See debug message for details"),

    @Value("InvalidCredentials")
    InvalidCredentials("Invalid credentials. Please retry your request with correct "),

    @Value("InsufficientCredentials")
    InsufficientCredentials("The credentials used to authenticate this request are not authorized to run this operation. Please retry your request with appropriate credentials"),

    @Value("WritesToCollectionDisallowed")
    WritesToCollectionDisallowed("This collection is configured to disallow any modifications to an existing entity or creation of new entities"),

    @Value("IndirectCollectionAccessDisallowed")
    IndirectCollectionAccessDisallowed("Please use the appropriate API to access this collection for this app backend"),

    @Value("AppProblem")
    AppProblem("There is a problem with this app backend that prevents execution of this operation. Please contact support@kinvey.com for assistance"),

    @Value("EntityNotFound")
    EntityNotFound("This entity not found in the collection"),

    @Value("CollectionNotFound")
    CollectionNotFound("This collection not found for this app backend"),

    @Value("AppNotFound")
    AppNotFound("This app backend not found"),

    @Value("UserNotFound")
    UserNotFound("This user does not exist for this app backend"),

    @Value("BlobNotFound")
    BlobNotFound("This blob not found for this app backend"),

    @Value("UserAlreadyExists")
    UserAlreadyExists("This username is already taken. Please retry your request with a different username"),

    @Value("StaleRequest")
    StaleRequest("The time window for this request has expired"),

    @Value("KinveyInternalErrorRetry")
    KinveyInternalErrorRetry("The Kinvey server encountered an unexpected error. Please retry your request"),

    @Value("KinveyInternalErrorStop")
    KinveyInternalErrorStop("The Kinvey server encountered an unexpected error. Please contact support@kinvey.com for assistance"),

    @Value("DuplicateEndUsers")
    DuplicateEndUsers("More than one user registered with this username for this application. Please contact support@kinvey.com for assistance"),

    @Value("APIVersionNotImplemented")
    APIVersionNotImplemented("This API version is not implemented. Please retry your request with a supported API version"),

    @Value("APIVersionNotAvailable")
    APIVersionNotAvailable("This API version is not available for your app. Please retry your request with a supported API version"),

    @Value("BLSyntaxError")
    BLSyntaxError("The Business Logic script has a syntax error(s). See debug message for details"),

    @Value("BLTimeoutError")
    BLTimeoutError("The Business Logic script did not complete in time. See debug message for details"),

    @Value("BLViolationError")
    BLViolationError("The Business Logic script violated a constraint. See debug message for details"),

    @Value("BLInternalError")
    BLInternalError("The Business Logic script did not complete. See debug message for details"),

    @Value()
    Unknown("Unknown server error");


    /**
     * Simple user readable message
     */
    private String message;

    KinveyErrorCodes(String message){
        this.message = message;
    }

    /**
     *
     * @return User readable message for given Kinvey error code
     */
    public String getMessage() {
        return message;
    }
}
