package org.apache.camel.component.redis;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

public class RedisConfiguration {
    private String command;
    private String channels;
    private Integer timeout;
    private String host;
    private Integer port;
    private RedisTemplate<String, String> redisTemplate;
    private RedisMessageListenerContainer listenerContainer;
    private RedisConnectionFactory connectionFactory;
    private RedisSerializer serializer;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public RedisTemplate<String, String> getRedisTemplate() {
        return redisTemplate != null ? redisTemplate : createDefaultTemplate();
    }

    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RedisMessageListenerContainer getListenerContainer() {
        return listenerContainer != null ? listenerContainer : createDefaultListenerContainer();
    }

    public void setListenerContainer(RedisMessageListenerContainer listenerContainer) {
        this.listenerContainer = listenerContainer;
    }

    public String getChannels() {
        return channels;
    }

    public void setChannels(String channels) {
        this.channels = channels;
    }

    public void setConnectionFactory(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public RedisConnectionFactory getConnectionFactory() {
        return connectionFactory != null ? connectionFactory : createDefaultConnectionFactory();
    }

    public RedisSerializer getSerializer() {
        return serializer != null ? serializer : createDefaultSerializer();
    }

    public void setSerializer(RedisSerializer serializer) {
        this.serializer = serializer;
    }

    private RedisConnectionFactory createDefaultConnectionFactory() {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        if (host != null) {
            jedisConnectionFactory.setHostName(host);
        }
        if (port != null) {
            jedisConnectionFactory.setPort(port);
        }
        jedisConnectionFactory.afterPropertiesSet();
        connectionFactory = jedisConnectionFactory;
        return jedisConnectionFactory;
    }

    private RedisTemplate<String, String> createDefaultTemplate() {
        redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(getConnectionFactory());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    private RedisMessageListenerContainer createDefaultListenerContainer() {
        listenerContainer = new RedisMessageListenerContainer();
        listenerContainer.setConnectionFactory(getConnectionFactory());
        listenerContainer.afterPropertiesSet();
        return listenerContainer;
    }

    private RedisSerializer createDefaultSerializer() {
        serializer = new JdkSerializationRedisSerializer();
        return serializer;
    }
}
