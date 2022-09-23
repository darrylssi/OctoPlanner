-- Create teaching staff group on startup
INSERT IGNORE INTO groups (ID, SHORT_NAME, LONG_NAME) VALUES (0, 'Teaching Staff', 'Users with the "Teacher" role')

-- Create members without groups on startup
-- SQL statement for this ^^^ goes here