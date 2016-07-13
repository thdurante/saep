package br.com.inf.saep.persistencia;

import br.com.inf.saep.persistencia.adapters.InterfaceAvaliavelAdapter;
import br.ufg.inf.es.saep.sandbox.dominio.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * Classe que representa o repositório de Pareceres.
 */
public class RepositorioDePareceres implements ParecerRepository {

    /**
     * Representação da base de dados.
     */
    private DBManager database;

    /**
     * Objeto para serialização e parse de documentos JSON.
     */
    private Gson gson;

    /**
     * Cria um novo repositório de Pareceres e Radocs, já inicializando o
     * serializador/parser Gson. A conexão com o banco de dados será aberta
     * em um momento posterior.
     * @param banco O nome do banco de dados.
     * @param servidor O nome do servidor.
     * @param porta A porta de conexão com o banco de dados.
     */
    public RepositorioDePareceres(String banco, String servidor, int porta) {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Avaliavel.class, new InterfaceAvaliavelAdapter())
                .serializeNulls()
                .setPrettyPrinting()
                .create();
        this.database = new DBManager(banco, servidor, porta);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void adicionaNota(String id, Nota nota) {
        MongoCollection pareceresCollection = database.abrirConexao("pareceres");

        if (byId(id) == null) {
            throw new IdentificadorDesconhecido("id desconhecido");
        }

        Document itemAvaliavelOriginalDocument = Document.parse(gson.toJson(nota.getItemOriginal()));
        MongoCursor cursor = pareceresCollection.find(and(eq("_id", id), eq("notas.original", itemAvaliavelOriginalDocument))).iterator();
        if (cursor.hasNext()) {
            removeNota(id, nota.getItemOriginal());
        }

        Document notaDocument = Document.parse(gson.toJson(nota));
        pareceresCollection.updateOne(
                new Document("_id", id),
                new Document("$push", new Document("notas", notaDocument))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNota(String id, Avaliavel original) {
        MongoCollection pareceresCollection = database.abrirConexao("pareceres");

        if (byId(id) == null) {
            throw new IdentificadorDesconhecido("id desconhecido");
        }

        Document avaliavelOriginalDocument = Document.parse(gson.toJson(original));

        pareceresCollection.updateOne(
                new Document("_id", id),
                new Document("$pull", new Document("notas", new Document("original", avaliavelOriginalDocument)))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persisteParecer(Parecer parecer) {
        MongoCollection pareceresCollection = database.abrirConexao("pareceres");

        if (byId(parecer.getId()) != null) {
            throw new IdentificadorExistente("já persistido");
        }

        Document parecerDocument = Document.parse(gson.toJson(parecer));
        parecerDocument.put("_id", parecerDocument.get("id"));
        parecerDocument.remove("id");

        pareceresCollection.insertOne(parecerDocument);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void atualizaFundamentacao(String parecer, String fundamentacao) {
        MongoCollection pareceresCollection = database.abrirConexao("pareceres");

        if (byId(parecer) == null) {
            throw new IdentificadorDesconhecido("id desconhecido");
        }

        pareceresCollection.updateOne(
                new Document("_id", parecer),
                new Document("$set", new Document("fundamentacao", fundamentacao))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Parecer byId(String id) {
        MongoCollection pareceresCollection = database.abrirConexao("pareceres");
        Document document = null;

        MongoCursor cursor = pareceresCollection.find(eq("_id", id)).iterator();
        if (!cursor.hasNext()) {
            return null;
        }

        while (cursor.hasNext()) {
            document = (Document) cursor.next();
            document.put("id", document.get("_id").toString());
            document.remove("_id");
        }

        return gson.fromJson(document.toJson(), Parecer.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeParecer(String id) {
        MongoCollection pareceresCollection = database.abrirConexao("pareceres");
        pareceresCollection.deleteOne(new Document("_id", id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Radoc radocById(String identificador) {
        MongoCollection radocsCollection = database.abrirConexao("radocs");

        Document document = null;

        MongoCursor cursor = radocsCollection.find(eq("_id", identificador)).iterator();
        if (!cursor.hasNext()) {
            return null;
        }

        while (cursor.hasNext()) {
            document = (Document) cursor.next();
            document.put("id", document.get("_id").toString());
            document.remove("_id");
        }

        return gson.fromJson(document.toJson(), Radoc.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String persisteRadoc(Radoc radoc) {
        MongoCollection radocsCollection = database.abrirConexao("radocs");

        if (byId(radoc.getId()) != null) {
            throw new IdentificadorExistente("já persistido");
        }

        Document radocDocument = Document.parse(gson.toJson(radoc));
        radocDocument.put("_id", radocDocument.get("id"));
        radocDocument.remove("id");

        radocsCollection.insertOne(radocDocument);
        return radoc.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeRadoc(String identificador) {
        MongoCollection radocsCollection = database.abrirConexao("radocs");

        verificaSeRadocReferenciadoPorParecer(identificador); // Se for referenciado, a exception já é lançada
        radocsCollection.deleteOne(new Document("_id", identificador));
    }

    /**
     * Recupera a lista de Pareceres e verifica para cada um deles se a
     * lista de radocs contém o identificador do Radoc que se deseja remover.
     * <p>Caso algum {@link Parecer} possua em sua lista de radocs referenciados,
     * o identificado do Radoc que se deseja Remover, então uma exception
     * {@link ExisteParecerReferenciandoRadoc} é lançada.</p>
     * @param identificador O identificador único do Radoc.
     */
    private void verificaSeRadocReferenciadoPorParecer(String identificador) {
        MongoCollection pareceresCollection = database.abrirConexao("pareceres");
        Document document;
        Parecer parecer;

        MongoCursor cursor = pareceresCollection.find().iterator();
        while (cursor.hasNext()) {
            document = (Document) cursor.next();
            document.put("id", document.get("_id").toString());
            document.remove("_id");
            parecer = gson.fromJson(document.toJson(), Parecer.class);

            List<String> listaDeRadocsReferenciados = parecer.getRadocs();
            for (String idRadoc : listaDeRadocsReferenciados) {
                if (identificador.equals(idRadoc)) {
                    throw new ExisteParecerReferenciandoRadoc("Existe Parecer referenciando o Radoc.");
                }
            }
        }
    }

    /**
     * Método chamado no tearDown dos testes para limpar a base de dados.
     */
    public static void clearDB() {
        MongoCollection pareceresCollection = new DBManager("saep-sandbox", "server.thiagodurante.com.br",27017).abrirConexao("pareceres");
        pareceresCollection.deleteMany(new Document());

        MongoCollection radocsCollection = new DBManager("saep-sandbox", "server.thiagodurante.com.br", 27017).abrirConexao("radocs");
        radocsCollection.deleteMany(new Document());
    }
}
