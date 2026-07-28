-- DRAKEN-4569: speeds up the batch query driving the base64-to-binary backfill
-- (AttachmentMigrationRepository.findIdsForMigration), which repeatedly selects
-- the lowest-id attachments still holding their content in the legacy base64
-- 'file' column:
--
--     where content is null and file is not null and length(file) > 0 order by id
--
-- No existing index covers 'content', so the optimizer can only walk the
-- clustered primary key and filter row by row. That is cheap while most rows are
-- still pending - the first batch is found immediately - but the cost grows as
-- the backfill progresses: every already-migrated row (content is not null) has
-- to be stepped over to reach the next batch of pending ones, so the final
-- batches approach a full scan of the table.
--
-- A one-byte prefix is sufficient because the predicate only distinguishes null
-- from non-null, and it keeps the index small on a longblob column. InnoDB
-- appends the primary key to secondary indexes implicitly, but 'id' is listed
-- explicitly so the index is self-documenting and also satisfies the
-- 'order by id' without a filesort.
--
-- algorithm/lock are pinned deliberately: adding a secondary index to InnoDB is
-- an online operation, and pinning them makes the statement fail fast rather
-- than silently degrade into a blocking table copy on the (multi-gigabyte)
-- production attachment table. If a target environment cannot honour them, drop
-- the two clauses and schedule the migration for a maintenance window instead.
--
-- 'if not exists' makes the statement a no-op when the index is already present.
-- Flyway already guarantees a single execution per schema, so this covers the
-- case where the index was created outside Flyway - most plausibly a DBA adding
-- it up front during a maintenance window to keep this migration instant on the
-- large production table. MariaDB then downgrades the duplicate to a note
-- (1061) and the statement still succeeds, algorithm/lock clauses included.
alter table if exists attachment
   add index if not exists idx_attachment_content (content(1), id),
   algorithm = inplace,
   lock = none;
