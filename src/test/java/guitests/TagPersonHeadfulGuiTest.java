package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import guitests.guihandles.PersonCardHandle;
import guitests.guihandles.TagPersonDialogHandle;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for tagging persons that need to be run in headful mode
 *
 * Reason: Selection of multiple persons using click + shortcut + click + ... does not work in headless mode
 */
public class TagPersonHeadfulGuiTest extends GuiTestBase {
    @Override
    protected AddressBook getInitialData() {
        return td.book;
    }
    
    @Test
    public void tagMultiplePersonsAccelerator() {
        selectMultiplePersons(td.alice, td.benson, td.charlie);

        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandle.searchAndAcceptTags("frie");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        waitForGracePeriod("Tag: friends", alicePersonCard, bensonPersonCard, charliePersonCard);
    }

    @Test
    public void tagMultiplePersonsAccelerator_multipleTags() {
        selectMultiplePersons(td.alice, td.benson, td.charlie);
        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandle.searchAndAcceptTags("frie", "coll");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard, bensonPersonCard, charliePersonCard);
    }

    @Test
    public void tagMultiplePersonsAccelerator_multipleTagsAndCancelDuringGracePeriod() {
        selectMultiplePersons(td.alice, td.benson, td.charlie);
        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandle.searchAndAcceptTags("frie", "coll");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        cancelDuringGracePeriod("Tag: friends, Tag: colleagues", "", alicePersonCard, bensonPersonCard,
                                                                     charliePersonCard);
    }

    @Test
    public void tagAndUntagMultiplePersonsAccelerator() {
        selectMultiplePersons(td.alice, td.benson, td.charlie);
        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandle.searchAndAcceptTags("frie");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        waitForGracePeriod("Tag: friends", alicePersonCard, bensonPersonCard, charliePersonCard);

        assertSelectedCardHandles(alicePersonCard, bensonPersonCard, charliePersonCard);
        TagPersonDialogHandle multiplePersonsTagDialogHandleTwo = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandleTwo.searchAndAcceptTags("frie");
        multiplePersonsTagDialogHandleTwo.pressEnter();

        waitForGracePeriod("", alicePersonCard, bensonPersonCard, charliePersonCard);
    }

    @Test
    public void tagAndUntagMultiplePersonsAccelerator_multipleTags() {
        selectMultiplePersons(td.alice, td.benson, td.charlie);
        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandle.searchAndAcceptTags("frie", "coll");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard, bensonPersonCard, charliePersonCard);

        assertSelectedCardHandles(alicePersonCard, bensonPersonCard, charliePersonCard);
        TagPersonDialogHandle multiplePersonsTagDialogHandleTwo = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandleTwo.searchAndAcceptTags("frie");
        multiplePersonsTagDialogHandleTwo.pressEnter();

        waitForGracePeriod("Tag: colleagues", alicePersonCard, bensonPersonCard, charliePersonCard);
    }

    @Test
    public void tagAndUntagMultiplePersonsAccelerator_multipleTagsAndCancelDuringGracePeriod() {
        selectMultiplePersons(td.alice, td.benson, td.charlie);
        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandle.searchAndAcceptTags("frie", "coll");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard, bensonPersonCard, charliePersonCard);

        assertSelectedCardHandles(alicePersonCard, bensonPersonCard, charliePersonCard);
        TagPersonDialogHandle multiplePersonsTagDialogHandleTwo = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandleTwo.searchAndAcceptTags("frie");
        multiplePersonsTagDialogHandleTwo.pressEnter();

        cancelDuringGracePeriod("Tag: colleagues", "Tag: friends, Tag: colleagues", alicePersonCard, bensonPersonCard,
                                                                                    charliePersonCard);
    }

    /**
     * Attempts to select the cards of the given persons
     *
     * Note: May not work correctly if there are multiple persons of the same first name
     * @param persons
     */
    private void selectMultiplePersons(Person... persons) {
        List<Person> personList = Arrays.asList(persons);
        personListPanel.selectMultiplePersons(personList);

        List<PersonCardHandle> personCardHandleList = personList.stream()
                .map(personListPanel::getPersonCardHandle)
                .collect(Collectors.toCollection(ArrayList::new));
        assertTrue(personListPanel.getSelectedCards().containsAll(personCardHandleList));
    }

    //TODO: The following helper methods are copied from TagPersonGuiTest - extract them into util?
    private void assertSelectedCardHandles(PersonCardHandle... personCardHandles) {
        for (PersonCardHandle personCardHandle : personCardHandles) {
            assertTrue(personListPanel.getSelectedCards().contains(personCardHandle));
        }
    }

    private void assertTagsBeforeGracePeriod(String expectedTags, PersonCardHandle... personCardHandles) {
        for (PersonCardHandle personCardHandle : personCardHandles) {
            assertEquals(expectedTags, personCardHandle.getTags());
            assertTrue(personCardHandle.isShowingGracePeriod("Editing"));
        }
    }

    private void assertTagsAfterGracePeriod(String expectedTags, PersonCardHandle... personCardHandles) {
        for (PersonCardHandle personCardHandle : personCardHandles) {
            assertEquals(expectedTags, personCardHandle.getTags());
            assertFalse(personCardHandle.isShowingGracePeriod("Editing"));
        }
    }

    private void waitForGracePeriod(String expectedTags, PersonCardHandle... personCardHandles) {
        assertTagsBeforeGracePeriod(expectedTags, personCardHandles);
        sleepForGracePeriod();
        assertTagsAfterGracePeriod(expectedTags, personCardHandles);
    }

    private void cancelDuringGracePeriod(String expectedTagsBeforeCancel, String expectedTagsAfterCancel,
                                         PersonCardHandle... personCardHandles) {
        assertTagsBeforeGracePeriod(expectedTagsBeforeCancel, personCardHandles);
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertTagsAfterGracePeriod(expectedTagsAfterCancel, personCardHandles);
    }
}
