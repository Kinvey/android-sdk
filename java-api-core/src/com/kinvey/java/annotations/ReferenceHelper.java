package com.kinvey.java.annotations;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.FieldInfo;

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

    public static <T extends GenericJson> GenericJson processReferences(T gson, final ReferenceListener listener){

        GenericJson ret = new GenericJson();

        ClassInfo classInfo = ClassInfo.of(gson.getClass());


        for (String name : gson.keySet()){
            //find field info
            FieldInfo f = classInfo.getFieldInfo(name);
            if (f != null){
                if (f.getField().isAnnotationPresent(KinveyReference.class)){
                    KinveyReference ref = f.getField().getAnnotation(KinveyReference.class);
                    Object reference = f.getValue(gson);
                    if (GenericJson.class.isAssignableFrom(f.getType())) {
                        processReferences((GenericJson)reference, listener);
                        String id = listener.onUnsavedReferenceFound(ref.collection(), (GenericJson) f.getValue(gson));
                        ret.put(name, new com.kinvey.java.model.KinveyReference(ref.collection(), id));
                        continue;
                    } else if (f.getType().isArray() || Collection.class.isAssignableFrom(f.getType())){
                        //update
                        Object collection = f.getValue(gson);

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

                        ret.put(name, listReferences);
                        continue;
                    }
                }
            }

            ret.put(name, gson.get(name));

        }

        return ret;
    }
}
