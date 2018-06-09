package com.github.bachelorpraktikum.visualisierbar.abslexer;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;

import javax.swing.text.*;
import java.awt.*;


public class SyntaxLexer {


    public SyntaxLexer()
        {}

    /**
     * Returns a styled document with syntax highlighting
     * @param toLex the string to be highlighted
     * @return a styled document
     */
    public Document lex(String toLex)
        {Document response = new DefaultStyledDocument();

         CharStream stream = new ANTLRInputStream(toLex);
         AbsLexer lexer = new AbsLexer(stream);

         for (Token token = lexer.nextToken(); token.getType() != Token.EOF; token = lexer.nextToken())
            {try {response.insertString(response.getLength(), token.getText(), getStyle(token.getType()));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }

         return response;
        }


    /**
     * Returns a specific style for the given Token-Type
     * @param type Token-Type
     * @return given Style
     */
    private Style getStyle(int type)
        {switch(type) {
            case AbsLexer.EndOfLineComment:
                return getCommandStyle();
            case AbsLexer.TraditionalComment:
                return getCommandStyle();
            case AbsLexer.INTLITERAL:
                return getLiteralsStyle();
            case AbsLexer.TYPE_IDENTIFIER:
                return null;
            case AbsLexer.STRINGLITERAL:
                return getStringStyle();
            case AbsLexer.IDENTIFIER:
                return null;
            case AbsLexer.T__0:                 // ?
                return null;
            case AbsLexer.T__1:                 // "," in Parameterlisten
                return null;
            case AbsLexer.T__2:                 // "(" bei Funktionen
                return null;
            case AbsLexer.T__3:                 // ")" bei Funktionen
                return null;
            case AbsLexer.T__4:                 // ?
                return null;
            case AbsLexer.T__5:                 // "new"
                return getDeclarationStyle();
            case AbsLexer.T__6:                 // "local"
                return getDeclarationStyle();
            case AbsLexer.T__7:                 // "await"
                return getDeclarationStyle();
            case AbsLexer.T__8:                 // ?
                return null;
            case AbsLexer.T__9:                 // ?
                return null;
            case AbsLexer.T__10:                // "[" bei HTTP etc
                return null;
            case AbsLexer.T__11:                // "]" bei HTTP etc
                return null;
            case AbsLexer.T__12:                // "this" bei internen Klassenbeziehungen
                return getDeclarationStyle();
            case AbsLexer.T__13:                // "null"
                return getDeclarationStyle();
            case AbsLexer.T__14:                // "if"
                return getDeclarationStyle();
            case AbsLexer.T__15:                // ?
                return null;
            case AbsLexer.T__16:                // ?
                return null;
            case AbsLexer.T__17:                // "case"
                return getDeclarationStyle();
            case AbsLexer.T__18:                // "{"
                return  null;
            case AbsLexer.T__19:                // "}"
                return null;
            case AbsLexer.T__20:                // ?
                return null;
            case AbsLexer.T__21:                // "=" Zuweisungsoperator
                return null;
            case AbsLexer.T__22:                // ?
                return null;
            case AbsLexer.T__23:                // "=>" Operator
                return null;
            case AbsLexer.T__24:                // ";"
                return null;
            case AbsLexer.T__25:                // "_" in "_ => skip" - Klausel
                return null;
            case AbsLexer.T__26:                //  ":" in [HTTP:
                return null;
            case AbsLexer.T__27:                // "skip"
                return getDeclarationStyle();
            case AbsLexer.T__28:                // "return
                return getDeclarationStyle();
            case AbsLexer.T__29:                // ?
                return null;
            case AbsLexer.T__30:                // ?
                return null;
            case AbsLexer.T__31:                // ?
                return null;
            case AbsLexer.T__32:                // ?
                return null;
            case AbsLexer.T__33:                // ?
                return null;
            case AbsLexer.T__34:                // ?
                return null;
            case AbsLexer.T__35:                // ?
                return null;
            case AbsLexer.T__36:                // "duration"
                return getDeclarationStyle();
            case AbsLexer.T__37:                // ?
                return null;
            case AbsLexer.T__38:                // ?
                return null;
            case AbsLexer.T__39:                // ?
                return null;
            case AbsLexer.T__40:                // ?
                return null;
            case AbsLexer.T__41:                // ?
                return null;
            case AbsLexer.T__42:                // ?
                return null;
            case AbsLexer.T__43:                // ?
                return null;
            case AbsLexer.T__44:                // ?
                return null;
            case AbsLexer.T__45:                // ?
                return null;
            case AbsLexer.T__46:                // ?
                return null;
            case AbsLexer.T__47:                // ?
                return null;
            case AbsLexer.T__48:                // "interface"
                return getDeclarationStyle();
            case AbsLexer.T__49:                // ?
                return null;
            case AbsLexer.T__50:                // "class"
                return getDeclarationStyle();
            case AbsLexer.T__51:                // "implements"
                return getDeclarationStyle();
            case AbsLexer.T__52:                // ?
                return null;
            case AbsLexer.T__53:                // "module"
                return getDeclarationStyle();
            case AbsLexer.T__54:                // "export"
                return getDeclarationStyle();
            case AbsLexer.T__55:                // "from" in "export * from ..."
                return getFromStyle();
            case AbsLexer.T__56:                // "import"
                return getDeclarationStyle();
            case AbsLexer.T__57:                // ?
                return null;
            case AbsLexer.T__58:                // ?
                return null;
            case AbsLexer.T__59:                // "adds"
                return getDeclarationStyle();
            case AbsLexer.T__60:                // "modifies"
                return getDeclarationStyle();
            case AbsLexer.T__61:                // ?
                return null;
            case AbsLexer.T__62:                // "delta"
                return getDeclarationStyle();
            case AbsLexer.T__63:                // ?
                return null;
            case AbsLexer.T__64:                // ?
                return null;
            case AbsLexer.T__65:                // ?
                return null;
            case AbsLexer.T__66:                // ?
                return null;
            case AbsLexer.T__67:                // ?
                return null;
            case AbsLexer.T__68:                // ?
                return null;
            case AbsLexer.T__69:                // "productline"
                return getDeclarationStyle();
            case AbsLexer.T__70:                // "features"
                return getDeclarationStyle();
            case AbsLexer.T__71:                // ?
                return null;
            case AbsLexer.T__72:                // "after"
                return getDeclarationStyle();
            case AbsLexer.T__73:                // "when"
                return getDeclarationStyle();
            case AbsLexer.T__74:                // ?
                return null;
            case AbsLexer.T__75:                // "product"
                return getDeclarationStyle();
            case AbsLexer.T__76:                // "group"
                return getDeclarationStyle();
            case AbsLexer.T__77:                // "oneof"
                return getFromStyle();
            case AbsLexer.T__78:                // ?
                return null;
            case AbsLexer.T__79:                // ?
                return null;
            case AbsLexer.T__80:                // ?
                return null;
            case AbsLexer.T__81:                // ?
                return null;
            case AbsLexer.T__82:                // ?
                return null;
            case AbsLexer.T__83:                // ?
                return null;
            case AbsLexer.T__84:                // ?
                return null;
            case AbsLexer.T__85:                // "root"
                return getDeclarationStyle();
            case AbsLexer.T__86:                // ?
                return null;
            case 90:                            // Leerzeichen
                return null;

            default: return null;
         }
        }

    /**
     * Style for commands
     * @return the style for commands
     */
    private Style getCommandStyle()
        {Style commentStyle = new StyleContext().addStyle("comment", null);
         StyleConstants.setForeground(commentStyle, new Color(2, 125,0));
         StyleConstants.setItalic(commentStyle, true);
         return commentStyle;
        }

    /**
     * Style for declarations
     * @return the style for declarations
     */
    private Style getDeclarationStyle()
        {Style declarationStyle = new StyleContext().addStyle("declaration", null);
         StyleConstants.setForeground(declarationStyle, new Color(178, 23, 101));
         StyleConstants.setBold(declarationStyle, true);
         return declarationStyle;
        }

    /**
     * Style for literals
     * @return the style for literals
     */
    private Style getLiteralsStyle()
        {Style literalStyle = new StyleContext().addStyle("literal", null);
         StyleConstants.setForeground(literalStyle, new Color(203, 0, 5));
         return literalStyle;
        }

    /**
     * Style for strings
     * @return the style for strings
     */
    private Style getStringStyle()
        {Style stringStyle = new StyleContext().addStyle("string", null);
         StyleConstants.setForeground(stringStyle, new Color(72, 72, 72));
         StyleConstants.setItalic(stringStyle, true);
         return stringStyle;
        }

    /**
     * Style for from
     * @return the style for from
     */
    private Style getFromStyle()
        {Style stringStyle = new StyleContext().addStyle("from", null);
         StyleConstants.setItalic(stringStyle, true);
         return stringStyle;
        }
}
