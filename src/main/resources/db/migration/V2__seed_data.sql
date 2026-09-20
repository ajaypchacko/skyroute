-- V2: demo data. All of it is fictional (SkyRoute is a demo airline).

-- 20 flights between MEL, SYD, BNE, ADL and PER, departing over the next 2-21 hours
INSERT INTO flights (flight_number, origin, destination, scheduled_departure) VALUES
    ('SR101', 'MEL', 'SYD', date_trunc('hour', now()) + interval '2 hours'),
    ('SR102', 'SYD', 'MEL', date_trunc('hour', now()) + interval '3 hours'),
    ('SR103', 'MEL', 'BNE', date_trunc('hour', now()) + interval '4 hours'),
    ('SR104', 'BNE', 'MEL', date_trunc('hour', now()) + interval '5 hours'),
    ('SR105', 'MEL', 'ADL', date_trunc('hour', now()) + interval '6 hours'),
    ('SR106', 'ADL', 'MEL', date_trunc('hour', now()) + interval '7 hours'),
    ('SR107', 'MEL', 'PER', date_trunc('hour', now()) + interval '8 hours'),
    ('SR108', 'PER', 'MEL', date_trunc('hour', now()) + interval '9 hours'),
    ('SR109', 'SYD', 'BNE', date_trunc('hour', now()) + interval '10 hours'),
    ('SR110', 'BNE', 'SYD', date_trunc('hour', now()) + interval '11 hours'),
    ('SR111', 'SYD', 'ADL', date_trunc('hour', now()) + interval '12 hours'),
    ('SR112', 'ADL', 'SYD', date_trunc('hour', now()) + interval '13 hours'),
    ('SR113', 'SYD', 'PER', date_trunc('hour', now()) + interval '14 hours'),
    ('SR114', 'PER', 'SYD', date_trunc('hour', now()) + interval '15 hours'),
    ('SR115', 'BNE', 'ADL', date_trunc('hour', now()) + interval '16 hours'),
    ('SR116', 'ADL', 'BNE', date_trunc('hour', now()) + interval '17 hours'),
    ('SR117', 'BNE', 'PER', date_trunc('hour', now()) + interval '18 hours'),
    ('SR118', 'PER', 'BNE', date_trunc('hour', now()) + interval '19 hours'),
    ('SR119', 'ADL', 'PER', date_trunc('hour', now()) + interval '20 hours'),
    ('SR120', 'PER', 'ADL', date_trunc('hour', now()) + interval '21 hours');

-- 100 fake passengers with fake mobile numbers in the +614XXXXXXXX format.
-- No real SMS is ever sent; notifications are only saved to the database.
INSERT INTO passengers (name, mobile_number)
SELECT 'Passenger ' || n, '+614' || lpad(n::text, 8, '0')
FROM generate_series(1, 100) AS n;

-- Every passenger gets one booking (5 passengers per flight)...
INSERT INTO bookings (passenger_id, flight_id)
SELECT p.id, f.id
FROM passengers p
JOIN flights f ON f.flight_number = 'SR' || (101 + (p.id - 1) % 20);

-- ...and every third passenger gets a second booking on a different flight.
-- ON CONFLICT skips the rare case where the second flight equals the first.
INSERT INTO bookings (passenger_id, flight_id)
SELECT p.id, f.id
FROM passengers p
JOIN flights f ON f.flight_number = 'SR' || (101 + (p.id * 7) % 20)
WHERE p.id % 3 = 0
ON CONFLICT (passenger_id, flight_id) DO NOTHING;
