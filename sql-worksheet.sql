-- 1. Create a new local developer account
CREATE USER dev_user IDENTIFIED BY "DevPassword123!";

-- 2. Allow the user to connect to the database
GRANT CONNECT TO dev_user;

-- 3. Allow the user to create database objects (tables, views, etc.)
GRANT RESOURCE TO dev_user;

-- 4. Give the user permission to store unlimited data in the tablespace
GRANT UNLIMITED TABLESPACE TO dev_user;