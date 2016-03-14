package com.kinvey.java.annotations;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.FieldInfo;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Prots on 3/11/16.
 */
public abstract class ReferenceHelper {

    public interface ReferenceListener{
        String onUnsavedReferenceFound(String collection, GenericJson object);
    }

    public static <T extends GenericJson> T processReferences(T gson, final ReferenceListener listener) throws IllegalAccessException, InstantiationException {


        for (Field f : gson.getClass().getDeclaredFields()){
            //find field info
            if (f.isAnnotationPresent(KinveyReference.class)){
                KinveyReference ref = f.getAnnotation(KinveyReference.class);
                f.setAccessible(true);
                Object reference = f.get(gson);
                if (GenericJson.class.isAssignableFrom(f.getType()) && reference != null) {
                    processReferences((GenericJson)reference, listener);
                    String id = listener.onUnsavedReferenceFound(ref.collection(), (GenericJson) f.get(gson));
                    gson.put(ref.fieldName(), new com.kinvey.java.model.KinveyReference(ref.collection(), id));
                    continue;
                } else if (f.getType().isArray() || Collection.class.isAssignableFrom(f.getType()) && reference != null){
                    //update
                    Object collection = f.get(gson);

                    List<com.kinvey.java.model.KinveyReference> listReferences = new ArrayList<>();

                    if (f.getType().isArray()){

                        int size = Array.getLength(collection);
                        Class<? extends GenericJson> clazz = (Class<? extends GenericJson>)f.getType().getComponentType();
                        for (int i = 0 ; i < size; i++){
                            processReferences((GenericJson) Array.get(collection, i), listener);
                            String id = listener.onUnsavedReferenceFound(ref.collection(), (GenericJson) Array.get(collection, i));
                            listReferences.add( new com.kinvey.java.model.KinveyReference(ref.collection(), id));
                        }
                    } else {
                        ParameterizedType genericSuperclass = (ParameterizedType)f.getGenericType();
                        Class<? extends GenericJson> clazz = (Class)genericSuperclass.getActualTypeArguments()[0];
                        for (GenericJson val : (Collection<? extends GenericJson>)collection){
                            processReferences((GenericJson) val, listener);
                            String id = listener.onUnsavedReferenceFound(ref.collection(), val);
                            listReferences.add( new com.kinvey.java.model.KinveyReference(ref.collection(), id));
                        }
                    }

                    gson.put(ref.fieldName(), listReferences);
                    continue;
                }
            }
        }

        return gson;
    }
}
