package address.model.datatypes;

import java.time.LocalDate;
import java.util.List;

/**
 * Defines all collection and value based accessors for the Person domain object.
 */
public interface ReadablePerson {

    String getFirstName();
    String getLastName();
    /**
     * @return first-last format full name
     */
    String fullName();

    String getGithubUserName();
    /**
     * @return github profile url
     */
    String profilePageUrl();

    String getStreet();
    String getPostalCode();
    String getCity();

    LocalDate getBirthday();
    /**
     * @return birthday date-formatted as string
     */
    String birthdayString();

    /**
     * @see ObservablePerson#getTags()
     * @return a List view of this Person's tags
     */
    List<Tag> getTags();
    /**
     * @return string representation of this Person's tags
     */
    String tagsString();
}