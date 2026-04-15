-- Users (mobile app)
INSERT INTO users (username, created_at) VALUES ('ana', NOW());
INSERT INTO users (username, created_at) VALUES ('john', NOW());
INSERT INTO users (username, created_at) VALUES ('maja', NOW());

-- Operators (browser, password is "password123" bcrypt hashed)
INSERT INTO operators (username, password_hash, created_at)
VALUES ('operatorMike', '$2a$10$23AjKQOmViQlCwBhLJ/VmeAHBP2rqsPtebhmBZWg3LHY5Zh6RC2ue', NOW());

INSERT INTO operators (username, password_hash, created_at)
VALUES ('operatorLucy', '$2a$10$23AjKQOmViQlCwBhLJ/VmeAHBP2rqsPtebhmBZWg3LHY5Zh6RC2ue', NOW());

-- Chats
INSERT INTO chats (id_user, id_operator, room, status, created_at, acquired_at)
VALUES (1, NULL, 'TEHNIKA', 'WAITING', NOW(), NULL);

INSERT INTO chats (id_user, id_operator, room, status, created_at, acquired_at)
VALUES (2, NULL, 'STORITVE', 'WAITING', NOW(), NULL);

INSERT INTO chats (id_user, id_operator, room, status, created_at, acquired_at)
VALUES (3, NULL, 'POGOVOR', 'WAITING', NOW(), NULL);

INSERT INTO chats (id_user, id_operator, room, status, created_at, acquired_at)
VALUES (1, 1, 'TEHNIKA', 'ACTIVE', NOW(), NOW());