--changeset dorixid:insert_extended_files_data_v2 endDelimiter:GO

-- ==========================================================
-- 1. ГЕНЕРИРУЕМ 100 ФАЙЛОВ С НОВОЙ СТРУКТУРОЙ
-- ==========================================================
INSERT INTO public.files (id, name, bucket, key, content_type, uploader_id)
SELECT
    i,
    'asset_' || i || (CASE WHEN i % 5 = 0 THEN '.pdf' ELSE '.png' END) as name,
    'voatix-bucket' as bucket,
    'storage/' ||
    CASE
        WHEN i <= 30 THEN 'avatars/'
        WHEN i <= 60 THEN 'ideas/'
        ELSE 'comments/'
        END || 'file_' || i || (CASE WHEN i % 5 = 0 THEN '.pdf' ELSE '.png' END) as key,
    CASE
        WHEN i % 5 = 0 THEN 'application/pdf'
        ELSE 'image/png'
        END as content_type,
    (SELECT id FROM public.users ORDER BY random() LIMIT 1) as uploader_id
FROM generate_series(1, 100) AS i;

-- Обновляем сиквенс файлов
SELECT setval(pg_get_serial_sequence('public.files', 'id'), 100);

-- ==========================================================
-- 2. АВАТАРКИ ПОЛЬЗОВАТЕЛЕЙ (Users)
-- ==========================================================
DO $$
    BEGIN
        FOR i IN 1..20 LOOP
                UPDATE public.users SET avatar_id = i WHERE id = i;
            END LOOP;
    END $$;

-- ==========================================================
-- 3. АВАТАРКИ ПРОЕКТОВ (Projects)
-- ==========================================================
UPDATE public.projects SET avatar = 21 WHERE id = 1;
UPDATE public.projects SET avatar = 22 WHERE id = 2;
UPDATE public.projects SET avatar = 23 WHERE id = 3;

-- ==========================================================
-- 4. ФАЙЛЫ ДЛЯ ИДЕЙ (Ideas)
-- ==========================================================
DO $$
    DECLARE
        f_id INT;
        i_id BIGINT;
    BEGIN
        FOR f_id IN 24..60 LOOP
                i_id := (SELECT id FROM public.ideas ORDER BY random() LIMIT 1);
                UPDATE public.files SET idea_id = i_id WHERE id = f_id;
            END LOOP;
    END $$;

-- ==========================================================
-- 5. ФАЙЛЫ ДЛЯ КОММЕНТАРИЕВ (Comments)
-- ==========================================================
DO $$
    DECLARE
        f_id INT;
        c_id BIGINT;
    BEGIN
        FOR f_id IN 61..100 LOOP
                c_id := (SELECT id FROM public.comments ORDER BY random() LIMIT 1);
                UPDATE public.files SET comment_id = c_id WHERE id = f_id;
            END LOOP;
    END $$;