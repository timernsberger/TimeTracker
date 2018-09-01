package com.times6.timeTracker.db.dynamo;

import lombok.Data;

@Data
public class DatabaseConfig {
	private String accessKey;
	private String secret;
	private String region;
}
