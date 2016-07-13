package br.com.inf.saep.persistencia;

import br.ufg.inf.es.saep.sandbox.dominio.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;

/**
 * Classe que representa o repositório de Resoluções.
 */
public class RepositorioDeResolucoes implements ResolucaoRepository {

    /**
     * Representação da collection Resolucoes advinda do banco de dados.
     */
    private MongoCollection resolucoesCollection;

    /**
     * Representação da collection Tipos advinda do banco de dados.
     */
    private MongoCollection tiposCollection;

    /**
     * Objeto para serialização e parse de documentos JSON.
     */
    private Gson gson;

    /**
     * Cria um novo repositório de Resoluções e Tipos, já abrindo a conexão com o
     * banco de dados e inicializando o serializador/parser Gson.
     * @param banco O nome do banco de dados.
     * @param servidor O nome do servidor.
     * @param porta A porta de conexão com o banco de dados.
     */
    public RepositorioDeResolucoes(String banco, String servidor, int porta) {
        this.resolucoesCollection = new DBManager(banco, servidor, porta).abrirConexao("resolucoes");
        this.tiposCollection = new DBManager(banco, servidor, porta).abrirConexao("tipos");
        this.gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resolucao byId(String id) {
        Document document = null;

        MongoCursor cursor = resolucoesCollection.find(eq("_id", id)).iterator();
        if (!cursor.hasNext()) {
            return null;
        }

        while (cursor.hasNext()) {
            document = (Document) cursor.next();
            document.put("id", document.get("_id").toString());
            document.remove("_id");
        }

        return gson.fromJson(document.toJson(), Resolucao.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String persiste(Resolucao resolucao) {
        if (resolucao.getNome() == null || resolucao.getNome().isEmpty()) {
            throw new CampoExigidoNaoFornecido("nome");
        }

        if (resolucao.getDescricao() == null || resolucao.getDescricao().isEmpty()) {
            throw new CampoExigidoNaoFornecido("descricao");
        }

        if (resolucao.getDataAprovacao() == null) {
            throw new CampoExigidoNaoFornecido("dataAprovacao");
        }

        if (resolucao.getRegras() == null || resolucao.getRegras().size() < 1) {
            throw new CampoExigidoNaoFornecido("regras");
        }

        if (byId(resolucao.getId()) != null) {
            throw new IdentificadorExistente("id");
        }

        Document resolucaoDocument = Document.parse(gson.toJson(resolucao));

        resolucaoDocument.put("_id", resolucaoDocument.get("id"));
        resolucaoDocument.remove("id");

        resolucoesCollection.insertOne(resolucaoDocument);

        return resolucaoDocument.get("_id").toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(String identificador) {
        DeleteResult deleteResult = resolucoesCollection.deleteOne(eq("_id", identificador));

        if (deleteResult.wasAcknowledged() && deleteResult.getDeletedCount() > 0) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> resolucoes() {
        List<String> identificadoresDasResolucoes = new ArrayList<>();
        Document document;

        MongoCursor cursor = resolucoesCollection.find().iterator();
        while (cursor.hasNext()) {
            document = (Document) cursor.next();
            identificadoresDasResolucoes.add(document.get("_id").toString());
        }

        return identificadoresDasResolucoes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persisteTipo(Tipo tipo) {
        if (tipoPeloCodigo(tipo.getId()) != null) {
            throw new IdentificadorExistente("id");
        }

        Document tipoDocument = Document.parse(gson.toJson(tipo));
        tiposCollection.insertOne(tipoDocument);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeTipo(String codigo) {
        verificaSeTipoUsadoPorAlgumaResolucao(codigo); // Se for usado, a exception já é lançada
        tiposCollection.deleteOne(eq("id", codigo));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tipo tipoPeloCodigo(String codigo) {
        Tipo tipo;
        Document document = null;

        MongoCursor cursor = tiposCollection.find(eq("id", codigo)).iterator();
        if (!cursor.hasNext()) {
            return null;
        }

        while (cursor.hasNext()) {
            document = (Document) cursor.next();
            document.remove("_id");
        }

        tipo = gson.fromJson(document.toJson(), Tipo.class);
        return tipo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Tipo> tiposPeloNome(String nome) {
        List<Tipo> tiposRecuperados = new ArrayList<>();
        Tipo tipo;
        Document document;

        Pattern regex = Pattern.compile(nome);

        MongoCursor cursor = tiposCollection.find(eq("nome", regex)).iterator();
        while (cursor.hasNext()) {
            document = (Document) cursor.next();
            document.remove("_id");
            tipo = gson.fromJson(document.toJson(), Tipo.class);

            tiposRecuperados.add(tipo);
        }

        return tiposRecuperados;
    }

    /**
     * Método chamado no tearDown dos testes para limpar a base de dados.
     */
    public void clearDB() {
        resolucoesCollection.deleteMany(new Document());
        tiposCollection.deleteMany(new Document());
    }

    /**
     * Recupera a lista de todas as Resoluções e verifica para cada uma delas se
     * alguma de suas regras possui o atributo tipoRelato igual ao codigo (ou id) do
     * Tipo que se deseja deletar.
     * <p>Caso alguma {@link Regra} de alguma {@link Resolucao}, possua o atributo
     * tipoRelato == codigo do {@link Tipo}, então uma exception {@link ResolucaoUsaTipoException}
     * é lançada.</p>
     * @param codigo O código ou id do {@link Tipo} que se deseja remover.
     */
    private void verificaSeTipoUsadoPorAlgumaResolucao(String codigo) {
        Document document;
        Resolucao resolucao;

        MongoCursor cursor = resolucoesCollection.find().iterator();
        while (cursor.hasNext()) {
            document = (Document) cursor.next();
            document.put("id", document.get("_id").toString());
            document.remove("_id");
            resolucao = gson.fromJson(document.toJson(), Resolucao.class);

            List<Regra> regras = resolucao.getRegras();
            for (Regra r : regras) {
                if (codigo.equals(r.getTipoRelato())) {
                    throw new ResolucaoUsaTipoException("A Resolução [ID: " +resolucao.getId() + "] usa o Tipo.");
                }
            }
        }
    }
}
