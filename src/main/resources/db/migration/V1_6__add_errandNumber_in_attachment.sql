alter table attachment
    drop foreign key FK_attachment_errand_id;

alter table attachment
    add errand_number varchar(255) null;

START TRANSACTION;
UPDATE attachment AS b
    JOIN errand AS a ON b.errand_id = a.id
SET b.errand_number = a.errand_number
where errand_id is not null;
COMMIT;

create index attachment_errand_number_idx on attachment (errand_number);
