package br.com.inf.saep.persistencia;

import br.ufg.inf.es.saep.sandbox.dominio.*;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RepositorioDeResolucoesTest {

    private RepositorioDeResolucoes repositorioDeResolucoes;

    /*
    * PRIVATE METHODS
    * */

    /**
     * Recupera a lista de regras da Resolução CONSUNI No 32/2013.
     * @return A lista de regras da Resolução.
     */
    private List<Regra> getListaDeRegras() {
        // REGRA 1
        List<String> dependenciasRegra1 = new ArrayList<>();
        dependenciasRegra1.add("cha = carga horária anual");

        Regra regra1 = new Regra(
                "pontosAPGrad",                                 // variavel
                Regra.EXPRESSAO,                                // tipo
                "Aulas presenciais na graduação",               // descricao
                0,                                              // valorMaximo
                0,                                              // valorMinimo
                "10 * (cha / 32)",                              // expressao
                null,                                           // entao
                null,                                           // senao
                null,                                           // tipoRelato
                0,                                              // pontosPorItem
                dependenciasRegra1                              // dependeDe
        );

        // REGRA 2
        List<String> dependenciasRegra2 = new ArrayList<>();
        dependenciasRegra2.add("cha = carga horária anual");

        Regra regra2 = new Regra(
                "pontosAEADGrad",                               // variavel
                Regra.EXPRESSAO,                                // tipo
                "Aulas de EAD na graduação",                    // descricao
                0,                                              // valorMaximo
                0,                                              // valorMinimo
                "10 * (cha / 32)",                              // expressao
                null,                                           // entao
                null,                                           // senao
                null,                                           // tipoRelato
                0,                                              // pontosPorItem
                dependenciasRegra2                              // dependeDe
        );

        List<Regra> regras = new ArrayList<>();
        regras.add(regra1);
        regras.add(regra2);

        return regras;
    }

    /**
     * Recupera a data de aprovação da Resolução CONSUNI No 32/2013.
     * @return A data de aprovação da Resolução.
     */
    private Date getDataAprovacao() {
        Calendar c = Calendar.getInstance();
        c.set(2013, Calendar.SEPTEMBER, 27);
        return c.getTime();
    }

    /**
     * Recupera uma Resolução válida, ou seja, que satisfaz a todas as condições
     * determinadas por seu construtor.
     * @param id O identificador único da Resolução.
     * @return Resolução válida.
     */
    private Resolucao getResolucaoValida(String id) {
        return new Resolucao(
                id,
                "CONSUNI No 32/2013",
                "Dispõe sobre normas para avaliação de pessoal docente em relação ao " +
                        "estágio probatório, à progressão funcional e à promoção na Carreira " +
                        "do Magistério Superior, e revoga as disposições em contrário.",
                getDataAprovacao(),
                getListaDeRegras()
        );
    }

    /**
     * Recupera a lista de atributos do Tipo.
     * @return Atributos do Tipo.
     */
    private Set<Atributo> getAtributosDoTipo() {
        Set<Atributo> atributos = new HashSet<>();
        atributos.add(new Atributo("cha", "carga horária anual", Atributo.REAL));
        return atributos;
    }

    /**
     * Recupera uma instância válido de Tipo com valores já setados.
     * @return A instância de Tipo.
     */
    private Tipo getTipoValido1() {
        Tipo tipo = new Tipo(
                "APG",
                "Aula presencial na graduação",
                "Disciplina ministrada na graduação, apenas na modalidade presencial",
                getAtributosDoTipo()
        );
        return tipo;
    }

    /**
     * Recupera uma instância válido de Tipo com valores já setados.
     * @return A instância de Tipo.
     */
    private Tipo getTipoValido2() {
        Tipo tipo = new Tipo(
                "AEADG",
                "Aula do ensino à distância na graduação",
                "Disciplina ministrada na graduação, apenas na modalidade de ensino à distância",
                getAtributosDoTipo()
        );
        return tipo;
    }

    /**
     * Cria e persiste uma {@link Resolucao} teste com uma {@link Regra} que possui o
     * atributo tipoRelato igual ao id do {@link Tipo} que se deseja remover.
     */
    private void persisteResolucaoTesteRemocaoDeTipo() {
        List<String> dependenciasRegraTeste = new ArrayList<>();
        dependenciasRegraTeste.add("cha = carga horária anual");

        Regra regraTeste = new Regra(
                "pontosAPGrad",                                 // variavel
                Regra.PONTOS,                                   // tipo
                "desc",                                         // descricao
                10,                                             // valorMaximo
                0,                                              // valorMinimo
                "10 * (cha / 32)",                              // expressao
                null,                                           // entao
                null,                                           // senao
                "AEADG",                                        // tipoRelato
                10,                                             // pontosPorItem
                dependenciasRegraTeste                          // dependeDe
        );

        List<Regra> regras = new ArrayList<>();
        regras.add(regraTeste);

        String id = UUID.randomUUID().toString();
        Resolucao resolucao = new Resolucao(
                id,
                "Teste remoção de tipo utilizado",
                "Descrição",
                getDataAprovacao(),
                regras
        );

        repositorioDeResolucoes.persiste(resolucao);
    }

    /*
    * TESTS
    * */

    @Before
    public void setUp() {
        this.repositorioDeResolucoes = new RepositorioDeResolucoes("saep-sandbox", "server.thiagodurante.com.br", 27017);
    }

    @AfterClass
    public static void tearDown() {
        new RepositorioDeResolucoes("saep-sandbox", "server.thiagodurante.com.br", 27017).clearDB();
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void persisteResolucaoSemNenhumAtributoObrigatorio() {
        Resolucao resolucao = new Resolucao(null, null, null, null, null);
        repositorioDeResolucoes.persiste(resolucao);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void persisteResolucaoSemId() {
        Resolucao resolucao = new Resolucao(
                null,
                "CONSUNI No 32/2013",
                "Dispõe sobre normas para avaliação de pessoal docente em relação ao " +
                        "estágio probatório, à progressão funcional e à promoção na Carreira " +
                        "do Magistério Superior, e revoga as disposições em contrário.",
                getDataAprovacao(),
                getListaDeRegras()
        );
        repositorioDeResolucoes.persiste(resolucao);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void persisteResolucaoSemNome() {
        String id = UUID.randomUUID().toString();
        Resolucao resolucao = new Resolucao(
                id,
                null,
                "Dispõe sobre normas para avaliação de pessoal docente em relação ao " +
                        "estágio probatório, à progressão funcional e à promoção na Carreira " +
                        "do Magistério Superior, e revoga as disposições em contrário.",
                getDataAprovacao(),
                getListaDeRegras()
        );
        repositorioDeResolucoes.persiste(resolucao);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void persisteResolucaoSemDescricao() {
        String id = UUID.randomUUID().toString();
        Resolucao resolucao = new Resolucao(
                id,
                "CONSUNI No 32/2013",
                null,
                getDataAprovacao(),
                getListaDeRegras()
        );
        repositorioDeResolucoes.persiste(resolucao);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void persisteResolucaoSemDataAprovacao() {
        String id = UUID.randomUUID().toString();
        Resolucao resolucao = new Resolucao(
                id,
                "CONSUNI No 32/2013",
                "Dispõe sobre normas para avaliação de pessoal docente em relação ao " +
                        "estágio probatório, à progressão funcional e à promoção na Carreira " +
                        "do Magistério Superior, e revoga as disposições em contrário.",
                null,
                getListaDeRegras()
        );
        repositorioDeResolucoes.persiste(resolucao);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void persisteResolucaoSemRegras() {
        String id = UUID.randomUUID().toString();
        Resolucao resolucao = new Resolucao(
                id,
                "CONSUNI No 32/2013",
                "Dispõe sobre normas para avaliação de pessoal docente em relação ao " +
                        "estágio probatório, à progressão funcional e à promoção na Carreira " +
                        "do Magistério Superior, e revoga as disposições em contrário.",
                getDataAprovacao(),
                null
        );
        repositorioDeResolucoes.persiste(resolucao);
    }

    @Test(expected = IdentificadorExistente.class)
    public void persisteResolucaoComIdentificadorJaExistente() {
        String id = UUID.randomUUID().toString();
        repositorioDeResolucoes.persiste(getResolucaoValida(id));
        repositorioDeResolucoes.persiste(getResolucaoValida(id));
    }

    @Test
    public void persisteResolucaoValida() {
        String id = UUID.randomUUID().toString();
        Resolucao resolucao = getResolucaoValida(id);
        String identificador = repositorioDeResolucoes.persiste(resolucao);
        assertNotNull("id retornado não deve ser null", identificador);
    }

    @Test
    public void recuperaPorId() {
        String idGerado = UUID.randomUUID().toString();
        String id = repositorioDeResolucoes.persiste(getResolucaoValida(idGerado));
        Resolucao resolucao = repositorioDeResolucoes.byId(id);

        assertNotNull("resolução retornada não deve ser null", resolucao);
        assertNotNull("dataAprovação não deve ser null", resolucao.getDataAprovacao());

        assertEquals("id deve ser igual", id, resolucao.getId());
        assertEquals("nome deve ser igual", "CONSUNI No 32/2013", resolucao.getNome());
        assertEquals("deve possuir regras", 2, resolucao.getRegras().size());
    }

    @Test
    public void recuperaPorIdInexistente() {
        String id = "47799feae0e499143ca77b54";
        Resolucao resolucao = repositorioDeResolucoes.byId(id);

        assertNull("resolução não encontrada, retorno deve ser null", resolucao);
    }

    @Test
    public void listaIdsDeResolucoes() {
        List<String> identificadoresDasResolucoes = repositorioDeResolucoes.resolucoes();

        System.out.println("Total de Resoluções: " +identificadoresDasResolucoes.size());
        for (String s : identificadoresDasResolucoes) {
            System.out.println(s);
        }

        assertNotNull("a lista de ids não deve ser null", identificadoresDasResolucoes);
        assertNotEquals("a lista de ids não deve ser vazia", 0, identificadoresDasResolucoes.size());
    }

    @Test
    public void removeResolucaoValida() {
        String idGerado = UUID.randomUUID().toString();
        String id = repositorioDeResolucoes.persiste(getResolucaoValida(idGerado));
        Boolean resultadoRemocao = repositorioDeResolucoes.remove(id);
        assertEquals("deve ser true se a resolucao foi deletada", true, resultadoRemocao);
    }

    @Test
    public void removeResolucaoInexistente() {
        String id = "3779de2fe0e49916ef449f23";
        Boolean resultadoRemocao = repositorioDeResolucoes.remove(id);
        assertEquals("deve ser false se a resolucao não foi deletada", false, resultadoRemocao);
    }

    @Test(expected = IdentificadorExistente.class)
    public void persisteTipoComIdJaPersistido() {
        repositorioDeResolucoes.persisteTipo(getTipoValido1());
        repositorioDeResolucoes.persisteTipo(getTipoValido1());
    }

    @Test
    public void persisteTipoValido() {
        Tipo tipo1 = getTipoValido1();
        assertEquals("id do tipo deve ser o mesmo", "APG", tipo1.getId());
        assertEquals("nome do tipo deve ser o mesmo", "Aula presencial na graduação", tipo1.getNome());
        assertEquals("descricao do tipo deve ser a mesma", "Disciplina ministrada na graduação, apenas na modalidade presencial", tipo1.getDescricao());
        assertEquals("tipo deve ter somente 1 atributo", 1, tipo1.getAtributos().size());

        Tipo tipo2 = getTipoValido2();
        assertEquals("id do tipo deve ser o mesmo", "AEADG", tipo2.getId());
        assertEquals("nome do tipo deve ser o mesmo", "Aula do ensino à distância na graduação", tipo2.getNome());
        assertEquals("descricao do tipo deve ser a mesma", "Disciplina ministrada na graduação, apenas na modalidade de ensino à distância", tipo2.getDescricao());
        assertEquals("tipo deve ter somente 1 atributo", 1, tipo2.getAtributos().size());

        repositorioDeResolucoes.persisteTipo(tipo1);
        repositorioDeResolucoes.persisteTipo(tipo2);
    }

    @Test
    public void recuperaTipoPeloCodigoExistente() {
        Tipo tipo1 = repositorioDeResolucoes.tipoPeloCodigo("APG");
        assertNotNull("não deve ser null", tipo1);
        assertEquals("id do tipo deve ser o mesmo", "APG", tipo1.getId());
        assertEquals("nome do tipo deve ser o mesmo", "Aula presencial na graduação", tipo1.getNome());
        assertEquals("descricao do tipo deve ser a mesma", "Disciplina ministrada na graduação, apenas na modalidade presencial", tipo1.getDescricao());
        assertEquals("tipo deve ter somente 1 atributo", 1, tipo1.getAtributos().size());

        Tipo tipo2 = repositorioDeResolucoes.tipoPeloCodigo("AEADG");
        assertNotNull("não deve ser null", tipo2);
        assertEquals("id do tipo deve ser o mesmo", "AEADG", tipo2.getId());
        assertEquals("nome do tipo deve ser o mesmo", "Aula do ensino à distância na graduação", tipo2.getNome());
        assertEquals("descricao do tipo deve ser a mesma", "Disciplina ministrada na graduação, apenas na modalidade de ensino à distância", tipo2.getDescricao());
        assertEquals("tipo deve ter somente 1 atributo", 1, tipo2.getAtributos().size());
    }

    @Test
    public void recuperaTipoPeloCodigoInvalido() {
        Tipo tipo = repositorioDeResolucoes.tipoPeloCodigo("ABCDE");
        assertEquals("tipo deve ser null", null, tipo);
    }

    @Test
    public void removeTipoPeloCodigo() {
        repositorioDeResolucoes.removeTipo("APG");
    }

    @Test(expected = ResolucaoUsaTipoException.class)
    public void removeTipoQueEstaSendoUsadoPorUmaResolucao() {
        persisteResolucaoTesteRemocaoDeTipo();
        repositorioDeResolucoes.removeTipo("AEADG");
    }

    @Test
    public void recuperaListaDeTiposPorNome() {
        List<Tipo> tiposRetornados1 = repositorioDeResolucoes.tiposPeloNome("graduação");
        assertNotNull("não deve ser null", tiposRetornados1);
        assertThat("mais de um tipo retornado", tiposRetornados1.size(), is(greaterThan(0)));

        List<Tipo> tiposRetornados2 = repositorioDeResolucoes.tiposPeloNome("à distância");
        assertNotNull("não deve ser null", tiposRetornados2);
        assertThat("mais de um tipo retornado", tiposRetornados1.size(), is(greaterThan(0)));

        List<Tipo> tiposRetornados3 = repositorioDeResolucoes.tiposPeloNome("abobrinha");
        assertNotNull("não deve ser null", tiposRetornados3);
        assertEquals("deve ser vazio", 0, tiposRetornados3.size());
    }
}
