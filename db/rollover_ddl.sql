-- ============================================================================
--  DDL para habilitar el "rollover" de temporadas (tocho5)
--  Ejecutar en pgAdmin 4 sobre la base de datos del proyecto (schema: public)
--
--  Hace dos cosas:
--   1) Vuelve season.season_id AUTOINCREMENTAL (secuencia), respetando los IDs
--      que ya existen (la secuencia arranca en MAX(season_id)+1).
--   2) Crea un UNIQUE (season_id, category_id, team_id) en standing_team para
--      que el sembrado de posiciones sea idempotente (ON CONFLICT DO NOTHING).
--
--  Corre TODO el archivo de una vez (está envuelto en una transacción).
-- ============================================================================

BEGIN;

-- ----------------------------------------------------------------------------
-- 0) CHEQUEO PREVIO (opcional pero recomendado)
--    Si esta consulta devuelve filas, hay standings duplicados y el paso 2
--    fallará. Revísalos/límpialos antes de crear el índice UNIQUE.
--    (Puedes correrla aparte primero; aquí queda como referencia comentada.)
-- ----------------------------------------------------------------------------
-- SELECT season_id, category_id, team_id, COUNT(*)
-- FROM public.standing_team
-- GROUP BY season_id, category_id, team_id
-- HAVING COUNT(*) > 1;


-- ----------------------------------------------------------------------------
-- 1) season.season_id AUTOINCREMENTAL
-- ----------------------------------------------------------------------------

-- 1.a) Crear la secuencia (si no existe) ligada a la columna season.season_id
CREATE SEQUENCE IF NOT EXISTS public.season_season_id_seq
    OWNED BY public.season.season_id;

-- 1.b) Poner la secuencia en el valor correcto segun los datos actuales.
--      - Si ya hay temporadas: is_called = true  -> el proximo id sera MAX+1.
--      - Si la tabla esta vacia: is_called = false -> el proximo id sera 1.
SELECT setval(
    'public.season_season_id_seq',
    (SELECT COALESCE(MAX(season_id), 1) FROM public.season),
    (SELECT COUNT(*) > 0 FROM public.season)
);

-- 1.c) Hacer que los INSERT sin season_id tomen el valor de la secuencia
ALTER TABLE public.season
    ALTER COLUMN season_id SET DEFAULT nextval('public.season_season_id_seq');


-- ----------------------------------------------------------------------------
-- 2) UNIQUE en standing_team para idempotencia del sembrado
-- ----------------------------------------------------------------------------
CREATE UNIQUE INDEX IF NOT EXISTS ux_standing_team_season_cat_team
    ON public.standing_team (season_id, category_id, team_id);


COMMIT;

-- ============================================================================
--  Verificacion rapida (correr despues, fuera de la transaccion):
--
--  -- La columna debe mostrar default = nextval('public.season_season_id_seq'::regclass)
--  SELECT column_name, column_default
--  FROM information_schema.columns
--  WHERE table_schema='public' AND table_name='season' AND column_name='season_id';
--
--  -- El indice unique debe aparecer
--  SELECT indexname FROM pg_indexes
--  WHERE schemaname='public' AND tablename='standing_team'
--    AND indexname='ux_standing_team_season_cat_team';
-- ============================================================================
