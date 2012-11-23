package io.beancounter.storm.analyses;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.Tweet;
import org.codehaus.jackson.map.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import storm.redis.JedisPoolConfigSerializable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class MentionCountBolt extends BaseRichBolt {

    private final static int DATABASE = 8;

    private final static int MAX_USERS = 5;

    private Map map;

    private TopologyContext topologyContext;

    private OutputCollector outputCollector;

    private ObjectMapper mapper;

    private JedisPool pool;

    private JedisPoolConfigSerializable config;

    private String address;

    public MentionCountBolt(JedisPoolConfigSerializable config, String address) {
        this.config = config;
        this.address = address;
    }

    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.map = map;
        this.topologyContext = topologyContext;
        this.outputCollector = outputCollector;
        this.mapper = new ObjectMapper();
        pool = new JedisPool(
                config,
                address
        );
    }

    public void execute(Tuple tuple) {
        String jsonValue = tuple.getString(0);
        outputCollector.ack(tuple);
        Tweet tweet;
        try {
            tweet = (Tweet) mapper.readValue(jsonValue, Activity.class).getObject();
        } catch (IOException e) {
            return;
        }
        Map<String, Integer> oldMentionCounts = getOldMentionCounts();
        Set<String> tweetMentions = tweet.getMentionedUsers();
        for (String username : oldMentionCounts.keySet()) {
            if (tweetMentions.contains(username)) {
                oldMentionCounts.put(
                        username,
                        oldMentionCounts.get(username) + 1
                );
                tweetMentions.remove(username);
            }
        }
        for (String username : tweetMentions) {
            oldMentionCounts.put(username, 1);
        }
        ValueComparator bvc = new ValueComparator(oldMentionCounts);
        Map<String, Integer> sortedValue = new TreeMap<String, Integer>(bvc);
        sortedValue.putAll(oldMentionCounts);

        Map<String, Integer> limitedSortedValue = new TreeMap<String, Integer>();
        int i = 0;
        for (Map.Entry<String, Integer> value : sortedValue.entrySet()) {
            if (i > MAX_USERS) {
                continue;
            }
            limitedSortedValue.put(value.getKey(), value.getValue());
            i++;
        }
        Values values = getValues("_most_mentioned_users_", limitedSortedValue);
        outputCollector.emit(values);
    }

    private Map<String, Integer> getOldMentionCounts() {
        Jedis jedis = getJedisResource(DATABASE);
        String jsonValue;
        try {
            jsonValue = jedis.get("_most_mentioned_users_");
        } finally {
            pool.returnResource(jedis);
        }
        if (jsonValue == null) {
            return new HashMap<String, Integer>();
        }
        try {
            return (Map<String, Integer>) mapper.readValue(jsonValue, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("error while deserializing in json the value", e);
        }
    }

    private Jedis getJedisResource(int database) {
        Jedis jedis = pool.getResource();
        boolean isConnectionIssue = false;
        try {
            jedis.select(database);
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while selecting database [" + database + "]";
            throw new RuntimeException(errMsg, e);
        } catch (Exception e) {
            pool.returnResource(jedis);
            final String errMsg = "Error while selecting database [" + database + "]";
            throw new RuntimeException(errMsg, e);
        } finally {
            if (isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            }
        }
        return jedis;
    }

    private Values getValues(String keyword, Map<String, Integer> value) {
        String jsonValue;
        try {
            jsonValue = mapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new RuntimeException("error while serializing in json the value", e);
        }
        return new Values(DATABASE, keyword, jsonValue);
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("database", "key", "value"));
    }
}
