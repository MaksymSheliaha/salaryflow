-- 1. Hibernate автоматично створив порожню ТАБЛИЦЮ 'department_stats',
-- бо побачив клас @Entity. Нам треба її видалити, щоб створити VIEW.
DROP TABLE IF EXISTS department_stats CASCADE;

-- 2. Також на всяк випадок видаляємо View, якщо вона була
DROP VIEW IF EXISTS department_stats CASCADE;

-- 3. Тепер створюємо VIEW
CREATE VIEW department_stats AS
SELECT
    d.id,
    d.name,
    d.location,
    COUNT(e.id) AS employees,
    COALESCE(AVG(e.salary), 0) AS salary,
    0.0 AS age,
    0.0 AS experience
FROM department d
         LEFT JOIN employee e ON d.id = e.department_id
GROUP BY d.id, d.name, d.location;