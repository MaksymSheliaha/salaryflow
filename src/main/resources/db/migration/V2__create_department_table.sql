CREATE TABLE "department" (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL
);

ALTER TABLE IF EXISTS employee
    ADD CONSTRAINT fk_employee_department
        FOREIGN KEY (department_id)
        REFERENCES department
        ON DELETE SET NULL;
