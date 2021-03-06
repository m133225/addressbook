package address;

import address.model.UserPrefs;
import address.model.datatypes.ReadOnlyAddressBook;
import address.storage.StorageAddressBook;
import address.sync.RemoteManager;
import address.sync.cloud.CloudManipulator;
import address.sync.cloud.model.CloudAddressBook;
import address.util.Config;
import address.util.GuiSettings;
import address.testutil.TestUtil;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.util.function.Supplier;

/**
 * This class is meant to override some properties of MainApp so that it will be suited for
 * testing
 */
public class TestApp extends MainApp {

    public static final String SAVE_LOCATION_FOR_TESTING = TestUtil.appendToSandboxPath("sampleData.xml");
    protected static final String DEFAULT_CLOUD_LOCATION_FOR_TESTING = TestUtil.appendToSandboxPath("sampleCloudData.xml");
    protected static final String DEFAULT_PREF_FILE_LOCATION_FOR_TESTING = TestUtil.appendToSandboxPath("pref_testing.json");
    protected Supplier<ReadOnlyAddressBook> initialDataSupplier = () -> null;
    protected Supplier<CloudAddressBook> initialCloudDataSupplier = () -> null;
    protected String saveFileLocation = SAVE_LOCATION_FOR_TESTING;
    protected CloudManipulator remote;

    public TestApp() {
    }

    public TestApp(Supplier<ReadOnlyAddressBook> initialDataSupplier, String saveFileLocation,
                   Supplier<CloudAddressBook> initialCloudDataSupplier) {
        super();
        this.initialDataSupplier = initialDataSupplier;
        this.saveFileLocation = saveFileLocation;
        this.initialCloudDataSupplier = initialCloudDataSupplier;

        // If some initial local data has been provided, write those to the file
        if (initialDataSupplier.get() != null) {
            TestUtil.createDataFileWithData(
                    new StorageAddressBook(this.initialDataSupplier.get()),
                    this.saveFileLocation);
        }
    }

    @Override
    protected Config initConfig(String configFilePath) {
        Config config = super.initConfig(configFilePath);
        config.setAppTitle("Test App");
        config.setLocalDataFilePath(saveFileLocation);
        config.setPrefsFileLocation(new File(DEFAULT_PREF_FILE_LOCATION_FOR_TESTING));
        // Use default cloud test data if no data is supplied
        if (initialCloudDataSupplier.get() == null) config.setCloudDataFilePath(DEFAULT_CLOUD_LOCATION_FOR_TESTING);
        return config;
    }

    @Override
    protected UserPrefs initPrefs(Config config) {
        UserPrefs userPrefs = super.initPrefs(config);
        double x = Screen.getPrimary().getVisualBounds().getMinX();
        double y = Screen.getPrimary().getVisualBounds().getMinY();
        userPrefs.setGuiSettings(new GuiSettings(600.0, 600.0, (int) x, (int) y));
        return userPrefs;
    }

    @Override
    protected RemoteManager initRemoteManager(Config config) {
        if (initialCloudDataSupplier.get() == null) {
            remote = new CloudManipulator(config);
        } else {
            remote = new CloudManipulator(config, initialCloudDataSupplier.get());
        }
        return new RemoteManager(remote);
    }

    @Override
    public void start(Stage primaryStage) {
        ui.start(primaryStage);
        updater.start(getUpdateInformationNotifier(ui));
        storageManager.start();
        syncManager.start();
        remote.start(primaryStage);
    }

    public void deregisterHotKeys(){
        keyBindingsManager.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
