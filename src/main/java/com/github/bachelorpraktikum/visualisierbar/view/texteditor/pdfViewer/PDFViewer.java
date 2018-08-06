package com.github.bachelorpraktikum.visualisierbar.view.texteditor.pdfViewer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.io.File;
import java.util.ArrayList;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.annotations.*;
import org.icepdf.core.pobjects.graphics.text.LineText;
import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

import javax.swing.*;

public class PDFViewer {

    private File fileToPDF;
    private SwingController controller;

    public PDFViewer(File fileToPDF){
        this.fileToPDF = fileToPDF;

        // Erstelle die GUI für die PDF-Darstellung
        controller = new SwingController();
        SwingViewBuilder factory = new SwingViewBuilder(controller);
        JPanel viewerComponentPanel = factory.buildViewerPanel();

        ComponentKeyBinding.install(controller, viewerComponentPanel);

        JFrame window = new JFrame(fileToPDF.getName());
        window.getContentPane().add(viewerComponentPanel);
        window.pack();
        window.setExtendedState(Frame.MAXIMIZED_BOTH);
        window.setVisible(true);

        // Öffne das die entsprechende PDF
        controller.openDocument(fileToPDF.toString());

        // Verberge die Toolbar und setzte den View-Mode auf Seitenfortlaufend
        controller.setToolBarVisible(false);
        controller.setPageViewMode(0,true);
    }



    /**
     * Displays the given page in the PDF-Window. Start counting at 0
     * @param page pagenumber to display
     */
    public void setPage(int page)
        {controller.showPage(page);}


    /**
     * Adds a highlight to the shown PDF
     * @param startPageNumber Number of Page where the highlight starts
     * @param endPageNumber Number of Page where the highlight ends
     * @param startLineCoords Y-Coordinate where the highlight starts
     * @param endLineCoords Y-Coordinate where the highlight ends
     */
    public void highlight(int startPageNumber,int endPageNumber, int startLineCoords, int endLineCoords)
        {Page page = controller.getDocument().getPageTree().getPage(startPageNumber);

         // Generiere den Markup - (Anfang?)
         int height = endPageNumber == startPageNumber ? endLineCoords - startLineCoords : (int) page.getSize(0).getHeight() - startLineCoords;
         int start = endPageNumber == startPageNumber ? (int) page.getSize(0).getHeight() - (startLineCoords + (endLineCoords - startLineCoords)) : 0;
         TextMarkupAnnotation markupAnnotation = (TextMarkupAnnotation) AnnotationFactory.buildAnnotation(
            controller.getDocument().getPageTree().getLibrary(),
            Annotation.SUBTYPE_HIGHLIGHT,
            new Rectangle(
                    new Point(0,start),
                    new Dimension((int) page.getSize(0).getWidth(),height))
         );

         ArrayList<Shape> highlightBounds = new ArrayList<>();
         highlightBounds.add(
                 new Rectangle(
                         new Point(0,start),
                         new Dimension((int) page.getSize(0).getWidth(),height)
                 )
         );

         GeneralPath highlightPath = new GeneralPath();
         for(Shape bounds : highlightBounds)
            {highlightPath.append(bounds, false);}

         markupAnnotation.setMarkupBounds(highlightBounds);
         markupAnnotation.setMarkupPath(highlightPath);


         // Färbe das Markup ein  und aktualisiere den Bildschirm
         markupAnnotation.setTextMarkupColor(new Color(87, 128, 211));
         markupAnnotation.setOpacity(50);
         markupAnnotation.resetAppearanceStream(getPageTransform());


         // Lade den Markup in das Dokument
         page.addAnnotation(markupAnnotation);

         if(startPageNumber < endPageNumber)
            {highlight(startPageNumber + 1, endPageNumber, 0, endLineCoords);}
        }


    /**
     * Helps to find highlighting Positions
     * @param pageStart
     * @param pageEnd
     */
    public void funnyDebug(int pageStart, int pageEnd)
        {Page page = controller.getDocument().getPageTree().getPage(pageStart);
         try {
            for(LineText pl : page.getViewText().getPageLines())
                {System.out.println(pl.toString() + "  Bottom of Line: " + Math.round(page.getSize(0).getHeight() - pl.getBounds().y) + "  Top of Line: " + Math.round((page.getSize(0).getHeight() - pl.getBounds().y) - pl.getBounds().height)); }
         }
         catch (InterruptedException e) {
            e.printStackTrace();
         }
         if(pageStart < pageEnd)
            {funnyDebug(pageStart+1, pageEnd);}
        }



    private AffineTransform getPageTransform() {
        Page currentPage = controller.getDocument().getPageTree().getPage(controller.getCurrentPageNumber());
        AffineTransform at = currentPage.getPageTransform(
                controller.getDocumentViewController().getDocumentViewModel().getPageBoundary(),
                controller.getDocumentViewController().getDocumentViewModel().getViewRotation(),
                controller.getDocumentViewController().getDocumentViewModel().getViewZoom());

        try {
            at = at.createInverse();
        } catch (NoninvertibleTransformException e1) {
            e1.printStackTrace();
        }
        return at;
    }

}
