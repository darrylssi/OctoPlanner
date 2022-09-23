-- Create teaching staff group on startup
INSERT INTO GROUPS (ID, SHORT_NAME, LONG_NAME) SELECT 0, 'Teaching Staff', 'Users with the "Teacher" role' WHERE NOT EXISTS (SELECT ID FROM GROUPS WHERE ID = '0')



