package pl.javastart.mockitomailing.model;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class UserMessageProviderTest {

    User user;
    List<Signup> signupList;
    UserMessageProvider userMessageProvider = new UserMessageProvider();
    LocalDate today = LocalDate.of(2017, 10, 10);
    Scanner scanner;

    @Before
    public void init() {
        user = new User("Jan", "Jan@gmail.com");
        signupList = new ArrayList<>();
    }

    @Test
    public void shouldContainUserNameInHeader() {
        Signup signupOne = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 10, 10), LocalDate.of(2017, 10, 11));

        String text = userMessageProvider.prepareMessage(user, signupOne, signupList, today);
        scanner = new Scanner(text);
        String header = scanner.nextLine();

        assertThat(header, containsString("Jan"));
        scanner.close();
    }

    @Test
    public void shouldSpellSuffixesCorrectly() {
        Signup signupOne = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 10, 10), LocalDate.of(2017, 10, 11));
        Signup signupTwo = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 10, 10), LocalDate.of(2017, 10, 13));
        Signup signupThree = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 10, 10), LocalDate.of(2017, 11, 10));
        Signup signupFour = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 10, 10), LocalDate.of(2018, 1, 10));
        Signup signupFive = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 10, 10), LocalDate.of(2018, 4, 10));

        String text = userMessageProvider.prepareMessage(user, signupOne, signupList, today);
        String textTwo = userMessageProvider.prepareMessage(user, signupTwo, signupList, today);
        String textThree = userMessageProvider.prepareMessage(user, signupThree, signupList, today);
        String textFour = userMessageProvider.prepareMessage(user, signupFour, signupList, today);
        String textFive = userMessageProvider.prepareMessage(user, signupFive, signupList, today);
        assertThat(text, containsString("dzień"));
        assertThat(textTwo, containsString("dni"));
        assertThat(textThree, containsString("miesiąc"));
        assertThat(textFour, containsString("miesiące"));
        assertThat(textFive, containsString("miesięcy"));
        System.out.println(textFour);
    }
    @Test
    public void shouldContainCorrectCourseName() {
        Signup signupOne = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 10, 11), LocalDate.of(2017, 10, 11));
        Signup signupTwo = new Signup("Jan", "Java Podstawy", LocalDate.of(2017, 10, 11), LocalDate.of(2017, 10, 11));

        String text = userMessageProvider.prepareMessage(user, signupOne, signupList, today);
        String textTwo = userMessageProvider.prepareMessage(user, signupTwo, signupList, today);
        assertThat(text, containsString("Kurs Spring"));
        assertThat(textTwo, containsString("Java Podstawy"));
    }

    @Test
    public void shouldReturnMessageWithoutCoursesWhenThereIsNoOtherCourses() {
        Signup signupOne = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 10, 11), LocalDate.of(2017, 10, 11));
        signupList.add(signupOne);

        String text = userMessageProvider.prepareMessage(user, signupOne, signupList, today);
        assertThat(text, not(containsString("Dostęp do Twoich pozostałych kursów")));
    }

    @Test
    public void shouldContainOtherCoursesWithRemainingTime() {
        Signup signupOne = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 10, 11), LocalDate.of(2017, 10, 11));
        Signup signupTwo = new Signup("Jan", "Java Podstawy", LocalDate.of(2017, 10, 11), LocalDate.of(2017, 10, 11));
        Signup signupThree = new Signup("Jan", "Android", LocalDate.of(2017, 10, 11), LocalDate.of(2017, 12, 21));
        signupList.add(signupOne);
        signupList.add(signupTwo);
        signupList.add(signupThree);

        String text = userMessageProvider.prepareMessage(user, signupOne, signupList, today);
        assertThat(text, containsString("Dostęp do Twoich pozostałych kursów:"));
        assertThat(text, containsString("Java Podstawy - 1 dzień"));
        assertThat(text, containsString("Android - 2 miesiące i 11 dni"));
    }

    @Test
    public void shouldContainAsciiDot() {
        Signup signupOne = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 10, 11), LocalDate.of(2017, 10, 11));
        Signup signupTwo = new Signup("Jan", "Java Podstawy", LocalDate.of(2017, 10, 11), LocalDate.of(2017, 10, 11));
        Signup signupThree = new Signup("Jan", "Android", LocalDate.of(2017, 10, 11), LocalDate.of(2017, 12, 21));
        signupList.add(signupOne);
        signupList.add(signupTwo);
        signupList.add(signupThree);

        String text = userMessageProvider.prepareMessage(user, signupOne, signupList, today);
        assertThat(text, containsString("Dostęp do Twoich pozostałych kursów:"));
        assertThat(text, containsString( (char) 8226 + " Java Podstawy - 1 dzień"));
        assertThat(text, containsString((char) 8226 + " Android - 2 miesiące i 11 dni"));
    }
}