package com.github.bachelorpraktikum.visualisierbar.view.texteditor;
/**
 * Not used yet, it can be used for Search-result-highlighting
 */

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class SyntaxHighlighter {
    JTextComponent textComp;
    Highlighter highlighter;
    Document doc;

    public SyntaxHighlighter(JTextComponent textComp)
        {this.textComp = textComp;
         highlighter = textComp.getHighlighter();
         doc = textComp.getDocument();
        }

    public void addHighlighter(String pattern) throws BadLocationException
        {String text = doc.getText(0, doc.getLength());
         int pos = 0;

         while((pos = text.indexOf(pattern, pos)) >= 0)
            {highlighter.addHighlight(pos, pos + pattern.length(), new SyntaxHighlightPainter(Color.MAGENTA));
             pos += pattern.length();
            }
        }
}
