CREATE TABLE "employee"(
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    gender VARCHAR(20),
    email VARCHAR(255) UNIQUE,
    salary DOUBLE PRECISION NOT NULL ,
    hire_date TIMESTAMP NOT NULL ,
    birthday TIMESTAMP,
    position VARCHAR(50),
    department_id UUID
)