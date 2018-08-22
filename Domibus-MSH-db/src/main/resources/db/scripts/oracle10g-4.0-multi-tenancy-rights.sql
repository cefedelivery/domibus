-- *********************************************************************
-- This script should be run for each existing domain in a multi-tenancy installation of Domibus.
--
-- It will assign proper privileges for <general_schema> in order to access <domain_schema> objects.
--
-- Change domain_schema and general_schema values accordingly.
--
-- *********************************************************************
DECLARE
  domain_schema varchar2(100) := 'edelivery_default';
  general_schema varchar2(100) := 'edelivery_general';
BEGIN
    FOR t IN (
        SELECT
            object_name,
            object_type
        FROM
            all_objects
        WHERE
            owner = UPPER(domain_schema)
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
            EXECUTE IMMEDIATE 'GRANT SELECT, UPDATE, INSERT, DELETE ON '
            || domain_schema || '.'
            || t.object_name
            || ' TO ' || general_schema;

        ELSIF
            t.object_type IN (
                'VIEW',
                'SEQUENCE'
            )
        THEN
            EXECUTE IMMEDIATE 'GRANT SELECT ON '
            || domain_schema || '.'
            || t.object_name
            || ' TO ' || general_schema;

        END IF;
    END LOOP;
END;