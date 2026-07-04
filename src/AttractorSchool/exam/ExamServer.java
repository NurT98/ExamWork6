package AttractorSchool.exam;

import com.sun.net.httpserver.HttpExchange;

import AttractorSchool.exam.model.Patient;
import AttractorSchool.exam.repository.PatientRepository;
import java.io.IOException;
import AttractorSchool.server.BasicServer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExamServer extends BasicServer {
    private final PatientRepository repository = new PatientRepository();

    public ExamServer(String host, int port) throws IOException {
        super(host, port);
        registerGet("/", this::handlePatients);
        registerGet("/calendar", this::handleSchedule);
        registerGet("/patients", this::handlePatients);
    }

    private void handlePatients(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();

        if (query == null || !query.startsWith("date=")) {
            redirect303(exchange, "/calendar");
            return;
        }

        String date = query.substring(5);
        List<Patient> patients = repository.findByDate(date);

        Map<String, Object> data = Map.of(
                "date", date,
                "patients", patients
        );

        renderTemplate(exchange, "patients.ftlh", data);
    }

    public void handleSchedule(HttpExchange exchange) {
        LocalDate now = LocalDate.now().withDayOfMonth(1);
        int dayOfWeek = now.getDayOfWeek().getValue();

        List<Map<String, Object>> calendarDays = new ArrayList<>();

        for (int i = 1; i < dayOfWeek; i++) {
            calendarDays.add(Map.of("empty", true, "isToday", false));
        }

        for (int i = 1; i <= now.lengthOfMonth(); i++) {
            LocalDate date = now.withDayOfMonth(i);
            LocalDate today = LocalDate.now();
            calendarDays.add(Map.of(
                    "date", date.toString(),
                    "dayNumber", i,
                    "isToday", date.equals(today),
                    "count", repository.findByDate(date.toString()).size()
            ));
        }

        Map<String, Object> data = Map.of("calendarDays", calendarDays, "currentMonth", now.getMonth().name());
        renderTemplate(exchange, "calendar.ftlh", data);
    }

}
