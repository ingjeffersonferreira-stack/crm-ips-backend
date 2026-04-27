-- =========================================================
-- CRM IPS — Migración: Módulo de Clientes
-- Aplicar sobre la BD existente con las tablas users/roles ya creadas
-- =========================================================

-- ENUM: estado comercial del cliente
DO $$ BEGIN
  CREATE TYPE client_commercial_status AS ENUM (
    'NEW', 'INTERESTED', 'NEGOTIATION', 'PAUSED', 'WON', 'LOST'
  );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- TABLA: clients
CREATE TABLE IF NOT EXISTS clients (
  id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  business_name      VARCHAR(220) NOT NULL,
  nit                VARCHAR(40)  NULL,
  main_email         VARCHAR(180) NULL,
  main_phone         VARCHAR(40)  NULL,
  address            VARCHAR(240) NULL,
  city               VARCHAR(120) NULL,
  commercial_status  client_commercial_status NOT NULL DEFAULT 'NEW',
  owner_user_id      UUID NULL REFERENCES users(id) ON DELETE SET NULL,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- TRIGGER: auto-update updated_at
-- (requiere que set_updated_at() ya exista — se crea en el script base)
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'trg_clients_updated_at'
  ) THEN
    CREATE TRIGGER trg_clients_updated_at
    BEFORE UPDATE ON clients
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END $$;

-- ÍNDICES
CREATE INDEX IF NOT EXISTS idx_clients_nit            ON clients (nit);
CREATE INDEX IF NOT EXISTS idx_clients_owner_status   ON clients (owner_user_id, commercial_status);
CREATE INDEX IF NOT EXISTS idx_clients_business_name  ON clients (business_name);
