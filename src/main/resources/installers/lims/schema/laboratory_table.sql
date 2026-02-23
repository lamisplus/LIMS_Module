BEGIN TRANSACTION;

-- 1. Remove commas
UPDATE laboratory_result lr
SET
    result_reported = REPLACE(result_reported, ',', ''),
    result_report   = REPLACE(result_report, ',', '')
FROM laboratory_sample ls
JOIN laboratory_test lt ON lt.id = ls.test_id
WHERE
    lr.test_id = ls.test_id
    AND lt.lab_test_id = 16
    AND (
        result_reported LIKE '%,%' OR
        result_report LIKE '%,%'
    );


-- 2. Remove <, >, <=, >= symbols
UPDATE laboratory_result lr
SET
    result_reported = CASE
        WHEN UPPER(TRIM(result_reported)) LIKE '<%' OR
             UPPER(TRIM(result_reported)) LIKE '>%' OR
             UPPER(TRIM(result_reported)) LIKE '<=%' OR
             UPPER(TRIM(result_reported)) LIKE '>=%'
        THEN TRIM(REGEXP_REPLACE(result_reported, '[<>]=?', ''))
        ELSE result_reported
    END,
    result_report = CASE
        WHEN UPPER(TRIM(result_report)) LIKE '<%' OR
             UPPER(TRIM(result_report)) LIKE '>%' OR
             UPPER(TRIM(result_report)) LIKE '<=%' OR
             UPPER(TRIM(result_report)) LIKE '>=%'
        THEN TRIM(REGEXP_REPLACE(result_report, '[<>]=?', ''))
        ELSE result_report
    END
FROM laboratory_sample ls
JOIN laboratory_test lt ON lt.id = ls.test_id
WHERE
    lr.test_id = ls.test_id
    AND lt.lab_test_id = 16
    AND (
        UPPER(TRIM(result_reported)) LIKE '<%' OR
        UPPER(TRIM(result_reported)) LIKE '>%' OR
        UPPER(TRIM(result_report)) LIKE '<%' OR
        UPPER(TRIM(result_report)) LIKE '>%'
    );


-- 3. Strip units (e.g. 120mg → 120)
UPDATE laboratory_result lr
SET
    result_reported = CASE
        WHEN result_reported ~ '^[0-9]+[A-Za-z/]'
        THEN SUBSTRING(result_reported FROM '^([0-9]+)')
        ELSE result_reported
    END,
    result_report = CASE
        WHEN result_report ~ '^[0-9]+[A-Za-z/]'
        THEN SUBSTRING(result_report FROM '^([0-9]+)')
        ELSE result_report
    END
FROM laboratory_sample ls
JOIN laboratory_test lt ON lt.id = ls.test_id
WHERE
    lr.test_id = ls.test_id
    AND lt.lab_test_id = 16
    AND (
        result_reported ~ '^[0-9]+[A-Za-z/]' OR
        result_report ~ '^[0-9]+[A-Za-z/]'
    );


-- 4. Normalize TITERMIN → 10
UPDATE laboratory_result lr
SET
    result_reported = '10',
    result_report   = '10'
FROM laboratory_sample ls
JOIN laboratory_test lt ON lt.id = ls.test_id
WHERE
    lr.test_id = ls.test_id
    AND lt.lab_test_id = 16
    AND (
        UPPER(TRIM(result_reported)) = 'TITERMIN' OR
        UPPER(TRIM(result_report)) = 'TITERMIN'
    );


-- 5. Normalize NOT DETECTED variants → 0
UPDATE laboratory_result lr
SET
    result_reported = '0',
    result_report   = '0'
FROM laboratory_sample ls
JOIN laboratory_test lt ON lt.id = ls.test_id
WHERE
    lr.test_id = ls.test_id
    AND lt.lab_test_id = 16
    AND (
        UPPER(TRIM(result_reported)) IN (
            'NOTDETECTED', 'TARGETNOTDETECTED',
            'TARGET NOT DETECTED', 'TARGET NOT,DETECTED', 'NOT DETECTED'
        )
        OR UPPER(TRIM(result_report)) IN (
            'NOTDETECTED', 'TARGETNOTDETECTED',
            'TARGET NOT DETECTED', 'TARGET NOT,DETECTED', 'NOT DETECTED'
        )
        OR UPPER(TRIM(result_reported)) LIKE '%0%X%0%'
        OR UPPER(TRIM(result_report)) LIKE '%0%X%0%'
    );

COMMIT;