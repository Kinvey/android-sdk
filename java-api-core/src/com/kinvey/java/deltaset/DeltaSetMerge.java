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

package com.kinvey.java.deltaset;

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;
import java.util.List;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Prots on 12/11/15.
 */
public abstract class DeltaSetMerge {

    public static  <T> List<String> getIdsForUpdate(List<T> cache, List<DeltaSetItem> items) throws IOException {

        
        HashSet<String> idsToUpdate = new HashSet<String>();


        Map<String, T> cachedMap = listToMap(cache);
        Map<String, DeltaSetItem> onlineMap = listToMap(items);


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

        List<String> ret = new ArrayList<String>();
        ret.addAll(idsToUpdate);

        return ret;

    }

    private static <T> Map<String, T> listToMap(List<T> arr) throws IOException {
        HashMap<String, T> ret = new HashMap<String, T>();

        for (T i: arr){
            if (i instanceof GenericJson && ((GenericJson) i).containsKey("_id")){
                ret.put(((GenericJson) i).get("_id").toString(), i);
            }
        }
        return ret;
    }

    public static <T> List<T> merge(List<DeltaSetItem> order, List<T> cache, List<T> online, JsonObjectParser parser) throws IOException {
        Map<String, T> cacheMap = listToMap(cache),
                onlineMap = listToMap(online);

        ArrayList<T> orderedResult = new ArrayList<T>(order.size());

        for (DeltaSetItem item : order){
            //prefer online
            if (onlineMap.containsKey(item.getId())){
                orderedResult.add(onlineMap.get(item.getId()));
            } else if (cacheMap.containsKey(item.getId())){
                orderedResult.add(cacheMap.get(item.getId()));
            }
        }

        return orderedResult;


    };

}
