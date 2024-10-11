ALTER TABLE IF EXISTS errand_extra_parameters
    RENAME TO errand_extra_parameters_old;

CREATE TABLE IF NOT EXISTS errand_extra_parameters
(
    errand_id      bigint      NOT NULL,
    display_name   varchar(255),
    id             varchar(36) NOT NULL,
    parameters_key varchar(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS errand_extra_parameter_values
(
    extra_parameter_id varchar(36) NOT NULL,
    value              varchar(255)
) ENGINE = InnoDB;

-- Migrate data to errand_extra_parameters
INSERT INTO errand_extra_parameters (errand_id, display_name, id, parameters_key)
SELECT errand_id,
       NULL   AS display_name,
       UUID() AS id,
       extra_parameter_key
FROM errand_extra_parameters_old;

-- Migrate data to parameter_values
INSERT INTO errand_extra_parameter_values (extra_parameter_id, value)
SELECT UUID() AS extra_parameter_id,
       extra_parameter_value
FROM errand_extra_parameters_old;
