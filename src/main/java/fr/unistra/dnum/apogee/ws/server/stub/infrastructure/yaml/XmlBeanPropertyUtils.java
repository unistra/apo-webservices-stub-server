package fr.unistra.dnum.apogee.ws.server.stub.infrastructure.yaml;

import jakarta.xml.bind.annotation.XmlType;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.*;

/** {@link PropertyUtils} extension
 * <ul>
 *     <li>using {@link XmlType#propOrder()} property ordering</li>
 *     <li>and decorating Tableau* properties w/ {@link TableauProperty}</li>
 * </ul>
 **/
public class XmlBeanPropertyUtils extends PropertyUtils {

    @Override
    protected Set<Property> createPropertySet(Class<?> type, BeanAccess bAccess) {
        Set<Property> properties = super.createPropertySet(type, bAccess);
        XmlType xmlType = type.getAnnotation(XmlType.class);
        return xmlType != null ? orderProperties(properties,xmlType.propOrder()) : properties;
    }

    private Set<Property> orderProperties(Set<Property> propertySet, String... propOrder) {
        if (propOrder == null || propOrder.length == 0)
            return propertySet;
        else {
            TreeSet<Property> ordered = new TreeSet<>(new PropertyOrder(propOrder));
            ordered.addAll(propertySet);
            return ordered;
        }
    }

    private static class PropertyOrder implements Comparator<Property> {
        private final Map<String,Integer> propOrder;

        private PropertyOrder(String... propOrder) {
            this.propOrder = new TreeMap<>();
            int order = 0;
            for (String name : propOrder)
                this.propOrder.put(name, order++);
        }
        private int getOrder(Property property) {
            return propOrder.getOrDefault(property.getName(), Integer.MAX_VALUE);
        }

        @Override
        public int compare(Property lhs, Property rhs) {
            if (lhs == rhs || lhs.equals(rhs)) return 0;
            int cmp = Integer.compare(getOrder(lhs), getOrder(rhs));
            return cmp != 0 ? cmp : lhs.getName().compareTo(rhs.getName());
        }

    }

    @Override
    protected Map<String, Property> getPropertiesMap(Class<?> type, BeanAccess bAccess) {
        Map<String, Property> propertiesMap = super.getPropertiesMap(type, bAccess);
        for (Property prop : propertiesMap.values())
            if (prop.getType().getSimpleName().startsWith("Tableau"))
                propertiesMap.put(prop.getName(), new TableauProperty<>(prop));
        return propertiesMap;
    }

    /**
     * Describe Tableau wrapper type {@link Property}
     * (has only one readonly property of {@link Collection} type
     * and a no-arg constructor)
     * @param <T> Tableau container type
     * @param <E> containee type
     */
    public static class TableauProperty<T,E> extends Property {
        private static final PropertyUtils PROPERTY_UTILS_WITH_RO = propertyUtilsWithAllowReadOnly();
        private static PropertyUtils propertyUtilsWithAllowReadOnly() {
            PropertyUtils propertyUtils = new PropertyUtils();
            propertyUtils.setAllowReadOnlyProperties(true);
            return propertyUtils;
        }

        private final Property delegate;
        private final Property itemProperty;
        private final Class<E> itemType;
        private final Constructor<T> declaredConstructor;
        private final Class<T> tableauType;

        /** @param delegate a Tableau wrapper type property
         *                      with itself exactly one property
         *                      of a {@link Collection} subtype
         *                      and a no-arg constructor */
        public TableauProperty(Property delegate) {
            super(delegate.getName(), delegate.getType());
            this.delegate = delegate;
            @SuppressWarnings("unchecked")
            Class<T> type = (Class<T>) delegate.getType();
            this.tableauType = type;
            Set<Property> properties = PROPERTY_UTILS_WITH_RO.getProperties(type);
            assert properties.size() == 1; // a wrapper type must have exactly one property
            this.itemProperty = properties.iterator().next();
            assert itemProperty.getActualTypeArguments().length == 1; // item property is a collection type (has one type argument)
            @SuppressWarnings("unchecked")
            Class<E> itemTypeArgument = (Class<E>) itemProperty.getActualTypeArguments()[0];
            this.itemType = itemTypeArgument;
            try {
                this.declaredConstructor = type.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(e);
            }
        }

        /** @return a List concrete type ({@link ArrayList}) */
        @Override
        public Class<?> getType() {
            return ArrayList.class;
        }

        /** @return exactly one type argument of this collection wrapper type */
        @Override
        public Class<?>[] getActualTypeArguments() {
            return new Class<?>[] { itemType } ;
        }

        /** wrap if necessary before {@link Property#set(Object, Object)} */
        @Override
        public void set(Object object, Object value) throws Exception {
            if (value == null || tableauType.isInstance(value))
                delegate.set(object, value);
            else {
                T tableau = declaredConstructor.newInstance();
                @SuppressWarnings("unchecked")
                Collection<E> item = (Collection<E>) itemProperty.get(tableau);
                @SuppressWarnings("unchecked")
                Collection<E> valueAsCollection = (Collection<E>) value;
                item.addAll(valueAsCollection);
                delegate.set(object, tableau);
            }
        }

        /** unwrap after {@link Property#get(Object)} */
        @Override
        public Object get(Object object) {
            @SuppressWarnings("unchecked")
            T tableau = (T) delegate.get(object);
            return tableau != null ? itemProperty.get(tableau) : null;
        }

        @Override
        public List<Annotation> getAnnotations() {
            return delegate.getAnnotations();
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            return delegate.getAnnotation(annotationType);
        }

        @Override
        public int hashCode() {
            return getName().hashCode() + tableauType.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof TableauProperty<?, ?> tableauProperty
                    && getName().equals(tableauProperty.getName())
                    && tableauType.equals(tableauProperty.tableauType);
        }

    }
}
