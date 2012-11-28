package io.beancounter.storm.analyses;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.Coordinates;
import io.beancounter.commons.model.activity.Tweet;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.ws.rs.core.MediaType;
import java.awt.geom.Path2D;
import java.util.Map;

/**
 * Drops any Tweets which do not contain geo-location data. Additionally filters
 * out Tweets which do contain location data, but are not coming from the
 * specified country.
 *
 * For Tweets with location data and in the specified country, it will emit a
 * tuple containing:
 *      [ lat:double, long:double, text:string ]
 *
 * @author Alex Cowell
 */
public class GeoTagFilter extends BaseRichBolt {

    private static final String COUNTRY_CODE = "countryCode";

    private final String countryCode;
    private final Path2D.Double italyBoundingBox;
//    private Client client;
    private ObjectMapper mapper;

    private OutputCollector collector;

    public GeoTagFilter(String countryCode) {
        this.countryCode = countryCode;

        double minLong = 6.627730622759041;
        double minLat = 35.49495394783578;
        double maxLong = 18.52114715613408;
        double maxLat = 47.092550464892554;

        italyBoundingBox = new Path2D.Double();
        italyBoundingBox.moveTo(minLong, minLat);
        italyBoundingBox.lineTo(minLong, maxLat);
        italyBoundingBox.lineTo(maxLong, maxLat);
        italyBoundingBox.lineTo(maxLong, minLat);
        italyBoundingBox.closePath();
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
//        client = Client.create();
        mapper = new ObjectMapper();
    }

    @Override
    public void execute(Tuple tuple) {
        String tweetJson = tuple.getString(0);
        collector.ack(tuple);

        Tweet tweet;
        try {
            tweet = (Tweet) mapper.readValue(tweetJson, Activity.class).getObject();
        } catch (Exception ex) {
            return;
        }

        Coordinates coordinates = tweet.getGeo();
        if (coordinates != null && isInCountry(coordinates)) {
            collector.emit(new Values(coordinates.getLat(), coordinates.getLon(), tweet.getText()));
        }
    }

    private boolean isInCountry(Coordinates coordinates) {
        /*
        WebResource resource = client.resource("http://ws.geonames.org/countryCode");
        String geoNamesResponse = resource
                .queryParam("lat", String.valueOf(coordinates.getLat()))
                .queryParam("lng", String.valueOf(coordinates.getLon()))
                .queryParam("type", "json")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
        JSONObject json = (JSONObject) JSONValue.parse(geoNamesResponse);

        return json.containsKey(COUNTRY_CODE) && countryCode.equals(json.get(COUNTRY_CODE));
        */

        return italyBoundingBox.contains(coordinates.getLon(), coordinates.getLat());
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("lat", "long", "text"));
    }
}