package pl.javastart.mockitomailing.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class UserMessageProvider {

    public String prepareMessage(User user, Signup signup, List<Signup> userSignups, LocalDate today) {
        if (userSignups.size() == 1) {
            return prepareSingleCourseMessage(user, signup, today);
        } else {
            return prepareMultiCourseMessage(user, signup, userSignups, today);
        }
    }

    private String prepareMultiCourseMessage(User user, Signup signup, List<Signup> userSignups, LocalDate today) {
        return prepareHeader(user) + prepareBody(today, signup) + prepareOtherCoursesTimeRemaining(signup, userSignups,
                today) + prepareFooter();
    }

    private String prepareSingleCourseMessage(User user, Signup signup, LocalDate today) {
        return prepareHeader(user) + prepareBody(today, signup) + prepareFooter();
    }

    public String prepareTitle() {
        return "";
    }

    private String prepareFooter() {
        return "\nPozdrawiamy";
    }

    private String prepareBody(LocalDate today, Signup signup) {
        return "za " + timeRemaining(today, signup) +
                " kończy Ci się dostęp do kursu, " + signup.getCourse() +
                ". Wykoszystaj maksymalnie ten czas!";
    }

    private String prepareOtherCoursesTimeRemaining(Signup signup, List<Signup> userSignups, LocalDate today) {
        return "\nDostęp do Twoich pozostałych kursów: \n" + listOtherCourses(signup, userSignups, today);
    }

    private String listOtherCourses(Signup signup, List<Signup> userSignups, LocalDate today) {
        return userSignups
                .stream()
                .filter(signupInStream -> !signupInStream.equals(signup))
                .map(signupInStream -> (char) 8226 + " " + signupInStream.getCourse() + " - " + timeRemaining(today, signupInStream))
                .collect(Collectors.joining("\n"));
    }

    private String timeRemaining(LocalDate today, Signup signup) {
        LocalDate localDate = LocalDate.of(signup.getAccessTo().getYear(), signup.getAccessTo().getMonth(), signup.getAccessTo().getDayOfMonth());
        String result = "";

        long monthsDifference = ChronoUnit.MONTHS.between(today, localDate);
        if (monthsDifference != 0) {
            result += monthsDifference;
            result += getRightMonthSpelling(monthsDifference);
            localDate = localDate.minus(monthsDifference, ChronoUnit.MONTHS);
        }

        long daysDifference = ChronoUnit.DAYS.between(today, localDate);
        if (daysDifference != 0) {
            if (!result.isEmpty()) {
                result += " i ";
            }
            result += daysDifference;
            result += getRightDaySpelling(daysDifference);
        }
        return result;
    }

    private String getRightDaySpelling(long daysDifference) {
        if (daysDifference == 1) {
            return " dzień";
        } else {
            return " dni";
        }
    }

    private String getRightMonthSpelling(long monthsDifference) {

        if (monthsDifference == 1) {
            return " miesiąc";
        } else if (monthsDifference > 1 && monthsDifference < 5) {
            return " miesiące";
        } else {
            return " miesięcy";
        }
    }


    private String prepareHeader(User user) {
        return "Cześć " + user.getName() + ",\n";
    }
}
