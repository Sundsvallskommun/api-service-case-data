START TRANSACTION;
alter table errand
    add column case_type_tmp varchar(255);

update errand
    set case_type_tmp = case_type;

alter table errand
    drop column case_type;

alter table errand
    change column case_type_tmp case_type varchar(255);

alter table attachment
    add column category_tmp varchar(255);

update attachment
    set category_tmp = category;

alter table attachment
    drop column category;

alter table attachment
    change column category_tmp category varchar(255);
COMMIT;