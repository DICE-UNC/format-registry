DROP DATABASE IF EXISTS formatreg;
DROP ROLE IF EXISTS formatreg;

CREATE USER formatreg WITH PASSWORD '$PASSWORD';
CREATE DATABASE formatreg WITH OWNER formatreg;