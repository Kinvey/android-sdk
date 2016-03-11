package com.kinvey.java.annotations;

import com.google.api.client.json.GenericJson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Prots on 3/11/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KinveyReference {
    String collection();
    Class<? extends GenericJson> itemClass();
}
