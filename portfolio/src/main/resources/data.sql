-- Create default project on startup
INSERT INTO project (ID, PROJECT_NAME, PROJECT_DESCRIPTION, PROJECT_START_DATE, PROJECT_END_DATE, PROJECT_CREATION_DATE)
SELECT * FROM (SELECT
                      0 AS id,
                      CONCAT('Project ', CAST((SELECT EXTRACT(YEAR FROM CURRENT_DATE )) AS VARCHAR)) AS name,
                      '' as description,
                      CURRENT_DATE as start,
                      (SELECT DATEADD(month, 8, CURRENT_DATE) AS end_date),
                      CURRENT_DATE as creation) as temp
WHERE NOT EXISTS (SELECT * FROM PROJECT);
