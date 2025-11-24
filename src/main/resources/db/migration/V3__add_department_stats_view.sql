CREATE OR REPLACE VIEW department_stats AS
SELECT
    d.id,
    d.name,
    d.location,
    COUNT(e.id) AS employees,
    COALESCE(AVG(e.salary), 0) AS salary,
    COALESCE(AVG(EXTRACT(YEAR FROM AGE(CURRENT_DATE, e.birthday))), 0) AS age,
    COALESCE(AVG(EXTRACT(YEAR FROM AGE(CURRENT_DATE, e.hire_date))), 0) AS experience
FROM department d
         LEFT JOIN employee e ON d.id = e.department_id
GROUP BY d.id, d.name, d.location;
