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

package com.kinvey.android.cache

import io.realm.DynamicRealmObject
import io.realm.RealmQuery
import java.lang.reflect.InvocationTargetException
import java.util.*

/**
 * Created by Prots on 2/1/16.
 */
object QueryHelper {
    @JvmStatic
    fun prepareRealmQuery(realmQuery: RealmQuery<DynamicRealmObject>,
                          queryMap: Map<String, Any>?, isIgnoreIn: Boolean)
            : RealmQuery<DynamicRealmObject> {
        for ((field, params) in queryMap ?: mapOf()) {
            when {
                field.equals("\$or", ignoreCase = true) -> {
                    realmQuery.beginGroup()
                    if (params.javaClass.isArray) {
                        val components = params as Array<Map<String, Any>>
                        if (components != null && components.size > 0) {
                            realmQuery.beginGroup()
                            prepareRealmQuery(realmQuery, components[0], isIgnoreIn)
                            realmQuery.endGroup()
                            for (i in 1 until components.size) {
                                realmQuery.or()
                                realmQuery.beginGroup()
                                prepareRealmQuery(realmQuery, components[i], isIgnoreIn)
                                realmQuery.endGroup()
                            }
                        }
                    }
                    realmQuery.endGroup()
                }
                field.equals("\$and", ignoreCase = true) -> and(realmQuery, params, isIgnoreIn)
                params is Map<*, *> -> for ((operation, value) in params as Map<String, Any>) {
                    if (operation.equals("\$in", ignoreCase = true)) {
                        if (!isIgnoreIn) {
                            `in`(realmQuery, field, value)
                        } else {
                            realmQuery.beginGroup()
                            realmQuery.isNotEmpty("_id")
                            realmQuery.endGroup()
                        }
                    } else if (operation.equals("\$nin", ignoreCase = true)) {
                        realmQuery.beginGroup().not()
                        `in`(realmQuery, field, value)
                        realmQuery.endGroup()
                    } else if (operation.equals("\$gt", ignoreCase = true)) {
                        gt(realmQuery, field, value)
                    } else if (operation.equals("\$lt", ignoreCase = true)) {
                        lt(realmQuery, field, value)
                    } else if (operation.equals("\$gte", ignoreCase = true)) {
                        gte(realmQuery, field, value)
                    } else if (operation.equals("\$lte", ignoreCase = true)) {
                        lte(realmQuery, field, value)
                    } else if (operation.equals("\$ne", ignoreCase = true)) {
                        notEqualTo(realmQuery, field, value)
                    } else if (operation.equals("\$not", ignoreCase = true)) {
                        val newMap: MutableMap<String, Any> = LinkedHashMap()
                        newMap[field] = value as Map<String?, Any?>
                        realmQuery.not()
                        prepareRealmQuery(realmQuery, newMap, isIgnoreIn)
                    } else {
                        throw UnsupportedOperationException("this query is not supported by cache")
                    }
                }
                else -> equalTo(realmQuery, field, params)
            }
        }
        return realmQuery
    }

    @JvmStatic
    fun prepareRealmQuery(realmQuery: RealmQuery<DynamicRealmObject>, queryMap: Map<String, Any>?): RealmQuery<DynamicRealmObject> {
        return prepareRealmQuery(realmQuery, queryMap, false)
    }

    private fun and(query: RealmQuery<DynamicRealmObject>, params: Any, isIgnoreIn: Boolean) {
        query.beginGroup()
        if (params.javaClass.isArray) {
            val components = params as Array<Map<String, Any>>
            if (!components.isNullOrEmpty()) {
                query.beginGroup()
                prepareRealmQuery(query, components[0], isIgnoreIn)
                query.endGroup()
                for (i in 1 until components.size) {
                    query.beginGroup()
                    prepareRealmQuery(query, components[i], isIgnoreIn)
                    query.endGroup()
                }
            }
        }
        query.endGroup()
    }

    private fun `in`(query: RealmQuery<*>, field: String, params: Any) {
        if (params.javaClass.isArray) {
            val operatorParams = params as Array<Any>
            query.beginGroup()
            if (operatorParams != null && operatorParams.size > 0) {
                equalTo(query, field, operatorParams[0])
                for (i in 1 until operatorParams.size) {
                    query.or()
                    equalTo(query, field, operatorParams[i])
                }
            }
            query.endGroup()
        }
    }

    private fun gt(query: RealmQuery<*>, field: String, param: Any) {
        try {
            if (param is Number) {
                val m = query.javaClass.getMethod("greaterThan", String::class.java,
                        param.javaClass.getDeclaredField("TYPE").get(param) as Class<*>)
                m.invoke(query, field, param)
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }
    }

    private fun gte(query: RealmQuery<*>, field: String, param: Any) {
        try {
            if (param is Number) {
                val m = query.javaClass.getMethod("greaterThanOrEqualTo", String::class.java,
                        param.javaClass.getDeclaredField("TYPE").get(param) as Class<*>)
                m.invoke(query, field, param)
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }
    }

    private fun lte(query: RealmQuery<*>, field: String, param: Any) {
        try {
            if (param is Number) {
                val m = query.javaClass.getMethod("lessThanOrEqualTo", String::class.java,
                        param.javaClass.getDeclaredField("TYPE").get(param) as Class<*>)
                m.invoke(query, field, param)
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }
    }

    private fun lt(query: RealmQuery<*>, field: String, param: Any) {
        try {
            if (param is Number) {
                val m = query.javaClass.getMethod("lessThan", String::class.java,
                        param.javaClass.getDeclaredField("TYPE").get(param) as Class<*>)
                m.invoke(query, field, param)
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }
    }

    private fun equalTo(query: RealmQuery<*>, field: String, param: Any) {
        try {
            val m = query.javaClass.getMethod("equalTo", String::class.java,
                    param.javaClass)
            m.invoke(query, field, param)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    private fun notEqualTo(query: RealmQuery<*>, field: String, param: Any) {
        try {
            val m = query.javaClass.getMethod("notEqualTo", String::class.java, param.javaClass)
            m.invoke(query, field, param)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }
}