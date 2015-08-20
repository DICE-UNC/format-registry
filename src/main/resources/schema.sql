CREATE TABLE IF NOT EXISTS classes (
  id SERIAL PRIMARY KEY,
  uri VARCHAR(128) UNIQUE NOT NULL,
  name VARCHAR(128) NOT NULL
);

CREATE TABLE IF NOT EXISTS entities (
  id SERIAL PRIMARY KEY,
  class VARCHAR(128) NOT NULL REFERENCES classes(uri),
  uri VARCHAR(128) UNIQUE NOT NULL,
  name VARCHAR(128) NOT NULL
);

CREATE TABLE IF NOT EXISTS values (
  id SERIAL PRIMARY KEY,
  entity VARCHAR(128) NOT NULL REFERENCES entities(uri),
  uri VARCHAR(128) NOT NULL,
  value VARCHAR(4096) NOT NULL
);

--TODO: foreign keys tied to ids instead of uris
--TODO: more indexes if needed
--TODO: move support for the ontology into here as well
