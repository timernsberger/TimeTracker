package com.times6.timeTracker.db.sql;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.times6.timeTracker.Task;
import com.times6.timeTracker.db.DatabaseModule;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

public class DatabaseCreator {

    private Connection connection;

    @Inject
    public DatabaseCreator(Connection connection) {
        this.connection = connection;
    }

    public void createTables() throws Exception {
        getCreateScriptFiles().forEach(file -> {
            try {
                String script = readFile(file);
                executeScript(script, connection);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        });

    }

    private void executeScript(String script, Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(script);
    }

    private String readFile(Path path) throws IOException {
        return new String(Files.readAllBytes(path));
    }

    private Stream<Path> getCreateScriptFiles() throws IOException, URISyntaxException {
        Map<String, String> fsProperties = new HashMap<>();
        fsProperties.put("create", "true");
        URI folderUri = ClassLoader.getSystemResource("table_scripts").toURI();
        if(folderUri.getScheme().equals("jar")) {
            FileSystems.newFileSystem(folderUri, fsProperties);
        }
        return Files.list(Paths.get(folderUri));
    }

    public static void main(String[] args) throws Exception {
        final Injector injector = Guice.createInjector(new DatabaseModule());
        DatabaseCreator creator = injector.getInstance(DatabaseCreator.class);
        creator.createTables();
    }
}
