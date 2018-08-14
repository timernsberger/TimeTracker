package com.times6.timeTracker.db;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class DaoTestBase {
    protected AmazonDynamoDB dynamo;

    @BeforeEach
    public void createDatabase() {
        dynamo = DynamoDBEmbedded.create().amazonDynamoDB();
        new DatabaseCreator(dynamo).createTables();
    }

    @AfterEach
    public void clearDatabase() {
        dynamo.shutdown();
    }

}
