package com.kinvey.java;

import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import javax.activation.MimetypesFileTypeMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.UUID;

import com.kinvey.java.cache.AbstractKinveyCachedClientRequest;
import com.kinvey.java.cache.Cache;
import com.kinvey.java.cache.CachePolicy;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.model.AggregateEntity;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.query.MongoQueryFilter;

/**
 * Class for managing appData access to the Kinvey backend.
 *
 * @author mjsalinger
 * @author m0rganic
 * @since 2.0.2
 */
public class AppData<T> {


    private String collectionName;
    private Class<T> myClass;
    private AbstractClient client;

    private static final String ID_FIELD = "_id";

    private Cache<String , T> cache = null;
    private CachePolicy policy = CachePolicy.NOCACHE;
    private Object cacheLock = new Object();

    /**
     * Constructor to instantiate the AppData class.
     *
     * @param collectionName Name of the appData collection
     * @param myClass Class Type to marshall data between.
     */
    protected AppData(String collectionName, Class<T> myClass, AbstractClient client) {
        Preconditions.checkNotNull(collectionName, "collectionName must not be null.");
        Preconditions.checkNotNull(client, "client must not be null.");

        this.collectionName = collectionName;
        this.myClass = myClass;
        this.client = client;
    }

    /**
     * Sets the collectionName
     * @param collectionName Name of the appData collection.
     */
    public void setCollectionName(String collectionName) {
        Preconditions.checkNotNull(collectionName,"collectionName must not be null.");
        this.collectionName = collectionName;
    }

    /**
     * Gets the current collectionName
     * @return Name of appData collection
     */
    protected String getCollectionName() {
        return collectionName;
    }

    /**
     * Gets current class that this AppData instance references.
     * @return Current appData class for marshalling data
     */
    protected Class<T> getCurrentClass() {
        return myClass;
    }

    /**
     * Gets current client for this AppData
     * @return current client instance
     */
    protected AbstractClient getClient(){
        return this.client;
    }


    /**
     * Define a cache as well as the policy to use when interacting with the cache
     *
     * @param cache an implementation of the Cache interface, the cache itself
     * @param policy the policy defining behavior of the cache.
     */
    public void setCache(Cache cache, CachePolicy policy) {
        synchronized (cacheLock) {
            this.cache = cache;
            this.policy = policy;
        }
    }

    /**
     * Creates a new instance of {@link Query}
     *
     * @return New instance of Query object.
     */
    public Query query() {
        return new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
    }

    /**
     * Method to get an entity or entities.  Pass null to entityID to return all entities
     * in a collection.
     *
     * @param entityID entityID to get
     * @return Get object
     * @throws java.io.IOException
     */
    public GetEntity getEntity(String entityID) throws IOException {
        GetEntity getEntity = new GetEntity(entityID, myClass);
        client.initializeRequest(getEntity);
        return getEntity;
    }

    /**
     * Method to get a query of entities.  Pass an empty query to return all entities
     * in a collection.
     *
     * @param query Query to get
     * @return Get object
     * @throws java.io.IOException
     */
    public Get get(Query query) throws IOException {
        Preconditions.checkNotNull(query);
        Get get = new Get(query, Array.newInstance(myClass,0).getClass());
        client.initializeRequest(get);
        return get;
    }

    /**
     * Method to get all entities in a collection.
     *
     * @return Get Object
     * @throws IOException
     */

    public Get get() throws IOException {
        return get(new Query());
    }

    /**
     * Save (create or update) an entity to a collection.
     *
     * @param entity Entity to Save
     *
     * @return Save object
     * @throws IOException
     */
    public Save save(T entity) throws IOException {

        Save save;
        String sourceID;

        //TODO: add back in once LinkedResource is enabled
//        if (entity instanceof LinkedResource) {
//
//            for (String key : ((LinkedResource) entity).getAllFiles().keySet()) {
//                System.out.println("Kinvey - AppData" + " saving a  LinkedResource, " + key);
//                ((GenericJson) entity).put(key,
//                        (uploadAndGetLink(key, ((LinkedResource) entity).getAllFiles().get(key))));
//            }
//
//        }
        System.out.println("Kinvey - AppData" + " saving the entity");

        GenericJson jsonEntity = (GenericJson) entity;
        sourceID = (String) jsonEntity.get(ID_FIELD);

        if (sourceID != null) {
            save = new Save(entity, myClass, sourceID, SaveMode.PUT);
        } else {
            save = new Save(entity, myClass, SaveMode.POST);
        }

        client.initializeRequest(save);
        return save;
    }

    private HashMap<String,String> uploadAndGetLink(String key, LinkedFile myFile) throws IOException {
        HashMap<String,String> resourceMap = new HashMap<String, String>();
        String extension = getFileExtension(myFile.getFileName());
        InputStreamContent content = new InputStreamContent("application/octet-stream",
                new ByteArrayInputStream(myFile.getFileData()));
        content.setCloseInputStream(false);
        content.setRetrySupported(false);
        content.setLength(myFile.getFileData().length);

        StringBuilder resourceName = new StringBuilder();
        resourceName.append(collectionName).append(UUID.randomUUID()).append("-").append(key).append("-")
                .append(".").append(extension);

        String mimeType = new MimetypesFileTypeMap().getContentType(extension);
        String fileName = resourceName.toString();

        String uri = this.client.file().getUploadUrl(fileName).execute().getBlobTemporaryUri();
        this.client.file().upload(fileName, content).execute();

        resourceMap.put("_mime-type", mimeType);
        resourceMap.put("_loc",fileName);
        resourceMap.put("_type","resource");
        return resourceMap;
    }

    private static String getFileExtension(String name) {
        if (name == null) {
            return null;
        }


        int extIndex = name.lastIndexOf(".");

        if (extIndex == -1) {
            return "";
        } else {
            return name.substring(name.lastIndexOf(".") + 1);
        }
    }
    /**
     * Delete an entity from a collectionby ID.
     *
     * @param entityID entityID to delete
     * @return Delete object
     * @throws IOException
     */
    public Delete delete(String entityID) throws IOException {
        Preconditions.checkNotNull(entityID);
        Delete delete = new Delete(entityID);
        client.initializeRequest(delete);
        return delete;
    }

    /**
     * Delete an entity from a collection by Query.
     *
     * @param query query for entities to delete
     * @return Delete object
     * @throws IOException
     */
    public Delete delete(Query query) throws IOException {
        Preconditions.checkNotNull(query);
        Delete delete = new Delete(query);
        client.initializeRequest(delete);
        return delete;
    }

    /**
     * Retrieve a group by COUNT on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param query  optional query to filter by (null for all records in a collection)
     * @return Aggregate object
     * @throws IOException
     */
    public Aggregate count(ArrayList<String> fields, Query query) throws IOException {
        Preconditions.checkNotNull(fields);
        return aggregate(fields, AggregateEntity.AggregateType.COUNT, null, query);
    }

    /**
     * Retrieve a group by SUM on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param sumField field to sum
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     */
    public Aggregate sum(ArrayList<String> fields, String sumField, Query query) throws IOException {
        Preconditions.checkNotNull(fields);
        Preconditions.checkNotNull(sumField);
        return aggregate(fields, AggregateEntity.AggregateType.SUM, sumField, query);
    }

    /**
     * Retrieve a group by MAX on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param maxField field to obtain max value from
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     */
    public Aggregate max(ArrayList<String> fields, String maxField, Query query) throws IOException {
        Preconditions.checkNotNull(fields);
        Preconditions.checkNotNull(maxField);
        return aggregate(fields, AggregateEntity.AggregateType.MAX, maxField, query);
    }

    /**
     * Retrieve a group by MIN on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param minField field to obtain MIN value from
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     */
    public Aggregate min(ArrayList<String> fields, String minField, Query query) throws IOException {
        Preconditions.checkNotNull(fields);
        Preconditions.checkNotNull(minField);
        return aggregate(fields, AggregateEntity.AggregateType.MIN, minField, query);
    }

    /**
     * Retrieve a group by AVERAGE on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param averageField field to average
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     */
    public Aggregate average(ArrayList<String> fields, String averageField, Query query) throws IOException {
        Preconditions.checkNotNull(fields);
        Preconditions.checkNotNull(averageField);
        return aggregate(fields, AggregateEntity.AggregateType.AVERAGE, averageField, query);
    }

    /**
     * Private helper method to create AggregateEntity and return an intialize Aggregate Request Object
     * @param fields fields to group by
     * @param type Type of aggregation
     * @param aggregateField Field to aggregate on
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     */
    private Aggregate aggregate(ArrayList<String> fields, AggregateEntity.AggregateType type, String aggregateField,
                                   Query query) throws IOException {
        AggregateEntity entity = new AggregateEntity(fields, type, aggregateField, query, client);
        Aggregate aggregate = new Aggregate(entity, myClass);
        client.initializeRequest(aggregate);
        return aggregate;
    }

    /**
     * Generic Get class, extends AbstractKinveyJsonClientRequest<T[]>.  Constructs the HTTP request object for Get
     * requests.
     *
     */
    public class Get extends AbstractKinveyCachedClientRequest<T[]> {

        private static final String REST_PATH = "appdata/{appKey}/{collectionName}/" +
                "{?query,sort,limit,skip,resolve,resolve_depth,retainReference}";


        @Key
        private String collectionName;
        @Key("query")
        private String queryFilter;
        @Key("sort")
        private String sortFilter;
        @Key("limit")
        private String limit;
        @Key("skip")
        private String skip;

        @Key("resolve")
        private String resolve;
        @Key("resolve_depth")
        private String resolve_depth;
        @Key("retainReferences")
        private String retainReferences;


        Get(Query query, Class myClass) {
            super(client, "GET", REST_PATH, null, myClass);
            super.setCache(cache, policy);
            this.collectionName= AppData.this.collectionName;
            this.queryFilter = query.getQueryFilterJson(client.getJsonFactory());
            int queryLimit = query.getLimit();
            int querySkip = query.getSkip();
            this.limit = queryLimit > 0 ? Integer.toString(queryLimit) : null;
            this.skip = querySkip > 0 ? Integer.toString(querySkip) : null;
            this.sortFilter = query.getSortString();

        }


        Get(Query query, Class myClass, List<String> resolves, int resolve_depth, boolean retain){
            super(client, "GET", REST_PATH, null, myClass);
            super.setCache(cache, policy);
            this.collectionName= AppData.this.collectionName;
            this.queryFilter = query.getQueryFilterJson(client.getJsonFactory());
            int queryLimit = query.getLimit();
            int querySkip = query.getSkip();
            this.limit = queryLimit > 0 ? Integer.toString(queryLimit) : null;
            this.skip = querySkip > 0 ? Integer.toString(querySkip) : null;
            this.sortFilter = query.getSortString();

            this.resolve = Joiner.on(",").join(resolves);
            this.resolve_depth = resolve_depth > 0 ? Integer.toString(resolve_depth) : null;
            this.retainReferences = Boolean.toString(retain);


        }

        @Override
        public T[] execute() throws IOException {
            return super.execute();
// TODO: make this asynchonous
//          T[] myEntities = super.execute();
//            for (T myEntity : myEntities) {
//
//
//            }
//            if (myClass.isInstance(LinkedResource.class)) {
//                for (String key : (String[]) ((LinkedResource) myEntity).keySet().toArray()) {
//                    if (((LinkedResource) myEntity).get(key).getClass().isInstance(HashMap.class)
//                            && ((HashMap<String,Object>) ((LinkedResource) myEntity).get(key))
//                            .containsKey("_mime-type")) {
//                        LinkedFile file = downloadFile((HashMap<String,String>)(((LinkedResource) myEntity).get(key)));
//                        ((LinkedResource) myEntity).remove(key);
//                        ((LinkedResource) myEntity).put(key,file);
//                    }
//                }
//            }
//            return myEntity;
        }

        private LinkedFile downloadFile(HashMap<String,String> fileMetaData) throws IOException {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            client.file().download(fileMetaData.get("_loc")).executeAndDownloadTo(bos);
            return new LinkedFile(bos.toByteArray(),fileMetaData.get("_loc"));
        }
    }


    /**
     * Generic Get class, extends AbstractKinveyJsonClientRequest<T>.  Constructs the HTTP request object for Get
     * requests.
     *
     */
    public class GetEntity extends AbstractKinveyCachedClientRequest<T> {

        private static final String REST_PATH = "appdata/{appKey}/{collectionName}/{entityID}" +
                "{resolve,resolve_depth,retainReference}";


        @Key
        private String entityID;
        @Key
        private String collectionName;

        @Key("resolve")
        private String resolve;
        @Key("resolve_depth")
        private String resolve_depth;
        @Key("retainReferences")
        private String retainReferences;




        GetEntity(String entityID, Class<T> myClass) {
            super(client, "GET", REST_PATH, null, myClass);
            super.setCache(cache, policy);
            this.collectionName= AppData.this.collectionName;
            this.entityID = entityID;
        }

        GetEntity(String entityID, Class<T> myClass, List<String> resolves, int resolve_depth, boolean retain){
            super(client, "GET", REST_PATH, null, myClass);
            super.setCache(cache, policy);
            this.collectionName= AppData.this.collectionName;
            this.entityID = entityID;

            this.resolve = Joiner.on(",").join(resolves);            
            this.resolve_depth = resolve_depth > 0 ? Integer.toString(resolve_depth) : null;
            this.retainReferences = Boolean.toString(retain);

        }



        @Override
        public T execute() throws IOException {
            T myEntity = super.execute();
            //TODO: rethink LinkedResource
//            if (myClass.isInstance(LinkedResource.class)) {
//                for (String key : ((LinkedResource) myEntity).keySet()) {
//                    if (((LinkedResource) myEntity).get(key).getClass().isInstance(HashMap.class)
//                            && ((HashMap<String,Object>) ((LinkedResource) myEntity).get(key))
//                            .containsKey("_mime-type")) {
//                        LinkedFile file = downloadFile((HashMap<String,String>)(((LinkedResource) myEntity).get(key)));
//                        ((LinkedResource) myEntity).remove(key);
//                        ((LinkedResource) myEntity).put(key,file);
//                    }
//                }
//            }
            return myEntity;
        }

        private LinkedFile downloadFile(HashMap<String,String> fileMetaData) throws IOException {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            client.file().download(fileMetaData.get("_loc")).executeAndDownloadTo(bos);
            return new LinkedFile(bos.toByteArray(),fileMetaData.get("_loc"));
        }
    }

    /** used internally **/
    private enum SaveMode {
        POST,
        PUT
    }

    /**
     * Generic Save<T> class, extends AbstractKinveyJsonClientRequest<T>.  Constructs the HTTP request object for
     * Create / Update requests.
     *
     */
    public class Save extends AbstractKinveyJsonClientRequest<T> {
        private static final String REST_PATH = "appdata/{appKey}/{collectionName}/{entityID}";
        @Key
        private String collectionName;
        @Key
        private String entityID;


        Save(T entity, Class<T> myClass, String entityID, SaveMode update) {
            super(client, update.toString(), REST_PATH, entity,myClass);
            this.collectionName = AppData.this.collectionName;
            if (update.equals(SaveMode.PUT)) {
                this.entityID = entityID;
            }
        }

        Save(T entity, Class<T> myClass, SaveMode update) {
            this(entity, myClass, null, update);
        }
    }

    /**
     * Generic Delete class, extends AbstractKinveyJsonClientRequest<T>.  Constructs the HTTP request object
     * for Delete requests.
     *
     */
    public class Delete extends AbstractKinveyJsonClientRequest<KinveyDeleteResponse> {
        private static final String REST_PATH = "appdata/{appKey}/{collectionName}/{entityID}" +
                "{?query,sort,limit,skip,resolve,resolve_depth,retainReference}";

        @Key
        private String entityID;
        @Key
        private String collectionName;
        @Key("query")
        private String queryFilter;
        @Key("sort")
        private String sortFilter;
        @Key("limit")
        private String limit;
        @Key("skip")
        private String skip;

        Delete(String entityID) {
            super(client, "DELETE", REST_PATH, null, KinveyDeleteResponse.class);
            this.entityID = entityID;
            this.collectionName = AppData.this.collectionName;
        }

        Delete(Query query) {
            super(client, "DELETE", REST_PATH, null, KinveyDeleteResponse.class);
            this.collectionName= AppData.this.collectionName;
            this.queryFilter = query.getQueryFilterJson(client.getJsonFactory());
            int queryLimit = query.getLimit();
            int querySkip = query.getSkip();
            this.limit = queryLimit > 0 ? Integer.toString(queryLimit) : null;
            this.skip = querySkip > 0 ? Integer.toString(querySkip) : null;
            this.sortFilter = query.getSortString();

        }
    }

    /**
     * Generic Aggregate<T> class, extends AbstractKinveyJsonClientRequest<T>.  Constructs the HTTP request object for
     * Aggregate requests.
     *
     */
    public class Aggregate extends AbstractKinveyJsonClientRequest<T> {
        private static final String REST_PATH = "appdata/{appKey}/{collectionName}/_group";
        @Key
        private String collectionName;

        Aggregate(AggregateEntity entity, Class<T> myClass) {
            super(client, "POST", REST_PATH, entity,myClass);
            this.collectionName = AppData.this.collectionName;
        }
    }
    
}


