package com.github.bachelorpraktikum.visualisierbar.model;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.*;

public class logicalGroup {

    @Nonnull
    private LinkedHashSet<Element> elements;
    @Nonnull
    private String name;
    @Nonnull
    private String kind;



    public static final class GroupFactory{

        @Nonnull
        private static final Map<String, logicalGroup> groups= new LinkedHashMap<>();


        @Nonnull
        public static logicalGroup create(@Nonnull String name, @Nonnull String kind, @Nonnull LinkedList<Element> elements){
            if(groups.containsKey(name)){throw new IllegalArgumentException("group already exist " + name);}
            else{
                logicalGroup g= new  logicalGroup(name, kind, elements);
                groups.put(g.getName(),g);
                return g;
            }
        }

        @Nonnull
        public static logicalGroup create(@Nonnull String name, @Nonnull String kind){
            if(groups.containsKey(name)){throw new IllegalArgumentException("group already exist " + name);}
            else{
                logicalGroup g= new  logicalGroup(name, kind);
                groups.put(g.getName(),g);
                return g;
            }
        }


        public static logicalGroup get(String name) {
            if(name.equals("")) return null;
            logicalGroup group =groups.get(Objects.requireNonNull(name));
            if(group==null){
                throw new IllegalArgumentException("unknown group: " + name);
            }
            return  group;
        }

        @Nonnull
        public static Collection<logicalGroup> getAll() {
            return Collections.unmodifiableCollection(groups.values());
        }

        public static boolean checkAffiliated(@Nonnull logicalGroup logicalGroup) {
            return groups.get(logicalGroup.getName()) == logicalGroup;
        }

        public static boolean NameExists(@Nonnull String name) {
            logicalGroup group = groups.get(Objects.requireNonNull(name));
            return group != null;
        }


    }


    private logicalGroup(@Nonnull String name, @Nonnull String kind, @Nonnull LinkedList<Element> elements)
        {this.name = name;
         this.kind = kind;
         this.elements = new LinkedHashSet<>();
         this.elements.addAll(elements);
        }

    private logicalGroup(@Nonnull String name, @Nonnull String kind)
        {this.name = name;
         this.kind = kind;
         this.elements = new LinkedHashSet<>();
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
    public LinkedHashSet<Element> getElements(){
        return elements;
    }

    public void removeElement(Element element){
        elements.remove(element);
    }


}
