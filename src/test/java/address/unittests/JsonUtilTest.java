package address.unittests;

import address.model.AddressBook;
import address.model.datatypes.Tag;
import address.model.datatypes.Person;
import address.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    @Test
    /**
     * Due to updatedAt field, we can check JSON string correct value. As such, we just confirm there is no exception
     */
    public void jsonUtil_getJsonStringObjectRepresentation_noExceptionThrown() throws JsonProcessingException {
        Tag sampleTag = new Tag("Tag");
        Person samplePerson = new Person("First", "Last");
        samplePerson.setCity("Singapore");
        samplePerson.setPostalCode("123456");
        List<Tag> tag = new ArrayList<>();
        tag.add(sampleTag);
        samplePerson.setTags(tag);
        samplePerson.setBirthday(LocalDate.of(1980, 3, 18));
        samplePerson.setGithubUserName("FirstLast");

        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(Arrays.asList(samplePerson));
        addressBook.setTags(Arrays.asList(sampleTag));

        JsonUtil.toJsonString(addressBook);
    }

    @Test
    public void jsonUtil_readJsonStringToObjectInstance_correctObject() throws IOException {
        String jsonString = "{\n" +
                "  \"persons\" : [ {\n" +
                "    \"firstName\" : \"First\",\n" +
                "    \"lastName\" : \"Last\",\n" +
                "    \"street\" : \"\",\n" +
                "    \"postalCode\" : \"123456\",\n" +
                "    \"city\" : \"Singapore\",\n" +
                "    \"githubUsername\" : \"FirstLast\",\n" +
                "    \"birthday\" : \"1980-03-18\",\n" +
                "    \"tags\" : [ {\n" +
                "      \"name\" : \"Tag\"\n" +
                "    } ],\n" +
                "    \"birthday\" : \"1980-03-18\"\n" +
                "  } ],\n" +
                "  \"tags\" : [ {\n" +
                "    \"name\" : \"Tag\"\n" +
                "  } ]\n" +
                "}";
        AddressBook addressBook = JsonUtil.fromJsonString(jsonString, AddressBook.class);
        assertEquals(1, addressBook.getPersons().size());
        assertEquals(1, addressBook.getTags().size());

        Person person = addressBook.getPersons().get(0);
        Tag tag = addressBook.getTags().get(0);

        assertEquals("Tag", tag.getName());

        assertEquals("First", person.getFirstName());
        assertEquals("Last", person.getLastName());
        assertEquals("Singapore", person.getCity());
        assertEquals("123456", person.getPostalCode());
        assertEquals(tag, person.getTags().get(0));
        assertEquals(LocalDate.of(1980, 3, 18), person.getBirthday());
        assertEquals("FirstLast", person.getGithubUserName());
    }

    @Test
    public void jsonUtil_writeThenReadObjectToJson_correctObject() throws IOException {
        // Write
        Tag sampleTag = new Tag("Tag");
        Person samplePerson = new Person("First", "Last");
        samplePerson.setCity("Singapore");
        samplePerson.setPostalCode("123456");
        List<Tag> tag = new ArrayList<>();
        tag.add(sampleTag);
        samplePerson.setTags(tag);
        samplePerson.setBirthday(LocalDate.of(1980, 3, 18));
        samplePerson.setGithubUserName("FirstLast");

        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(Arrays.asList(samplePerson));
        addressBook.setTags(Arrays.asList(sampleTag));

        String jsonString = JsonUtil.toJsonString(addressBook);

        // Read
        AddressBook addressBookRead = JsonUtil.fromJsonString(jsonString, AddressBook.class);
        assertEquals(1, addressBookRead.getPersons().size());
        assertEquals(1, addressBookRead.getTags().size());

        Person person = addressBookRead.getPersons().get(0);
        Tag tagRead = addressBookRead.getTags().get(0);

        assertEquals("Tag", tagRead.getName());

        assertEquals("First", person.getFirstName());
        assertEquals("Last", person.getLastName());
        assertEquals("Singapore", person.getCity());
        assertEquals("123456", person.getPostalCode());
        assertEquals(tagRead, person.getTags().get(0));
        assertEquals(LocalDate.of(1980, 3, 18), person.getBirthday());
        assertEquals("FirstLast", person.getGithubUserName());
    }
}
