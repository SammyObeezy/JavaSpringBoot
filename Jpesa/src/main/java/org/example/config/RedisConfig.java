package org.example.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RedisConfig {

    private static final JedisPool pool;
    public static final int TTL_SECONDS;

    static {
        try {
            Properties props = new Properties();
            try (InputStream input = RedisConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
                props.load(input);
            }

            String host = props.getProperty("redis.host", "localhost");
            int port = Integer.parseInt(props.getProperty("redis.port", "6379"));
            TTL_SECONDS = Integer.parseInt(props.getProperty("redis.ttl", "600"));

            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(128); // Max active connections

            pool = new JedisPool(poolConfig, host, port);
        } catch (IOException e){
            throw new RuntimeException("Failed to load Redis config", e);
        }
    }

    public static Jedis getConnection(){
        return pool.getResource();
    }
}
