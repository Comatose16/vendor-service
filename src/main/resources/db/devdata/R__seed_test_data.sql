-- 1. Seed Venues around Inglewood, CA
INSERT INTO venues (name, address, location)
VALUES
    (
        'The Nile Bar',
        '207 S Market St, Inglewood, CA 90301',
        ST_SetSRID(ST_MakePoint(-118.353230, 33.961680), 4326)
    ),
    (
        'Three Weavers Brewing Company',
        '1065 E Imperial Hwy, Inglewood, CA 90301',
        ST_SetSRID(ST_MakePoint(-118.337420, 33.930980), 4326)
    ),
    (
        'Tom''s Watch Bar',
        '3900 W Century Blvd, Inglewood, CA 90303',
        ST_SetSRID(ST_MakePoint(-118.342120, 33.945620), 4326)
    ),
    (
        'Martin’s Cocina y Cantina',
        '160 S Market St, Inglewood, CA 90301',
        ST_SetSRID(ST_MakePoint(-118.353010, 33.962100), 4326)
    );

-- 2. Seed Active Happy Hour Events & Flash Specials
INSERT INTO events (event_type, title, description, start_time, end_time, venue_id, drink_specials_detail, food_specials_detail, condition_details, is_active)
VALUES
    (
        'HAPPY_HOUR',
        'Market Street After-Work Specials',
        '$2 off all local craft draft beers and house spirits',
        NOW() - INTERVAL '1 hour',
        NOW() + INTERVAL '3 hours',
        (SELECT id FROM venues WHERE name = 'The Nile Bar' LIMIT 1),
        ' $3 Draft Beers',
        '$5 cheese burgers',
        '',
        TRUE
    ),
    (
        'FLASH_DEAL',
        'Game Day IPA Pint Special',
        'Flash sale on Expatriate West Coast IPA',
        NOW() - INTERVAL '30 minutes',
        NOW() + INTERVAL '2 hours',
        (SELECT id FROM venues WHERE name = 'Three Weavers Brewing Company' LIMIT 1),
        'Expatriate IPA',
        '$4 Chicken Wings',
        'With Valid ID',
        FALSE
    ),
    (
        'HAPPY_HOUR',
        'Pre-Game Tequila & Tacos',
        'Half off signature margaritas and street tacos',
        NOW() - INTERVAL '2 hours',
        NOW() + INTERVAL '2 hours',
        (SELECT id FROM venues WHERE name = 'Martin’s Cocina y Cantina' LIMIT 1),
        '$8 House Margarita',
        '$3 Street Tacos',
        '',
        TRUE
    ),
    (
        'HAPPY_HOUR',
        'Pre-Game Tequila & Tacos',
        'Half off signature margaritas and street tacos',
        NOW() - INTERVAL '2 hours',
        NOW() + INTERVAL '2 hours',
        (SELECT id FROM venues WHERE name = 'Tom''s Watch Bar' LIMIT 1),
        '$9 LIT',
        '$5 Chicken Fingers',
        '',
        TRUE
    );