--changeset dorixid:insert_extended_files_data_v6 endDelimiter:GO

-- ==========================================================
-- 1. ГЕНЕРИРУЕМ 100 ФАЙЛОВ В ФОРМАТЕ testImage(i).jpg
-- ==========================================================
INSERT INTO public.files (id, name, bucket, key, content_type, uploader_id)
SELECT
    i,
    -- Имя для отображения: testImage(1).jpg
    'testImage(' || i || ').jpg' as name,

    -- Твой бакет
    'images' as bucket,

    -- КЛЮЧ: строго соответствует твоему формату без пробела
    'testImage(' || i || ').jpg' as key,

    -- Тип контента (все JPEG)
    'image/jpeg' as content_type,

    -- Рандомный загрузчик из базы
    (SELECT id FROM public.users ORDER BY random() LIMIT 1) as uploader_id
FROM generate_series(1, 100) AS i;

-- Обновляем счетчик ID
SELECT setval(pg_get_serial_sequence('public.files', 'id'), (SELECT MAX(id) FROM public.files));

-- ==========================================================
-- 2. ПРИВЯЗКИ (Остаются как были, по ID)
-- ==========================================================

-- Аватарки пользователей (1-20)
DO $$
    BEGIN
        FOR i IN 1..20 LOOP
                UPDATE public.users SET avatar_id = i WHERE id = i;
            END LOOP;
    END $$;

-- Аватарки проектов (21-23)
UPDATE public.projects SET avatar = 21 WHERE id = 1;
UPDATE public.projects SET avatar = 22 WHERE id = 2;
UPDATE public.projects SET avatar = 23 WHERE id = 3;

-- Привязка к Идеям (24-60)
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

-- Привязка к Комментариям (61-100)
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