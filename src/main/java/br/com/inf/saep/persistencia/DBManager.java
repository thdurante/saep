package br.com.inf.saep.persistencia;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * Encapsula a conexão com o servidor de banco de dados.
 * Retorna uma Collection que vai ser trabalhada nos repositórios.
 */
public class DBManager {

    /**
     * Informações do servidor.
     */
    private String servidor;
    private int porta;

    /**
     * Identificador da base de dados.
     */
    private String database;

    /**
     * Retorna uma nova instância de DBManager.
     * @param database O nome da base de dados.
     * @param servidor O endereço do servidor.
     * @param porta A porta de conexão com o servidor.
     */
    public DBManager(String database, String servidor, int porta) {
        this.database = database;
        this.servidor = servidor;
        this.porta = porta;
    }

    /**
     * Abre uma conexão com o servidor de banco de dados e retorna a Collection desejada.
     * @return A collection identificada pela nome informado no construtor.
     */
    public MongoCollection abrirConexao (String collection) {
        MongoClient mongoClient = new MongoClient(new ServerAddress(servidor, porta));
        MongoDatabase db = mongoClient.getDatabase(database);

        return db.getCollection(collection);
    }
}
