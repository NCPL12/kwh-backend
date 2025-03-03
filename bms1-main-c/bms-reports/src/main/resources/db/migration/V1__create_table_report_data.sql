create table report_data (
    report_id int NOT NULL AUTO_INCREMENT,
    timestamp bigint,
    rbd_olympus_dg02_active_power varchar(255),
    rbd_olympus_dg02_avg_current varchar(255),
    rbd_olympus_f_aw_ahu2_dpt varchar(255),
    PRIMARY KEY (report_id)
)