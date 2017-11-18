package pl.javastart.mockitomailing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.javastart.mockitomailing.model.Signup;
import pl.javastart.mockitomailing.model.User;
import pl.javastart.mockitomailing.model.UserMessageProvider;
import pl.javastart.mockitomailing.util.DateProvider;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class NotifierSenderTest {

    @Mock
    Database database;

    @Mock
    MailSystem mailSystem;

    @Mock
    DateProvider dateProvider;

    @Mock
    UserMessageProvider messageProvider;

    @Mock
    EmailAddressChecker emailAddressChecker;

    NotifierSender notifierSender;
    List<User> users;
    List<Signup> signups;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        users = new ArrayList<>();
        signups = new ArrayList<>();

        User userOne = new User("Jan", "Jan@gmail.com");
        User userTwo = new User("Marian", "Marian@gmail.com");
        User userThree = new User("Kamil", "Kamil@onet.pl");
        users.add(userOne);
        users.add(userTwo);
        users.add(userThree);

        when(database.getAllUsers()).thenReturn(users);
        when(database.getAllSignups()).thenReturn(signups);

        when(dateProvider.getCurrentDate()).thenReturn(LocalDate.of(2017, 10, 10));

        when(messageProvider.prepareMessage(ArgumentMatchers.isA(User.class), any(), any(), any())).thenReturn(
                "Message");
        when(messageProvider.prepareTitle()).thenReturn("Title");

        when(emailAddressChecker.checkmail(anyString())).thenReturn(true);

        notifierSender = new NotifierSender(mailSystem, database, dateProvider, messageProvider, emailAddressChecker);
    }

    @Test
    public void shouldInvokeMethodSendEmail() {
        Signup appropriateSignup = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 10, 11));
        signups.add(appropriateSignup);

        notifierSender.prepareAndSendMails();
        verify(mailSystem, atLeastOnce()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void shouldInvokeMethodPrepareAndSendEmailWithUserEmailAddress() {

        Signup appropriateSignupOne = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 10, 11));
        Signup appropriateSignupTwo = new Signup("Marian", "Java Podstawy", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 11, 10));
        signups.add(appropriateSignupOne);
        signups.add(appropriateSignupTwo);

        ArgumentCaptor<String> email = ArgumentCaptor.forClass(String.class);

        notifierSender.prepareAndSendMails();
        verify(mailSystem, times(2)).sendEmail(email.capture(), anyString(), anyString());

        assertThat(email.getAllValues().get(0), is("Jan@gmail.com"));
        assertThat(email.getAllValues().get(1), is("Marian@gmail.com"));
    }

    @Test
    public void shouldInvokePrepareMessageMethod() {
        Signup appropriateSignup = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 10, 11));
        signups.add(appropriateSignup);

        notifierSender.prepareAndSendMails();
        verify(messageProvider, atLeastOnce()).prepareMessage(ArgumentMatchers.isA(User.class), any(), any(), any());
    }

    @Test
    public void shouldInvokePrepareTitleMethod() {
        Signup appropriateSignup = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 10, 11));
        signups.add(appropriateSignup);

        notifierSender.prepareAndSendMails();
        verify(messageProvider, atLeastOnce()).prepareTitle();
    }

    @Test
    public void shouldSendEmailsOnlyToAppropriateUsers() {
        Signup appropriateSignupOne = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 10, 11));
        Signup appropriateSignupTwo = new Signup("Marian", "Java Podstawy", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 11, 10));
        Signup inappropriateSignup = new Signup("Kamil", "Kurs Spring", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 12, 11));

        signups.add(appropriateSignupOne);
        signups.add(appropriateSignupTwo);
        signups.add(inappropriateSignup);

        notifierSender.prepareAndSendMails();

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailSystem, times(2)).sendEmail(emailCaptor.capture(), anyString(), anyString());
        assertThat(emailCaptor.getAllValues(), hasItems("Jan@gmail.com", "Marian@gmail.com"));
        assertThat(emailCaptor.getAllValues(), not(hasItems("Kamil@onet.pl")));
    }

    @Test
    public void shouldNotSEndEmailToUserWithoutOrWithWrongEmail() {
        Signup appropriateSignupOne = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 10, 11));
        Signup appropriateSignupTwo = new Signup("Marian", "Java Podstawy", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 11, 10));
        Signup appropriateSignupThree = new Signup("Kamil", "Kurs Spring", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 12, 11));
        signups.add(appropriateSignupOne);
        signups.add(appropriateSignupTwo);
        signups.add(appropriateSignupThree);

        when(emailAddressChecker.checkmail(anyString())).thenReturn(false);

        notifierSender.prepareAndSendMails();
        verifyZeroInteractions(mailSystem);
    }

    @Test
    public void shouldSendOnlyOneEmailToUserWithMoreThanOneApplicableSignups() {
        Signup appropriateSignupOne = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 10, 11));
        Signup appropriateSignupTwo = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 11, 10));
        signups.add(appropriateSignupOne);
        signups.add(appropriateSignupTwo);

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);

        notifierSender.prepareAndSendMails();
        verify(mailSystem, times(1)).sendEmail(emailCaptor.capture(), anyString(), anyString());
        assertThat(emailCaptor.getAllValues().size(), is(not(2)));
    }

    @Test
    public void shouldInvokePrepareMessageMethodWithAppropriateSignupArgAndSignupListArg() {
        Signup appropriateSignupOne = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 10, 11));
        Signup appropriateSignupTwo = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 11, 10));
        Signup inappropriateSignupButShouldBeInSignupListArg = new Signup("Jan", "Kurs Spring",
                LocalDate.of(2017, 8, 10), LocalDate.of(2017, 11, 12));
        Signup inappropriateSignupOne = new Signup("Marian", "Java Podstawy", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 12, 12));
        Signup inappropriateSignupTwo = new Signup("Kamil", "Kurs Spring", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 12, 12));

        signups.add(appropriateSignupOne);
        signups.add(appropriateSignupTwo);
        signups.add(inappropriateSignupOne);
        signups.add(inappropriateSignupTwo);
        signups.add(inappropriateSignupButShouldBeInSignupListArg);

        ArgumentCaptor<Signup> signupCaptor = ArgumentCaptor.forClass(Signup.class);
        ArgumentCaptor<List<Signup>> signupListCaptor = ArgumentCaptor.forClass(List.class);

        notifierSender.prepareAndSendMails();
        verify(messageProvider, times(1)).prepareMessage(ArgumentMatchers.isA(User.class), signupCaptor.capture(), signupListCaptor.capture(), any());

        assertThat(signupCaptor.getValue(), equalTo(appropriateSignupOne));
        assertThat(signupListCaptor.getValue(), hasItems(appropriateSignupOne, appropriateSignupTwo, inappropriateSignupButShouldBeInSignupListArg));
        assertThat(signupListCaptor.getAllValues().size(), is(1));
        assertThat(signupListCaptor.getValue().size(), is(3));
    }

    @Test
    public void shouldDoNothingWhenNoUsersInDatabase() {
        users.clear();
        notifierSender.prepareAndSendMails();
        assertThat(users.size(), is(0));
        verifyZeroInteractions(emailAddressChecker, messageProvider, mailSystem);
    }

    @Test
    public void shouldDoNothingWhenNoSignupsInDatabase() {
        notifierSender.prepareAndSendMails();
        assertThat(signups.size(), is(0));
        verifyZeroInteractions(emailAddressChecker, messageProvider, mailSystem);
    }

    @Test
    public void shouldDoNothingWhenNoAcceptableSignupsInDatabase() {
        Signup inappropriateSignupOne = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 12, 11));
        Signup inappropriateSignupTwo = new Signup("Marian", "Kurs Spring", LocalDate.of(2017, 8, 10),
                LocalDate.of(2017, 12, 13));
        signups.add(inappropriateSignupOne);
        signups.add(inappropriateSignupTwo);

        notifierSender.prepareAndSendMails();
        verifyZeroInteractions(emailAddressChecker, messageProvider, mailSystem);
    }

    @Test
    public void shouldWorkProperlyWithLeapYears() {
        when(dateProvider.getCurrentDate()).thenReturn(LocalDate.of(2020, 2, 29));

        Signup appropriateSignupOne = new Signup("Jan", "Kurs Spring", LocalDate.of(2017, 8, 10),
                LocalDate.of(2020, 3, 7));
        Signup appropriateSignupTwo = new Signup("Marian", "Kurs Spring", LocalDate.of(2017, 8, 10),
                LocalDate.of(2020, 3, 29));
        Signup inappropriateSignupOne = new Signup("Marian", "Kurs Spring", LocalDate.of(2017, 8, 10),
                LocalDate.of(2020, 3, 30));
        signups.add(appropriateSignupOne);
        signups.add(appropriateSignupTwo);
        signups.add(inappropriateSignupOne);

        notifierSender.prepareAndSendMails();
        verify(mailSystem, times(2)).sendEmail(any(), any(), any());
    }
}