package br.com.inf.saep.persistencia.adapters;

import br.ufg.inf.es.saep.sandbox.dominio.Avaliavel;
import br.ufg.inf.es.saep.sandbox.dominio.Pontuacao;
import br.ufg.inf.es.saep.sandbox.dominio.Relato;
import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Adapter para a interface Avaliavel a ser utilizado pelo Gson.
 * <p>O Adapter é necessário pois existem duas classes que implementam a
 * interface {@link br.ufg.inf.es.saep.sandbox.dominio.Avaliavel} e o Gson
 * precisa saber exatamente qual classe serializar/desserializar.</p>
 */
public class InterfaceAvaliavelAdapter implements JsonSerializer<Avaliavel>, JsonDeserializer<Avaliavel> {

    @Override
    public Avaliavel deserialize(JsonElement json, Type tipoDaClasse, JsonDeserializationContext contexto) throws JsonParseException {
        JsonObject objetoJson = json.getAsJsonObject();
        Type tipo = objetoJson.has("tipo") ? Relato.class : Pontuacao.class;
        return contexto.deserialize(json, tipo);
    }

    @Override
    public JsonElement serialize(Avaliavel src, Type tipoDeSrc, JsonSerializationContext contexto) {
        Type tipo = src.getClass();
        return contexto.serialize(src, tipo);
    }
}
