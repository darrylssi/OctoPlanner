-- Create default project on startup
INSERT INTO project (ID, PROJECT_NAME, PROJECT_DESCRIPTION, PROJECT_START_DATE, PROJECT_END_DATE, PROJECT_CREATION_DATE)
  SELECT * FROM (SELECT
                      0,
                      CONCAT('Project ', CAST((SELECT YEAR(CURRENT_DATE )) AS CHAR(4))),
                      '',
                      CURRENT_DATE,
(SELECT DATE_ADD(CURRENT_DATE, INTERVAL '0:8' YEAR_MONTH)),
CURRENT_DATE as creation) as temp
WHERE NOT EXISTS (SELECT * FROM project);
