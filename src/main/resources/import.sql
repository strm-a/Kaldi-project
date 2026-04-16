-- Users (mobile app)
INSERT INTO users (username, created_at) VALUES ('ana', NOW());
INSERT INTO users (username, created_at) VALUES ('john', NOW());
INSERT INTO users (username, created_at) VALUES ('maja', NOW());

-- Operators (identity is handled by Keycloak; this table just stores app-side profile rows keyed by username)
INSERT INTO operators (username, created_at) VALUES ('mikeOperator', NOW());
INSERT INTO operators (username, created_at) VALUES ('lucyOperator', NOW());
INSERT INTO operators (username, created_at) VALUES ('aliceOperator', NOW());

-- Chats: 3 waiting (id 1-3), 1 active (id 4)
INSERT INTO chats (id_user, id_operator, room, status, created_at, acquired_at)
VALUES (1, NULL, 'TEHNIKA', 'WAITING', NOW(), NULL);

INSERT INTO chats (id_user, id_operator, room, status, created_at, acquired_at)
VALUES (2, NULL, 'STORITVE', 'WAITING', NOW(), NULL);

INSERT INTO chats (id_user, id_operator, room, status, created_at, acquired_at)
VALUES (3, NULL, 'POGOVOR', 'WAITING', NOW(), NULL);

INSERT INTO chats (id_user, id_operator, room, status, created_at, acquired_at)
VALUES (1, 1, 'TEHNIKA', 'ACTIVE', NOW(), NOW());

-- Opening message for each waiting chat
INSERT INTO messages (id_chat, sender_type, sender_id, content, time_sent)
VALUES (1, 'USER', 1, 'HeLLO? IS THIS LUCY?', NOW());

INSERT INTO messages (id_chat, sender_type, sender_id, content, time_sent)
VALUES (2, 'USER', 2, 'I broke my phone screen and I bought warranty, what do I need to provide?', NOW());

INSERT INTO messages (id_chat, sender_type, sender_id, content, time_sent)
VALUES (3, 'USER', 3, 'Heey, I just want to chat a little if you have time.', NOW());

-- Active chat conversation (user 1 <-> operator 1 in room TEHNIKA)
INSERT INTO messages (id_chat, sender_type, sender_id, content, time_sent)
VALUES (4, 'USER',     1, 'Hi, my internet has been dropping every 10 minutes since this morning.', NOW() - INTERVAL '10 minutes');

INSERT INTO messages (id_chat, sender_type, sender_id, content, time_sent)
VALUES (4, 'OPERATOR', 1, 'Hi! Sorry to hear that. Can you tell me the model of your router?',        NOW() - INTERVAL '9 minutes');

INSERT INTO messages (id_chat, sender_type, sender_id, content, time_sent)
VALUES (4, 'USER',     1, 'It''s the one you guys sent me, the white Huawei box.',                    NOW() - INTERVAL '8 minutes');

INSERT INTO messages (id_chat, sender_type, sender_id, content, time_sent)
VALUES (4, 'OPERATOR', 1, 'Got it. Could you try unplugging it for 30 seconds and plugging it back in?', NOW() - INTERVAL '7 minutes');

INSERT INTO messages (id_chat, sender_type, sender_id, content, time_sent)
VALUES (4, 'USER',     1, 'Okay, doing it now... back up. Lights look normal.',                       NOW() - INTERVAL '5 minutes');

INSERT INTO messages (id_chat, sender_type, sender_id, content, time_sent)
VALUES (4, 'OPERATOR', 1, 'Great. Give it a couple minutes and let me know if it drops again.',      NOW() - INTERVAL '4 minutes');
