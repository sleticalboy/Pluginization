package com.sleticalboy.pluginization.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created on 20-10-30.
 *
 * @author binli
 */
public final class Reflecter {

    private Reflecter() {
        //no instance
    }

    // wrapped for dynamic proxy
    public static Prox with(ClassLoader loader) {
        return new Prox(loader);
    }

    // wrapped for reflect
    /**
     * if you pass a string instance here, you must do like this:
     * <pre>
     *     Reflecter.on(((Object) "str"))
     * </pre>
     * or there'll be a java.lang.ClassNotFoundException occurred.
     */
    public static Reflekt on(Object instance) {
        return on(instance.getClass(), instance);
    }

    /**
     * @param clazz the full name of a class
     */
    public static Reflekt on(String clazz) {
        return on(forName(clazz), null);
    }

    public static Reflekt on(String clazz, Object instance) {
        return on(forName(clazz), instance);
    }

    public static Reflekt on(Class<?> clazz) {
        return on(clazz, null);
    }

    public static Reflekt on(Class<?> clazz, Object instance) {
        return new Reflekt(clazz, instance);
    }

    public static Class<?> forName(String clazz) {
        try {
            return Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            throw new ReflectException("forName()", e);
        }
    }

    public static class Reflekt {
        private final Object mObj;
        private final Class<?> mCls;

        private Reflekt(Class<?> cls, Object obj) {
            mObj = obj;
            mCls = cls;
        }

        // instance construct
        public Object create() {
            return create(null);
        }

        public Object create(Class<?>[] parameters, Object... args) {
            try {
                final Constructor<?> constructor = mCls.getDeclaredConstructor(parameters);
                constructor.setAccessible(true);
                return constructor.newInstance(args);
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException
                    | InvocationTargetException e) {
                if (e instanceof NoSuchMethodException) {
                    // try recurse constructor from parent
                    Class<?> clazz = mCls.getSuperclass();
                    if (clazz != null && clazz != Object.class) {
                        return on(clazz).create(parameters, args);
                    }
                }
                throw new ReflectException("construct()", e);
            }
        }

        // field set and get
        public void set(String field, Object value) {
            try {
                final Field f = mCls.getDeclaredField(field);
                f.setAccessible(true);
                f.set(mObj, value);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                if (e instanceof NoSuchFieldException) {
                    // try recurse field from parent
                    final Class<?> clazz = mCls.getSuperclass();
                    if (clazz != null && clazz != Object.class) {
                        on(clazz, mObj).set(field, value);
                    }
                }
                throw new ReflectException("set()", e);
            }
        }

        public Object get(String field) {
            try {
                final Field f = mCls.getDeclaredField(field);
                f.setAccessible(true);
                return f.get(mObj);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                if (e instanceof NoSuchFieldException) {
                    // try recurse field from parent
                    final Class<?> clazz = mCls.getSuperclass();
                    if (clazz != null && clazz != Object.class) {
                        return on(clazz, mObj).get(field);
                    }
                }
                throw new ReflectException("get()", e);
            }
        }

        // method call
        public Object call(String method) {
            return call(method, null);
        }

        public Object call(String method, Class<?>[] parameters, Object... args) {
            try {
                final Method m = mCls.getDeclaredMethod(method, parameters);
                m.setAccessible(true);
                return m.invoke(mObj, args);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                if (e instanceof NoSuchMethodException) {
                    // try recurse method from parent
                    final Class<?> clazz = mCls.getSuperclass();
                    if (clazz != null && clazz != Object.class) {
                        return on(clazz, mObj).call(method, parameters, args);
                    }
                }
                throw new ReflectException("call()", e);
            }
        }

        @Override
        public String toString() {
            return "Reflekt{" + "mObj=" + mObj + ", mCls=" + mCls + '}';
        }
    }

    public static final class Prox {

        private final ClassLoader mLoader;
        private Class<?>[] mInterfaces;

        private Prox(ClassLoader loader) {
            mLoader = loader;
        }

        public Prox on(String... interfaces) {
            if (null == interfaces || interfaces.length == 0) {
                throw new IllegalArgumentException("interfaces is empty");
            }
            Class<?>[] classes = new Class<?>[interfaces.length];
            for (int i = 0; i < interfaces.length; i++) {
                classes[i] = forName(interfaces[i]);
            }
            mInterfaces = classes;
            return this;
        }

        public Prox on(Class<?>... interfaces) {
            if (null == interfaces || interfaces.length == 0) {
                throw new IllegalArgumentException("interfaces is empty");
            }
            mInterfaces = interfaces;
            return this;
        }

        public Object handle(InvocationHandler handler) {
            return Proxy.newProxyInstance(mLoader, mInterfaces, handler);
        }
    }
}