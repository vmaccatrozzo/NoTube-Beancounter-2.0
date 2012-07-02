package tv.notube.commons.lupedia;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.extractors.DefaultExtractor;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import tv.notube.commons.nlp.NLPEngine;
import tv.notube.commons.nlp.NLPEngineException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public final class LUpediaNLPEngineImpl implements NLPEngine {

    public final static String QUERY_PATTERN = "http://lupedia.ontotext.com/lookup/text2json?lookupText=%s&threshold=%s";

    @Override
    public Collection<URI> enrich(String text) throws NLPEngineException {
        try {
            text = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is not supported on this platform", e);
        }
        URL query;
        // TODO (high) this should be configurable
        String queryStr = String.format(QUERY_PATTERN, text, 0.85);
        try {
            query = new URL(queryStr);
        } catch (MalformedURLException e) {
            throw new NLPEngineException("url [" + queryStr + "] seems to be ill-formed", e);
        }
        InputStream is = getStream(query);
        ObjectMapper mapper = new ObjectMapper();
        List<LUpediaEntity> entities;
        try {
            entities = mapper.readValue(is, TypeFactory.defaultInstance().constructCollectionType(List.class, LUpediaEntity.class));
        } catch (IOException e) {
            throw new NLPEngineException(
                    "error while parsing json from [" + queryStr + "]",
                    e
            );
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new NLPEngineException(
                        "error while closing stream from [" + query.toString() + "]",
                        e
                );
            }
        }
        Set<URI> result = new HashSet<URI>();
        for(LUpediaEntity entity : entities) {
            try {
                result.add(new URI(entity.getInstanceUri()));
            } catch (URISyntaxException e) {
                // unlikely but could happen
            }
        }
        return result;
    }

    private InputStream getStream(URL query) throws NLPEngineException {
        URLConnection urlConnection;
        try {
            urlConnection = query.openConnection();
        } catch (IOException e) {
            throw new NLPEngineException(
                    "Error while opening connection from [" + query + "]", e
            );
        }
        try {
            return urlConnection.getInputStream();
        } catch (IOException e) {
            throw new NLPEngineException(
                    "Error getting stream from [" + query + "]", e
            );
        }
    }

    @Override
    public Collection<URI> enrich(URL url) throws NLPEngineException {
        String text;
        try {
            text = ArticleExtractor.INSTANCE.getText(url);
        } catch (BoilerpipeProcessingException e) {
            throw new NLPEngineException("Error while removing boiler plate from [" + url +  "]", e);
        }
        return enrich(text);
    }
}
