package com.kinvey.java.deltaset;

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Prots on 12/11/15.
 */
public abstract class DeltaSetMerge {

    public static  <T> String[] getIdsForUpdate(T[] cache, DeltaSetItem[] items, JsonObjectParser parser) throws IOException {

        DeltaSetMergeResponse response = new DeltaSetMergeResponse();

        HashSet<String> idsToUpdate = new HashSet<String>();

        for (DeltaSetItem online : items){
            idsToUpdate.add(online.getId());
        }

        for (T cached : cache){
            if (cached instanceof GenericJson){
                DeltaSetItem cachedLmt = parser.parseAndClose(
                        new StringReader(((GenericJson) cached).toPrettyString()),
                        DeltaSetItem.class);
                for (DeltaSetItem online: items){
                    if (cachedLmt.getId().equals(online.getId())){
                        if (cachedLmt.getKmd().getLmt().equals(online.getKmd().getLmt())){
                            idsToUpdate.remove(cachedLmt.getId());
                            break;
                        }
                    }
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

    private static <T> Map<String, T> arrayToMap(T[] arr, JsonObjectParser parser) throws IOException {
        HashMap<String, T> ret = new HashMap<String, T>();
        for (T i: arr){
            if (i instanceof GenericJson) {
                DeltaSetItem item = parser.parseAndClose(
                        new StringReader(((GenericJson) i).toPrettyString()),
                        DeltaSetItem.class);
                ret.put(item.getId(), i);
            }
        }
        return ret;
    }

    public static <T> T[] merge(DeltaSetItem[] order, T[] cache, T[] online, JsonObjectParser parser) throws IOException {
        Map<String, T> cacheMap = arrayToMap(cache, parser),
                onlineMap = arrayToMap(online, parser);

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
