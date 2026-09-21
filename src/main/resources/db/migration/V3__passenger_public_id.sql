-- V3: give every passenger an unguessable public id.
-- The API uses this UUID instead of the sequential database id (1, 2, 3...),
-- so nobody can read other passengers' notifications by counting upwards.
-- gen_random_uuid() is built into PostgreSQL. Existing rows each get their own value.

ALTER TABLE passengers ADD COLUMN public_id UUID NOT NULL DEFAULT gen_random_uuid();

CREATE UNIQUE INDEX uq_passengers_public_id ON passengers (public_id);
