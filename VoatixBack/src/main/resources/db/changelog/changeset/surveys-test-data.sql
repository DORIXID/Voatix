--liquibase formatted sql

--changeset dorixid:insert_surveys_and_votes_with_users endDelimiter:GO

-- ==========================================================
-- 1. ЗАПОЛНЕНИЕ ТАБЛИЦЫ SURVEYS (теперь с user_id)
-- ==========================================================
INSERT INTO public.surveys (id, project_id, user_id, title, description, start_date, end_date, type)
VALUES
-- Опросы для CoffeeWay (Project 1). Автор: manager1 (ID 2) или admin1 (ID 1)
(1, 1, 2, 'Новый сорт месяца', 'Какое зерно вы бы хотели видеть в альтернативе в марте?', '2026-02-01 10:00:00', '2030-03-01 10:00:00', 'RADIO_BUTTON'),
(2, 1, 2, 'Летнее меню', 'Какой освежающий напиток добавить первым?', '2026-02-01 12:00:00', '2026-02-15 12:00:00', 'CHECKBOX'),
(3, 1, 3, 'Мероприятия', 'В какое время вам удобнее посещать каппинги?', '2026-02-02 09:00:00', '2026-02-28 18:00:00', 'RADIO_BUTTON'),

-- Опрос для EcoOffice (Project 2). Автор: startup_sam (ID 5)
(4, 2, 5, 'Раздельный сбор', 'Нужны ли дополнительные баки для электроники?', '2026-02-01 08:00:00', '2026-03-01 08:00:00', 'RADIO_BUTTON');

-- Обновляем сиквенс
SELECT setval(pg_get_serial_sequence('public.surveys', 'id'), 4);

-- ==========================================================
-- 2. ЗАПОЛНЕНИЕ ТАБЛИЦЫ VOTINGPOINTS (Варианты ответов)
-- ==========================================================
-- (Остается без изменений, так как связь идет через survey_id)
INSERT INTO public.votingpoints (id, survey_id, title)
VALUES
    (1, 1, 'Эфиопия Иргачефф (Цветочный профиль)'),
    (2, 1, 'Бразилия Серрадо (Орехово-шоколадный)'),
    (3, 1, 'Кения АА (Ягодная кислотность)'),
    (4, 2, 'Эспрессо-Тоник Розмарин'),
    (5, 2, 'Бамбл-кофе с карамелью'),
    (6, 2, 'Колд Брю на кокосовом молоке'),
    (7, 3, 'Суббота 11:00'),
    (8, 3, 'Воскресенье 16:00'),
    (9, 3, 'Будни после 19:00'),
    (10, 4, 'Да, очень нужны'),
    (11, 4, 'Нет, достаточно обычных'),
    (12, 4, 'Мне все равно');

SELECT setval(pg_get_serial_sequence('public.votingpoints', 'id'), 12);

-- ==========================================================
-- 3. ГЕНЕРАЦИЯ ГОЛОСОВ (PointEstimates)
-- ==========================================================
DO
$$
    DECLARE
        survey_rec RECORD;
        point_id_val BIGINT;
        user_id_val BIGINT;
        users_count INT := 20;
    BEGIN
        FOR survey_rec IN SELECT id FROM public.surveys
            LOOP
                FOR user_id_val IN 1..users_count
                    LOOP
                        IF random() > 0.2 THEN
                            point_id_val := (
                                SELECT id
                                FROM public.votingpoints
                                WHERE survey_id = survey_rec.id
                                ORDER BY random()
                                LIMIT 1
                            );

                            INSERT INTO public.pointestimates (point_id, user_id)
                            VALUES (point_id_val, user_id_val)
                            ON CONFLICT DO NOTHING;
                        END IF;
                    END LOOP;
            END LOOP;
    END
$$;

GO