package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
public class DatabaseConfig {

    private static final HikariDataSource dataSource;

    static {
        try{
            Properties props = loadProperties();
            HikariConfig config = new HikariConfig();

            // Basic Connection Params
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            config.setDriverClassName(props.getProperty("db.driver"));

            // Pool Tuning
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("hikari.maximumPoolSize")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("hikari.minimumIdle")));
            config.setIdleTimeout(Integer.parseInt(props.getProperty("hikari.idleTimeout")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("hikari.connectionTimeout")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("hikari.maxLifeTime")));

            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            throw new RuntimeException("CRITICAL: Failed to initialize database connection pool", e);
        }
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("application.properties")){
            if (input == null) {
                throw new RuntimeException("application.properties not found in classpath");
            }
            props.load(input);
        } catch (IOException e){
            throw new RuntimeException("Error loading configuration properties", e);
        }
        return props;
    }

    // Provides a connection from the pool
    public static Connection getConnection() throws SQLException{
        return dataSource.getConnection();
    }

    // Useful if we need to pass the DataSource to other libraries
    public static DataSource getDataSource(){
        return dataSource;
    }

    // Cleanup method fpr application shutdown
    public static void close(){
        if (dataSource != null && !dataSource.isClosed()){
            dataSource.close();
        }
    }
}
