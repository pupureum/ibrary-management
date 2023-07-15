INSERT IGNORE INTO member (name, login_id, password, role, created_at)
VALUES ('admin', 'admin@gmail.com', '$2a$10$oPEmMyyJ9HrOmpiz85uaUuFYmYkiZEdo20xYJp5.EHIEu9MaVbKxS', 'ADMIN', NOW());

INSERT IGNORE INTO book_category (category_name)
VALUES ('경제경영'),
       ('자기계발'),
       ('시/에세이'),
       ('인문'),
       ('소설'),
       ('국어/외국어'),
       ('정치/사회'),
       ('역사/문화'),
       ('과학/공학'),
       ('IT/프로그래밍'),
       ('건강/의학'),
       ('가정/생활/요리'),
       ('여행/취미'),
       ('예술/대중문화'),
       ('아동'),
       ('청소년'),
       ('교재/수험서');