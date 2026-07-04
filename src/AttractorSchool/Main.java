package AttractorSchool;

import AttractorSchool.exam.ExamServer;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            new ExamServer("localhost", 8990).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
