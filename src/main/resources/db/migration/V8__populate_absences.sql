WITH emp AS (
    SELECT array_agg(id) AS ids FROM employee
)
INSERT INTO absence (id, employee_id, type, start_date, end_date, comment)
SELECT
    uuid_generate_v4(),
    emp.ids[floor(random() * array_length(emp.ids, 1) + 1)],
    -- ВИПРАВЛЕНО ТУТ: множимо на 3, бо у масиві 3 елементи
    (ARRAY['VACATION', 'SICK_LEAVE', 'DAY_OFF'])[floor(random() * 3 + 1)],
    raw_data.start_d,
    raw_data.start_d + (floor(random() * 14) + 1)::int,
    'Auto-generated absence record #' || i
FROM
    generate_series(1, 40) AS i, -- Можете повернути 4000, якщо треба більше даних
    emp,
    LATERAL (
        SELECT (CURRENT_DATE - (floor(random() * 730))::int) AS start_d
        ) AS raw_data;