package com.github.bachelorpraktikum.dbvisualization.view;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

public final class TooltipUtil {
    private TooltipUtil() {
    }

    public static void install(Node node, Tooltip tooltip) {
        node.setOnMouseEntered(event -> {
            Bounds bounds = node.localToScreen(node.getBoundsInLocal());
            tooltip.show(node, bounds.getMinX(), bounds.getMaxY());
        });

        node.setOnMouseExited(event -> tooltip.hide());
    }
}
