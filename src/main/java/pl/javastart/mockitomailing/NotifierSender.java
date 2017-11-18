package pl.javastart.mockitomailing;

import pl.javastart.mockitomailing.model.Signup;
import pl.javastart.mockitomailing.model.User;
import pl.javastart.mockitomailing.model.UserMessageProvider;
import pl.javastart.mockitomailing.util.DateProvider;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class NotifierSender {

    private MailSystem mailSystem;
    private Database database;
    private DateProvider dateProvider;
    private UserMessageProvider messageProvider;
    private EmailAddressChecker emailAddressChecker;

    public NotifierSender(MailSystem mailSystem, Database database, DateProvider dateProvider, UserMessageProvider messageProvider, EmailAddressChecker emailAddressChecker) {
        this.mailSystem = mailSystem;
        this.database = database;
        this.dateProvider = dateProvider;
        this.messageProvider = messageProvider;
        this.emailAddressChecker = emailAddressChecker;
    }

    public void prepareAndSendMails() {
        LocalDate today = dateProvider.getCurrentDate();
        List<User> allUsers = database.getAllUsers();
        List<Signup> signups = database.getAllSignups();

        if (!signups.isEmpty()) {

            Map<String, Signup> usersNameWithAcceptableSignup = getUsersWithAcceptableSignup(today, signups);

            if (!usersNameWithAcceptableSignup.isEmpty()) {
                Map<String, List<Signup>> usersWithAllSignups = getUserWithAllSignups(signups,
                        usersNameWithAcceptableSignup);

                allUsers
                        .stream()
                        .filter(user -> emailAddressChecker.checkmail(user.getEmail())
                                && usersNameWithAcceptableSignup.containsKey(user.getName()))
                        .forEach(user -> mailSystem.sendEmail(user.getEmail(),
                                messageProvider.prepareTitle(),
                                messageProvider.prepareMessage(user, usersNameWithAcceptableSignup.get(user.getName()),
                                        usersWithAllSignups.get(user.getName()),
                                        today)));
            }
        }
    }
    private Map<String, List<Signup>> getUserWithAllSignups(List<Signup> signups, Map<String, Signup> usersNameAndAcceptableSignup) {
        return signups
                .stream()
                .filter(signup -> usersNameAndAcceptableSignup.containsKey(signup.getUser()))
                .collect(Collectors.groupingBy(Signup::getUser));
    }

    private Map<String, Signup> getUsersWithAcceptableSignup(LocalDate today, List<Signup> signups) {
        return signups
                .stream()
                .filter(signup -> isAcceptableSignup(today, signup))
                .collect(Collectors.toMap(Signup::getUser, signup -> signup,
                        (duplicateOne, duplicateTwo) -> duplicateOne));
    }

    private boolean isAcceptableSignup(LocalDate today, Signup signup) {
        long monthsDifference = ChronoUnit.MONTHS.between(today, signup.getAccessTo());
        long daysDifference = ChronoUnit.DAYS.between(today, signup.getAccessTo());

        if (monthsDifference == 3 || monthsDifference == 1) {
            return true;
        } else if (daysDifference == 7 || daysDifference == 1) {
            return true;
        }
        return false;
    }
}
