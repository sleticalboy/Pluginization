package com.sleticalboy.pluginization.util;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Array;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * Created on 20-10-30.
 *
 * @author binli
 */
public final class DexMerger {

    private static final String TAG = "DexMerger";

    private DexMerger() {
        //no instance
    }

    public static void merge(Context context, String[] files) {
        if (null == files || files.length == 0) {
            return;
        }
        // 1. 根据宿主的 ClassLoader , 获取宿主的 dexElements 字段 :
        //    1.1. 反射出 BaseDexClassLoader 的 pathList 宇段 ,它 是 DexPathList 类型的
        //    1.2. 反射出 DexPathList 的 dexElements 字段, 这是个数组
        // 2. 根据插件的 apkFile, 反射出 一个 Element 类型的对象,这就是插件 dex
        // 3. 把插件 dex 和 宿主 dexElements 合并成一个新的 dex 数组
        // 4. 替换宿主之前的 dexElements 字段
        ClassLoader pathLoader = context.getClassLoader();
        Log.d(TAG, "loader: " + pathLoader);
        // 1.1
        Object pathList = Reflecter.on(pathLoader).get("pathList");
        Log.d(TAG, "pathList: " + pathList);
        // 1.2
        Object dexElements = Reflecter.on(pathList).get("dexElements");
        for (int i = 0, len = Array.getLength(dexElements); i < len; i++) {
            Log.d(TAG, "original: " + Array.get(dexElements, i));
        }
        // 2
        for (String dex : files) {
            dexElements = doMerge(dexElements, new DexClassLoader(dex, dex, null, pathLoader));
        }
        for (int i = 0, len = Array.getLength(dexElements); i < len; i++) {
            Log.d(TAG, "total: " + Array.get(dexElements, i));
        }
        // 4
        Reflecter.on(pathList).set("dexElements", dexElements);
    }

    private static String extractDex(String file) {
        return null;
    }

    private static Object doMerge(Object originalElements, DexClassLoader dexLoader) {
        Log.d(TAG, "dexLoader: " + dexLoader + ", dexLoader: " + dexLoader);
        Object obj = Reflecter.on(dexLoader).get("pathList");
        obj = Reflecter.on(obj).get("dexElements");
        return mergeArray(obj, originalElements);
    }

    public static Object mergeArray(Object left, Object right) {
        if (left == null && right == null) {
            return null;
        }
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        // 获取返回值类型
        final Class<?> retType = left.getClass().getComponentType();
        // 修复包的数组长度
        final int len = Array.getLength(left);
        // 系统原有的数组长度
        final int lenOld = Array.getLength(right);
        // 新数组
        final Object ret = Array.newInstance(retType, len + lenOld);
        for (int i = 0; i < len + lenOld; i++) {
            if (i < len) {
                // 首先从修复包的数组开始遍历添加到新数组
                Array.set(ret, i, Array.get(left, i));
            } else {
                // 接着从系统原有的数组开始遍历添加到新数组
                Array.set(ret, i, Array.get(right, i - len));
            }
        }
        return ret;
    }
}
