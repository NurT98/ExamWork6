package AttractorSchool.exam;

import AttractorSchool.exam.model.Patient;
import AttractorSchool.exam.repository.PatientRepository;
import AttractorSchool.server.BasicServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ExamServer extends BasicServer {
    private final PatientRepository repository = new PatientRepository();

    public ExamServer(String host, int port) throws IOException {
        super(host, port);
        registerGet("/", this::handlePatients);
        registerGet("/calendar", this::handleSchedule);
        registerGet("/patients", this::handlePatients);
        registerPost("/add-patient", this::handleAddPatient);
        registerPost("/delete-patient", this::handleDeletePatient);
        registerGet("/add-patient-page", this::handleAddPatientPage);
    }

    private void handlePatients(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.contains("date=")) {
            redirect303(exchange, "/calendar");
            return;
        }

        String date = query.split("&")[0].substring(5);

        Map<String, Object> data = Map.of(
                "date", date,
                "patients", repository.findByDate(date),
                "query", query
        );
        renderTemplate(exchange, "patients.ftlh", data);
    }

    public void handleSchedule(HttpExchange exchange) {
        LocalDate now = LocalDate.now().withDayOfMonth(1);
        int daysInMonth = now.lengthOfMonth();
        int offset = now.getDayOfWeek().getValue() - 1;

        List<Map<String, Object>> calendarDays = new ArrayList<>();

        for (int i = 0; i < offset; i++) calendarDays.add(Map.of("empty", true, "isToday", false));

        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = now.withDayOfMonth(i);
            calendarDays.add(Map.of(
                    "date", date.toString(),
                    "dayNumber", i,
                    "isToday", date.equals(LocalDate.now()),
                    "count", repository.findByDate(date.toString()).size()
            ));
        }

        renderTemplate(exchange, "calendar.ftlh", Map.of(
                "calendarDays", calendarDays,
                "currentMonth", now.getMonth().name()
        ));
    }

    private void handleAddPatient(HttpExchange exchange) {
        Map<String, String> params = parseQuery(getBody(exchange));
        String dateStr = params.get("date");

        String error = validatePatient(params);
        if (error != null) {
            redirect303(exchange, String.format("/patients?date=%s&error=%s", dateStr, error));
            return;
        }

        String fullName = params.get("lastName") + " " + params.get("firstName") + " " + params.get("patronymic");

        repository.add(new Patient(
                params.get("time"),
                fullName,
                params.get("birthDate"),
                params.get("type"),
                params.getOrDefault("symptoms", "Нет анамнеза"),
                dateStr
        ));

        redirect303(exchange, "/patients?date=" + dateStr);
    }

    private String validatePatient(Map<String, String> params) {
        if (!isValidBirthDate(params.get("birthDate"))) return "date";

        LocalDateTime appointment = LocalDateTime.of(
                LocalDate.parse(params.get("date")),
                LocalTime.parse(params.get("time"))
        );

        if (appointment.isBefore(LocalDateTime.now())) return "past";

        if (isTimeConflict(params.get("date"), params.get("time"))) return "interval";

        return null;
    }

    private boolean isTimeConflict(String date, String time) {
        LocalTime newTime = LocalTime.parse(time);
        return repository.findByDate(date).stream()
                .map(p -> LocalTime.parse(p.getTime()))
                .anyMatch(existingTime -> Math.abs(ChronoUnit.MINUTES.between(existingTime, newTime)) < 20);
    }

    private boolean isValidBirthDate(String dateStr) {
        try {
            LocalDate birthDate = LocalDate.parse(dateStr);
            LocalDate minDate = LocalDate.now().minusYears(90);
            LocalDate maxDate = LocalDate.now();
            return !birthDate.isBefore(minDate) && !birthDate.isAfter(maxDate);
        } catch (Exception e) {
            return false;
        }
    }

    private void handleDeletePatient(HttpExchange exchange) {
        Map<String, String> params = parseQuery(getBody(exchange));
        repository.removeByName(params.get("fullName"));
        redirect303(exchange, "/patients?date=" + params.get("date"));
    }

    private Map<String, String> parseQuery(String body) {
        if (body == null || body.isEmpty()) return Collections.emptyMap();

        Map<String, String> params = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                params.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }

    private void handleAddPatientPage(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        String date = (query != null && query.contains("date=")) ? query.split("&")[0].substring(5) : LocalDate.now().toString();
        renderTemplate(exchange, "add-patient.ftlh", Map.of(
                "date", date,
                "generate", new PatientGenerator()
        ));
    }

    public static class PatientGenerator {
        private static final List<String> WORDS = new ArrayList<>();
        private static final Random RANDOM = new Random();

        static {
            try {
                String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("data/lorem.txt")));
                String[] splitWords = content.split("\\W+");
                Collections.addAll(WORDS, splitWords);
            } catch (Exception e) {
                WORDS.add("Ошибочка");
            }
        }

        private String getRandomWord() {
            return WORDS.isEmpty() ? "..." : WORDS.get(RANDOM.nextInt(WORDS.size()));
        }

        public String getAnamnez() {
            return getRandomWord();
        }

        public String getName() {
            return getRandomWord();
        }

        public String getLastName() {
            return getRandomWord();
        }

        public String getPatronymic() {
            return getRandomWord();
        }
    }

}