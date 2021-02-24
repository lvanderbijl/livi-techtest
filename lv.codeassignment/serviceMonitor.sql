CREATE TABLE ServiceMonitors
(
    id SERIAL PRIMARY KEY,
    monitor_name varchar(1024),
    url_port int,
    url_path varchar(1024),
    url_domain varchar(255),
    created_date timestamp,
    modified_date timestamp
);

CREATE TABLE PollResults
(
    id SERIAL PRIMARY KEY,
    monitor_id int,
    monitor_url varchar(4096),
    monitor_status varchar(255),
    poll_date timestamp
);