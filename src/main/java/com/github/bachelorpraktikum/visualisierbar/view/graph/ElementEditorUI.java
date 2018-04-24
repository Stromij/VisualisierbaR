package com.github.bachelorpraktikum.visualisierbar.view.graph;

import com.github.bachelorpraktikum.visualisierbar.model.Edge;
import com.github.bachelorpraktikum.visualisierbar.model.Element;
import com.github.bachelorpraktikum.visualisierbar.model.LogicalGroup;
import com.github.bachelorpraktikum.visualisierbar.model.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Optional;

public class ElementEditorUI extends GridPane{

    private TextField eName;
    private ChoiceBox<String> direction;

    ElementEditorUI(Element element){
        super();
        this.setHgap(10);
        this.setVgap(5);
        eName= new TextField(element.higherName());
        direction= new ChoiceBox<>();
        for (Edge e : element.getNode().getEdges()) {
            Node otherNode = e.getOtherNode(element.getNode());
            String output = otherNode.getAbsName() == null ? otherNode.getName() : otherNode.getAbsName().concat(" | ").concat(otherNode.getName());
            direction.getItems().addAll(output);  //set direction choice box Items
        }
        if(element.getDirection()!=null){
           Node node = element.getDirection();
            String output = node.getAbsName() == null ? node.getName() : node.getAbsName().concat(" | ").concat(node.getName());

            direction.getSelectionModel().select(output);
        }
        ChoiceBox<String> logicalGroup = new ChoiceBox<>();                                                             //logical Group choice box
        logicalGroup.getItems().add("new Group");                                                                       //0 is newGroup
        logicalGroup.getItems().add("no Group");                                                                        //1 is no Group
        LogicalGroup.in(element.getNode().getGraph().getContext()).getAll().forEach((a) -> {                            //add all existing groups as options
            logicalGroup.getItems().add(a.getName());
        });
        if (element.getLogicalGroup() == null)
            logicalGroup.getSelectionModel().select(1);                                                             //select no Group as default
        else
            logicalGroup.getSelectionModel().select(element.getLogicalGroup().getName());
        direction.setOnAction(directionEvent -> {                                                                       //when Direction changes redraw element to indicate direction

            String NodeName = direction.getValue();
            Node otherNode = Node.in(element.getNode().getGraph().getContext()).get(NodeName);
            element.setDirection(otherNode);
            element.getNode().getGraph().rebuildComposite(element.getNode());

        });

        logicalGroup.setOnAction(lgEvent -> {
            if (logicalGroup.getSelectionModel().getSelectedIndex() == 0) {
                javafx.scene.control.Dialog<Pair<String,LogicalGroup.Kind>> groupCreationDialog = new javafx.scene.control.Dialog<>();

                groupCreationDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);                           //Dialog for new Group creation
                ((javafx.scene.control.Button) groupCreationDialog.getDialogPane().lookupButton(ButtonType.APPLY)).setDefaultButton(true);
                groupCreationDialog.setTitle("Create new Group");
                groupCreationDialog.setHeaderText(null);
                GridPane gCgP= new GridPane();
                gCgP.setHgap(10);
                gCgP.setVgap(10);
                javafx.scene.control.TextField GroupCreationName = new javafx.scene.control.TextField();
                gCgP.add(GroupCreationName,0,0);
                ChoiceBox<LogicalGroup.Kind> groupKind= new ChoiceBox<>();
                groupKind.getItems().addAll(LogicalGroup.Kind.values());
                groupKind.setValue(LogicalGroup.Kind.DEFAULT);
                gCgP.add(groupKind,0,1);
                groupCreationDialog.getDialogPane().setContent(gCgP);
                groupCreationDialog.setResultConverter((dialogButton) -> {
                    if (dialogButton == ButtonType.APPLY) {
                        return new Pair<>(GroupCreationName.getText(), groupKind.getValue());
                    } else
                        return null;
                });

                Optional<Pair<String,LogicalGroup.Kind>> result = groupCreationDialog.showAndWait();                                    // Actual Code for Creation of a new Group
                if (result.isPresent()) {
                    if (LogicalGroup.in(element.getNode().getGraph().getContext()).NameExists(result.get().getKey())) {

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setGraphic(null);
                        alert.setHeaderText(null);
                        alert.setContentText("Group already exists");
                        alert.showAndWait();
                        if (element.getLogicalGroup() == null)
                            logicalGroup.getSelectionModel().select(1);//select noGroup string because element is in no group since name is taken
                        else
                            logicalGroup.setValue(element.getLogicalGroup().getName());

                    } else {
                        LogicalGroup newGroup = LogicalGroup.in(element.getNode().getGraph().getContext()).create(result.get().getKey(), result.get().getValue());
                        if (element.getLogicalGroup() != null) {
                            element.getLogicalGroup().removeElement(element);
                        }
                        newGroup.addElement(element);
                        element.setLogicalGroup(newGroup);
                        logicalGroup.getItems().add(newGroup.getName());
                        logicalGroup.getSelectionModel().select(newGroup.getName());
                    }
                } else {
                    if (element.getLogicalGroup() == null)
                        logicalGroup.getSelectionModel().select(1);
                    else
                        logicalGroup.getSelectionModel().select(element.getLogicalGroup().getName());
                }
            } else {                                                                                                                                //end of Group creation code
                if (element.getLogicalGroup() != null) {                                                                                            //code for switching Group
                    element.getLogicalGroup().removeElement(element);
                }
                if(logicalGroup.getSelectionModel().getSelectedIndex()!=1)
                    LogicalGroup.in(element.getNode().getGraph().getContext()).get(logicalGroup.getValue()).addElement(element);
            }
        });

        this.add(eName,0,0);
        Label type = new Label(element.getType().getName());
        type.setMinWidth(200);
        this.add(type,1,0);
        this.add(logicalGroup,2,0);
        if (element.getType().isComposite()) this.add(direction, 3, 0);
    }
    public String getName(){
        return eName.getText();
    }

}
