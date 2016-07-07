package address.sync.cloud;

import address.exceptions.DataConversionException;
import address.sync.cloud.model.CloudAddressBook;
import address.util.AppLogger;
import address.util.LoggerManager;
import address.util.XmlUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CloudFileHandler {
    private static final AppLogger logger = LoggerManager.getLogger(CloudFileHandler.class);
    private static final String CLOUD_DIRECTORY = "cloud/";

    public CloudAddressBook readCloudAddressBookFromFile(String cloudDataFilePath) throws FileNotFoundException,
            DataConversionException {
        File cloudFile = new File(cloudDataFilePath);
        try {
            logger.debug("Reading from cloud file '{}'.", cloudFile.getName());
            CloudAddressBook CloudAddressBook = XmlUtil.getDataFromFile(cloudFile, CloudAddressBook.class);
            if (CloudAddressBook.getName() == null) throw new DataConversionException("AddressBook name is null.");
            return CloudAddressBook;
        } catch (FileNotFoundException e) {
            logger.warn("Cloud file '{}' not found.", cloudFile.getName());
            throw e;
        } catch (DataConversionException e) {
            logger.warn("Error reading from cloud file '{}'.", cloudFile.getName());
            throw e;
        }
    }

    public CloudAddressBook readCloudAddressBookFromCloudFile(String addressBookName) throws FileNotFoundException,
            DataConversionException {
        File cloudFile = getCloudDataFile(addressBookName);
        try {
            logger.debug("Reading from cloud file '{}'.", cloudFile.getName());
            CloudAddressBook CloudAddressBook = XmlUtil.getDataFromFile(cloudFile, CloudAddressBook.class);
            if (CloudAddressBook.getName() == null) throw new DataConversionException("AddressBook name is null.");
            return CloudAddressBook;
        } catch (FileNotFoundException e) {
            logger.warn("Cloud file '{}' not found.", cloudFile.getName());
            throw e;
        } catch (DataConversionException e) {
            logger.warn("Error reading from cloud file '{}'.", cloudFile.getName());
            throw e;
        }
    }

    public void writeCloudAddressBookToCloudFile(CloudAddressBook CloudAddressBook) throws FileNotFoundException,
            DataConversionException {
        String addressBookName = CloudAddressBook.getName();
        File cloudFile = getCloudDataFile(addressBookName);
        try {
            logger.info("Writing to cloud file '{}'.", cloudFile.getName());
            XmlUtil.saveDataToFile(cloudFile, CloudAddressBook);
        } catch (FileNotFoundException | DataConversionException e) {
            logger.warn("Error writing to cloud file '{}'.", cloudFile.getName());
            throw e;
        }
    }

    /**
     * Attempts to create a file with an empty address book
     * Deletes any existing file on the same path
     *
     * @param addressBookName
     * @throws IOException
     * @throws DataConversionException
     */
    public void initializeAddressBook(String addressBookName) throws IOException, DataConversionException {
        File cloudFile = getCloudDataFile(addressBookName);
        if (cloudFile.exists()) {
            cloudFile.delete();
        }

        createCloudFile(new CloudAddressBook(addressBookName));
    }

    /**
     * Attempts to create an empty address book on the cloud
     * Fails if address book already exists
     *
     * @param addressBookName
     * @throws IOException
     * @throws DataConversionException
     * @throws IllegalArgumentException if cloud file already exists
     */
    public void createAddressBook(String addressBookName) throws IOException, DataConversionException,
            IllegalArgumentException {
        createCloudFile(new CloudAddressBook(addressBookName));
    }

    /**
     * Attempts to create the cloud file in the cloud directory, containing an empty address book
     * File will be named the same as the address book
     *
     * The cloud directory will also be created if it does not exist
     *
     * @param cloudAddressBook
     * @throws IOException
     * @throws DataConversionException
     * @throws IllegalArgumentException if cloud file already exists
     */
    private void createCloudFile(CloudAddressBook cloudAddressBook) throws IOException, DataConversionException, IllegalArgumentException {
        File cloudFile = getCloudDataFile(cloudAddressBook.getName());
        if (cloudFile.exists()) {
            logger.warn("Cannot create an address book that already exists: '{}'.", cloudAddressBook.getName());
            throw new IllegalArgumentException("AddressBook '" + cloudAddressBook.getName() + "' already exists!");
        }


        File cloudDirectory = new File(CLOUD_DIRECTORY);
        if (!cloudDirectory.exists() && !cloudDirectory.mkdir()) {
            logger.warn("Error creating directory: '{}'", CLOUD_DIRECTORY);
            throw new IOException("Error creating directory: " + CLOUD_DIRECTORY);
        }


        if (!cloudFile.createNewFile()) {
            logger.warn("Error creating cloud file: '{}'", getCloudDataFilePath(cloudAddressBook.getName()));
            throw new IOException("Error creating cloud file for address book: " + getCloudDataFilePath(cloudAddressBook.getName()));
        }

        writeCloudAddressBookToCloudFile(cloudAddressBook);
    }

    private File getCloudDataFile(String addressBookName) {
        return new File(getCloudDataFilePath(addressBookName));
    }

    private String getCloudDataFilePath(String addressBookName) {
        return CLOUD_DIRECTORY + addressBookName;
    }
}
