INSERT IGNORE INTO member (name, login_id, password, role, created_at)
VALUES ('admin', 'admin@gmail.com', '$2a$10$oPEmMyyJ9HrOmpiz85uaUuFYmYkiZEdo20xYJp5.EHIEu9MaVbKxS', 'ADMIN', NOW());

INSERT IGNORE INTO book_category (category_name, created_at)
VALUES ('경제경영', NOW()),
       ('자기계발', NOW()),
       ('시/에세이', NOW()),
       ('인문', NOW()),
       ('소설', NOW()),
       ('국어/외국어', NOW()),
       ('정치/사회', NOW()),
       ('역사/문화', NOW()),
       ('과학/공학', NOW()),
       ('IT/프로그래밍', NOW()),
       ('건강/의학', NOW()),
       ('가정/생활/요리', NOW()),
       ('여행/취미', NOW()),
       ('예술/대중문화', NOW()),
       ('아동', NOW()),
       ('청소년', NOW()),
       ('교재/수험서', NOW());