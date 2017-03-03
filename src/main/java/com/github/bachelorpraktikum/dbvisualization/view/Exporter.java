package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.view.graph.Graph;
import com.github.bachelorpraktikum.dbvisualization.view.graph.GraphShape;
import com.github.bachelorpraktikum.dbvisualization.view.graph.adapter.CoordinatesAdapter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Transform;
import javax.imageio.ImageIO;

public class Exporter {

    private static final double pixelScale = 2.0;

    public static void exportTrainDetail(LineChart<Double, Double> chart, File file) {
        String fileType = file.getPath().substring(file.getPath().length() - 3);

        if (fileType.equals("dat")) {
            exportTrainDetailAsGNU(chart.getData().get(0).getData(), file);
        } else {
            exportTrainDetailAsImage(chart, file, fileType);
        }
    }

    public static void exportGraph(Graph graph, File file) {
        String fileType = file.getPath().substring(file.getPath().length() - 3);

        if (fileType.equals("dat")) {
            exportGraphAsGNU(graph, file);
        } else {
            exportGraphAsImage(graph, file, fileType);
        }
    }

    /**
     * Exports a snapshot of the main rail graph as a PNG or JPG Image.
     *
     * @param graph Graph object that will be used
     * @param file Export file
     * @param fileType Which file type to use
     */
    private static void exportGraphAsImage(Graph graph, File file, String fileType) {
        try {
            Bounds localBounds = graph.getGroup().getBoundsInLocal();
            Bounds screenBounds = graph.getGroup().localToScreen(localBounds);

            // aim for a 3000x2500 pixel image
            double scaleX = 3000 / screenBounds.getWidth();
            double scaleY = 2500 / screenBounds.getHeight();
            double snapScale = (scaleX > scaleY) ? scaleX : scaleY;
            SnapshotParameters snp = new SnapshotParameters();
            snp.setTransform(Transform.scale(snapScale, snapScale));
            WritableImage image = graph.getGroup().snapshot(snp, null);

            ImageIO.write(SwingFXUtils.fromFXImage(image, null), fileType, file);
        } catch (IOException e) {
        }
    }

    /**
     * Exports the main rail graph as a gnuplot file.
     * The gnuplot file contains the coordinates of one node per line
     * and two consecutive lines form an edge.
     *
     * Example usage of an output file could be:
     * plot 'filename' using 1:2 with lines lc rgb "black" notitle, \
     * 'filename' using 1:2:(0.1) with circles fill solid lc rgb "black" notitle
     *
     * @param graph Graph object that will be used
     * @param file Export file
     */
    private static void exportGraphAsGNU(Graph graph, File file) {
        try {
            //TODO: export node symbols maybe?
            FileWriter fileWriter = new FileWriter(file);
            CoordinatesAdapter coordinatesAdapter = graph.getCoordinatesAdapter();

            for (Map.Entry<Edge, GraphShape<Edge>> entry : graph.getEdges().entrySet()) {
                Point2D p1 = coordinatesAdapter.apply(entry.getKey().getNode1());
                Point2D p2 = coordinatesAdapter.apply(entry.getKey().getNode2());
                fileWriter.write(String.format("%f, %f\r\n", p1.getX(), -p1.getY()));
                fileWriter.write(String.format("%f, %f\r\n", p2.getX(), -p2.getY()));
                fileWriter.write("\r\n");
            }

            fileWriter.close();
        } catch (IOException e) {
        }
    }

    /**
     * Exports a generic LineChart as a PNG or JPG Image.
     *
     * @param chart Chart that will be used
     * @param file Export file
     * @param fileType Which file type to use
     */
    private static void exportTrainDetailAsImage(LineChart chart, File file, String fileType) {
        try {
            Bounds bounds = chart.localToScreen(chart.getBoundsInLocal());
            // aim for a 1920x1080 pixel image
            double scaleX = 1920 / bounds.getWidth();
            double scaleY = 1080 / bounds.getHeight();
            double snapScale = (scaleX > scaleY) ? scaleX : scaleY;
            SnapshotParameters snp = new SnapshotParameters();
            snp.setTransform(Transform.scale(snapScale, snapScale));
            WritableImage image = chart.snapshot(new SnapshotParameters(), null);

            ImageIO.write(SwingFXUtils.fromFXImage(image, null), fileType, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Exports LineChart data in a file for usage with gnuplot.
     *
     * @param chart Chart that will be used
     * @param file Export file
     */
    private static void exportTrainDetailAsGNU(ObservableList<Data<Double, Double>> chart,
        File file) {
        try {
            FileWriter fileWriter = new FileWriter(file);

            for (Data<Double, Double> data : chart) {
                Double xValue = data.getXValue();
                Double yValue = data.getYValue();

                fileWriter.write(String.format("%f %f\r\n", xValue, yValue));
            }

            fileWriter.close();
        } catch (IOException e) {
        }
    }
}
