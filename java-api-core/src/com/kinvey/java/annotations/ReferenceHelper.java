package com.kinvey.java.annotations;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.FieldInfo;

import java.util.Collection;

/**
 * Created by Prots on 3/11/16.
 */
public abstract class ReferenceHelper {

    private interface ReferenceListener{
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
                    if (GenericJson.class.isAssignableFrom(f.getClass())) {
                        processReferences((GenericJson)reference, listener);
                        String id = listener.onUnsavedReferenceFound(ref.collection(), (GenericJson) f.getValue(gson));
                        continue;
                    } else if (f.getType().isArray() || Collection.class.isAssignableFrom(f.getType())){
                        //update
                        continue;
                    }
                }
            }

            ret.put(name, gson.get(name));

        }

        return ret;
    }
}
