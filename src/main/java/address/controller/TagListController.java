package address.controller;

import address.model.datatypes.tag.Tag;
import address.model.ModelManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TagListController {
    Stage stage;
    MainController mainController;
    ModelManager modelManager;

    @FXML
    private AnchorPane mainPane;
    @FXML
    private ScrollPane tags;

    @FXML
    private void initialize() {
    }

    public void setStage(Stage stage) {
        stage.getScene().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) stage.close();
        });
        this.stage = stage;
    }

    public void setTags(ObservableList<Tag> tagList) {
        tags.setContent(getTagsVBox(tagList, mainController, modelManager));
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setModelManager(ModelManager modelManager) {
        this.modelManager = modelManager;
    }

    public VBox getTagsVBox(ObservableList<Tag> tagList, MainController mainController,
                            ModelManager modelManager) {
        VBox vBox = new VBox();

        if (tagList.size() == 0) {
            vBox.getChildren().add(TagCardController.getDummyTagCard(this, mainController, modelManager));
            return vBox;
        }
        tagList.stream()
                .forEach(tag -> {
                    TagCardController tagCardController = new TagCardController(tag, mainController, modelManager, this);
                    vBox.getChildren().add(tagCardController.getLayout());
                });
        return vBox;
    }

    public void refreshList() {
        setTags(modelManager.getTagsAsReadOnlyObservableList());
        stage.sizeToScene();
    }
}
