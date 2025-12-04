CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

INSERT INTO department (id, name, location)
SELECT uuid_generate_v4(),
       'Department ' || to_char(i, 'FM000'),
       'Location ' || to_char(((i - 1) % 10) + 1, 'FM00')
FROM generate_series(1, 100) AS s(i);

WITH dep AS (
    SELECT array_agg(id) AS ids FROM department
)
INSERT INTO employee (
    id, first_name, last_name, gender, email, salary,
    hire_date, birthday, position, department_id
)
SELECT
    uuid_generate_v4(),                                                     -- ✔ id

    (ARRAY['John','Alice','Mark','Eve','Robert','Sophie','Daniel','Linda',
     'Michael','Julia','Chris','Emma','David','Olivia','Nick','Sara']
        )[floor(random()*16 + 1)],                                          -- first_name

    (ARRAY['Smith','Johnson','Williams','Brown','Jones','Garcia','Miller',
            'Davis','Rodriguez','Martinez','Hernandez','Lopez','Gonzalez',
            'Wilson','Anderson','Thomas'])[floor(random()*16 + 1)],         -- last_name

    (ARRAY['MALE','FEMALE'])[floor(random()*2 + 1)],                        -- gender

    'user' || gs || '_' || floor(random()*10000) || '@example.com',         -- email

    round((3000 + random()*7000)::numeric, 2),                               -- salary

    (DATE '2015-01-01' + (random()*3650)::int)::timestamp,                   -- hire_date

    (DATE '1960-01-01' + (random()*20000)::int)::timestamp,                  -- birthday

    (ARRAY['EMPLOYEE', 'MANAGER', 'JUNIOR_DEVELOPER', 'SENIOR_DEVELOPER', 'TEAM_LEAD', 'DIRECTOR'])[floor(random()*6+1)], -- position

    dep.ids[floor(random() * array_length(dep.ids, 1) + 1)]                  -- department_id
FROM generate_series(1, 2000) AS gs,
    dep;
