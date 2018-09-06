package com.times6.timeTracker.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.times6.timeTracker.db.sql.DatabaseConfig;
import com.times6.timeTracker.db.sql.SqlTaskDao;
import com.times6.timeTracker.db.sql.SqlTaskTypeDao;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import org.avaje.datasource.DataSourceConfig;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseModule extends AbstractModule {
    private static final String CONFIG_FILE = "config/sqlConfig.json";

    @Override
    protected void configure() {
        bind(TaskDao.class).to(SqlTaskDao.class);
        bind(TaskTypeDao.class).to(SqlTaskTypeDao.class);
    }

    @Provides
    private EbeanServer getEbeanServer(DatabaseConfig config) {
        ServerConfig serverConfig = new ServerConfig();
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setDriver("org.postgresql.Driver");
        dataSourceConfig.setUsername(config.getUsername());
        dataSourceConfig.setPassword(config.getPassword());
        dataSourceConfig.setUrl(config.getConnectionUrl());
        serverConfig.setDataSourceConfig(dataSourceConfig);
        serverConfig.addPackage("com.times6.timeTracker.db.sql");

        return EbeanServerFactory.create(serverConfig);
    }

    @Provides
    private Connection getConnection(DatabaseConfig config) throws SQLException {
        return DriverManager.getConnection(config.getConnectionUrl(), config.getUsername(), config.getPassword());
    }

    @Provides
    private DatabaseConfig getConfig(ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(new File(CONFIG_FILE), DatabaseConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("failed to read config from " + CONFIG_FILE, e);
        }
    }
}
