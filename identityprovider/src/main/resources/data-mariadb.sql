-- Create teaching staff group on startup
INSERT IGNORE INTO 'groups' (ID, SHORT_NAME, LONG_NAME) VALUES (0, 'Teaching Staff', 'Users with the "Teacher" role');

-- Create members without groups on startup
INSERT IGNORE INTO 'groups' (ID, SHORT_NAME, LONG_NAME) VALUES (1, 'Members Without A Group', 'Users who are not part of any groups');