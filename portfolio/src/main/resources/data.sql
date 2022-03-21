-- Create default project on startup
-- Project #1 and its sprints
INSERT INTO project (ID, PROJECT_NAME, PROJECT_DESCRIPTION, PROJECT_START_DATE, PROJECT_END_DATE)
SELECT * FROM (SELECT
                      0 AS id,
                      'Cool Project 2022' AS name,
                      'Create the coolest app EVER' as description,
                      '2022-01-01' as start_date,
                      SELECT '2022-30-12' as end_date
                ) as temp
WHERE NOT EXISTS (SELECT * FROM PROJECT where ID=0);

insert into sprint (ID, parent_project_id, sprint_name, sprint_label, sprint_description, sprint_start_date, sprint_end_date)
select * from (SELECT
                    1 as id,
                    0 as project_id,
                    'Sprint #1' as name,
                    'Icy Girl' as label,
                    'Create a community area for all ice-cream lovers' as description,
                    '2022-01-03' as start_date,
                    '2022-01-31' as end_date
                ) as temp
WHERE NOT EXISTS (SELECT * FROM SPRINT where ID=1);

insert into sprint (ID, parent_project_id, sprint_name, sprint_label, sprint_description, sprint_start_date, sprint_end_date)
select * from (SELECT
                    2 as id,
                    0 as project_id,
                    'Sprint #2' as name,
                    'Freezing my world' as label,
                    'Create a prison area for people who like waffle cones' as description,
                    '2022-02-03' as start_date,
                    '2022-02-31' as end_date
                ) as temp
WHERE NOT EXISTS (SELECT * FROM SPRINT where ID=2);

-- Project #2
INSERT INTO project (ID, PROJECT_NAME, PROJECT_DESCRIPTION, PROJECT_START_DATE, PROJECT_END_DATE)
SELECT * FROM (SELECT
                    2 AS id,
                    'Icy Project 2023' AS name,
                    'Freeze the thymeleaf dev in time, until the end of time >:(' as description,
                    '2023-01-05' as start_date,
                    '2023-05-05' as end_date
                ) as temp
WHERE NOT EXISTS (SELECT * FROM PROJECT where ID=1);

insert into sprint (ID, parent_project_id, sprint_name, sprint_label, sprint_description, sprint_start_date, sprint_end_date)
select * from (SELECT
                    3 as id,
                    1 as project_id,
                    'Skate #1' as name,
                    'Ban-hammer' as label,
                    'Get his github account banned for his crimes' as description,
                    '2023-02-03' as start_date,
                    '2023-02-31' as end_date
                ) as temp
WHERE NOT EXISTS (SELECT * FROM SPRINT where ID=3);