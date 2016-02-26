package com.kinvey.java.deltaset;

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonObjectParser;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Prots on 12/11/15.
 */
public abstract class DeltaSetMerge {

    public static  <T> String[] getIdsForUpdate(T[] cache, DeltaSetItem[] items) throws IOException {

        
        HashSet<String> idsToUpdate = new HashSet<String>();


        Map<String, T> cachedMap = arrayToMap(cache);
        Map<String, DeltaSetItem> onlineMap = arrayToMap(items);


        //assume all data have to be updated
        idsToUpdate.addAll(onlineMap.keySet());

        for (String cachedId : cachedMap.keySet()){
            if (onlineMap.containsKey(cachedId)){
                DeltaSetItem onlineItem = onlineMap.get(cachedId);
                T cachedItem = cachedMap.get(cachedId);
                DeltaSetItem.KMD cachedKMD = null;
                if (cachedItem instanceof GenericJson && ((GenericJson) cachedItem).containsKey("_kmd")){
                    Object kmd = ((GenericJson) cachedItem).get("_kmd");
                    if (kmd instanceof Map){
                        cachedKMD = new DeltaSetItem.KMD(((Map)kmd).get("lmt").toString());
                    }
                }
                if (cachedKMD == null || cachedKMD.getLmt().equals(onlineItem.getKmd().getLmt())){
                    idsToUpdate.remove(cachedId);
                }
            }

        }

        String[] ret = new String[idsToUpdate.size()];
        int i = 0;
        for (String s : idsToUpdate){
            ret[i++] = s;
        }

        return ret;

    }

    private static <T> Map<String, T> arrayToMap(T[] arr) throws IOException {
        HashMap<String, T> ret = new HashMap<String, T>();

        for (T i: arr){
            if (i instanceof GenericJson && ((GenericJson) i).containsKey("_id")){
                ret.put(((GenericJson) i).get("_id").toString(), i);
            }
        }
        return ret;
    }

    public static <T> T[] merge(DeltaSetItem[] order, T[] cache, T[] online, JsonObjectParser parser) throws IOException {
        Map<String, T> cacheMap = arrayToMap(cache),
                onlineMap = arrayToMap(online);

        ArrayList<T> orderedResult = new ArrayList<T>(order.length);

        for (DeltaSetItem item : order){
            //prefer online
            if (onlineMap.containsKey(item.getId())){
                orderedResult.add(onlineMap.get(item.getId()));
            } else if (cacheMap.containsKey(item.getId())){
                orderedResult.add(cacheMap.get(item.getId()));
            }
        }

        return orderedResult.toArray(cache);


    };

}
