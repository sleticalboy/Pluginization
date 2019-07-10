package com.sleticalboy.pluginization.util;

import android.util.Log;

import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created on 19-7-9.
 *
 * @author leebin
 */
public final class Reflections {

    private static final String TAG = "Reflections";

    private Reflections() {
        throw new AssertionError();
    }

    /**
     * 反射获取字段值
     *
     * @param target    类对象或全类名或实例
     * @param fieldName 字段名
     * @param instance  对象实例，当反射静态字段时此参数传 null
     * @return 字段值
     */
    public static Object getField(Object target, String fieldName, @Nullable Object instance) {
        if (target == null && instance == null) {
            throw new IllegalArgumentException("target and instance are both null.");
        }
        if (target instanceof Class) {
            return getField(((Class) target), fieldName, instance);
        } else if (target instanceof String) {
            return getField(((String) target), fieldName, instance);
        } else {
            // case 1: target != null && instance == null, most possible static field
            // case 2: target != null && instance != null, most possible target == instance
            // case 3: target == null && instance != null, common field
            if (target != null) {
                return getField(target.getClass(), fieldName, instance);
            } else {
                return getField(instance.getClass(), fieldName, instance);
            }
        }
    }

    private static Object getField(String clsName, String fieldName, Object instance) {
        Class<?> targetCls = null;
        try {
            targetCls = onObj(clsName);
            return getField(targetCls, fieldName, instance);
        } catch (Throwable e) {
            return getField0(targetCls, fieldName, instance);
        }
    }

    private static Object getField(Class cls, String fieldName, Object instance) {
        try {
            final Field f = cls.getField(fieldName);
            f.setAccessible(true);
            return f.get(instance);
        } catch (Throwable e) {
            return getField0(cls, fieldName, instance);
        }
    }

    private static Object getField0(Class cls, String fieldName, Object instance) {
        try {
            final Field f = cls.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(instance);
        } catch (Throwable e) {
            Log.e(TAG, "getField0() error cls: " + cls.getName() + ", fieldName: " + fieldName, e);
            return null;
        }
    }

    /**
     * 反射设置字段值
     *
     * @param target    类对象或全类名或实例
     * @param instance  对象实例，当反射静态字段时此参数传 null
     * @param fieldName 字段名
     * @param field     要设置的字段值
     */
    public static void setField(Object target, @Nullable Object instance,
                                String fieldName, Object field) {
        if (target == null && instance == null) {
            throw new IllegalArgumentException("target and instance can not be both null.");
        }
        if (target instanceof Class) {
            setField(((Class) target), instance, field, fieldName);
        } else if (target instanceof String) {
            setField(((String) target), instance, field, fieldName);
        } else {
            if (instance != null) {
                setField(instance.getClass(), instance, field, fieldName);
            } else {
                Log.w(TAG, "setField() error target: " + target + ", instance: null, fieldName: "
                        + fieldName + ", field: " + field);
            }
        }
    }

    private static void setField(String cls, Object instance, Object field, String fieldName) {
        Class<?> targetCls = null;
        try {
            // targetCls = Class.forName(cls);
            targetCls = onObj(cls);
            setField(targetCls, instance, field, fieldName);
        } catch (Throwable e) {
            setField0(targetCls, instance, field, fieldName);
        }
    }

    private static void setField(Class cls, Object instance, Object field, String fieldName) {
        try {
            final Field f = cls.getField(fieldName);
            f.setAccessible(true);
            f.set(instance, field);
        } catch (Throwable e) {
            setField0(cls, instance, field, fieldName);
        }
    }

    private static void setField0(Class cls, Object instance, Object field, String fieldName) {
        try {
            final Field f = cls.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(instance, field);
        } catch (Throwable e) {
            Log.e(TAG, "setField0() error cls: " + cls.getName() + ", obj: " + instance
                    + ", field: " + fieldName, e);
        }
    }

    public static Object invokeMethod(Object instance, String methodName, Object... args) {
        Class<?>[] paramTypes = null;
        final Class<?> targetCls = instance.getClass();
        try {
            if (args != null && args.length != 0) {
                paramTypes = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    paramTypes[i] = args[i].getClass();
                }
            }
            final Method m = targetCls.getMethod(methodName, paramTypes);
            m.setAccessible(true);
            return m.invoke(instance, args);
        } catch (Throwable e) {
            return invokeMethod0(targetCls, instance, methodName, paramTypes, args);
        }
    }

    private static Object invokeMethod0(Class<?> targetCls, Object instance, String methodName,
                                        Class[] paramTypes, Object... args) {
        try {
            final Method m = targetCls.getDeclaredMethod(methodName, paramTypes);
            m.setAccessible(true);
            return m.invoke(instance, args);
        } catch (Throwable e) {
            Log.e(TAG, "invokeMethod() error obj: " + instance + ", methodName: " + methodName
                    + ", args: " + Arrays.toString(args), e);
            return null;
        }
    }

    public static Class onObj(Object target) throws Throwable {
        if (target == null) {
            throw new IllegalArgumentException("onObj(): target is null.");
        }
        if (target instanceof Class) {
            return ((Class) target);
        } else if (target instanceof String) {
            return Class.forName(((String) target));
        } else {
            return target.getClass();
        }
    }
}
