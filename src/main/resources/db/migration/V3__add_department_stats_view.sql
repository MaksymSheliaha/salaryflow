CREATE OR REPLACE VIEW department_stats AS
SELECT
    d.id,
    d.name,
    d.location,
    COUNT(e) AS employees,
    AVG(e.salary) AS salary,
    AVG(EXTRACT(YEAR FROM AGE(CURRENT_DATE, e.birthday))) AS age,
    AVG(EXTRACT(YEAR FROM AGE(CURRENT_DATE, e.hire_date))) AS experience
FROM department d
         JOIN employee e ON e.department_id = d.id
GROUP BY d.id;
