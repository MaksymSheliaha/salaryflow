package com.msk.salaryflow.configuration;

import com.msk.salaryflow.entity.Absence;
import com.msk.salaryflow.entity.AbsenceType;
import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.repository.AbsenceRepository;
import com.msk.salaryflow.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final AbsenceRepository absenceRepository;

    @Override
    public void run(String... args) throws Exception {
        // Перевіряємо, чи пуста база MongoDB, щоб не дублювати дані при кожному запуску
        if (absenceRepository.count() > 0) {
            System.out.println(">>> MongoDB already has data. Skipping initialization.");
            return;
        }

        System.out.println(">>> Generating test data for MongoDB (Absences)...");

        // 1. Дістаємо всіх працівників з Postgres (щоб прив'язати відпустки до реальних ID)
        List<Employee> employees = employeeRepository.findAll();

        if (employees.isEmpty()) {
            System.out.println(">>> No employees found in PostgreSQL. Cannot generate absences.");
            return;
        }

        Random random = new Random();
        AbsenceType[] types = AbsenceType.values();

        // 2. Генеруємо по 2-5 записів для кожного працівника (або для перших 50)
        int count = 0;
        for (Employee emp : employees) {
            // Для прикладу беремо тільки перших 50 працівників, щоб не спамити занадто багато
            if (count++ > 50) break;

            int absencesToGenerate = random.nextInt(4) + 1; // від 1 до 4 записів

            for (int i = 0; i < absencesToGenerate; i++) {
                Absence absence = new Absence();
                absence.setId(UUID.randomUUID());
                absence.setEmployeeId(emp.getId().toString()); // ВАЖЛИВО: беремо реальний ID з Postgres

                // Випадковий тип
                absence.setType(types[random.nextInt(types.length)]);

                // Випадкова дата старту (десь у 2024-2025 роках)
                int randomDay = random.nextInt(365);
                LocalDate start = LocalDate.of(2024, 1, 1).plusDays(randomDay);
                absence.setStartDate(start);

                // Дата кінця (через 1-14 днів)
                absence.setEndDate(start.plusDays(random.nextInt(14) + 1));

                // Коментар
                absence.setComment("Auto-generated test absence #" + random.nextInt(1000));

                absenceRepository.save(absence);
            }
        }

        System.out.println(">>> Successfully generated absences for existing employees!");
    }
}