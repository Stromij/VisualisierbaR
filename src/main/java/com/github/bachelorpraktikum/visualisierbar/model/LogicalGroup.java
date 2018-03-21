package com.github.bachelorpraktikum.visualisierbar.model;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.ref.WeakReference;
import java.util.*;

public class LogicalGroup {

    @Nonnull
    private LinkedHashSet<Element> elements;
    @Nonnull
    private String name;
    @Nonnull
    private Kind type;


    public enum Kind {
        SIGNAL, SWITCH, LIMITER, DEFAULT
    }

    @ParametersAreNonnullByDefault
    public static final class GroupFactory{

        private static final Map<Context, WeakReference<GroupFactory>> instances = new WeakHashMap<>();
        private static final int INITIAL_NODES_CAPACITY = 32;


        @Nonnull
        private final Map<String, LogicalGroup> groups;

        private static GroupFactory getInstance(Context context){
            GroupFactory result = instances.computeIfAbsent(context, ctx -> {
                GroupFactory factory = new GroupFactory(ctx);
                ctx.addObject(factory);
                return new WeakReference<>(factory);
            }).get();

            if (result == null) {
                throw new IllegalStateException();
            }
            return result;
        }

        private GroupFactory(Context ctx) {
            this.groups = new LinkedHashMap<>(INITIAL_NODES_CAPACITY);
        }




        @Nonnull
        public LogicalGroup create(@Nonnull String name, @Nonnull Kind kind, @Nonnull LinkedHashSet<Element> elements){
            if(groups.containsKey(name)){throw new IllegalArgumentException("group already exist " + name);}
            else{
                LogicalGroup g= new  LogicalGroup(name, kind, elements);
                groups.put(g.getName(),g);
                return g;
            }
        }

        @Nonnull
        public LogicalGroup create(@Nonnull String name, @Nonnull Kind kind){
            if(groups.containsKey(name)){throw new IllegalArgumentException("group already exist " + name);}
            else{
                LogicalGroup g= new  LogicalGroup(name, kind);
                groups.put(g.getName(),g);
                return g;
            }
        }


        public LogicalGroup get(String name) {
            LogicalGroup group =groups.get(Objects.requireNonNull(name));
            if(group==null){
                throw new IllegalArgumentException("unknown group: " + name);
            }
            return  group;
        }

        @Nonnull
        public Collection<LogicalGroup> getAll() {
            return Collections.unmodifiableCollection(groups.values());
        }

        public boolean checkAffiliated(@Nonnull LogicalGroup LogicalGroup) {
            return groups.get(LogicalGroup.getName()) == LogicalGroup;
        }

        public boolean NameExists(@Nonnull String name) {
            LogicalGroup group = groups.get(Objects.requireNonNull(name));
            return group != null;
        }


    }
    /**
     * Gets the {@link GroupFactory} instance for the given {@link Context}.
     *
     * @param context the context
     * @return the factory
     * @throws NullPointerException if context is null
     */
    public static GroupFactory in(Context context) {
        return GroupFactory.getInstance(context);
    }


    private LogicalGroup(@Nonnull String name, @Nonnull Kind kind, @Nonnull LinkedHashSet<Element> elements)
        {this.name = name;
         this.type = kind;
         this.elements = new LinkedHashSet<>();
         this.elements.addAll(elements);
        }

    private LogicalGroup(@Nonnull String name, @Nonnull Kind kind)
        {this.name = name;
         this.type = kind;
         this.elements = new LinkedHashSet<>();
        }

    @Nonnull
    public String getName()
        {return name;}

    @Nonnull
    public Kind getKind()
        {return type;}

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
     * Returns a String of the ABS representation of this LogicalGroup
     * @return the ABS-Code
     */
    @Nonnull
    public String toABS()
        {String rowOfElements = "";
         for(Element t : elements)
            {rowOfElements = rowOfElements.concat(t.higherName().concat(", "));}

         if(type == Kind.SIGNAL)
            {return String.format("[HTTPName: \"%s\"]Signal %s = new local SignalImpl(%s \"%s\", %s);\n",
                    name, name, rowOfElements, name, null);}
         if(type == Kind.SWITCH)
            {return String.format("[HTTPName: \"%s\"]Switch %s = new local SwitchImpl(%s %s, %s, %s, \"%s\");\n",
                    name, name, rowOfElements, null, null, null, name);
            }
         if(type == Kind.LIMITER)
            {return String.format("[HTTPName: \"%s\"]SpeedLimiter %s = new SpeedLimiterImpl(%s %s, \"%s\");\n",
                    name, name, rowOfElements, null, name);}

         return "// Type of logical group not supported";
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
