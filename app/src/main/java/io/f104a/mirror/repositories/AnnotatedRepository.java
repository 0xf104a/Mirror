package io.f104a.mirror.repositories;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import io.f104a.mirror.tools.ITool;

/**
 * A repository which collects classes with certain ann
 * @param <A> Annotation which annotates T classes to be collected
 * @param <T> A class implementations/extensions of which to be stored
 */
public final class AnnotatedRepository<A extends Annotation, T> implements IRepository<Class<T>> {
    private final Class<A> annotationType;
    private final List<String> basePackages;


    public AnnotatedRepository(Class<A> annotationType, String... basePackages) {
        this.annotationType = Objects.requireNonNull(annotationType);
        this.basePackages = Arrays.asList(basePackages);
    }

    /** Returns classes annotated with A, enforcing optional requires() and sorted by optional order(). */
    public List<Class<T>> types() {
        List<Class<T>> found = new ArrayList<Class<T>>();

        for (String pkg : basePackages) {
            Reflections r = new Reflections(pkg, Scanners.TypesAnnotated);
            Set<Class<?>> annotated = r.getTypesAnnotatedWith(annotationType);

            for (Class<?> c : annotated) {
                A ann = c.getAnnotation(annotationType);

                // Enforce requires() if present
                Class<?> required = readClassAttr(ann, "requires", Object.class);
                if (required != Object.class && !required.isAssignableFrom(c)) {
                    throw new IllegalStateException("@" + annotationType.getSimpleName() + " on "
                            + c.getName() + " but it does not implement/extend " + required.getName());
                }

                Class<T> casted = (Class<T>) c;

                found.add(casted);
            }
        }

        found.sort(Comparator.comparingInt(c -> {
            A ann = c.getAnnotation(annotationType);
            return readIntAttr(ann, "order", 0);
        }));

        return found.stream().distinct().collect(Collectors.toList());
    }

    private static int readIntAttr(Annotation ann, String name, int defVal) {
        try {
            Method m = ann.annotationType().getMethod(name);
            Object v = m.invoke(ann);
            return (v instanceof Integer) ? (Integer) v : defVal;
        } catch (NoSuchMethodException e) {
            return defVal; // attribute not defined on the annotation
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> readClassAttr(Annotation ann, String name, Class<?> defVal) {
        try {
            Method m = ann.annotationType().getMethod(name);
            Object v = m.invoke(ann);
            return (v instanceof Class<?>) ? (Class<?>) v : defVal;
        } catch (NoSuchMethodException e) {
            return defVal; // attribute not defined on the annotation
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Class<T>> getAllRegistered() {
        return types();
    }
}
