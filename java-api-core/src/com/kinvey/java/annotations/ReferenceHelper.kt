/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 *
 */

package com.kinvey.java.annotations

import com.google.api.client.json.GenericJson
import com.kinvey.java.Logger.Companion.INFO
import com.kinvey.java.model.KinveyReference
import java.lang.reflect.Array
import java.lang.reflect.ParameterizedType
import java.util.*

/**
 * Created by Prots on 3/11/16.
 */
object ReferenceHelper {
    @Throws(IllegalAccessException::class, InstantiationException::class)
    @JvmStatic
    fun <T : GenericJson> processReferences(gson: T?, listener: ReferenceListener): T? {
        INFO("Start ReferenceHelper.processReferences(T gson, final ReferenceListener listener)")
        gson?.javaClass?.declaredFields?.let { fieldsList ->
            for (f in fieldsList) {
                //find field info

                if (f.isAnnotationPresent(com.kinvey.java.annotations.KinveyReference::class.java)) {
                    val ref: com.kinvey.java.annotations.KinveyReference = f.getAnnotation(com.kinvey.java.annotations.KinveyReference::class.java)
                    f.isAccessible = true
                    val reference: Any? = f.get(gson)
                    if (GenericJson::class.java.isAssignableFrom(f.type) && reference != null) {
                        processReferences(reference as GenericJson, listener)
                        val id = listener.onUnsavedReferenceFound(ref.collection, f.get(gson) as GenericJson)
                        gson.put(ref.fieldName, KinveyReference(ref.collection, id))
                        continue
                    } else if (f.type.isArray || Collection::class.java.isAssignableFrom(f.type) && reference != null) {
                        //update

                        val collection: Any = f.get(gson)
                        val listReferences: MutableList<KinveyReference> = ArrayList()
                        if (f.type.isArray) {
                            val size = Array.getLength(collection)
                            val clazz = f.type.componentType as Class<out GenericJson>
                            for (i in 0 until size) {
                                processReferences(Array.get(collection, i) as GenericJson, listener)
                                val id = listener.onUnsavedReferenceFound(ref.collection, Array.get(collection, i) as GenericJson)
                                listReferences.add(KinveyReference(ref.collection, id))
                            }
                        } else {
                            val genericSuperclass = f.genericType as ParameterizedType
                            val clazz = genericSuperclass.actualTypeArguments[0] as Class<*>
                            for (`val` in collection as Collection<GenericJson>) {
                                processReferences(`val`, listener)
                                val id = listener.onUnsavedReferenceFound(ref.collection, `val`)
                                listReferences.add(KinveyReference(ref.collection, id))
                            }
                        }
                        gson.put(ref.fieldName, listReferences)
                        continue
                    }
                }
            }
        }

        INFO("Finish ReferenceHelper.processReferences(T gson, final ReferenceListener listener) and return gson")
        return gson
    }

    interface ReferenceListener {
        fun onUnsavedReferenceFound(collection: String, item: GenericJson?): String
    }
}