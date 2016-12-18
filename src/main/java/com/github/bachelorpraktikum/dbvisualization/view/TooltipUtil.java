package com.github.bachelorpraktikum.dbvisualization.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public final class TooltipUtil {
    private TooltipUtil() {
    }

    public static void install(Node node, Tooltip tooltip) {
        Timeline waitForHide = new Timeline(new KeyFrame(
                Duration.millis(300),
                ae -> tooltip.hide()
        ));

        node.setOnMouseEntered(event -> {
            waitForHide.stop();
            Bounds bounds = node.localToScreen(node.getBoundsInLocal());
            tooltip.show(node, bounds.getMinX(), bounds.getMaxY());
        });

        node.setOnMouseExited(event -> {
            waitForHide.play();
        });
    }
}
