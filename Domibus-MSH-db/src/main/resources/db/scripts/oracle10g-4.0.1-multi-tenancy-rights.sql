-- *********************************************************************
-- This script should be run for each existing domain in a multi-tenancy installation of Domibus.
--
-- It will assign proper privileges for <GENERAL_SCHEMA> in order to access <DOMAIN_SCHEMA> objects.
--
-- Change DOMAIN_SCHEMA and GENERAL_SCHEMA values accordingly.
--
-- *********************************************************************
DECLARE
  DOMAIN_SCHEMA varchar2(100) := 'domain_schema';
  GENERAL_SCHEMA varchar2(100) := 'general_schema';
  sql_stmt  VARCHAR2(300);
BEGIN
    FOR t IN (
        SELECT
            object_name,
            object_type
        FROM
            all_objects
        WHERE
            owner = UPPER(DOMAIN_SCHEMA)
            AND   object_type IN (
                'TABLE',
                'VIEW',
                'SEQUENCE'
            )
    ) LOOP
        IF
            t.object_type IN (
                'TABLE'
            )
        THEN
            sql_stmt := 'GRANT SELECT, UPDATE, INSERT, DELETE ON '
            || DOMAIN_SCHEMA || '.'
            || t.object_name
            || ' TO ' || GENERAL_SCHEMA;
            EXECUTE IMMEDIATE sql_stmt;
            DBMS_OUTPUT.PUT_LINE(sql_stmt);
        ELSIF
            t.object_type IN (
                'VIEW',
                'SEQUENCE'
            )
        THEN
            sql_stmt := 'GRANT SELECT ON '
            || DOMAIN_SCHEMA || '.'
            || t.object_name
            || ' TO ' || GENERAL_SCHEMA;
            EXECUTE IMMEDIATE sql_stmt;
            DBMS_OUTPUT.PUT_LINE(sql_stmt);
        END IF;
    END LOOP;
END;
/