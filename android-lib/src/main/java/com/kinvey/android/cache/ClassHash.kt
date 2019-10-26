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

import com.google.api.client.json.GenericJson
import com.google.api.client.util.ClassInfo
import com.google.api.client.util.FieldInfo
import com.kinvey.android.Client.Companion.sharedInstance
import com.kinvey.java.Constants
import com.kinvey.java.model.KinveyMetaData
import com.kinvey.java.model.KinveyMetaData.AccessControlList
import com.kinvey.java.model.KinveyMetaData.AccessControlList.Companion.GR
import com.kinvey.java.model.KinveyMetaData.AccessControlList.Companion.GW
import com.kinvey.java.model.KinveyMetaData.AccessControlList.Companion.R
import com.kinvey.java.model.KinveyMetaData.AccessControlList.Companion.W
import com.kinvey.java.model.KinveyMetaData.AccessControlList.Companion.fromMap
import io.realm.*
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Prots on 1/27/16.
 */
object ClassHash {

    private enum class SelfReferenceState { DEFAULT, LIST, CLASS, SUBCLASS, CLASS_AND_LIST, SUBLIST }

    //supported fields
    // boolean, byte, short, ìnt, long, float, double, String, Date and byte[]

    fun isAllowed(f: FieldInfo): Boolean {
        var allowed = false
        for (c in ALLOWED) {
            if (f.type == c) {
                allowed = true
                break
            }
        }
        if (GenericJson::class.java.isAssignableFrom(f.type)) {
            allowed = true
        } else if (isArrayOrCollection(f.type)) {
            val underlying = getUnderlying(f.field)
            if (underlying != null && GenericJson::class.java.isAssignableFrom(underlying)) {
                allowed = true
            }
        }
        return allowed
    }

    private const val KMD = "_kmd"
    private const val ACL = "_acl"
    const val TTL = "__ttl__"
    private const val ID = "_id"
    private const val ITEMS = "_items"
    private const val TEMP_ID = "temp_"

    private val PRIVATE_FIELDS: HashSet<String?> = object : HashSet<String?>() {
        init {
            add(TTL)
        }
    }
    private val EMBEDDED_OBJECT_PRIVATE_FIELDS: HashSet<String?> = object : HashSet<String?>() {
        init {
            add(TTL)
            add(ACL)
            add(KMD)
            add(ID)
        }
    }

    private val ALLOWED =
        arrayOf<Class<out Any>>(
            java.lang.Boolean::class.java,
            java.lang.Byte::class.java,
            java.lang.Short::class.java,
            java.lang.Integer::class.java,
            java.lang.Long::class.java,
            java.lang.Float::class.java,
            java.lang.Double::class.java,
            java.lang.String::class.java,

            Boolean::class.java,
            Byte::class.java,
            Short::class.java,
            Int::class.java,
            Long::class.java,
            Float::class.java,
            Double::class.java,
            String::class.java,
            Date::class.java,
            ByteArray::class.java,
            Array<Byte>::class.java
        )

    @JvmStatic
    fun getClassHash(clazz: Class<out GenericJson>): String {
        return getClassHash(clazz, SelfReferenceState.DEFAULT, ArrayList())
    }

    private fun getClassHash(clazz: Class<out GenericJson>, selfReferenceState: SelfReferenceState, classes: MutableList<String>): String {
        val sb = StringBuilder()
        val fields = getClassFieldsAndParentClassFields(clazz)
        if (classes.contains(clazz.simpleName)) {
            return clazz.name
        } else {
            classes.add(clazz.simpleName)
        }
        for (f in fields) {
            val classesList: MutableList<String> = ArrayList(classes)
            val fieldInfo = FieldInfo.of(f) ?: continue
            if (isArrayOrCollection(fieldInfo.type)) {
                val underlying = getUnderlying(f)
                if (underlying != null && GenericJson::class.java.isAssignableFrom(underlying)) {
                    if (!underlying.simpleName.equals(clazz.simpleName, ignoreCase = true)) {
                        if (selfReferenceState == SelfReferenceState.DEFAULT) {
                            val innerHash = getClassHash(underlying as Class<out GenericJson>, selfReferenceState, classesList)
                            sb.append("[").append(fieldInfo.name).append("]:").append(innerHash).append(";")
                        }
                    } else if (selfReferenceState == SelfReferenceState.DEFAULT) {
                        classes.add(clazz.simpleName)
                        val innerHash = getClassHash(underlying as Class<out GenericJson>, SelfReferenceState.SUBCLASS, classesList)
                        sb.append("[").append(fieldInfo.name).append("]:").append(innerHash).append(";")
                    }
                }
            } else if (GenericJson::class.java.isAssignableFrom(fieldInfo.type)) {
                if (!f.type.simpleName.equals(clazz.simpleName, ignoreCase = true)) {
                    if (selfReferenceState == SelfReferenceState.DEFAULT) {
                        val innerHash = getClassHash(fieldInfo.type as Class<out GenericJson>, selfReferenceState, classesList)
                        sb.append(fieldInfo.name).append(":").append(innerHash).append(";")
                    }
                } else if (selfReferenceState == SelfReferenceState.DEFAULT) {
                    val innerHash = getClassHash(fieldInfo.type as Class<out GenericJson>, SelfReferenceState.SUBCLASS, classesList)
                    sb.append(fieldInfo.name).append(":").append(innerHash).append(";")
                }
            } else {
                for (c in ALLOWED) {
                    if (fieldInfo.type == c) {
                        if (fieldInfo.name != ID && fieldInfo.name != TTL) {
                            sb.append(fieldInfo.name).append(":").append(c.name).append(";")
                        }
                        break
                    }
                }
            }
        }
        sb.append(ID).append(":").append(String::class.java.name).append(";")
        sb.append(TTL).append(":").append(Long::class.java.name).append(";")
        var hashtext: String
        try {
            val m: MessageDigest = MessageDigest.getInstance("MD5")
            m.reset()
            m.update(sb.toString().toByteArray())
            val digest: ByteArray = m.digest()
            val bigInt = BigInteger(1, digest)
            hashtext = bigInt.toString(16)
            while (hashtext.length < 32) {
                hashtext = "0$hashtext"
            }
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } finally {
            hashtext = sb.toString()
        }
        return hashtext
    }

    @JvmStatic
    fun createScheme(name: String, realm: DynamicRealm, clazz: Class<out GenericJson>): RealmObjectSchema {
        val schema = createSchemeFromClass(name, realm, clazz, SelfReferenceState.DEFAULT, ArrayList())
        val shortName = TableNameManager.getShortName(name, realm)
        if (!schema.hasField(KinveyMetaData.KMD) && !name.endsWith(Constants.UNDERSCORE + KinveyMetaData.KMD)
                && !name.endsWith(Constants.UNDERSCORE + AccessControlList.ACL)) {
            val innerScheme = createSchemeFromClass(shortName + Constants.UNDERSCORE + KinveyMetaData.KMD,
                    realm, KinveyMetaData::class.java, SelfReferenceState.DEFAULT, ArrayList())
            schema.addRealmObjectField(KinveyMetaData.KMD, innerScheme)
        }
        if (!schema.hasField(AccessControlList.ACL)
                && !name.endsWith(Constants.UNDERSCORE + KinveyMetaData.KMD)
                && !name.endsWith(Constants.UNDERSCORE + AccessControlList.ACL)) {
            val innerScheme = createSchemeFromClass(shortName + Constants.UNDERSCORE + AccessControlList.ACL,
                    realm, AccessControlList::class.java, SelfReferenceState.DEFAULT, ArrayList())
            schema.addRealmObjectField(AccessControlList.ACL, innerScheme)
        }
        if (!schema.hasField(TTL)) {
            schema.addField(TTL, Long::class.java)
        }
        return schema
    }

    /**
     * Migrate from old table name to new table name
     * @param name table name to rename
     * @param realm Realm object
     * @param clazz Class in table
     */
    @JvmStatic
    fun migration(name: String, realm: DynamicRealm, clazz: Class<out GenericJson>) {
        rename(name, null, realm, clazz)
    }

    /**
     * Rename realm table
     * @param oldName old table name
     * @param newName new table name
     * @param realm Realm object
     * @param clazz Class in table
     */
    private fun rename(oldName: String, newName: String?, realm: DynamicRealm, clazz: Class<out GenericJson>) {
        val shortName: String = newName ?: TableNameManager.createShortName(oldName, realm)
        val schema: RealmSchema = realm.schema
        schema.rename(oldName, shortName)
        val fields = getClassFieldsAndParentClassFields(clazz)
        for (f in fields) {
            val fieldInfo = FieldInfo.of(f) ?: continue
            if (fieldInfo.type.isArray || Collection::class.java.isAssignableFrom(fieldInfo.type)) {
                val underlying = getUnderlying(f)
                if (underlying != null && GenericJson::class.java.isAssignableFrom(underlying)) {
                    rename(oldName + Constants.UNDERSCORE + fieldInfo.name,
                            TableNameManager.createShortName(shortName + Constants.UNDERSCORE + fieldInfo.name, realm),
                            realm, fieldInfo.type as Class<out GenericJson>)
                } else {
                    for (c in ALLOWED) {
                        if (underlying == c) {
                            schema.rename(oldName, shortName)
                            break
                        }
                    }
                }
            } else if (GenericJson::class.java.isAssignableFrom(fieldInfo.type)) {
                rename(oldName + Constants.UNDERSCORE + fieldInfo.name,
                        TableNameManager.createShortName(shortName + Constants.UNDERSCORE + fieldInfo.name, realm),
                        realm, fieldInfo.type as Class<out GenericJson>)
            }
        }

        // remove unnecessary tables ..._acl_kmd
        if (schema.contains(oldName + Constants.UNDERSCORE + AccessControlList.ACL + Constants.UNDERSCORE + KinveyMetaData.KMD)) {
            val objectSchema = schema.get(oldName + Constants.UNDERSCORE + AccessControlList.ACL)
            objectSchema?.removeField(KinveyMetaData.KMD)
            schema.remove(oldName + Constants.UNDERSCORE + AccessControlList.ACL + Constants.UNDERSCORE + KinveyMetaData.KMD)
        }

        // rename _kmd table
        if (schema.contains(oldName + Constants.UNDERSCORE + KinveyMetaData.KMD)) {
            schema.rename(oldName + Constants.UNDERSCORE + KinveyMetaData.KMD,
                    TableNameManager.createShortName(shortName + Constants.UNDERSCORE + KinveyMetaData.KMD, realm))
        }

        // rename or create _acl table
        if (schema.contains(oldName + Constants.UNDERSCORE + AccessControlList.ACL)) {
            schema.rename(oldName + Constants.UNDERSCORE + AccessControlList.ACL,
                    TableNameManager.createShortName(TableNameManager.getShortName(oldName, realm)
                            + Constants.UNDERSCORE + AccessControlList.ACL, realm))
        } else {
            val objectSchema = schema.get(shortName)
            val innerScheme = createSchemeFromClass(shortName + Constants.UNDERSCORE + AccessControlList.ACL, realm, AccessControlList::class.java,
                    SelfReferenceState.DEFAULT, ArrayList()) // TODO: 24.11.2017 check
            objectSchema?.addRealmObjectField(AccessControlList.ACL, innerScheme)

            //get table
            val results = realm.where(shortName).findAll()

            //add "_acl" field to each item
            for (realmObject in results) {
                val acl = AccessControlList()
                acl.set("creator", sharedInstance().activeUser?.id)
                val innerObject = saveClassData(shortName + Constants.UNDERSCORE + AccessControlList.ACL,
                        realm,
                        AccessControlList::class.java,
                        acl, SelfReferenceState.DEFAULT,
                        ArrayList())
                realmObject.setObject(AccessControlList.ACL, innerObject)
            }
        }
    }

    /**
     * Fix to _acl_kmd tables
     * @param name Collection name
     * @param realm Realm object
     * @param clazz Class in table
     */
    @JvmStatic
    fun checkAclKmdFields(name: String, realm: DynamicRealm, clazz: Class<out GenericJson>) {
        val schema: RealmSchema = realm.schema
        if (schema.contains(name + Constants.UNDERSCORE + AccessControlList.ACL + Constants.UNDERSCORE + KinveyMetaData.KMD)) {
            val table = TableNameManager.getShortName(TableNameManager.getShortName(name, realm) + Constants.UNDERSCORE + AccessControlList.ACL, realm)
            table?.let { t ->
                val objectSchema = schema.get(t)
                objectSchema?.removeField(KinveyMetaData.KMD)
                schema.remove(name + Constants.UNDERSCORE + AccessControlList.ACL + Constants.UNDERSCORE + KinveyMetaData.KMD)
            }
        }
        fillNewAclFields(name, false, realm, clazz)
    }

    /**
     * Fix to _acl_kmd tables
     * Fill acl fields, which were created in migration process in versions before 3.0.11
     * @param name Collection name
     * @param isFill False if don't need to fill acl field (false is used for acl field of main item)
     * @param realm Realm object
     * @param clazz Class in table
     */
    private fun fillNewAclFields(name: String, isFill: Boolean, realm: DynamicRealm, clazz: Class<out GenericJson>) {
        val shortName = TableNameManager.getShortName(name, realm)
        val schema: RealmSchema = realm.schema
        val fields = getClassFieldsAndParentClassFields(clazz)
        for (f in fields) {
            val fieldInfo = FieldInfo.of(f) ?: continue
            if (fieldInfo.type.isArray || Collection::class.java.isAssignableFrom(fieldInfo.type)) {
                val underlying = getUnderlying(f)
                if (underlying != null && GenericJson::class.java.isAssignableFrom(underlying)) {
                    fillNewAclFields(shortName + Constants.UNDERSCORE + fieldInfo.name, true, realm, fieldInfo.type as Class<out GenericJson>)
                }
            } else if (GenericJson::class.java.isAssignableFrom(fieldInfo.type)) {
                fillNewAclFields(shortName + Constants.UNDERSCORE + fieldInfo.name, true, realm, fieldInfo.type as Class<out GenericJson>)
            }
        }
        if (isFill) {
            // fill _acl table
            val table = TableNameManager.getShortName(shortName + Constants.UNDERSCORE + AccessControlList.ACL, realm) ?: ""
            if (schema.contains(table)) {
                val results: List<DynamicRealmObject> = realm.where(shortName).findAll()
                //add "_acl" field to each item
                for (realmObject in results) {
                    if (realmObject.get<Any?>("_acl") == null) {
                        val acl = AccessControlList()
                        acl.set("creator", sharedInstance().activeUser?.id)
                        val innerObject = saveClassData(shortName + Constants.UNDERSCORE + AccessControlList.ACL,
                                realm,
                                AccessControlList::class.java,
                                acl, SelfReferenceState.DEFAULT,
                                ArrayList())
                        realmObject.setObject(AccessControlList.ACL, innerObject)
                    }
                }
            }
        }
    }

    private fun createSchemeFromClass(name: String, realm: DynamicRealm, clazz: Class<out GenericJson>,
                                      selfReferenceState: SelfReferenceState, classes: MutableList<String>): RealmObjectSchema {
        val shortName: String = TableNameManager.createShortName(name, realm)
        val schema: RealmObjectSchema = realm.schema.create(shortName)
        val fields = getClassFieldsAndParentClassFields(clazz)
        var state: SelfReferenceState?
        classes.add(clazz.simpleName)
        for (f in fields) {
            state = null
            val classesList: MutableList<String> = ArrayList(classes)
            val fieldInfo = FieldInfo.of(f) ?: continue
            if (Collection::class.java.isAssignableFrom(fieldInfo.type)) {
                val underlying = getUnderlying(f)
                if (underlying != null && GenericJson::class.java.isAssignableFrom(underlying)) {
                    if (!underlying.simpleName.equals(clazz.simpleName, ignoreCase = true)) {
                        state = selfReferenceState
                    } else if (selfReferenceState == SelfReferenceState.DEFAULT || selfReferenceState == SelfReferenceState.SUBCLASS) {
                        state = SelfReferenceState.LIST
                    } else if (selfReferenceState == SelfReferenceState.LIST) {
                        state = SelfReferenceState.CLASS
                    } else if (selfReferenceState == SelfReferenceState.CLASS) {
                        state = SelfReferenceState.SUBLIST
                    } else if (selfReferenceState == SelfReferenceState.SUBLIST) {
                        state = SelfReferenceState.SUBLIST
                    }
                    if (state != null) {
                        if (selfReferenceState == SelfReferenceState.SUBLIST) {
                            val table = TableNameManager.getShortName(TableNameManager.getOriginalName(
                                    TableNameManager.getShortName(name, realm), realm), realm) ?: ""
                            val schemaObj = realm.schema.get(table)
                            schemaObj?.let { schema.addRealmObjectField(fieldInfo.name, it) }
                        } else {
                            val innerScheme = createSchemeFromClass(shortName + Constants.UNDERSCORE + fieldInfo.name, realm,
                                    underlying as Class<out GenericJson>, state, classesList)
                            schema.addRealmListField(fieldInfo.name, innerScheme)
                        }
                    }
                } else {
                    for (c in ALLOWED) {
                        if (underlying == c) {
                            val innerScheme: RealmObjectSchema = realm.schema.create(TableNameManager.createShortName(shortName + Constants.UNDERSCORE + fieldInfo.name, realm))
                            if (!innerScheme.hasField(ID)) {
                                innerScheme.addField(ID, String::class.java, FieldAttribute.PRIMARY_KEY)
                            }
                            innerScheme.addField(fieldInfo.name + ITEMS, underlying)
                            schema.addRealmListField(fieldInfo.name, innerScheme)
                            break
                        }
                    }
                }
            } else if (GenericJson::class.java.isAssignableFrom(fieldInfo.type)) {
                if (!f.type.simpleName.equals(clazz.simpleName, ignoreCase = true)) {
                    if (classes.contains(f.type.simpleName)) {
                        if (selfReferenceState == SelfReferenceState.DEFAULT) {
                            val innerScheme = createSchemeFromClass(shortName + Constants.UNDERSCORE + fieldInfo.name, realm,
                                    fieldInfo.type as Class<out GenericJson>, SelfReferenceState.SUBCLASS, classesList)
                            schema.addRealmObjectField(fieldInfo.name, innerScheme)
                        } else if (selfReferenceState == SelfReferenceState.SUBCLASS) {
                            schema.addRealmObjectField(fieldInfo.name, schema)
                        }
                    } else {
                        val innerScheme = createSchemeFromClass(shortName + Constants.UNDERSCORE + fieldInfo.name, realm,
                                fieldInfo.type as Class<out GenericJson>, selfReferenceState, classesList)
                        schema.addRealmObjectField(fieldInfo.name, innerScheme)
                    }
                } else if (selfReferenceState == SelfReferenceState.DEFAULT) {
                    val innerScheme = createSchemeFromClass(shortName + Constants.UNDERSCORE + fieldInfo.name, realm,
                            fieldInfo.type as Class<out GenericJson>, SelfReferenceState.SUBCLASS, classesList)
                    schema.addRealmObjectField(fieldInfo.name, innerScheme)
                } else if (selfReferenceState == SelfReferenceState.SUBCLASS || selfReferenceState == SelfReferenceState.CLASS_AND_LIST) {
                    schema.addRealmObjectField(fieldInfo.name, schema)
                } else if (selfReferenceState == SelfReferenceState.LIST) {
                    val innerScheme = createSchemeFromClass(shortName + "_" + fieldInfo.name, realm,
                            fieldInfo.type as Class<out GenericJson>, SelfReferenceState.CLASS_AND_LIST, classesList)
                    schema.addRealmObjectField(fieldInfo.name, innerScheme)
                }
            } else {
                for (c in ALLOWED) {
                    if (fieldInfo.type == c) {
                        if (fieldInfo.name != ID) {
                            schema.addField(fieldInfo.name, fieldInfo.type)
                        }
                        break
                    }
                }
            }
        }
        if (!schema.hasField(ID)) {
            schema.addField(ID, String::class.java, FieldAttribute.PRIMARY_KEY)
        }
        return schema
    }

    @JvmStatic
    fun saveData(name: String, realm: DynamicRealm, clazz: Class<out GenericJson>?, obj: GenericJson?): DynamicRealmObject? {
        val realmObject = saveClassData(name, realm, clazz, obj, SelfReferenceState.DEFAULT, ArrayList())
        val shortName = TableNameManager.getShortName(name, realm)
        if (obj?.containsKey(KinveyMetaData.KMD) == false
                && !name.endsWith(Constants.UNDERSCORE + KinveyMetaData.KMD)
                && !name.endsWith(Constants.UNDERSCORE + AccessControlList.ACL)) {
            val metadata = KinveyMetaData()
            metadata.set(KinveyMetaData.LMT, String.format(Locale.US, Constants.TIME_FORMAT,
                    Calendar.getInstance(TimeZone.getTimeZone(Constants.Z))))
            metadata.set(KinveyMetaData.ECT, String.format(Locale.US, Constants.TIME_FORMAT,
                    Calendar.getInstance(TimeZone.getTimeZone(Constants.Z))))
            val innerObject = saveClassData(shortName + Constants.UNDERSCORE + KinveyMetaData.KMD,
                    realm,
                    KinveyMetaData::class.java,
                    metadata,
                    SelfReferenceState.DEFAULT,
                    ArrayList())
            realmObject?.setObject(KinveyMetaData.KMD, innerObject)
        }
        if (obj?.containsKey(AccessControlList.ACL) == false
                && !name.endsWith(Constants.UNDERSCORE + AccessControlList.ACL)
                && !name.endsWith(Constants.UNDERSCORE + KinveyMetaData.KMD)
                && realm.schema.contains(TableNameManager.getShortName(shortName + Constants.UNDERSCORE + AccessControlList.ACL, realm) ?: "")) {
            val acl = AccessControlList()
            acl.set("creator", sharedInstance().activeUser?.id)
            val innerObject = saveClassData(shortName + Constants.UNDERSCORE + AccessControlList.ACL,
                    realm,
                    AccessControlList::class.java,
                    acl, SelfReferenceState.DEFAULT,
                    ArrayList())
            realmObject?.setObject(AccessControlList.ACL, innerObject)
        }
        //set dynamic fields
        obj?.let {
            if (realmObject?.get<Any>(TTL) !== obj[TTL]) {
                realmObject?.set(TTL, obj[TTL])
            }
        }
        return realmObject
    }

    private fun saveClassData(name: String, realm: DynamicRealm, clazz: Class<out GenericJson>?,
                              obj: GenericJson?, selfReferenceState: SelfReferenceState, classes: MutableList<String>): DynamicRealmObject? {
        val shortName = TableNameManager.getShortName(name, realm)
        val fields = getClassFieldsAndParentClassFields(clazz)
        var dynObject: DynamicRealmObject? = null
        if (obj?.containsKey(ID) == true && obj[ID] != null) {
            dynObject = realm.where(shortName)
                    .equalTo(ID, obj[ID] as String)
                    .findFirst()
        } else { obj?.run { obj[ID] = TEMP_ID + UUID.randomUUID().toString() } }
        var kmdId: String? = null
        var aclId: String? = null
        if (dynObject == null) {
            obj?.run { dynObject = realm.createObject(shortName, obj[ID]) }
        } else {
            if (dynObject?.hasField(KinveyMetaData.KMD) == true
                    && dynObject?.getObject(KinveyMetaData.KMD) != null) {
                kmdId = dynObject?.getObject(KinveyMetaData.KMD)?.getString(ID)
            }
            if (dynObject?.hasField(AccessControlList.ACL) == true
                    && dynObject?.getObject(AccessControlList.ACL) != null) {
                aclId = dynObject?.getObject(AccessControlList.ACL)?.getString(ID)
            }
        }
        if (obj?.containsKey(KinveyMetaData.KMD) == true
                && realm.schema.contains(TableNameManager.getShortName(shortName + Constants.UNDERSCORE + KinveyMetaData.KMD, realm) ?: "")) {
            val kmd = obj[KinveyMetaData.KMD] as Map<String, Any>?
            if (kmd != null) {
                val metadata = KinveyMetaData.fromMap(kmd)
                metadata.set(ID, kmdId)
                val innerObject = saveClassData(shortName + Constants.UNDERSCORE + KinveyMetaData.KMD,
                        realm,
                        KinveyMetaData::class.java,
                        metadata,
                        selfReferenceState,
                        ArrayList())
                dynObject?.setObject(KinveyMetaData.KMD, innerObject)
            }
        }
        if (obj?.containsKey(AccessControlList.ACL) == true
            && realm.schema.contains(
                TableNameManager.getShortName(shortName + Constants.UNDERSCORE + AccessControlList.ACL, realm) ?: "")) {
            val acl = obj[AccessControlList.ACL] as Map<String, Any>?
            if (acl != null) {
                val accessControlList = fromMap(acl)
                accessControlList.set(ID, aclId)
                val innerObject = saveClassData(shortName + Constants.UNDERSCORE + AccessControlList.ACL,
                        realm,
                        AccessControlList::class.java,
                        accessControlList,
                        selfReferenceState, ArrayList())
                dynObject?.setObject(AccessControlList.ACL, innerObject)
            }
        }
        var state: SelfReferenceState?
        clazz?.simpleName?.let { classes.add(it) }
        for (f in fields) {
            val classesList: MutableList<String> = ArrayList(classes)
            val fieldInfo = FieldInfo.of(f) ?: continue
            state = null
            if (isArrayOrCollection(f.type) && fieldInfo.getValue(obj) != null) {
                val underlying = getUnderlying(f)
                val list = RealmList<Any?>()
                val collection = fieldInfo.getValue(obj)
                if (f.type.isArray) {
                    if (!underlying?.simpleName.equals(clazz?.simpleName, ignoreCase = true)) {
                        state = selfReferenceState
                    } else if (selfReferenceState == SelfReferenceState.DEFAULT || selfReferenceState == SelfReferenceState.SUBCLASS) {
                        state = SelfReferenceState.LIST
                    } else if (selfReferenceState == SelfReferenceState.LIST) {
                        state = SelfReferenceState.CLASS
                    } else if (selfReferenceState == SelfReferenceState.CLASS) {
                        state = SelfReferenceState.SUBLIST
                    } else if (selfReferenceState == SelfReferenceState.SUBLIST) {
                        state = SelfReferenceState.SUBLIST
                    }
                    state?.let { st ->
                        collection?.let {
                            for (element in it as Array<GenericJson>) {
                                list.add(saveClassData(
                                        if (selfReferenceState == SelfReferenceState.SUBLIST) name
                                        else shortName + Constants.UNDERSCORE + fieldInfo.name,
                                        realm,
                                        underlying as Class<out GenericJson>?,
                                        element,
                                        st, classesList))
                            }
                        }
                    }
                } else {
                    if (underlying != null && GenericJson::class.java.isAssignableFrom(underlying)) {
                        if (!underlying.simpleName.equals(clazz?.simpleName, ignoreCase = true)) {
                            state = selfReferenceState
                        } else if (selfReferenceState == SelfReferenceState.DEFAULT || selfReferenceState == SelfReferenceState.SUBCLASS) {
                            state = SelfReferenceState.LIST
                        } else if (selfReferenceState == SelfReferenceState.LIST) {
                            state = SelfReferenceState.CLASS
                        } else if (selfReferenceState == SelfReferenceState.CLASS) {
                            state = SelfReferenceState.SUBLIST
                        } else if (selfReferenceState == SelfReferenceState.SUBLIST) {
                            state = SelfReferenceState.SUBLIST
                        }
                        if (state != null) {
                            for (genericJson in collection as Collection<GenericJson>) {
                                list.add(saveClassData(
                                        if (selfReferenceState == SelfReferenceState.SUBLIST) name
                                        else shortName + Constants.UNDERSCORE + fieldInfo.name,
                                        realm,
                                        underlying as Class<out GenericJson>?,
                                        genericJson,
                                        state, classesList) as DynamicRealmObject)
                            }
                        }
                    } else {
                        var dynamicRealmObject: DynamicRealmObject? = null
                        for (o in collection as Collection<*>) {
                            val table = TableNameManager.getShortName(shortName + Constants.UNDERSCORE + fieldInfo.name, realm)
                            table?.let { dynamicRealmObject = realm.createObject(table, UUID.randomUUID().toString()) }
                            for (c in ALLOWED) {
                                if (underlying == c) {
                                    dynamicRealmObject?.set(fieldInfo.name + ITEMS, o)
                                    break
                                }
                            }
                            list.add(dynamicRealmObject)
                        }
                    }
                    dynObject?.setList(fieldInfo.name, list)
                }
            } else if (GenericJson::class.java.isAssignableFrom(fieldInfo.type)) {
                if (!f.type.simpleName.equals(clazz?.simpleName, ignoreCase = true)) {
                    state = selfReferenceState
                } else if (selfReferenceState == SelfReferenceState.DEFAULT) {
                    state = SelfReferenceState.SUBCLASS
                } else if (selfReferenceState == SelfReferenceState.SUBCLASS) {
                    state = SelfReferenceState.SUBCLASS
                }
                if (state != null) {
                    if (obj != null && fieldInfo.getValue(obj) != null) {
                        val innerObject = saveClassData(
                                if (selfReferenceState == SelfReferenceState.SUBCLASS &&
                                    classes.contains(f.type.simpleName)) name else shortName + Constants.UNDERSCORE + fieldInfo.name,
                                realm,
                                fieldInfo.type as Class<out GenericJson>,
                                obj[fieldInfo.name] as GenericJson, state, classesList)
                        dynObject?.setObject(fieldInfo.name, innerObject)
                    } else {
                        if (dynObject?.hasField(fieldInfo.name) == true) {
                            dynObject?.setNull(fieldInfo.name)
                        }
                    }
                }
            } else {
                if (fieldInfo.name != ID) {
                    for (c in ALLOWED) {
                        if (fieldInfo.type == c) {
                            dynObject?.set(fieldInfo.name, fieldInfo.getValue(obj))
                            break
                        }
                    }
                }
            }
        }
        return dynObject
    }

    /**
     * Cascade delete items by id
     * @param collection collection name
     * @param realm Realm object
     * @param clazz Class
     * @param id item id to delete
     * @return count of deleted items (it should be "1" in correct case)
     */
    @JvmStatic
    fun deleteClassData(collection: String, realm: DynamicRealm, clazz: Class<out GenericJson>, id: String): Int {
        return deleteClassData(collection, realm, clazz, id, SelfReferenceState.DEFAULT, ArrayList())
    }

    private fun deleteClassData(collection: String, realm: DynamicRealm, clazz: Class<out GenericJson>,
                                id: String, selfReferenceState: SelfReferenceState?, classes: MutableList<String>): Int {
        val shortName = TableNameManager.getShortName(collection, realm)
        val realmObject = realm.where(shortName).equalTo(ID, id).findFirst()
        val size = if (realmObject != null) 1 else 0
        if (realmObject == null) {
            return size
        }
        val fields = getClassFieldsAndParentClassFields(clazz)
        var state: SelfReferenceState? = null
        classes.add(clazz.simpleName)
        for (f in fields) {
            val classesList: MutableList<String> = ArrayList(classes)
            val fieldInfo = FieldInfo.of(f) ?: continue
            if (fieldInfo.type.isArray || Collection::class.java.isAssignableFrom(fieldInfo.type)) {
                val underlying = getUnderlying(f)
                if (underlying != null && GenericJson::class.java.isAssignableFrom(underlying)) {
                    val list: RealmList<DynamicRealmObject> = realmObject.getList(fieldInfo.name)
                    val ids: MutableList<String> = list
                            .filter { rObj -> rObj?.hasField(ID) == true && rObj.getString(ID) != null }
                            .mapNotNull { rObj -> rObj.getString(ID) }.toMutableList()
                    ids.forEach {
                        deleteClassData(shortName + Constants.UNDERSCORE + fieldInfo.name, realm,
                                underlying as Class<out GenericJson>, it, state, classesList)
                    }
                }
            } else if (GenericJson::class.java.isAssignableFrom(fieldInfo.type)) {
                if (!f.type.simpleName.equals(clazz.simpleName, ignoreCase = true)) {
                    state = selfReferenceState
                } else if (selfReferenceState == SelfReferenceState.DEFAULT) {
                    state = SelfReferenceState.SUBCLASS
                } else if (selfReferenceState == SelfReferenceState.SUBCLASS) {
                    state = SelfReferenceState.SUBCLASS
                }
                if (state != null) {
                    val dynObject = realmObject.getObject(fieldInfo.name)
                    if (dynObject?.hasField(ID) == true && dynObject.getString(ID) != null) {
                        deleteClassData(if (selfReferenceState == SelfReferenceState.SUBCLASS &&
                                classes.contains(f.type.simpleName)) collection else shortName + Constants.UNDERSCORE + fieldInfo.name, realm,
                                fieldInfo.type as Class<out GenericJson>, dynObject.getString(ID), state, classesList)
                    }
                }
            }
        }
        if (realmObject.hasField(AccessControlList.ACL)
                && !collection.endsWith(Constants.UNDERSCORE + KinveyMetaData.KMD)
                && !collection.endsWith(Constants.UNDERSCORE + AccessControlList.ACL)
                && realmObject.getObject(AccessControlList.ACL) != null && realmObject.getObject(AccessControlList.ACL)!!.hasField(ID)) {
            deleteClassData(shortName + Constants.UNDERSCORE + AccessControlList.ACL, realm,
                    AccessControlList::class.java, realmObject.getObject(AccessControlList.ACL)!!.getString(ID))
        }
        if (realmObject.hasField(KinveyMetaData.KMD)
                && !collection.endsWith(Constants.UNDERSCORE + KinveyMetaData.KMD)
                && !collection.endsWith(Constants.UNDERSCORE + AccessControlList.ACL)
                && realmObject.getObject(KinveyMetaData.KMD) != null && realmObject.getObject(KinveyMetaData.KMD)!!.hasField(ID)) {
            deleteClassData(shortName + Constants.UNDERSCORE + KinveyMetaData.KMD, realm,
                    KinveyMetaData::class.java, realmObject.getObject(KinveyMetaData.KMD)!!.getString(ID))
        }
        realmObject.deleteFromRealm()
        return size
    }

    @JvmStatic
    fun <T : GenericJson?> realmToObject(dynamic: DynamicRealmObject?, objectClass: Class<T>): T? {
        return realmToObject(dynamic, objectClass, false)
    }

    private fun <T : GenericJson?> realmToObject(dynamic: DynamicRealmObject?, objectClass: Class<T>, isEmbedded: Boolean): T? {
        if (dynamic == null) {
            return null
        }
        var ret: T? = null
        try {
            ret = objectClass.newInstance()
            val classInfo: ClassInfo = ClassInfo.of(objectClass)
            var info: FieldInfo?
            var o: Any? = null
            for (field in dynamic.fieldNames) {
                info = classInfo.getFieldInfo(field)
                if (isAclPermissionField(field) && dynamic.isNull(field)) {
                    continue
                }
                o = dynamic.get(field)
                if (info == null) {
                    //prevent private fields like "__ttl__" to be published
                    if (isEmbedded) {
                        if (!EMBEDDED_OBJECT_PRIVATE_FIELDS.contains(field)) {
                            if (o is DynamicRealmObject) {
                                ret?.put(field, realmToObject(o, GenericJson::class.java, true))
                            } else {
                                ret?.put(field, o)
                            }
                        }
                    } else {
                        if (!PRIVATE_FIELDS.contains(field)) {
                            if (o is DynamicRealmObject) {
                                ret?.put(field, realmToObject(o, GenericJson::class.java, true))
                            } else {
                                ret?.put(field, o)
                            }
                        }
                    }
                    continue
                }
                if (Number::class.java.isAssignableFrom(info.type)) {
                    val n: Number = dynamic.get(info.name)
                    when {
                        Long::class.java.isAssignableFrom(info.type) -> ret?.put(info.name, n.toLong())
                        java.lang.Integer::class.java.isAssignableFrom(info.type) or
                        Int::class.java.isAssignableFrom(info.type) -> ret?.put(info.name, n.toInt())
                        Byte::class.java.isAssignableFrom(info.type) -> ret?.put(info.name, n.toByte())
                        Short::class.java.isAssignableFrom(info.type) -> ret?.put(info.name, n.toShort())
                        Float::class.java.isAssignableFrom(info.type) -> ret?.put(info.name, n.toFloat())
                        Double::class.java.isAssignableFrom(info.type) -> ret?.put(info.name, n.toDouble())
                    }
                } else if (GenericJson::class.java.isAssignableFrom(info.type)) {
                    ret?.put(info.name, realmToObject(dynamic.getObject(info.name),
                            info.type as Class<out GenericJson>, true))
                } else if (isArrayOrCollection(info.type)) {
                    val underlying = getUnderlying(info.field)
                    if (underlying != null) {
                        val list = dynamic.getList(info.name)
                        if (underlying.isArray && GenericJson::class.java.isAssignableFrom(underlying)) {
                            val array = list?.map { item -> realmToObject(item, underlying, true) }?.toTypedArray()
                            ret?.put(info.name, array)
                        } else {
                            val c = if (GenericJson::class.java.isAssignableFrom(underlying)) {
                                list?.map { item -> realmToObject(item, underlying, true) }
                            } else {
                                list?.map { item -> item.get(info.name + ITEMS) as Any }
                            }
                            ret?.put(info.name, c)
                        }
                    }
                } else { //if you are here, then the field is a primitive
                    // realm keeps int values as a long, so we need to convert it back
                    if (info.type.isPrimitive &&
                       (info.type.simpleName == "int" || info.type.simpleName == "Integer")
                        && o is Long) {
                        ret?.put(info.name, o.toInt())
                    } else {
                        ret?.put(info.name, o)
                    }
                }
            }
            if (!isEmbedded && ret?.containsKey(KinveyMetaData.KMD) == false && dynamic.hasField(KinveyMetaData.KMD)) {
                ret.put(KinveyMetaData.KMD, realmToObject(dynamic.getObject(KinveyMetaData.KMD), KinveyMetaData::class.java, true))
            }
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return ret
    }

    private fun isAclPermissionField(field: String): Boolean {
        return when (field) {
            GW, GR, R, W -> true
            else -> false
        }
    }

    @JvmStatic
    fun isArrayOrCollection(clazz: Class<*>): Boolean {
        return clazz.isArray || Collection::class.java.isAssignableFrom(clazz)
    }

    private fun getUnderlying(f: Field): Class<GenericJson>? {
        val type = f.type
        val underlying: Class<GenericJson>?
        underlying = if (type.isArray) {
            type.componentType as Class<GenericJson>?
        } else {
            val genericSuperclass = f.genericType as ParameterizedType
            genericSuperclass.actualTypeArguments[0] as Class<GenericJson>?
        }
        return underlying
    }

    private fun getClassFieldsAndParentClassFields(clazz: Class<*>?): List<Field> {
        val fields: MutableList<Field> = ArrayList()
        clazz?.let { cls ->
            fields.addAll(listOf(*cls.declaredFields))
            cls.superclass?.let { sCls ->
                fields.addAll(listOf(*sCls.declaredFields))
            }
        }
        return fields
    }
}
