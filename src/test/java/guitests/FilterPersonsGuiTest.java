package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

public class FilterPersonsGuiTest extends GuiTestBase {
    private Person john, mary, annabelle, barney, charlie, danny;
    private Tag colleagues, friends, family;

    @Override
    public AddressBook getInitialData() {
        john = new Person("John", "Tan", 1);
        mary = new Person("Mary", "Jones", 2);
        annabelle = new Person("Annabelle", "Muellers", 3);
        barney = new Person("Barney", "Rogers", 4);
        charlie = new Person("Charlie", "Chip", 5);
        danny = new Person("Danny", "Rogers", 6);
        colleagues = new Tag("colleagues");
        friends = new Tag("friends");
        family = new Tag("family");

        john.setTags(Arrays.asList(colleagues, friends));
        john.setCity("Singapore");
        john.setGithubUsername("john123");
        john.setBirthday(LocalDate.of(1995, 10, 10));
        john.setStreet("Beach Road");

        mary.setTags(Collections.singletonList(friends));
        mary.setCity("California");
        mary.setBirthday(LocalDate.of(1960, 9, 20));

        annabelle.setTags(Collections.singletonList(friends));
        annabelle.setCity("Hawaii");
        annabelle.setBirthday(LocalDate.of(1998, 1, 30));

        barney.setCity("California");

        return new AddressBook(Arrays.asList(john, mary, annabelle, barney, charlie, danny),
                               Arrays.asList(colleagues, family, friends));

    }

    @Test
    public void filterPersons_singleQualifier() {
        personListPanel.enterFilterAndApply("tag:colleagues");
        assertTrue(personListPanel.isExactList(john));
    }

    @Test
    public void filterPersons_multipleQualifiers() {
        personListPanel.enterFilterAndApply("tag:friends city:california");
        assertTrue(personListPanel.isExactList(mary));
    }
}
