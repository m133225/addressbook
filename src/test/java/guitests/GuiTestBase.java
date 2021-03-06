package guitests;

import address.TestApp;
import address.events.EventManager;
import address.model.datatypes.AddressBook;
import address.sync.cloud.model.CloudAddressBook;
import address.testutil.TypicalTestData;
import address.testutil.TestUtil;
import guitests.guihandles.MainGuiHandle;
import guitests.guihandles.MainMenuHandle;
import guitests.guihandles.PersonListPanelHandle;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.testfx.api.FxToolkit;

import java.util.concurrent.TimeoutException;

public class GuiTestBase {

    TestApp testApp;
    GuiRobot guiRobot = new GuiRobot(); //TODO: remove this from here, only *Handle objects should use the robot

    /* Handles to GUI elements present at the start up are created in advance
     *   for easy access from child classes.
     */
    protected MainGuiHandle mainGui;
    protected MainMenuHandle mainMenu;
    protected PersonListPanelHandle personListPanel;
    protected TypicalTestData td = new TypicalTestData();


    @BeforeClass
    public static void setupSpec() {
        try {
            FxToolkit.registerPrimaryStage();
            FxToolkit.hideStage();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setup() throws Exception {
        FxToolkit.setupStage((stage) -> {
            mainGui = new MainGuiHandle(guiRobot, stage);
            mainMenu = mainGui.getMainMenu();
            personListPanel = mainGui.getPersonListPanel();
        });
        EventManager.clearSubscribers();
        testApp = (TestApp) FxToolkit.setupApplication(() -> new TestApp(this::getInitialData, getDataFileLocation(),
                                                     this::selectFromInitialCloudData));
        FxToolkit.showStage();
    }

    /**
     * Override this in child classes to set the initial local data.
     * Return null to use the data in the file specified in {@link #getDataFileLocation()}
     */
    protected AddressBook getInitialData() {
        return TestUtil.generateSampleAddressBook();
    }

    private CloudAddressBook selectFromInitialCloudData() {
        return getInitialCloudData() == null
            ?  TestUtil.generateCloudAddressBook(getInitialData())
            : getInitialCloudData();
    }

    /**
     * Override this in child classes to set the initial cloud data.
     * If not overridden, cloud data will be the same as local data.
     */
    protected CloudAddressBook getInitialCloudData() {
        return null;
    }

    /**
     * Override this in child classes to set the data file location.
     * @return
     */
    protected String getDataFileLocation() {
        return TestApp.SAVE_LOCATION_FOR_TESTING;
    }

    @After
    public void cleanup() throws TimeoutException {
        FxToolkit.cleanupStages();
        testApp.deregisterHotKeys();
    }


}
