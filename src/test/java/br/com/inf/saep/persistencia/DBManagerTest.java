package br.com.inf.saep.persistencia;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;

public class DBManagerTest {

    MongoClient mongoClient;

    /*
     *  PRIVATE METHODS
     */

    private void listDatabasesAndCollections() {
        ListDatabasesIterable<Document> databases = mongoClient.listDatabases();
        databases.forEach(new Block<Document>() {
            @Override
            public void apply(Document database) {
                String dbName = (String) database.get("name");
                System.out.println("- Database: " +dbName);

                MongoDatabase db = mongoClient.getDatabase(dbName);

                ListCollectionsIterable<Document> collections = db.listCollections();
                collections.forEach(new Block<Document>() {
                    @Override
                    public void apply(Document document) {
                        System.out.println("\t+ Collection: " +document.get("name"));
                    }
                });
            }
        });
        System.out.println("");
    }

    /*
     *  TESTS
     */

    @Before
    public void setUp() {
        mongoClient = new MongoClient(new ServerAddress("server.thiagodurante.com.br", 27017));
        listDatabasesAndCollections();
    }

    @Test
    public void testConnectionToServer() {
        assertNotNull(mongoClient);
    }
}
