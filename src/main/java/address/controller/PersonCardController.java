package address.controller;

import java.io.IOException;
import java.util.Optional;


import address.image.ImageManager;
import address.model.datatypes.person.ReadOnlyViewablePerson;

import commons.FxViewUtil;
import javafx.animation.FadeTransition;
import javafx.application.Platform;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class PersonCardController extends UiController{

    private enum PendingState {
        COUNTING_DOWN, SYNCING, SYNCING_DONE
    }

    public static final String DELETING_PENDING_STATE_MESSAGE = "Deleted";
    public static final String EDITING_PENDING_STATE_MESSAGE = "Edited";
    public static final String CREATED_PENDING_STATE_MESSAGE = "Created";

    @FXML
    private HBox cardPane;
    @FXML
    private ImageView profileImage;
    @FXML
    private Label firstName;
    @FXML
    private Label lastName;
    @FXML
    private Label address;
    @FXML
    private Label birthday;
    @FXML
    private Label tags;
    @FXML
    private Label pendingStateLabel;
    @FXML
    private ProgressIndicator syncIndicator;
    @FXML
    private HBox pendingStateHolder;
    @FXML
    private Label pendingCountdownIndicator;

    private ReadOnlyViewablePerson person;
    private FadeTransition deleteTransition;
    private StringProperty idTooltipString = new SimpleStringProperty("");

    {
        deleteTransition = new FadeTransition(Duration.millis(1000), cardPane);
        deleteTransition.setFromValue(1.0);
        deleteTransition.setToValue(0.1);
        deleteTransition.setCycleCount(1);
    }

    public PersonCardController(ReadOnlyViewablePerson person) {
        this.person = person;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/PersonListCard.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void initialize() {

        if (person.getGithubUsername().length() > 0) {
            setProfileImage();
        }

        FxViewUtil.configureCircularImageView(profileImage);

        initIdTooltip();
        firstName.textProperty().bind(person.firstNameProperty());
        lastName.textProperty().bind(person.lastNameProperty());

        address.textProperty().bind(new StringBinding(){
            {
                bind(person.streetProperty());
                bind(person.postalCodeProperty());
                bind(person.cityProperty());
            }
            @Override
            protected String computeValue() {
                return getAddressString(person.getStreet(), person.getCity(), person.getPostalCode());
            }

        });
        birthday.textProperty().bind(new StringBinding() {
            {
                bind(person.birthdayProperty()); //Bind property at instance initializer
            }

            @Override
            protected String computeValue() {
                if (person.birthdayString().length() > 0){
                    return "DOB: " + person.birthdayString();
                }
                return "";
            }
        });
        tags.textProperty().bind(new StringBinding() {
            {
                bind(person.getObservableTagList()); //Bind property at instance initializer
            }

            @Override
            protected String computeValue() {
                return person.tagsString();
            }
        });

        person.githubUsernameProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0) {
                setProfileImage();
            }
        });
        if (person.getSecondsLeftInPendingState() > 0) {
            setPendingStateMessage(person.getChangeInProgress());
            pendingStateHolder.setVisible(true);
            pendingCountdownIndicator.setVisible(true);
        }
        person.secondsLeftInPendingStateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() > 0) {
                setPendingStateMessage(person.getChangeInProgress());
                setVisibilitySettings(PendingState.COUNTING_DOWN);
            } else {
                cardPane.setStyle(null);
                setPendingStateMessage(person.getChangeInProgress());
                setVisibilitySettings(PendingState.SYNCING);
                person.onRemoteIdConfirmed((Integer id) -> {
                    setVisibilitySettings(PendingState.SYNCING_DONE);
                    pendingStateLabel.setText("");
                });
            }
        });
        pendingCountdownIndicator.textProperty().bind(person.secondsLeftInPendingStateProperty().asString());
    }

    private void setVisibilitySettings(PendingState state) {
        switch (state) {
            case COUNTING_DOWN:
                pendingStateHolder.setVisible(true);
                pendingCountdownIndicator.setVisible(true);
                break;
            case SYNCING:
                pendingStateHolder.setVisible(true);
                pendingCountdownIndicator.setVisible(false);
                syncIndicator.setVisible(true);
                break;
            case SYNCING_DONE:
                syncIndicator.setVisible(false);
                pendingStateHolder.setVisible(false);
                break;
        }
    }

    private void setPendingStateMessage(ReadOnlyViewablePerson.ChangeInProgress changeInProgress) {


        if (changeInProgress == ReadOnlyViewablePerson.ChangeInProgress.ADDING) {
            pendingStateLabel.setText(CREATED_PENDING_STATE_MESSAGE);
        } else if (changeInProgress == ReadOnlyViewablePerson.ChangeInProgress.EDITING) {
            pendingStateLabel.setText(EDITING_PENDING_STATE_MESSAGE);
        } else if (changeInProgress == ReadOnlyViewablePerson.ChangeInProgress.DELETING) {
            pendingStateLabel.setText(DELETING_PENDING_STATE_MESSAGE);
        }
    }

    private void initIdTooltip() {
        Tooltip tp = new Tooltip();
        tp.textProperty().bind(idTooltipString);
        firstName.setTooltip(tp);
        lastName.setTooltip(tp);
        idTooltipString.set(person.idString());
        person.onRemoteIdConfirmed(id -> idTooltipString.set(person.idString()));
    }

    /**
     * Asynchronously sets the profile image to the image view.
     * Involves making an internet connection with the image hosting server.
     */
    private void setProfileImage() {
        final Optional<String> profileImageUrl = person.githubProfilePicUrl();
        if (profileImageUrl.isPresent()){
            new Thread(() -> {
                Image image = ImageManager.getInstance().getImage(profileImageUrl.get());
                if (image != null && image.getHeight() > 0) {
                    profileImage.setImage(image);
                } else {
                    profileImage.setImage(ImageManager.getDefaultProfileImage());
                }
            }).start();
        }
    }

    public void handleDelete() {
        Platform.runLater(() -> deleteTransition.play());
    }

    public HBox getLayout() {
        return cardPane;
    }


    public static String getAddressString(String street, String city, String postalCode) {
        StringBuilder sb = new StringBuilder();
        if (street.length() > 0){
            sb.append(street).append(System.lineSeparator());
        }
        if (city.length() > 0){
            sb.append(city).append(System.lineSeparator());
        }
        if (postalCode.length() > 0){
            sb.append(postalCode);
        }
        return sb.toString();
    }
}
