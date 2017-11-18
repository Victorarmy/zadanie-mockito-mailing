package pl.javastart.mockitomailing.model;

import java.time.LocalDate;

public class Signup {

    private String user;
    private String course;
    private LocalDate accessFrom;
    private LocalDate accessTo;


    public Signup(String user, String course, LocalDate accessFrom, LocalDate accessTo) {
        this.user = user;
        this.course = course;
        this.accessFrom = accessFrom;
        this.accessTo = accessTo;
    }

    public LocalDate getAccessTo() {
        return accessTo;
    }

    public String getUser() {
        return user;
    }

    public String getCourse() {
        return course;
    }

    @Override
    public String toString() {
        return "Signup{" +
                "user='" + user + '\'' +
                ", course='" + course + '\'' +
                ", accessFrom=" + accessFrom +
                ", accessTo=" + accessTo +
                '}';
    }
}
