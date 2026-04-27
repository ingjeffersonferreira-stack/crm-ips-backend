-- =========================================================
-- CRM IPS — Migración: Contactos, Acciones y Seguimientos
-- Ejecutar después de V2__clients.sql
-- =========================================================

-- =========================================================
-- ENUMS
-- =========================================================

DO $$ BEGIN
  CREATE TYPE followup_type AS ENUM ('CALL','EMAIL','WHATSAPP','VISIT','MEETING');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE followup_result AS ENUM ('INTERESTED','NEGOTIATION','PAUSED','NO_RESPONSE','WON','LOST');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE action_type AS ENUM ('CALL','SEND_EMAIL','SEND_PROPOSAL','FOLLOW_UP','MEETING');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE action_status AS ENUM ('PENDING','COMPLETED','CANCELLED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- =========================================================
-- CONTACTOS
-- =========================================================

CREATE TABLE IF NOT EXISTS contacts (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  client_id  UUID NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
  full_name  VARCHAR(180) NOT NULL,
  job_title  VARCHAR(140) NULL,
  email      VARCHAR(180) NULL,
  phone      VARCHAR(40)  NULL,
  is_primary BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_contacts_updated_at') THEN
    CREATE TRIGGER trg_contacts_updated_at
    BEFORE UPDATE ON contacts
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END $$;

CREATE INDEX  IF NOT EXISTS idx_contacts_client ON contacts (client_id);

-- Un solo contacto principal por cliente
CREATE UNIQUE INDEX IF NOT EXISTS ux_contacts_primary_per_client
  ON contacts (client_id)
  WHERE is_primary = TRUE;

-- =========================================================
-- ACCIONES (próximas tareas)
-- =========================================================

CREATE TABLE IF NOT EXISTS actions (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  client_id           UUID NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
  contact_id          UUID NULL      REFERENCES contacts(id) ON DELETE SET NULL,
  responsible_user_id UUID NOT NULL  REFERENCES users(id)   ON DELETE RESTRICT,
  title               VARCHAR(220)   NOT NULL,
  type                action_type    NOT NULL DEFAULT 'FOLLOW_UP',
  status              action_status  NOT NULL DEFAULT 'PENDING',
  scheduled_at        TIMESTAMPTZ    NOT NULL,
  completed_at        TIMESTAMPTZ    NULL,
  created_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
  updated_at          TIMESTAMPTZ    NOT NULL DEFAULT now()
);

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_actions_updated_at') THEN
    CREATE TRIGGER trg_actions_updated_at
    BEFORE UPDATE ON actions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_actions_responsible_status_date
  ON actions (responsible_user_id, status, scheduled_at);

CREATE INDEX IF NOT EXISTS idx_actions_client_date
  ON actions (client_id, scheduled_at);

-- =========================================================
-- SEGUIMIENTOS
-- =========================================================

CREATE TABLE IF NOT EXISTS followups (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  client_id      UUID NOT NULL REFERENCES clients(id)   ON DELETE CASCADE,
  contact_id     UUID NULL      REFERENCES contacts(id)  ON DELETE SET NULL,
  user_id        UUID NOT NULL  REFERENCES users(id)     ON DELETE RESTRICT,
  type           followup_type   NOT NULL,
  result         followup_result NOT NULL,
  notes          TEXT            NULL,
  event_at       TIMESTAMPTZ     NOT NULL DEFAULT now(),
  next_action_id UUID NULL       REFERENCES actions(id)  ON DELETE SET NULL,
  created_at     TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_followups_client_event_at
  ON followups (client_id, event_at DESC);

CREATE INDEX IF NOT EXISTS idx_followups_user_event_at
  ON followups (user_id, event_at DESC);

-- =========================================================
-- END
-- =========================================================
