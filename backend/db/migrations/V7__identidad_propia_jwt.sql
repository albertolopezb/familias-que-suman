-- V7 · La identidad vuelve a la API: inicio de sesión propio con JWT (sin Auth0).
--
--   1. La contraseña de cada cuenta vuelve a la base, pero SOLO su hash Argon2id (nunca en claro).
--   2. Los permisos de administración viven aquí y viajan como claims dentro del JWT.
--   3. "Tokens válidos desde": para invalidar todas las sesiones de una cuenta de un jalón
--      (al cambiar su contraseña o sus permisos), aunque los tokens viejos no hayan vencido.
--   4. Se va el vínculo con Auth0.
--
-- Regla de siempre: este archivo no se edita una vez en main. Un cambio nuevo va en V8.

-- ─── 1. Contraseña (hash) ────────────────────────────────────────────────────
-- Argon2id con sal al azar por cuenta: aunque se robaran la tabla, no se puede "deshacer" el
-- hash y probar contraseñas cuesta memoria y tiempo. Vacío = la cuenta todavía no tiene
-- contraseña (las que venían de Auth0): no puede entrar hasta que se le asigne una.
-- (chr(36) es "$": así Flyway no confunde "$argon2id$" con un bloque $…$ de PostgreSQL.)
ALTER TABLE cuentas
    ADD COLUMN contrasena_hash text
        CHECK (contrasena_hash IS NULL OR starts_with(contrasena_hash, chr(36) || 'argon2id' || chr(36)));

-- ─── 2. Permisos ─────────────────────────────────────────────────────────────
-- Solo los cuatro que existen. Una cuenta sin permisos es una familia.
ALTER TABLE cuentas
    ADD COLUMN permisos text[] NOT NULL DEFAULT '{}'
        CHECK (permisos <@ ARRAY['administrar:catalogo', 'moderar:contenido', 'registrar:asistencias', 'atender:privacidad']::text[]);

-- ─── 3. Tokens válidos desde ─────────────────────────────────────────────────
ALTER TABLE cuentas ADD COLUMN tokens_validos_desde timestamptz NOT NULL DEFAULT now();

-- ─── 4. Adiós Auth0 ──────────────────────────────────────────────────────────
DROP INDEX IF EXISTS cuentas_auth0_sub_unico;
ALTER TABLE cuentas DROP COLUMN auth0_sub;
