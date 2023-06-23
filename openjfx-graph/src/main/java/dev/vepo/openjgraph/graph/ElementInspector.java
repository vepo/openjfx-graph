package dev.vepo.openjgraph.graph;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ElementInspector {
    public static <T> double getEdgeWeight(T element) {
        try {
            Class<?> clazz = element.getClass();
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Weight.class)) {
                    method.setAccessible(true);
                    Object value = method.invoke(element);
                    if (value instanceof Number n) {
                        return n.doubleValue();
                    }
                }
            }

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Weight.class)) {
                    field.setAccessible(true);
                    Object value = field.get(element);
                    if (value instanceof Number n) {
                        return n.doubleValue();
                    }
                }
            }
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(ElementInspector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 1.0;
    }
}
