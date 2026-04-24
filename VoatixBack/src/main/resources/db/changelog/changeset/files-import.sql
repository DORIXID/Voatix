--liquibase formatted sql

--changeset dorixid:insert_extended_files_data_v8 endDelimiter:GO

-- ==========================================================
-- 1. ГЕНЕРИРУЕМ 100 ФАЙЛОВ.
-- Колонки: id, name (1.jpg), bucket, content_type, uploader_id
-- ==========================================================
INSERT INTO public.files (id, name, bucket, content_type, uploader_id)
SELECT i,
       i || '.jpg',     -- Имя файла "1.jpg", "2.jpg" и т.д.
       'images',
       'image/jpeg',
       1                -- Привязываем к админу (ID=1)
FROM generate_series(1, 100) AS i;

-- Обновляем счетчик ID для таблицы файлов
SELECT setval(pg_get_serial_sequence('public.files', 'id'), (SELECT MAX(id) FROM public.files));

-- ==========================================================
-- 2. ПРИВЯЗКИ (Юзеры -> Аватары)
-- ==========================================================

-- Каждому юзеру (1-20) даем соответствующий файл (1-20) как аватар
UPDATE public.users u
SET avatar_id = i
FROM (SELECT generate_series(1, 20) AS i) s
WHERE u.id = s.i;

-- Аватарки проектов (Файлы 21-23 -> Проекты 1-3)
UPDATE public.projects SET avatar = 21 WHERE id = 1;
UPDATE public.projects SET avatar = 22 WHERE id = 2;
UPDATE public.projects SET avatar = 23 WHERE id = 3;

-- ==========================================================
-- 3. ПРИВЯЗКА ФАЙЛОВ К ОБЪЕКТАМ (Идеи и Комментарии)
-- ==========================================================

-- Привязка к Идеям (Файлы 24-60)
DO $$
    DECLARE
        f_id INT;
        target_idea_id BIGINT;
    BEGIN
        FOR f_id IN 24..60 LOOP
                SELECT id INTO target_idea_id FROM public.ideas ORDER BY random() LIMIT 1;
                IF target_idea_id IS NOT NULL THEN
                    UPDATE public.files SET idea_id = target_idea_id WHERE id = f_id;
                END IF;
            END LOOP;
    END $$;

-- Привязка к Комментариям (Файлы 61-100)
DO $$
    DECLARE
        f_id INT;
        target_comment_id BIGINT;
    BEGIN
        FOR f_id IN 61..100 LOOP
                SELECT id INTO target_comment_id FROM public.comments ORDER BY random() LIMIT 1;
                IF target_comment_id IS NOT NULL THEN
                    UPDATE public.files SET comment_id = target_comment_id WHERE id = f_id;
                END IF;
            END LOOP;
    END $$;

GO