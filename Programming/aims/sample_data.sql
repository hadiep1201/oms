-- Disable triggers to prevent foreign key errors during truncation
SET session_replication_role = 'replica';

-- Truncate all tables
TRUNCATE TABLE 
    users_roles,
    admin_log,
    users,
    role,
    track,
    book_author,
    section,
    book,
    newspaper,
    cd,
    dvd,
    printable_product,
    disc_product,
    physical_product,
    product,
    delivery_info,
    payment_transaction,
    refund,
    invoice,
    orders
RESTART IDENTITY CASCADE;

SET session_replication_role = 'origin';

-- 1. Insert Roles
INSERT INTO role (role_id, role_name, description) VALUES
(1, 'ADMIN', 'Administrator role'),
(2, 'PRODUCT_MANAGER', 'Product Manager role');

-- 2. Insert Users
-- Passwords are set to "password123" (bcrypt hashed)
INSERT INTO users (user_id, email, hashed_password, user_name, avatar_url, status) VALUES
(1, 'admin@aims.com', '$2b$10$8KLssaUsPwR0vQpK0gRokuMROx6rZ.VVlXdwgJbT752Iv/JoMHxHq', 'admin', 'https://via.placeholder.com/150', 'ACTIVE'),
(2, 'manager@aims.com', '$2b$10$8KLssaUsPwR0vQpK0gRokuMROx6rZ.VVlXdwgJbT752Iv/JoMHxHq', 'product_manager', 'https://via.placeholder.com/150', 'ACTIVE'),
(3, 'testuser1@aims.com', '$2b$10$8KLssaUsPwR0vQpK0gRokuMROx6rZ.VVlXdwgJbT752Iv/JoMHxHq', 'Test User 1', 'https://via.placeholder.com/150', 'ACTIVE'),
(4, 'testuser2@aims.com', '$2b$10$8KLssaUsPwR0vQpK0gRokuMROx6rZ.VVlXdwgJbT752Iv/JoMHxHq', 'Test User 2', 'https://via.placeholder.com/150', 'ACTIVE'),
(5, 'testuser3@aims.com', '$2b$10$8KLssaUsPwR0vQpK0gRokuMROx6rZ.VVlXdwgJbT752Iv/JoMHxHq', 'Test User 3', 'https://via.placeholder.com/150', 'DEACTIVATED');

-- 3. Insert Users-Roles relationship
-- By default Hibernate maps this join table to users_roles with users_user_id and roles_role_id
INSERT INTO users_roles (users_user_id, roles_role_id) VALUES
(1, 1),
(2, 2);

-- 4. Insert Products (Base table: product)
-- Note: 'categlory' matches the column name in Product.java mapping
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES
(1, 'Clean Code: A Handbook of Agile Software Craftsmanship', 'Book', 'Even bad code can function. But if code isn''t clean, it can bring a development organization to its knees.', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 450000.00, 380000.00, 15, 'ACTIVE', 'BOOK'),
(2, 'Design Patterns: Elements of Reusable Object-Oriented Software', 'Book', 'Capturing a wealth of experience and conveying the principles of object-oriented design.', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 600000.00, 520000.00, 0, 'ACTIVE', 'BOOK'),
(3, 'Abbey Road - The Beatles (Remastered CD)', 'CD', 'The eleventh studio album by the English rock band the Beatles, released on 26 September 1969.', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 320000.00, 290000.00, 8, 'ACTIVE', 'CD'),
(4, 'Inception DVD', 'DVD', 'A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task.', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 180000.00, 150000.00, 25, 'ACTIVE', 'DVD'),
(5, 'Tuoi Tre Newspaper - Issue #145', 'Newspaper', 'Tuoi Tre daily newspaper, providing the latest news in Vietnam.', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 6000.00, 5000.00, 100, 'ACTIVE', 'NEWSPAPER');

-- 4b. Insert Physical Products
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES
(1, '9780123456789', 0.60, 23.50, 18.00, 3.00),
(2, '9780123456789', 0.85, 24.00, 19.00, 3.50),
(3, '9780123456789', 0.12, 12.00, 12.00, 1.00),
(4, '9780123456789', 0.15, 19.00, 13.50, 1.50),
(5, '9780123456789', 0.05, 40.00, 30.00, 0.10);

-- 5. Insert Printable Products
INSERT INTO printable_product (id, language, publication_date, publisher) VALUES
(1, 'English', '2008-08-11', 'Prentice Hall'),
(2, 'English', '1994-11-10', 'Addison-Wesley'),
(5, 'Vietnamese', '2026-05-24', 'Tuoi Tre Publishing');

-- 6. Insert Books
INSERT INTO book (id, cover_type, genre, nb_pages) VALUES
(1, 'Paperback', 'Software Engineering', 464),
(2, 'Hardcover', 'Software Engineering', 395);

-- 7. Insert Newspapers
INSERT INTO newspaper (id, editor_in_chief, issn, issue_number, publication_frequency) VALUES
(5, 'Nguyen The Thanh', '0868-3214', '145-2026', 'Daily');

-- 8. Insert Disc Products
INSERT INTO disc_product (id, genre, release_date) VALUES
(3, 'Rock', '1969-09-26'),
(4, 'Sci-Fi / Thriller', '2010-07-16');

-- 9. Insert CDs
INSERT INTO cd (id, artists, record_label) VALUES
(3, 'The Beatles', 'Apple Records');

-- 10. Insert DVDs
INSERT INTO dvd (id, director, disc_type, language, runtime, studio, subtitles) VALUES
(4, 'Christopher Nolan', 'Blu-ray', 'English', 148, 'Warner Bros. Pictures', 'English, Vietnamese, Spanish');

-- 11. Insert Book Authors
INSERT INTO book_author (author_id, book_id, name, dob) VALUES
(1, 1, 'Robert C. Martin', '1952-12-05'),
(2, 2, 'Erich Gamma', '1961-03-13'),
(3, 2, 'Richard Helm', NULL),
(4, 2, 'Ralph Johnson', '1955-10-07'),
(5, 2, 'John Vlissides', '1961-08-02');

-- 12. Insert CD Tracks
INSERT INTO track (track_id, cd_id, title, length) VALUES
(1, 3, 'Come Together', 259),
(2, 3, 'Something', 183),
(3, 3, 'Maxwell''s Silver Hammer', 207),
(4, 3, 'Oh! Darling', 206),
(5, 3, 'Octopus''s Garden', 171);

-- 13. Insert Newspaper Sections
INSERT INTO section (section_id, newspaper_id, title, description) VALUES
(1, 5, 'Thoi Su', 'Tin tuc trong nuoc va quoc te nong hoi'),
(2, 5, 'Kinh Te', 'Phan tich thi truong tai chinh, bat dong san'),
(3, 5, 'The Thao', 'Tin tuc bong da ngoai hang Anh va the thao nuoc nha');

-- 14. Insert Sample Order (Pending payment)
INSERT INTO orders (order_id, status, shipping_fee, created_date) VALUES
(1, 'PENDING_PAYMENT', 30000.00, '2026-05-24 08:30:00');

INSERT INTO order_detail (order_id, product_id, quantity, price) VALUES
(1, 1, 2, 380000.00), -- 2x Clean Code
(1, 4, 1, 150000.00); -- 1x Inception DVD

INSERT INTO delivery_info (delivery_info_id, order_id, receiver_name, email, phone_number, address, city) VALUES
(1, 1, 'John Doe', 'john@example.com', '0912345678', '123 Nguyen Trai Street, Thanh Xuan', 'Ha Noi');

-- 15. Insert Sample Order (Paid via VietQR, awaiting processing)
INSERT INTO orders (order_id, status, shipping_fee, created_date) VALUES
(2, 'PENDING_PROCESSING', 22000.00, '2026-05-24 07:15:00');

INSERT INTO order_detail (order_id, product_id, quantity, price) VALUES
(2, 3, 1, 290000.00); -- 1x Abbey Road CD

INSERT INTO delivery_info (delivery_info_id, order_id, receiver_name, email, phone_number, address, city) VALUES
(2, 2, 'Jane Smith', 'jane@example.com', '0987654321', '456 Le Loi Street, District 1', 'Ho Chi Minh');

INSERT INTO invoice (invoice_id, order_id, sub_total, shipping_fee, vat_amount, total_amount) VALUES
(1, 2, 290000.00, 22000.00, 29000.00, 341000.00);

INSERT INTO payment_transaction (transaction_id, invoice_id, method, transaction_datetime, transaction_status, transaction_content) VALUES
(1, 1, 'VietQR', '2026-05-24 07:20:00', 'SUCCESS', 'AIMS ORDER 2 PAYMENT');

-- Reset Auto-Increment Sequences to prevent conflicts on future inserts
SELECT setval(pg_get_serial_sequence('role', 'role_id'), COALESCE(MAX(role_id), 1)) FROM role;
SELECT setval(pg_get_serial_sequence('users', 'user_id'), COALESCE(MAX(user_id), 1)) FROM users;
SELECT setval(pg_get_serial_sequence('product', 'id'), COALESCE(MAX(id), 1)) FROM product;
SELECT setval(pg_get_serial_sequence('track', 'track_id'), COALESCE(MAX(track_id), 1)) FROM track;
SELECT setval(pg_get_serial_sequence('book_author', 'author_id'), COALESCE(MAX(author_id), 1)) FROM book_author;
SELECT setval(pg_get_serial_sequence('section', 'section_id'), COALESCE(MAX(section_id), 1)) FROM section;
SELECT setval(pg_get_serial_sequence('orders', 'order_id'), COALESCE(MAX(order_id), 1)) FROM orders;
SELECT setval(pg_get_serial_sequence('delivery_info', 'delivery_info_id'), COALESCE(MAX(delivery_info_id), 1)) FROM delivery_info;
SELECT setval(pg_get_serial_sequence('invoice', 'invoice_id'), COALESCE(MAX(invoice_id), 1)) FROM invoice;
SELECT setval(pg_get_serial_sequence('payment_transaction', 'transaction_id'), COALESCE(MAX(transaction_id), 1)) FROM payment_transaction;

-- Cập nhật ảnh cho Sách Clean Code (ID 1)
UPDATE product SET image_url = 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=500' WHERE id = 1;

-- Cập nhật ảnh cho Sách Design Patterns (ID 2)
UPDATE product SET image_url = 'https://images.unsplash.com/photo-1629654297299-c8506221ca97?w=500' WHERE id = 2;

-- Cập nhật ảnh cho CD Abbey Road (ID 3)
UPDATE product SET image_url = 'https://images.unsplash.com/photo-1539628399213-d6aa89c93074?w=500' WHERE id = 3;

-- Cập nhật ảnh cho DVD Inception (ID 4)
UPDATE product SET image_url = 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=500' WHERE id = 4;

-- Cập nhật ảnh cho Tờ báo Tuổi trẻ (ID 5)
UPDATE product SET image_url = 'https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=500' WHERE id = 5;

-- Added 25 test products
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (10, 'The Great Gatsby', 'DVD', 'Description for product 10', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'DVD');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (10, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO disc_product (id, genre, release_date) VALUES (10, 'Pop', '2024-01-01');
INSERT INTO dvd (id, director, disc_type, language, runtime, studio, subtitles) VALUES (10, 'Test Director', 'Blu-ray', 'English', 120, 'Test Studio', 'English, Spanish');
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (11, 'Thriller - Michael Jackson', 'Newspaper', 'Description for product 11', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'NEWSPAPER');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (11, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO printable_product (id, language, publication_date, publisher) VALUES (11, 'English', '2024-01-01', 'Test Publisher');
INSERT INTO newspaper (id, editor_in_chief, issn, issue_number, publication_frequency) VALUES (11, 'John Doe', '1234-5678', 1, 'Daily');
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (12, 'The Matrix DVD', 'Book', 'Description for product 12', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'BOOK');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (12, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO printable_product (id, language, publication_date, publisher) VALUES (12, 'English', '2024-01-01', 'Test Publisher');
INSERT INTO book (id, cover_type, genre, nb_pages) VALUES (12, 'Paperback', 'Education', 200);
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (13, 'The New York Times', 'CD', 'Description for product 13', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'CD');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (13, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO disc_product (id, genre, release_date) VALUES (13, 'Pop', '2024-01-01');
INSERT INTO cd (id, artists, record_label) VALUES (13, 'Test Artist', 'Test Label');
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (14, 'To Kill a Mockingbird', 'DVD', 'Description for product 14', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'DVD');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (14, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO disc_product (id, genre, release_date) VALUES (14, 'Pop', '2024-01-01');
INSERT INTO dvd (id, director, disc_type, language, runtime, studio, subtitles) VALUES (14, 'Test Director', 'Blu-ray', 'English', 120, 'Test Studio', 'English, Spanish');
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (15, 'Back in Black - AC/DC', 'Newspaper', 'Description for product 15', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'NEWSPAPER');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (15, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO printable_product (id, language, publication_date, publisher) VALUES (15, 'English', '2024-01-01', 'Test Publisher');
INSERT INTO newspaper (id, editor_in_chief, issn, issue_number, publication_frequency) VALUES (15, 'John Doe', '1234-5678', 1, 'Daily');
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (16, 'The Godfather DVD', 'Book', 'Description for product 16', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'BOOK');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (16, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO printable_product (id, language, publication_date, publisher) VALUES (16, 'English', '2024-01-01', 'Test Publisher');
INSERT INTO book (id, cover_type, genre, nb_pages) VALUES (16, 'Paperback', 'Education', 200);
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (17, 'The Washington Post', 'CD', 'Description for product 17', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'CD');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (17, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO disc_product (id, genre, release_date) VALUES (17, 'Pop', '2024-01-01');
INSERT INTO cd (id, artists, record_label) VALUES (17, 'Test Artist', 'Test Label');
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (18, '1984 by George Orwell', 'DVD', 'Description for product 18', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'DVD');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (18, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO disc_product (id, genre, release_date) VALUES (18, 'Pop', '2024-01-01');
INSERT INTO dvd (id, director, disc_type, language, runtime, studio, subtitles) VALUES (18, 'Test Director', 'Blu-ray', 'English', 120, 'Test Studio', 'English, Spanish');
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (19, 'The Dark Side of the Moon', 'Newspaper', 'Description for product 19', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'NEWSPAPER');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (19, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO printable_product (id, language, publication_date, publisher) VALUES (19, 'English', '2024-01-01', 'Test Publisher');
INSERT INTO newspaper (id, editor_in_chief, issn, issue_number, publication_frequency) VALUES (19, 'John Doe', '1234-5678', 1, 'Daily');
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (20, 'Pulp Fiction DVD', 'Book', 'Description for product 20', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'BOOK');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (20, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO printable_product (id, language, publication_date, publisher) VALUES (20, 'English', '2024-01-01', 'Test Publisher');
INSERT INTO book (id, cover_type, genre, nb_pages) VALUES (20, 'Paperback', 'Education', 200);
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (21, 'The Wall Street Journal', 'CD', 'Description for product 21', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'CD');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (21, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO disc_product (id, genre, release_date) VALUES (21, 'Pop', '2024-01-01');
INSERT INTO cd (id, artists, record_label) VALUES (21, 'Test Artist', 'Test Label');
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (22, 'Pride and Prejudice', 'DVD', 'Description for product 22', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'DVD');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (22, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO disc_product (id, genre, release_date) VALUES (22, 'Pop', '2024-01-01');
INSERT INTO dvd (id, director, disc_type, language, runtime, studio, subtitles) VALUES (22, 'Test Director', 'Blu-ray', 'English', 120, 'Test Studio', 'English, Spanish');
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (23, 'Rumours - Fleetwood Mac', 'Newspaper', 'Description for product 23', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'NEWSPAPER');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (23, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO printable_product (id, language, publication_date, publisher) VALUES (23, 'English', '2024-01-01', 'Test Publisher');
INSERT INTO newspaper (id, editor_in_chief, issn, issue_number, publication_frequency) VALUES (23, 'John Doe', '1234-5678', 1, 'Daily');
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (24, 'Forrest Gump DVD', 'Book', 'Description for product 24', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'BOOK');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (24, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO printable_product (id, language, publication_date, publisher) VALUES (24, 'English', '2024-01-01', 'Test Publisher');
INSERT INTO book (id, cover_type, genre, nb_pages) VALUES (24, 'Paperback', 'Education', 200);
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (25, 'USA Today', 'CD', 'Description for product 25', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'CD');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (25, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO disc_product (id, genre, release_date) VALUES (25, 'Pop', '2024-01-01');
INSERT INTO cd (id, artists, record_label) VALUES (25, 'Test Artist', 'Test Label');
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (26, 'The Catcher in the Rye', 'DVD', 'Description for product 26', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'DVD');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (26, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO disc_product (id, genre, release_date) VALUES (26, 'Pop', '2024-01-01');
INSERT INTO dvd (id, director, disc_type, language, runtime, studio, subtitles) VALUES (26, 'Test Director', 'Blu-ray', 'English', 120, 'Test Studio', 'English, Spanish');
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (27, 'Hotel California - Eagles', 'Newspaper', 'Description for product 27', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'NEWSPAPER');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (27, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO printable_product (id, language, publication_date, publisher) VALUES (27, 'English', '2024-01-01', 'Test Publisher');
INSERT INTO newspaper (id, editor_in_chief, issn, issue_number, publication_frequency) VALUES (27, 'John Doe', '1234-5678', 1, 'Daily');
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (28, 'The Shawshank Redemption DVD', 'Book', 'Description for product 28', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'BOOK');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (28, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO printable_product (id, language, publication_date, publisher) VALUES (28, 'English', '2024-01-01', 'Test Publisher');
INSERT INTO book (id, cover_type, genre, nb_pages) VALUES (28, 'Paperback', 'Education', 200);
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (29, 'Financial Times', 'CD', 'Description for product 29', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'CD');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (29, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO disc_product (id, genre, release_date) VALUES (29, 'Pop', '2024-01-01');
INSERT INTO cd (id, artists, record_label) VALUES (29, 'Test Artist', 'Test Label');
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (30, 'The Hobbit', 'DVD', 'Description for product 30', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'DVD');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (30, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO disc_product (id, genre, release_date) VALUES (30, 'Pop', '2024-01-01');
INSERT INTO dvd (id, director, disc_type, language, runtime, studio, subtitles) VALUES (30, 'Test Director', 'Blu-ray', 'English', 120, 'Test Studio', 'English, Spanish');
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (31, 'Led Zeppelin IV', 'Newspaper', 'Description for product 31', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'NEWSPAPER');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (31, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO printable_product (id, language, publication_date, publisher) VALUES (31, 'English', '2024-01-01', 'Test Publisher');
INSERT INTO newspaper (id, editor_in_chief, issn, issue_number, publication_frequency) VALUES (31, 'John Doe', '1234-5678', 1, 'Daily');
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (32, 'Fight Club DVD', 'Book', 'Description for product 32', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'BOOK');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (32, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO printable_product (id, language, publication_date, publisher) VALUES (32, 'English', '2024-01-01', 'Test Publisher');
INSERT INTO book (id, cover_type, genre, nb_pages) VALUES (32, 'Paperback', 'Education', 200);
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (33, 'The Guardian', 'CD', 'Description for product 33', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'CD');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (33, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO disc_product (id, genre, release_date) VALUES (33, 'Pop', '2024-01-01');
INSERT INTO cd (id, artists, record_label) VALUES (33, 'Test Artist', 'Test Label');
INSERT INTO product (id, title, categlory, general_description, image_url, original_value, current_price, stock_quantity, status, dtype) VALUES (34, 'Fahrenheit 451', 'DVD', 'Description for product 34', 'https://dictionary.cambridge.org/vi/images/thumb/book_noun_001_01679.jpg?version=6.0.78', 500000.0, 400000.0, 10, 'ACTIVE', 'DVD');
INSERT INTO physical_product (id, barcode, weight, length, height, width) VALUES (34, '9780123456789', 0.5, 20.0, 15.0, 2.0);
INSERT INTO disc_product (id, genre, release_date) VALUES (34, 'Pop', '2024-01-01');
INSERT INTO dvd (id, director, disc_type, language, runtime, studio, subtitles) VALUES (34, 'Test Director', 'Blu-ray', 'English', 120, 'Test Studio', 'English, Spanish');
