package com.github.bachelorpraktikum.dbvisualization.view;

import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public final class ContextMenuUtil {

    private ContextMenuUtil() {
    }

    public static void attach(Node node, List<MenuItem> menuItems) {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(menuItems);
        node.setOnContextMenuRequested(event -> {
            contextMenu.show(node, event.getScreenX(), event.getScreenY());
        });
    }
}