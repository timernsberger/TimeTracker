package com.times6.timeTracker.db.sql;

import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import org.avaje.datasource.DataSourceConfig;
import org.junit.jupiter.api.BeforeEach;

public abstract class DaoTestBase {
    protected static final String USER_ID = "test user";

    protected EbeanServer db;

    @BeforeEach
    public void createDatabase() {
        ServerConfig serverConfig = new ServerConfig();
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setUsername("");
        dataSourceConfig.setPassword("");
        dataSourceConfig.setDriver("org.h2.Driver");
        dataSourceConfig.setUrl("jdbc:h2:mem:test");
        serverConfig.setDataSourceConfig(dataSourceConfig);
        serverConfig.addPackage("com.times6.timeTracker.db.sql");
        serverConfig.setDdlGenerate(true);
        serverConfig.setDdlRun(true);

        db = EbeanServerFactory.create(serverConfig);
    }
}
