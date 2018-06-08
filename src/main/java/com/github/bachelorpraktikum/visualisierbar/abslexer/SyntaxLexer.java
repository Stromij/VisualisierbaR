package com.github.bachelorpraktikum.visualisierbar.abslexer;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import javax.swing.text.*;
import java.awt.*;


public class SyntaxLexer {


    public SyntaxLexer()
        {}

    public Document lex(String toLex)
        {Document response = new DefaultStyledDocument();

         CharStream stream = new ANTLRInputStream(toLex);
         AbsLexer lexer = new AbsLexer(stream);

         for (Token token = lexer.nextToken(); token.getType() != Token.EOF; token = lexer.nextToken())
            {try {System.out.println(token.getText() + " " + token.getType());
                  response.insertString(response.getLength(), token.getText(), getStyle(token.getType()));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }

         return response;
        }

    private Style getStyle(int type)
        {switch(type) {
            case AbsLexer.EndOfLineComment:
                return getCommandStyle();
            case AbsLexer.TraditionalComment:
                return getCommandStyle();
            case AbsLexer.INTLITERAL:
                return getLiteralsStyle();
            case AbsLexer.TYPE_IDENTIFIER:
                return getDeclarationStyle();
            case AbsLexer.STRINGLITERAL:
                return getStringStyle();
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
            case AbsLexer.T__15:
                return getDeclarationStyle();
            case AbsLexer.T__53:                // "module"
                return getDeclarationStyle();
            case 90:                            // Leerzeichen
                return null;

            default: return null;
         }
        }


    private Style getCommandStyle()
        {Style commentStyle = new StyleContext().addStyle("comment", null);
         StyleConstants.setForeground(commentStyle, new Color(2, 125,0));
         StyleConstants.setItalic(commentStyle, true);
         return commentStyle;
        }

    private Style getDeclarationStyle()
        {Style declarationStyle = new StyleContext().addStyle("declaration", null);
         StyleConstants.setForeground(declarationStyle, new Color(178, 23, 101));
         StyleConstants.setBold(declarationStyle, true);
         return declarationStyle;
        }

    private Style getFunctionStyle()
        {Style functionStyle = new StyleContext().addStyle("function", null);
         StyleConstants.setForeground(functionStyle, new Color(255, 139, 0));
         return functionStyle;
        }

    private Style getLiteralsStyle()
        {Style literalStyle = new StyleContext().addStyle("literal", null);
         StyleConstants.setForeground(literalStyle, new Color(203, 0, 5));
         return literalStyle;
        }

    private Style getStringStyle()
        {Style stringStyle = new StyleContext().addStyle("string", null);
         StyleConstants.setForeground(stringStyle, new Color(72, 72, 72));
         StyleConstants.setItalic(stringStyle, true);
         return stringStyle;
        }

    private Style getTestStyle()
        {Style testStyle = new StyleContext().addStyle("test", null);
         StyleConstants.setForeground(testStyle, new Color(255, 0, 209));
         StyleConstants.setBold(testStyle, true);
         return testStyle;
        }
}
