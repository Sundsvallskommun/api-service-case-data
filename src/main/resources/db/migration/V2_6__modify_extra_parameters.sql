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


alter table if exists errand_extra_parameter_values
    add constraint fk_errand_extra_parameter_values_parameter_id
        foreign key (extra_parameter_id)
            references errand_extra_parameters (id);


-- Migrate data from the old table to the new tables
ALTER TABLE errand_extra_parameters_old
    ADD COLUMN new_id VARCHAR(36);

UPDATE errand_extra_parameters_old
SET new_id = UUID()
WHERE new_id IS NULL;

INSERT INTO errand_extra_parameters (errand_id, display_name, id, parameters_key)
SELECT errand_id,
       NULL   AS display_name,
       new_id AS id,
       extra_parameter_key
FROM errand_extra_parameters_old;

INSERT INTO errand_extra_parameter_values (extra_parameter_id, value)
SELECT new_id AS extra_parameter_id,
       extra_parameter_value
FROM errand_extra_parameters_old;

-- Clean up the old table
DROP TABLE errand_extra_parameters_old;
