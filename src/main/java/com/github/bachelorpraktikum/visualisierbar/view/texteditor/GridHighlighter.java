package com.github.bachelorpraktikum.visualisierbar.view.texteditor;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class GridHighlighter implements Highlighter.HighlightPainter {




    private void paintLine(Graphics g, Rectangle r, int x2) {
        // int ytop = r.y + r.height - 3;
         int ytop = r.y ;
        g.fillRect(r.x, ytop, x2 - r.x, r.height);
    }

    // paint thick lines under a block of text
    public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {

        Rectangle r0 = null, r1 = null, rbounds = bounds.getBounds();
        int xmax = rbounds.x + rbounds.width; // x coordinate of right edge
        try {  // convert positions to pixel coordinates
            r0 = c.modelToView(p0);
            r1 = c.modelToView(p1);
        } catch (BadLocationException ex) { return; }
        if ((r0 == null) || (r1 == null)) return;

        g.setColor(new Color(234, 234, 234));

        // special case if p0 and p1 are on the same line
        if (r0.y == r1.y) {
            paintLine(g, r0, r1.x);
            return;
        }

        // first line, from p1 to end-of-line
        paintLine(g, r0, xmax);

        // all the full lines in between, if any (assumes that all lines have
        // the same height--not a good assumption with JEditorPane/JTextPane)
        r0.y += r0.height; // move r0 to next line
        r0.x = rbounds.x; // move r0 to left edge
        while (r0.y < r1.y) {
            paintLine(g, r0, xmax);
            r0.y += r0.height; // move r0 to next line
        }

        // last line, from beginning-of-line to p1
        paintLine(g, r0, r1.x);
    }
}
