CREATE TABLE absence (
                         id UUID PRIMARY KEY,
                         employee_id UUID NOT NULL,
                         type VARCHAR(50) NOT NULL,
                         start_date DATE NOT NULL,
                         end_date DATE NOT NULL,
                         comment TEXT,

                         CONSTRAINT fk_absence_employee
                             FOREIGN KEY (employee_id)
                                 REFERENCES employee(id)
                                 ON DELETE CASCADE
);

CREATE INDEX idx_absence_employee_id ON absence (employee_id);
CREATE INDEX idx_absence_dates ON absence (start_date, end_date);