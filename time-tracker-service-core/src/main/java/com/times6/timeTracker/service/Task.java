package com.times6.timeTracker.service;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Task {
	private String name;
	private String category;
	private Instant timeStarted;
	private Instant timeEnded;
}
