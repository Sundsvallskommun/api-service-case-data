-- DRAKEN-4668: drops the last remains of the base64 attachment storage. Nothing has read or written
-- 'file' since DRAKEN-4456, and idx_attachment_content (added by V1_16) existed solely for
-- AttachmentMigrationRepository.findIdsForMigration, which was deleted with the backfill job.
--
-- Two statements because they need different algorithms: dropping an index is INPLACE-only, dropping
-- a column is INSTANT. Both are pinned so this fails fast instead of silently degrading into a
-- blocking rebuild of the multi-gigabyte production table; if an environment rejects them, run the
-- drop manually in a maintenance window and 'if exists' makes this migration a no-op. Note that an
-- instant drop reclaims no disk space - that needs a separate table rebuild.
alter table if exists attachment
   drop index if exists idx_attachment_content,
   algorithm = inplace,
   lock = none;

alter table if exists attachment
   drop column if exists file,
   algorithm = instant;
