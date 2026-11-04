-- Audio files are deleted after transcription (data minimisation).
-- file_path is nulled on the record once the file is removed from disk.
ALTER TABLE uploads ALTER COLUMN file_path DROP NOT NULL;
