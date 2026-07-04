package AttractorSchool.exam.repository;


import AttractorSchool.exam.model.Patient;
import AttractorSchool.exam.service.Generator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class PatientRepository {
    private List<Patient> patients = new ArrayList<>();

    public PatientRepository() {
        LocalDate now = LocalDate.now().withDayOfMonth(1);
        int daysInMonth = now.lengthOfMonth();
        Random random = new Random();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDay = now.withDayOfMonth(day);
            String dateStr = currentDay.toString();

            int patientsCount = random.nextInt(6) + 5;

            for (int i = 0; i < patientsCount; i++) {
                int hour = random.nextInt(9) + 9;
                int minute = random.nextInt(4) * 15;
                String time = String.format("%02d:%02d", hour, minute);

                patients.add(new Patient(
                        time,
                        Generator.makeName(),
                        "1998-03-07",
                        (random.nextBoolean() ? "Первичный" : "Вторичный"),
                        Generator.makeDescription(),
                        dateStr
                ));
            }
        }
    }

    public List<Patient> findAll() {
        return patients;
    }

    public List<Patient> findByDate(String date) {
        return patients.stream()
                .filter(p -> p.getAppointmentDate().equals(date))
                .sorted(Comparator.comparing(Patient::getTime))
                .collect(Collectors.toList());
    }
}