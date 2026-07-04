package AttractorSchool.exam.model;

public class Patient {
    private String time;
    private String fullName;
    private String birthDate;
    private String type;
    private String symptoms;
    private String appointmentDate;

    public Patient(String time, String fullName, String birthDate, String type, String symptoms, String appointmentDate) {
        this.time = time;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.type = type;
        this.symptoms = symptoms;
        this.appointmentDate = appointmentDate;
    }

    public String getTime() {
        return time;
    }

    public String getFullName() {
        return fullName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getType() {
        return type;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }
}