-- Create teaching staff group on startup
INSERT INTO GROUPS (ID, SHORT_NAME, LONG_NAME) SELECT 0, 'Teaching Staff', 'Users with the "Teacher" role' WHERE NOT EXISTS (SELECT ID FROM GROUPS WHERE ID = '0');

-- Create members without groups on startup
INSERT INTO GROUPS (ID, SHORT_NAME, LONG_NAME) SELECT 1, 'Members Without A Group', 'Users who are not part of any groups' WHERE NOT EXISTS (SELECT ID FROM GROUPS WHERE ID = '1');