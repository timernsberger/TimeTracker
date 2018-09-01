package com.times6.timeTracker.db.dynamo;

import java.time.Instant;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

public class DynamoDBMapperInstantConverter implements DynamoDBTypeConverter<Long, Instant> {

	@Override
	public Long convert(Instant arg0) {
		return arg0.getEpochSecond();
	}

	@Override
	public Instant unconvert(Long arg0) {
		return Instant.ofEpochSecond(arg0);
	}

}
