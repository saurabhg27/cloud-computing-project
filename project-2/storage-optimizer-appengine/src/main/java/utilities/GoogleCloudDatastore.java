package utilities;

import java.util.logging.Logger;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;

public class GoogleCloudDatastore {
	private static Datastore mDataStoreClient = null;
	private static Logger mLogger = Logger.getLogger("GoogleCloudDatastore");
	
	static {
		mDataStoreClient = DatastoreOptions.getDefaultInstance().getService();
	}
	
	public static void putEntity(String kind, String entityName, String attributeName, String attributeValue) {
		
		mLogger.info("Received request for kind: " + kind + ", entity key:" + entityName + 
				", attribute name:" + attributeName + ", attribute value" + attributeValue);
		
		Key entityKey = mDataStoreClient.newKeyFactory().setKind(kind).newKey(entityName);
		
		Entity item = Entity.newBuilder(entityKey).set(attributeName, attributeValue).build();
		
		mDataStoreClient.put(item);
		
		mLogger.info("Finished putting entity key: "+ entityKey);
	}
	
	public static Entity getEntity(String kind, String entityName) {
		Key entityKey = mDataStoreClient.newKeyFactory().setKind(kind).newKey(entityName);
		
		return mDataStoreClient.get(entityKey);
	}
}
