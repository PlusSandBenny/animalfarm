-- UUID migration for owners/animals public identifiers.
-- Safe to run multiple times (idempotent checks included).
-- Run this before starting the new UUID-based application version.

USE animalfarm;

DELIMITER $$

DROP PROCEDURE IF EXISTS migrate_uuid_identifiers $$
CREATE PROCEDURE migrate_uuid_identifiers()
BEGIN
    DECLARE col_exists INT DEFAULT 0;
    DECLARE idx_exists INT DEFAULT 0;
    DECLARE col_type VARCHAR(64);

    -- owners.owner_id (UUID business key)
    SELECT COUNT(*) INTO col_exists
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'owners' AND column_name = 'owner_id';

    IF col_exists = 0 THEN
        ALTER TABLE owners ADD COLUMN owner_id CHAR(36) NULL;
    END IF;

    UPDATE owners
    SET owner_id = UUID()
    WHERE owner_id IS NULL
       OR owner_id = ''
       OR owner_id NOT REGEXP '^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$';

    ALTER TABLE owners MODIFY COLUMN owner_id CHAR(36) NOT NULL;

    SELECT COUNT(*) INTO idx_exists
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'owners' AND index_name = 'ux_owners_owner_id';

    IF idx_exists = 0 THEN
        ALTER TABLE owners ADD UNIQUE INDEX ux_owners_owner_id (owner_id);
    END IF;

    -- animals.animal_id should be UUID business key
    SELECT COUNT(*) INTO col_exists
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'animals' AND column_name = 'animal_id';

    IF col_exists = 0 THEN
        ALTER TABLE animals ADD COLUMN animal_id CHAR(36) NULL;
    ELSE
        ALTER TABLE animals MODIFY COLUMN animal_id CHAR(36) NULL;
    END IF;

    UPDATE animals
    SET animal_id = UUID()
    WHERE animal_id IS NULL
       OR animal_id = ''
       OR animal_id NOT REGEXP '^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$';

    ALTER TABLE animals MODIFY COLUMN animal_id CHAR(36) NOT NULL;

    SELECT COUNT(*) INTO idx_exists
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'animals' AND index_name = 'ux_animals_animal_id';

    IF idx_exists = 0 THEN
        ALTER TABLE animals ADD UNIQUE INDEX ux_animals_animal_id (animal_id);
    END IF;

    -- animals.parent_id: convert old BIGINT parent reference to UUID (animal_id)
    SELECT COUNT(*) INTO col_exists
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'animals' AND column_name = 'parent_id';

    IF col_exists > 0 THEN
        SELECT data_type INTO col_type
        FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'animals' AND column_name = 'parent_id'
        LIMIT 1;

        IF col_type IN ('bigint', 'int', 'integer', 'smallint', 'mediumint', 'tinyint') THEN
            SELECT COUNT(*) INTO col_exists
            FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = 'animals' AND column_name = 'parent_id_uuid';

            IF col_exists = 0 THEN
                ALTER TABLE animals ADD COLUMN parent_id_uuid CHAR(36) NULL;
            END IF;

            UPDATE animals child
            LEFT JOIN animals parent ON child.parent_id = parent.id
            SET child.parent_id_uuid = parent.animal_id
            WHERE child.parent_id IS NOT NULL;

            ALTER TABLE animals DROP COLUMN parent_id;
            ALTER TABLE animals CHANGE COLUMN parent_id_uuid parent_id CHAR(36) NULL;
        ELSE
            ALTER TABLE animals MODIFY COLUMN parent_id CHAR(36) NULL;
        END IF;
    ELSE
        ALTER TABLE animals ADD COLUMN parent_id CHAR(36) NULL;
    END IF;

    -- transfer_request_animals.animal_id: convert old numeric values to animal UUID
    SELECT COUNT(*) INTO col_exists
    FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'transfer_request_animals';

    IF col_exists > 0 THEN
        SELECT data_type INTO col_type
        FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'transfer_request_animals' AND column_name = 'animal_id'
        LIMIT 1;

        IF col_type IN ('bigint', 'int', 'integer', 'smallint', 'mediumint', 'tinyint') THEN
            SELECT COUNT(*) INTO col_exists
            FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = 'transfer_request_animals' AND column_name = 'animal_uuid';

            IF col_exists = 0 THEN
                ALTER TABLE transfer_request_animals ADD COLUMN animal_uuid CHAR(36) NULL;
            END IF;

            UPDATE transfer_request_animals tra
            LEFT JOIN animals a ON tra.animal_id = a.id
            SET tra.animal_uuid = a.animal_id;

            UPDATE transfer_request_animals
            SET animal_uuid = UUID()
            WHERE animal_uuid IS NULL OR animal_uuid = '';

            ALTER TABLE transfer_request_animals DROP COLUMN animal_id;
            ALTER TABLE transfer_request_animals CHANGE COLUMN animal_uuid animal_id CHAR(36) NOT NULL;
        ELSE
            ALTER TABLE transfer_request_animals MODIFY COLUMN animal_id CHAR(36) NOT NULL;
        END IF;
    END IF;
END $$

CALL migrate_uuid_identifiers() $$
DROP PROCEDURE migrate_uuid_identifiers $$

DELIMITER ;
