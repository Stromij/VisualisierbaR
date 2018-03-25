package com.github.bachelorpraktikum.visualisierbar.model;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.annotation.ElementType;
import java.lang.ref.WeakReference;
import java.util.*;

public class LogicalGroup {

    @Nonnull
    private LinkedList<Element> elements;
    @Nonnull
    private String name;
    @Nonnull
    private Kind type;


    public enum Kind {
        SIGNAL, SWITCH, LIMITER, DEFAULT
    }

    /**
     * Manages all instances of {@link LogicalGroup}.<br>
     * Ensures there is always only one instance of node per name per {@link Context}.
     */
    @ParametersAreNonnullByDefault
    public static final class GroupFactory{

        private static final Map<Context, WeakReference<GroupFactory>> instances = new WeakHashMap<>();
        private static final int INITIAL_NODES_CAPACITY = 32;


        @Nonnull
        private final Map<String, LogicalGroup> groups;
        @Nonnull
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



        /**
         * Potentially creates a new instance of {@link LogicalGroup}.<br>
         * If a group with the same name already exists, an exception is thrown
         *
         * @param name the unique name of this group
         * @param kind the {@link Kind} of this Group
         * @param  elements the Elements this group will contain
         * @return the created group
         * @throws IllegalArgumentException if the name is already taken
         * @throws NullPointerException if any of the arguments is null
         */
        @Nonnull
        public LogicalGroup create(@Nonnull String name, @Nonnull Kind kind, @Nonnull LinkedList<Element> elements){
            if(groups.containsKey(Objects.requireNonNull(name))){throw new IllegalArgumentException("group already exist " + name);}
            else{
                LogicalGroup g= new  LogicalGroup(name, Objects.requireNonNull(kind), Objects.requireNonNull(elements));
                groups.put(g.getName(),g);
                return g;
            }
        }

        /**
         * Potentially creates a new instance of {@link LogicalGroup}.<br>
         * If a group with the same name already exists, an exception is thrown
         * @param name the unique name of this group
         * @param kind the {@link Kind} of this Group
         * @return the created group
         * @throws IllegalArgumentException if the name is already taken
         * @throws NullPointerException if any of the arguments is null
         */
        @Nonnull
        public LogicalGroup create(@Nonnull String name, @Nonnull Kind kind){
            if(groups.containsKey(Objects.requireNonNull(name))){throw new IllegalArgumentException("group already exist " + name);}
            else{
                LogicalGroup g= new  LogicalGroup(name, Objects.requireNonNull(kind));
                groups.put(g.getName(),g);
                return g;
            }
        }

        /**
         * Gets the instance with the given unique name.
         *
         * @param name the instance's name
         * @return the instance with this name
         * @throws NullPointerException if the name is null
         * @throws IllegalArgumentException if there is no object associated with the name
         */
        public LogicalGroup get(@Nonnull String name) {
            LogicalGroup group =groups.get(Objects.requireNonNull(name));
            if(group==null){
                throw new IllegalArgumentException("unknown group: " + name);
            }
            return  group;
        }
        /**
         * Gets all instances in this {@link Context}.
         *
         * @return all instances
         */
        @Nonnull
        public Collection<LogicalGroup> getAll() {
            return Collections.unmodifiableCollection(groups.values());
        }
        /**
         * Checks whether the given instance of {@link LogicalGroup} was created by this factory.
         *
         * @param LogicalGroup instance to check affiliation for
         * @return whether the given object was created by this factory
         */
        public boolean checkAffiliated(@Nonnull LogicalGroup LogicalGroup) {
            return groups.get(LogicalGroup.getName()) == LogicalGroup;
        }

        /**
         * Check whether a name is already taken
         * @param name the Name to check
         * @return  true if name is already taken, false otherwise
         * @throws NullPointerException is name is null
         */
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


    private LogicalGroup(@Nonnull String name, @Nonnull Kind kind, @Nonnull LinkedList<Element> elements)
        {this.name = name;
         this.type = kind;
         this.elements = new LinkedList<>();
         this.elements.addAll(elements);
        }

    private LogicalGroup(@Nonnull String name, @Nonnull Kind kind)
        {this.name = name;
         this.type = kind;
         this.elements = new LinkedList<>();
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
    public LinkedList<Element> getElements(){
        return elements;
    }

    /**
     * Returns a String of the ABS representation of this LogicalGroup
     * @return the ABS-Code
     */
    @Nonnull
    public String toABS()
        {String rowOfElements = "";
         int counter = 0;
         for(Element t : elements)
            {rowOfElements = rowOfElements.concat(t.higherName().concat(", "));
             counter ++;
            }

         if(type == Kind.SIGNAL)
            {for(;counter < 6; counter++){rowOfElements = rowOfElements.concat("null, ");}
             return String.format("[HTTPName: \"%s\"]Signal %s = new local SignalImpl(%s\"%s\", %s);\n",
                    name, name, rowOfElements, name, null);}
         if(type == Kind.SWITCH)
            {for(;counter < 3; counter++){rowOfElements = rowOfElements.concat("null, ");}
             return String.format("[HTTPName: \"%s\"]Switch %s = new local SwitchImpl(%s%s, %s, %s, \"%s\");\n",
                    name, name, rowOfElements, null, null, null, name);
            }
         if(type == Kind.LIMITER)
            {for(;counter < 4; counter++){rowOfElements = rowOfElements.concat("null, ");}
             return String.format("[HTTPName: \"%s\"]SpeedLimiter %s = new SpeedLimiterImpl(%s%s, \"%s\");\n",
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
