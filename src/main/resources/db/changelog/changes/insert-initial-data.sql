-- Insert owners from contacts CSV
INSERT INTO owners (name, email, phone, address, postal_code, city, created_at)
VALUES 
-- Anders Collin
('Anders Collin', 'anders.collin@gmail.com', '+46736844130', '', '', '', NOW()),
-- Anders Drefeldt
('Anders Drefeldt', 'anders@drefeldt.se', '0732680875', 'Råslätt Skår 1', '516 95', 'Målsryd', NOW()),
-- Arne Jørgen Olafsen
('Arne Jørgen Olafsen', 'ajolafsen@gmail.com', '', '', '', '', NOW()),
-- Bertel Enlund
('Bertel Enlund', '', '', '', '', '', NOW()),
-- Björn Liljekvist
('Björn Liljekvist', '', '', 'Heimdalsgatan 28', '261 62', 'GLUMSLÖV', NOW()),
-- Björn Wastler
('Björn Wastler', '', '', 'Åkersbergsvägen, 13', '461 34', 'TROLLHÄTTAN', NOW()),
-- Börje Fäk
('Börje Fäk', '', '', 'Övre Fogelbergsgatan 3', '411 28', 'GÖTEBORG', NOW()),
-- Charlotte Hemvik
('Charlotte Hemvik', '', '', '', '', '', NOW()),
-- Claes Mangelus
('Claes Mangelus', '', '', 'Lunnatorpsgatan 12', '412 74', 'GÖTEBORG', NOW()),
-- Erik Billerby
('Erik Billerby', '', '', '', '', '', NOW()),
-- Erik Holmberg
('Erik Holmberg', '', '', '', '', '', NOW()),
-- Erik Norberg
('Erik Norberg', '', '', '', '', '', NOW()),
-- Göran Mellgren
('Göran Mellgren', '', '', '', '', '', NOW()),
-- Hans Broström
('Hans Broström', '', '', '', '', '', NOW()),
-- Ingvar Eriksson
('Ingvar Eriksson', '', '', '', '', '', NOW()),
-- Ingemar Olemyr
('Ingemar Olemyr', '', '', '', '', '', NOW()),
-- Johan Lundberg
('Johan Lundberg', '', '', '', '', '', NOW()),
-- Kalle Månsson
('Kalle Månsson', '', '', '', '', '', NOW()),
-- Kerstin Malm-Tronstad
('Kerstin Malm-Tronstad', '', '', '', '', '', NOW()),
-- Lars Bruce
('Lars Bruce', '', '', '', '', '', NOW()),
-- Lennart Lefdal
('Lennart Lefdal', '', '', '', '', '', NOW()),
-- Mats Fors
('Mats Fors', '', '', '', '', '', NOW()),
-- Mikael Törnbom
('Mikael Törnbom', '', '', '', '', '', NOW()),
-- Ove Östberg
('Ove Östberg', '', '', 'Fölvägen 32', '425 41', 'Hisings Kärra', NOW()),
-- Per Arnell
('Per Arnell', '', '', '', '', '', NOW()),
-- Per Eskilson
('Per Eskilson', '', '', '', '', '', NOW()),
-- Peter Svending
('Peter Svending', '', '', '', '', '', NOW()),
-- Per Thorén
('Per Thorén', '', '', '', '', '', NOW()),
-- Peter Wemminger
('Peter Wemminger', '', '', '', '', '', NOW()),
-- P&T Johansson
('P&T Johansson', '', '', '', '', '', NOW()),
-- Rolf Liljeros
('Rolf Liljeros', '', '', '', '', '', NOW()),
-- Stefan Walldal
('Stefan Walldal', '', '', '', '', '', NOW()),
-- Jonas Bengtsson
('Jonas Bengtsson', '', '', '', '', '', NOW()),
-- Palm
('Palm', '', '', '', '', '', NOW());

-- Insert properties with Apelgården prefix
INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:28'),
    3.640,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Anders Collin';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:26'),
    3.640,
    'Råslätt Skår 1, 516 95 Målsryd',
    id,
    NOW()
FROM owners WHERE name = 'Anders Drefeldt';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:15'),
    1.600,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Arne Jørgen Olafsen';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:24'),
    0.593,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Bertel Enlund';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:34'),
    3.640,
    'Heimdalsgatan 28, 261 62 GLUMSLÖV',
    id,
    NOW()
FROM owners WHERE name = 'Björn Liljekvist';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:22'),
    3.640,
    'Åkersbergsvägen 13, 461 34 TROLLHÄTTAN',
    id,
    NOW()
FROM owners WHERE name = 'Björn Wastler';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:44'),
    3.640,
    'Övre Fogelbergsgatan 3, 411 28 GÖTEBORG',
    id,
    NOW()
FROM owners WHERE name = 'Börje Fäk';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:18'),
    2.732,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Charlotte Hemvik';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:21'),
    3.204,
    'Lunnatorpsgatan 12, 412 74 GÖTEBORG',
    id,
    NOW()
FROM owners WHERE name = 'Claes Mangelus';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:40'),
    3.640,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Erik Billerby';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:16'),
    1.355,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Erik Holmberg';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:46'),
    3.640,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Erik Norberg';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:20'),
    3.640,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Göran Mellgren';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:27'),
    3.640,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Hans Broström';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:52'),
    1.888,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Ingvar Eriksson';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:33'),
    3.640,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Ingemar Olemyr';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:36'),
    3.640,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Johan Lundberg';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:30'),
    3.640,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Kalle Månsson';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:19'),
    1.355,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Kerstin Malm-Tronstad';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:17'),
    1.355,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Lars Bruce';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:29'),
    3.640,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Lennart Lefdal';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:32'),
    3.640,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Mats Fors';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:43'),
    3.640,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Mikael Törnbom';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:37'),
    3.640,
    'Fölvägen 32, 425 41 Hisings Kärra',
    id,
    NOW()
FROM owners WHERE name = 'Ove Östberg';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:50'),
    1.355,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Per Arnell';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:47'),
    3.640,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Per Eskilson';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:45'),
    3.640,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Peter Svending';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:38'),
    3.640,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Per Thorén';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:42'),
    3.640,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Peter Wemminger';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:51'),
    0.593,
    '',
    id,
    NOW()
FROM owners WHERE name = 'P&T Johansson';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:5'),
    0.593,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Rolf Liljeros';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:39'),
    3.640,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Stefan Walldal';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:41'),
    3.640,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Jonas Bengtsson';

INSERT INTO properties (property_designation, share_ratio, address, owner_id, created_at)
SELECT 
    CONCAT('Apelgården ', '1:14'),
    1.355,
    '',
    id,
    NOW()
FROM owners WHERE name = 'Palm';
