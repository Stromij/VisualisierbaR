package com.github.bachelorpraktikum.visualisierbar.view.texteditor;

import javax.swing.text.*;

public class TabSizeEditorKit extends StyledEditorKit{

    public static int tabSize;

    public ViewFactory getViewFactory() {
        return new MyViewFactory();
    }

    public void setTabSize(int charWidth)
        {tabSize = 28;
         System.out.println(charWidth + "ggggggggg");
        }

    static class MyViewFactory implements ViewFactory {

        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new LabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new CustomTabParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }

            return new LabelView(elem);
        }
    }



    static class CustomTabParagraphView extends ParagraphView {

        public CustomTabParagraphView(Element elem) {
            super(elem);
        }

        public float nextTabStop(float x, int tabOffset) {
            TabSet tabs = getTabSet();
            if(tabs == null) {
                return getTabBase() + (((int)x / tabSize + 1 ) * tabSize);
            }

            return super.nextTabStop(x, tabOffset);
        }

    }
}

