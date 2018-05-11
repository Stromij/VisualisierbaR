package com.github.bachelorpraktikum.visualisierbar.view.graph;

import com.github.bachelorpraktikum.visualisierbar.model.Edge;
import com.github.bachelorpraktikum.visualisierbar.model.Element;
import com.github.bachelorpraktikum.visualisierbar.model.LogicalGroup;
import com.github.bachelorpraktikum.visualisierbar.model.Node;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.antlr.v4.runtime.misc.Triple;

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

        logicalGroup.setOnAction((ActionEvent lgEvent) -> {
            if (logicalGroup.getSelectionModel().getSelectedIndex() == 0) {
                javafx.scene.control.Dialog<Triple<String,LogicalGroup.Kind, String>> groupCreationDialog = new javafx.scene.control.Dialog<>();

                groupCreationDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);                           //Dialog for new Group creation
                ((javafx.scene.control.Button) groupCreationDialog.getDialogPane().lookupButton(ButtonType.APPLY)).setDefaultButton(true);
                groupCreationDialog.setTitle("Create new Group");
                groupCreationDialog.setHeaderText(null);
                GridPane gCgP= new GridPane();
                gCgP.setHgap(10);
                gCgP.setVgap(10);
                javafx.scene.control.TextField GroupCreationName = new javafx.scene.control.TextField();
                javafx.scene.control.TextField GroupDefaultValue = new javafx.scene.control.TextField();
                GroupCreationName.setMinWidth(250);
                GroupDefaultValue.setMinWidth(250);
                gCgP.add(GroupCreationName,0,0);
                gCgP.add(GroupDefaultValue,0,1);
                GroupCreationName.setPromptText("Name of Group");
                GroupDefaultValue.setVisible(false);
                ChoiceBox<LogicalGroup.Kind> groupKind= new ChoiceBox<>();
                groupKind.getItems().addAll(LogicalGroup.Kind.values());
                groupKind.setMinWidth(250);
                groupKind.setValue(LogicalGroup.Kind.DEFAULT);
                groupKind.getSelectionModel().selectedIndexProperty().addListener((observable, oldVal, newVal) -> {
                    LogicalGroup.Kind kind = groupKind.getItems().get(observable.getValue().intValue());
                    if(kind == LogicalGroup.Kind.SIGNAL)
                        {GroupDefaultValue.setPromptText("ActiveZugFolge (default = null)");
                         GroupDefaultValue.setVisible(true);
                        }
                    else if(kind == LogicalGroup.Kind.LIMITER)
                        {GroupDefaultValue.setPromptText("Limit (default = 44)");
                         GroupDefaultValue.setVisible(true);
                        }
                    else if(kind == LogicalGroup.Kind.SWITCH)
                        {GroupDefaultValue.setPromptText("Direction (default = false)");
                         GroupDefaultValue.setVisible(true);
                        }
                    else
                        {GroupDefaultValue.setVisible(false);}
                });

                gCgP.add(groupKind,0,2);
                groupCreationDialog.getDialogPane().setContent(gCgP);
                groupCreationDialog.setResultConverter((dialogButton) -> {
                    if (dialogButton == ButtonType.APPLY) {
                        return new Triple<>(GroupCreationName.getText(), groupKind.getValue(), GroupDefaultValue.getText());
                    } else
                        return null;
                });

                Optional<Triple<String,LogicalGroup.Kind,String>> result = groupCreationDialog.showAndWait();                                    // Actual Code for Creation of a new Group
                if (result.isPresent()) {
                    if (LogicalGroup.in(element.getNode().getGraph().getContext()).NameExists(result.get().a)) {

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

                    } else if(result.get().a.length() == 0)
                        {Alert alert = new Alert(Alert.AlertType.ERROR);
                         alert.setTitle("Error");
                         alert.setGraphic(null);
                         alert.setHeaderText(null);
                         alert.setContentText("Invalid Group-Name!");
                         alert.showAndWait();
                         if (element.getLogicalGroup() == null)
                            logicalGroup.getSelectionModel().select(1);//select noGroup string because element is in no group since name is taken
                         else
                            logicalGroup.setValue(element.getLogicalGroup().getName());
                        } else {
                        LogicalGroup newGroup = LogicalGroup.in(element.getNode().getGraph().getContext()).create(result.get().a, result.get().b);
                        if(result.get().c.length() > 0)
                            {newGroup.setAdditional(result.get().c);}
                        System.out.println(result.get().c);
                        System.out.println(newGroup.getAdditional());
                        if (element.getLogicalGroup() != null) {
                            element.getLogicalGroup().removeElement(element);
                        }
                        String errorLog = newGroup.safeAddElement(element);
                        if(errorLog == null) {
                            element.setLogicalGroup(newGroup);
                            logicalGroup.getItems().add(newGroup.getName());
                            logicalGroup.getSelectionModel().select(newGroup.getName());
                        }
                        else{
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setGraphic(null);
                            alert.setHeaderText(null);
                            alert.setContentText(errorLog);
                            alert.showAndWait();
                            System.out.println(" 1 " + element.getLogicalGroup().getName());
                            logicalGroup.setValue(element.getLogicalGroup().getName());
                        }
                    }
                } else {
                    if (element.getLogicalGroup() == null)
                        logicalGroup.getSelectionModel().select(1);
                    else
                        logicalGroup.getSelectionModel().select(element.getLogicalGroup().getName());
                }
            } else {                                                                                                                                //end of Group creation code
                LogicalGroup oldLG = element.getLogicalGroup();
                if (element.getLogicalGroup() != null) {                                                                                            //code for switching Group
                    element.getLogicalGroup().removeElement(element);
                }
                if(logicalGroup.getSelectionModel().getSelectedIndex()!=1)
                    {String errorLog = LogicalGroup.in(element.getNode().getGraph().getContext()).get(logicalGroup.getValue()).safeAddElement(element);
                     if(errorLog != null){
                         Alert alert = new Alert(Alert.AlertType.ERROR);
                         alert.setTitle("Error");
                         alert.setGraphic(null);
                         alert.setHeaderText(null);
                         alert.setContentText(errorLog);
                         alert.showAndWait();
                         element.setLogicalGroup(oldLG);
                         logicalGroup.setValue(element.getLogicalGroup().getName());
                        }
                    }
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
