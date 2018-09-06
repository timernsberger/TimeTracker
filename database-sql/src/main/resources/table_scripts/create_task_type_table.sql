CREATE TABLE IF NOT EXISTS time_tracker.task_types (
    user_id varchar(256),
    category varchar(1024),
    task_name varchar(1024),
    PRIMARY KEY(user_id, category, task_name)
);
