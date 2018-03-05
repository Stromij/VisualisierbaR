package com.github.bachelorpraktikum.visualisierbar.model;

import javax.annotation.Nonnull;
import java.util.LinkedList;

public class Group {

    @Nonnull
    private LinkedList<Element> elements;
    @Nonnull
    private String name;
    @Nonnull
    private String kind;

    public Group(@Nonnull String name, @Nonnull String kind, @Nonnull LinkedList<Element> elements)
        {this.name = name;
         this.kind = kind;
         this.elements = elements;
        }

    public Group(@Nonnull String name, @Nonnull String kind)
        {this.name = name;
         this.kind = kind;
         this.elements = new LinkedList<>();
        }

    @Nonnull
    public String getName()
        {return name;}

    @Nonnull
    public String getKind()
        {return kind;}

    /**
     * Adding an element to the group if it doesn't exist in the group yet
     * @param elem element to be added
     * @return true if successful, false if not
     */
    public boolean addElement(@Nonnull Element elem)
        {if(elements.contains(elem)) return false;

         elements.add(elem);
         return true;
        }

    @Nonnull
    public LinkedList<Element> getElements()
        {return elements;}

}
