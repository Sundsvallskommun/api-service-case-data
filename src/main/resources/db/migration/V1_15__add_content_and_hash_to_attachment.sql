alter table if exists attachment
   add column content longblob,
   add column hash    varchar(64),
   add index idx_attachment_hash (hash);
