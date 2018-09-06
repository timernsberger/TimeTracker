CREATE TABLE IF NOT EXISTS time_tracker.tasks (
    user_id varchar(256),
    time_started timestamp,
    time_ended timestamp,
    category varchar(1024),
    task_name varchar(1024),
    PRIMARY KEY(user_id, time_started)
);
