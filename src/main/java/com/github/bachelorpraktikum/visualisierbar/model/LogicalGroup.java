package com.github.bachelorpraktikum.visualisierbar.model;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.*;

public class LogicalGroup {

    @Nonnull
    private LinkedHashSet<Element> elements;
    @Nonnull
    private String name;
    @Nonnull
    private String kind;



    public static final class GroupFactory{

        @Nonnull
        private static final Map<String, LogicalGroup> groups= new LinkedHashMap<>();


        @Nonnull
        public static LogicalGroup create(@Nonnull String name, @Nonnull String kind, @Nonnull LinkedHashSet<Element> elements){
            if(groups.containsKey(name)){throw new IllegalArgumentException("group already exist " + name);}
            else{
                LogicalGroup g= new  LogicalGroup(name, kind, elements);
                groups.put(g.getName(),g);
                return g;
            }
        }

        @Nonnull
        public static LogicalGroup create(@Nonnull String name, @Nonnull String kind){
            if(groups.containsKey(name)){throw new IllegalArgumentException("group already exist " + name);}
            else{
                LogicalGroup g= new  LogicalGroup(name, kind);
                groups.put(g.getName(),g);
                return g;
            }
        }


        public static LogicalGroup get(String name) {
            if(name.equals("")) return null;
            LogicalGroup group =groups.get(Objects.requireNonNull(name));
            if(group==null){
                throw new IllegalArgumentException("unknown group: " + name);
            }
            return  group;
        }

        @Nonnull
        public static Collection<LogicalGroup> getAll() {
            return Collections.unmodifiableCollection(groups.values());
        }

        public static boolean checkAffiliated(@Nonnull LogicalGroup LogicalGroup) {
            return groups.get(LogicalGroup.getName()) == LogicalGroup;
        }

        public static boolean NameExists(@Nonnull String name) {
            LogicalGroup group = groups.get(Objects.requireNonNull(name));
            return group != null;
        }


    }


    private LogicalGroup(@Nonnull String name, @Nonnull String kind, @Nonnull LinkedHashSet<Element> elements)
        {this.name = name;
         this.kind = kind;
         this.elements = new LinkedHashSet<>();
         this.elements.addAll(elements);
        }

    private LogicalGroup(@Nonnull String name, @Nonnull String kind)
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
     * Adding an element to the group if it doesn't exist in the group yet and sets the elements logical group to this group
     * @param elem element to be added
     * @return true if successful, false if not
     */
    public boolean addElement(@Nonnull Element elem)
        {if(elements.contains(elem)) return false;
        elem.setLogicalGroup(this);
         elements.add(elem);
         return true;
        }

    @Nonnull
    public LinkedHashSet<Element> getElements(){
        return elements;
    }

    /**
     * Removes an Element from this Group and sets its Logical Group to null
     * @param element the element to remove
     */
    public void removeElement(Element element){
        elements.remove(element);
        element.setLogicalGroup(null);
    }


}
