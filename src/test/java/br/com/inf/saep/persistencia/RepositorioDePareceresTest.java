package br.com.inf.saep.persistencia;

import br.ufg.inf.es.saep.sandbox.dominio.*;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RepositorioDePareceresTest {

    RepositorioDePareceres repositorioDePareceres;

    /*
    * PRIVATE METHODS
    * */

    /**
     * Recupera um Parecer válido.
     * @param id Id do Parecer.
     * @return Parecer válido.
     */
    private Parecer getParecerValido(String id) {
        return new Parecer(
                id,                                             // id
                "CONSUNI No 32/2013",                           // resolucaoId
                getListaDeRadocsUtilizadosNoParecer(),          // radocsIds
                getListaDePontuacoesObtidasPeloParecer(),       // pontuacoes
                "Fundamentação do parecer",                     // fundamentacao
                getListaDeNotasDoParecer()                      // notas
        );
    }

    /**
     * Recupera os ids dos Radocs utilizados em um parecer.
     * @return A lista de ids de Radocs que foram utilizados em um Parecer.
     */
    private List<String> getListaDeRadocsUtilizadosNoParecer() {
        List<String> listaDeRadocs = new ArrayList<>();
        listaDeRadocs.add(UUID.randomUUID().toString());

        return listaDeRadocs;
    }

    /**
     * Recupera a lista de Pontuações obtidas pelo Parecer.
     * @return A lista de pontuações.
     */
    private List<Pontuacao> getListaDePontuacoesObtidasPeloParecer() {
        List<Pontuacao> listaDePontuacoes = new ArrayList<>();
        listaDePontuacoes.add(new Pontuacao(
                "pontosAPGrad",     // nome
                new Valor(150)      // valor
        ));
        listaDePontuacoes.add(new Pontuacao(
                "pontosAEADGrad",   // nome
                new Valor(560)      // valor
        ));
        listaDePontuacoes.add(new Pontuacao(
                "aprovadoProm",     // nome
                new Valor(false)    // valor
        ));

        return listaDePontuacoes;
    }

    /**
     * Recupera a lista de Notas de um parecer.
     * @return Lista de Notas de um parecer.
     */
    private List<Nota> getListaDeNotasDoParecer() {
        List<Nota> listaDeNotas = new ArrayList<>();
        listaDeNotas.add(new Nota(
                new Pontuacao("aprovadoProm", new Valor(false)),
                new Pontuacao("aprovadoProm", new Valor(true)),
                "Alteração realizada porque um cálculo tinha sido feito de maneira errada."
        ));

        return listaDeNotas;
    }

    /**
     * Recupera uma Nota qualquer a ser adicionada a um Parecer.
     * @return A nota a ser adicionada a um Parecer.
     */
    private Nota getSampleNota() {
        return new Nota(
                new Pontuacao("nomeAtributo", new Valor("valor")),
                new Pontuacao("nomeAtributo", new Valor("novoValor")),
                "justificativa alteração"
        );
    }

    /**
     * Recupera um Radoc válido.
     * @param id Id único do Radoc.
     * @return Um Radoc válido.
     */
    private Radoc getRadocValido(String id) {
        return new Radoc(
                id,
                2016,
                getListaDeRelatosDoRadoc()
        );
    }

    /**
     * Recupera a lista de relatos de um dado Radoc.
     * @return Lista de relatos do Radoc.
     */
    private List<Relato> getListaDeRelatosDoRadoc() {
        List<Relato> listaDeRelatos = new ArrayList<>();

        Map<String, Valor> valoresRelato1 = new HashMap<>();
        valoresRelato1.put("Curso", new Valor("Engenharia de Software"));
        valoresRelato1.put("Disciplina", new Valor("Banco de Dados"));
        valoresRelato1.put("CHA", new Valor(64));
        valoresRelato1.put("Ano", new Valor(2016));
        valoresRelato1.put("Semestre", new Valor(1));
        valoresRelato1.put("Turma", new Valor("A"));

        listaDeRelatos.add(new Relato(
                "AEGP",
                valoresRelato1
        ));

        Map<String, Valor> valoresRelato2 = new HashMap<>();
        valoresRelato2.put("Descrição do Produto", new Valor("Trabalho publico em anais de congresso científico"));
        valoresRelato2.put("Título do Produto", new Valor("Monitoramento de Paisagens Urbanas com Redes de Sensores"));
        valoresRelato2.put("Autoria", new Valor("Autor"));
        valoresRelato2.put("Ano de publicação", new Valor(2013));
        valoresRelato2.put("Número de páginas", new Valor(342));

        listaDeRelatos.add(new Relato(
                "PROD",
                valoresRelato2
        ));

        return listaDeRelatos;
    }

    /**
     * Recupera um Parecer que referencia Radocs específicos.
     * @param parecerId Identificador único do Parecer.
     * @param idsRadocsReferenciados Lista com os ids dos Radocs referenciados.
     * @return Uma instância de Parecer que referencia Radocs específicos.
     */
    private Parecer getParecerQueReferenciaUmRadocEspecífico(String parecerId, List<String> idsRadocsReferenciados) {
        return new Parecer(
                parecerId,                                      // id
                "CONSUNI No 32/2013",                           // resolucaoId
                idsRadocsReferenciados,                         // radocsIds
                getListaDePontuacoesObtidasPeloParecer(),       // pontuacoes
                "Fundamentação do parecer",                     // fundamentacao
                getListaDeNotasDoParecer()                      // notas
        );
    }

    /*
    * TESTS
    * */

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        this.repositorioDePareceres = new RepositorioDePareceres("saep-sandbox", "server.thiagodurante.com.br", 27017);
    }

    @AfterClass
    public static void tearDown() {
        RepositorioDePareceres.clearDB();
    }

    @Test
    public void criaPontuacaoSemNome() {
        thrown.expect(CampoExigidoNaoFornecido.class);
        thrown.expectMessage("nome");
        new Pontuacao("", new Valor(12));
    }

    @Test
    public void criaPontuacaoSemValor() {
        thrown.expect(CampoExigidoNaoFornecido.class);
        thrown.expectMessage("valor");
        new Pontuacao("nome", null);
    }

    @Test
    public void criaNotaSemOrigem() {
        thrown.expect(CampoExigidoNaoFornecido.class);
        thrown.expectMessage("origem");
        new Nota(null, new Pontuacao("nome", new Valor("valor")), "justificativa");
    }

    @Test
    public void criaNotaSemDestino() {
        thrown.expect(CampoExigidoNaoFornecido.class);
        thrown.expectMessage("destino");
        new Nota(new Pontuacao("nome", new Valor("valor")), null, "justificativa");

    }

    @Test
    public void criaNotaSemJustificativa() {
        thrown.expect(CampoExigidoNaoFornecido.class);
        thrown.expectMessage("justificativa");
        new Nota(new Pontuacao("nome", new Valor("valor")), new Pontuacao("nome", new Valor("valor")), null);
    }

    @Test
    public void persisteParecerComIdDuplicado() {
        thrown.expect(IdentificadorExistente.class);
        thrown.expectMessage("já persistido");

        String id = UUID.randomUUID().toString();
        repositorioDePareceres.persisteParecer(getParecerValido(id));
        repositorioDePareceres.persisteParecer(getParecerValido(id));
    }

    @Test
    public void persisteParecerValido() {
        String id = UUID.randomUUID().toString();
        repositorioDePareceres.persisteParecer(getParecerValido(id));

        Parecer parecer = repositorioDePareceres.byId(id);
        assertNotNull("parecer não deve ser null", parecer);
    }

    @Test
    public void recuperaParecerPorId() {
        String id = UUID.randomUUID().toString();
        repositorioDePareceres.persisteParecer(getParecerValido(id));
        Parecer parecer = repositorioDePareceres.byId(id);

        assertEquals("id deve ser igual", id, parecer.getId());
        assertEquals("resolucao deve ser igual", "CONSUNI No 32/2013", parecer.getResolucao());
        assertEquals("deve possuir 1 radoc", 1, parecer.getRadocs().size());
        assertEquals("fundamentacao deve ser igual", "Fundamentação do parecer", parecer.getFundamentacao());
        assertEquals("deve possuir 3 pontuacoes", 3, parecer.getPontuacoes().size());
        assertNotNull("as pontuacoes não devem ser null", parecer.getPontuacoes().get(0));
        assertNotNull("as pontuacoes não devem ser null", parecer.getPontuacoes().get(1));
        assertNotNull("as pontuacoes não devem ser null", parecer.getPontuacoes().get(2));
        assertEquals("o atributo da pontuacao 0 deve coincidir", "pontosAPGrad", parecer.getPontuacoes().get(0).getAtributo());
        assertEquals("o atributo da pontuacao 1 deve coincidir", "pontosAEADGrad", parecer.getPontuacoes().get(1).getAtributo());
        assertEquals("o atributo da pontuacao 2 deve coincidir", "aprovadoProm", parecer.getPontuacoes().get(2).getAtributo());
        assertThat("o valor da pontuacao 0 deve coincidir", parecer.getPontuacoes().get(0).getValor().getFloat(), is((float) 150));
        assertThat("o valor da pontuacao 1 deve coincidir", parecer.getPontuacoes().get(1).getValor().getFloat(), is((float) 560));
        assertThat("o valor da pontuacao 2 deve coincidir", parecer.getPontuacoes().get(2).getValor().getBoolean(), is(false));
        assertNotNull("o get do item Avaliavel 0 não pode ser null", parecer.getPontuacoes().get(0).get("pontosAPGrad"));
        assertNotNull("o get do item Avaliavel 1 não pode ser null", parecer.getPontuacoes().get(1).get("pontosAEADGrad"));
        assertNotNull("o get do item Avaliavel 2 não pode ser null", parecer.getPontuacoes().get(2).get("aprovadoProm"));
        assertNull("o get do item Avaliavel 0 deve ser null caso o atributo não exista", parecer.getPontuacoes().get(0).get("atributoInexistente"));
        assertThat("o valor da pontuacao 0 deve coincidir pelo método get da interface Avaliavel", parecer.getPontuacoes().get(0).get("pontosAPGrad").getFloat(), is((float) 150));
        assertThat("o valor da pontuacao 1 deve coincidir pelo método get da interface Avaliavel", parecer.getPontuacoes().get(1).get("pontosAEADGrad").getFloat(), is((float) 560));
        assertThat("o valor da pontuacao 2 deve coincidir pelo método get da interface Avaliavel", parecer.getPontuacoes().get(2).get("aprovadoProm").getBoolean(), is(false));
        assertEquals("o tamanho da lista de notas deve ser igual a 1", 1, parecer.getNotas().size());
        assertEquals("o valor do item original de Notas[0] deve coincidir", false, parecer.getNotas().get(0).getItemOriginal().get("aprovadoProm").getBoolean());
        assertEquals("o valor do item alterado de Notas[0] deve coincidir", true, parecer.getNotas().get(0).getItemNovo().get("aprovadoProm").getBoolean());
        assertEquals("a justificativa de Notas[0] deve coincidir", "Alteração realizada porque um cálculo tinha sido feito de maneira errada.", parecer.getNotas().get(0).getJustificativa());
    }

    @Test
    public void adicionaNotaEmParecerInexistente() {
        thrown.expect(IdentificadorDesconhecido.class);
        thrown.expectMessage("id desconhecido");

        String parecerId = "id inexistente";
        repositorioDePareceres.adicionaNota(
                parecerId,
                new Nota(
                        new Pontuacao("nome", new Valor("valor")),
                        new Pontuacao("nome", new Valor("novoValor")),
                        "justificativa"
                )
        );
    }

    @Test
    public void adicionaNota() {
        Parecer parecer;

        String parecerId = UUID.randomUUID().toString();
        repositorioDePareceres.persisteParecer(getParecerValido(parecerId));

        parecer = repositorioDePareceres.byId(parecerId);
        assertNotNull("lista de notas antes da iserção deve ser diferente de null", parecer.getNotas());
        assertEquals("lista de notas antes da inserção deve ter tamanho 1", 1, parecer.getNotas().size());

        repositorioDePareceres.adicionaNota(
                parecerId,
                getSampleNota()
        );

        parecer = repositorioDePareceres.byId(parecerId);
        assertEquals("id deve coincidir", parecerId, parecer.getId());
        assertNotNull("lista de notas deve ser diferente de null", parecer.getNotas());
        assertEquals("lista de notas deve ter tamanho 2", 2, parecer.getNotas().size());
        assertEquals("justificativa da Nota 0 deve coincidir", "Alteração realizada porque um cálculo tinha sido feito de maneira errada.", parecer.getNotas().get(0).getJustificativa());
        assertEquals("valor original para o atributo ('nomeAtributo') da Nota 0 deve coincidir", false, parecer.getNotas().get(0).getItemOriginal().get("aprovadoProm").getBoolean());
        assertEquals("novo valor para o atributo ('nomeAtributo') da Nota 0 deve coincidir", true, parecer.getNotas().get(0).getItemNovo().get("aprovadoProm").getBoolean());
        assertEquals("justificativa da Nota 1 deve coincidir", "justificativa alteração", parecer.getNotas().get(1).getJustificativa());
        assertEquals("valor original para o atributo ('nomeAtributo') da Nota 1 deve coincidir", "valor", parecer.getNotas().get(1).getItemOriginal().get("nomeAtributo").getString());
        assertEquals("novo valor para o atributo ('nomeAtributo') da Nota 1 deve coincidir", "novoValor", parecer.getNotas().get(1).getItemNovo().get("nomeAtributo").getString());
    }

    @Test
    public void adicionaNotaEmAvaliavelQueJaPossuiNota() {
        Parecer parecer;
        Map<String, Valor> valoresRelatoVelho = new HashMap<>();
        Map<String, Valor> valoresRelatoNovo = new HashMap<>();

        // CRIA E PERSISTE UM PARECER COM DUAS NOTAS EM ITENS AVALIAVEIS DIFERENTES
        String parecerId = UUID.randomUUID().toString();
        repositorioDePareceres.persisteParecer(getParecerValido(parecerId));
        valoresRelatoVelho.put("Curso", new Valor("Engenharia de Software"));
        valoresRelatoNovo.put("Curso", new Valor("Ciência da Computação"));
        repositorioDePareceres.adicionaNota(
                parecerId,
                new Nota(
                        new Relato("COD", valoresRelatoVelho),
                        new Relato("COD", valoresRelatoNovo),
                        "Alterado o nome do curso para CC porque o digitador tinha errado."
                )
        );
        // limpa valores pq vou reusar mais na frente
        valoresRelatoNovo.remove("Curso");

        // RECUPERA O PARECER ANTES DA INSERÇÃO DE NOTA EM AVALIAVEL QUE JÁ POSSUI NOTA
        parecer = repositorioDePareceres.byId(parecerId);
        assertNotNull("lista de notas antes da iserção deve ser diferente de null", parecer.getNotas());
        assertEquals("lista de notas antes da inserção deve ter tamanho 2", 2, parecer.getNotas().size());
        assertEquals("o item novo da nota deve ter valor true", "Ciência da Computação", parecer.getNotas().get(1).getItemNovo().get("Curso").getString());
        assertEquals("a justificativa antes da adição da nova nota deve coincidir", "Alterado o nome do curso para CC porque o digitador tinha errado.", parecer.getNotas().get(1).getJustificativa());

        // INSERE UMA TERCEIRA NOTA (AGORA EM UM ITEM AVALIAVEL QUE JÁ POSSUI NOTA)
        valoresRelatoNovo.put("Curso", new Valor("Sistemas de Informação"));
        repositorioDePareceres.adicionaNota(
                parecerId,
                new Nota(
                        new Relato("COD", valoresRelatoVelho),
                        new Relato("COD", valoresRelatoNovo),
                        "Alterado o nome do curso para SI porque o digitador era burro e errou de novo."
                )
        );

        // RECUPERA O PARECER DEPOIS DA INSERÇÃO DE NOTA EM AVALIAVEL QUE JÁ POSSUI NOTA
        parecer = repositorioDePareceres.byId(parecerId);
        assertNotNull("lista de notas depois da iserção deve ser diferente de null", parecer.getNotas());
        assertEquals("lista de notas depois da inserção deve continuar com tamanho 2", 2, parecer.getNotas().size());
        assertEquals("o item novo da nota deve ter valor coincidente", "Sistemas de Informação", parecer.getNotas().get(1).getItemNovo().get("Curso").getString());
        assertEquals("a justificativa depois da adição da nova nota deve coincidir", "Alterado o nome do curso para SI porque o digitador era burro e errou de novo.", parecer.getNotas().get(1).getJustificativa());
    }

    @Test
    public void removeNotaEmParecerInexistente() {
        thrown.expect(IdentificadorDesconhecido.class);
        thrown.expectMessage("id desconhecido");

        String parecerId = "id inexistente";
        repositorioDePareceres.removeNota(
                parecerId,
                new Pontuacao("nome", new Valor("valor"))
        );
    }

    @Test
    public void removeNota() {
        Parecer parecer;

        // Cria um parecer com uma nota, adiciona uma segunda nota
        String parecerId = UUID.randomUUID().toString();
        repositorioDePareceres.persisteParecer(getParecerValido(parecerId));
        repositorioDePareceres.adicionaNota(parecerId, getSampleNota());

        // Recupera parecer antes da remoção
        parecer = repositorioDePareceres.byId(parecerId);
        assertNotNull("lista de notas antes da remoção deve ser diferente de null", parecer.getNotas());
        assertEquals("lista de notas antes da remoção deve ter tamanho 2", 2, parecer.getNotas().size());

        // Remove
        repositorioDePareceres.removeNota(parecerId, new Pontuacao("aprovadoProm", new Valor(false)));

        // Recupera parecer depois da remoção
        parecer = repositorioDePareceres.byId(parecerId);
        assertNotNull("lista de notas depois da remoção deve ser diferente de null", parecer.getNotas());
        assertEquals("lista de notas depois da remoção deve ter tamanho 1", 1, parecer.getNotas().size());
        assertEquals("a justificativa da Nota[0] depois da remoção deve coincidir", "justificativa alteração", parecer.getNotas().get(0).getJustificativa());
    }

    @Test
    public void atualizaFundamentacaoEmParecerInvalido() {
        thrown.expect(IdentificadorDesconhecido.class);
        thrown.expectMessage("id desconhecido");

        String parecerId = "id inexistente";
        repositorioDePareceres.atualizaFundamentacao(parecerId, "nova fundamentação");
    }

    @Test
    public void atualizaFundamentacao() {
        Parecer parecer;

        // Cria e persiste um parecer
        String parecerId = UUID.randomUUID().toString();
        repositorioDePareceres.persisteParecer(getParecerValido(parecerId));

        // Recupera parecer antes da atualização da fundamentação
        parecer = repositorioDePareceres.byId(parecerId);
        assertEquals("fundamentacao antes da atualização deve coincidir", "Fundamentação do parecer", parecer.getFundamentacao());

        // Atualiza fundamentação
        repositorioDePareceres.atualizaFundamentacao(parecerId, "Nova fundamentação");

        // Recupera parecer depois da atualização da fundamentação
        parecer = repositorioDePareceres.byId(parecerId);
        assertEquals("fundamentacao depois da atualização deve coincidir", "Nova fundamentação", parecer.getFundamentacao());
    }

    @Test
    public void removeParecer() {
        Parecer parecer;

        // Cria e persiste um parecer
        String parecerId = UUID.randomUUID().toString();
        repositorioDePareceres.persisteParecer(getParecerValido(parecerId));

        // Recupera parecer por ID antes da remoção
        parecer = repositorioDePareceres.byId(parecerId);
        assertNotNull("parecer recuperado por ID antes da remoção não deve ser null", parecer);

        // Remove parecer
        repositorioDePareceres.removeParecer(parecerId);

        // Recupera parecer por ID depois da remoção
        parecer = repositorioDePareceres.byId(parecerId);
        assertNull("parecer recuperado por ID depois da remoção deve ser null", parecer);

        // Remove parecer que não existe
        repositorioDePareceres.removeParecer("id de um parecer que não existe");
    }

    @Test
    public void criaRadocInvalidoSemId() {
        thrown.expect(CampoExigidoNaoFornecido.class);
        thrown.expectMessage("id");

        new Radoc(null, 2016, getListaDeRelatosDoRadoc());
    }

    @Test
    public void criaRadocInvalidoSemListaDeRelatos() {
        thrown.expect(CampoExigidoNaoFornecido.class);
        thrown.expectMessage("relatos");

        new Radoc("id", 2016, null);
    }

    @Test
    public void persisteRadoc() {
        String radocId = UUID.randomUUID().toString();
        String returnedId = repositorioDePareceres.persisteRadoc(getRadocValido(radocId));

        assertEquals("id do radoc utilizado na criação e id retornado pelo método persiste deve coincidir", radocId, returnedId);

        Radoc radocRecuperado = repositorioDePareceres.radocById(returnedId);
        assertNotNull("radoc recuperado não deve ser null", radocRecuperado);
    }

    @Test
    public void recuperaRadocById() {
        String radocId = UUID.randomUUID().toString();
        String returnedId = repositorioDePareceres.persisteRadoc(getRadocValido(radocId));

        Radoc radocRecuperado = repositorioDePareceres.radocById(returnedId);

        assertNotNull("radoc recuperado não deve ser null", radocRecuperado);
        assertEquals("o id do radoc recuperado deve coincidir", returnedId,radocRecuperado.getId());
        assertEquals("o ano base do radoc recuperado deve coincidir", 2016, radocRecuperado.getAnoBase());
        assertEquals("o tamanho da lista de relatos do radoc recuperado deve coincidir", 2, radocRecuperado.getRelatos().size());
        assertEquals("relatos[0].tipo do radoc recuperado deve coincidir", "AEGP", radocRecuperado.getRelatos().get(0).getTipo());
        assertEquals("relatos[1].tipo do radoc recuperado deve coincidir", "PROD", radocRecuperado.getRelatos().get(1).getTipo());

        assertEquals("relatos[0].get('Curso') do radoc recuperado deve coincidir", "Engenharia de Software", radocRecuperado.getRelatos().get(0).get("Curso").getString());
        assertEquals("relatos[0].get('Disciplina') do radoc recuperado deve coincidir", "Banco de Dados", radocRecuperado.getRelatos().get(0).get("Disciplina").getString());
        assertThat("relatos[0].get('CHA') do radoc recuperado deve coincidir", radocRecuperado.getRelatos().get(0).get("CHA").getFloat(), is((float) 64));
        assertThat("relatos[0].get('Ano') do radoc recuperado deve coincidir", radocRecuperado.getRelatos().get(0).get("Ano").getFloat(), is((float) 2016));
        assertThat("relatos[0].get('Semestre') do radoc recuperado deve coincidir", radocRecuperado.getRelatos().get(0).get("Semestre").getFloat(), is((float) 1));
        assertEquals("relatos[0].get('Turma') do radoc recuperado deve coincidir", "A", radocRecuperado.getRelatos().get(0).get("Turma").getString());
        assertEquals("relatos[0].getVariaveis().size() do radoc recuperado deve coincidir", 6, radocRecuperado.getRelatos().get(0).getVariaveis().size());

        assertEquals("relatos[1].get('Descrição do Produto') do radoc recuperado deve coincidir", "Trabalho publico em anais de congresso científico", radocRecuperado.getRelatos().get(1).get("Descrição do Produto").getString());
        assertEquals("relatos[1].get('Título do Produto') do radoc recuperado deve coincidir", "Monitoramento de Paisagens Urbanas com Redes de Sensores", radocRecuperado.getRelatos().get(1).get("Título do Produto").getString());
        assertEquals("relatos[1].get('Autoria') do radoc recuperado deve coincidir", "Autor", radocRecuperado.getRelatos().get(1).get("Autoria").getString());
        assertThat("relatos[1].get('Ano de publicação') do radoc recuperado deve coincidir", radocRecuperado.getRelatos().get(1).get("Ano de publicação").getFloat(), is((float) 2013));
        assertThat("relatos[1].get('Número de páginas') do radoc recuperado deve coincidir", radocRecuperado.getRelatos().get(1).get("Número de páginas").getFloat(), is((float) 342));
        assertEquals("relatos[1].getVariaveis().size() do radoc recuperado deve coincidir", 5, radocRecuperado.getRelatos().get(1).getVariaveis().size());
    }

    @Test
    public void recuperaRadocByIdInvalido() {
        Radoc radocRecuperado = repositorioDePareceres.radocById("id que não existe");
        assertNull("radoc recuperado com id invalido deve ser null", radocRecuperado);
    }

    @Test
    public void removeRadocSemReferenciaAlguma() {
        Radoc radocRecuperado;

        String radocId = UUID.randomUUID().toString();
        String returnedId = repositorioDePareceres.persisteRadoc(getRadocValido(radocId));
        repositorioDePareceres.persisteParecer(getParecerValido(UUID.randomUUID().toString()));
        radocRecuperado = repositorioDePareceres.radocById(returnedId);
        assertNotNull("o radoc recuperado antes da remoção não deve ser null", radocRecuperado);

        repositorioDePareceres.removeRadoc(returnedId);
        radocRecuperado = repositorioDePareceres.radocById(returnedId);
        assertNull("o radoc recuperado depois da remoção deve ser null", radocRecuperado);
    }

    @Test
    public void removeRadocReferenciadoPorParecer() {
        thrown.expect(ExisteParecerReferenciandoRadoc.class);
        thrown.expectMessage("Existe Parecer referenciando o Radoc.");

        String radocId = UUID.randomUUID().toString();
        String returnedId = repositorioDePareceres.persisteRadoc(getRadocValido(radocId));
        String parecerId = UUID.randomUUID().toString();
        List<String> listaDeRadocsReferenciados = new ArrayList<>();
        listaDeRadocsReferenciados.add(returnedId);
        Parecer parecer = getParecerQueReferenciaUmRadocEspecífico(parecerId, listaDeRadocsReferenciados);
        repositorioDePareceres.persisteParecer(parecer);

        repositorioDePareceres.removeRadoc(returnedId);
    }
}
