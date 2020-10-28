package com.sleticalboy.pluginization.util;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Created on 19-7-9.
 *
 * @author leebin
 */
public final class Proxies {

    private static final String TAG = "Proxies";

    private Proxies() {
        throw new AssertionError();
    }

    public static Object newAmProxy(final Context context, final InvocationHandler handler) {
        try {
            final Class<?>[] interfaces = {Reflections.refer("android.app.IActivityManager")};
            final Object proxyAm = Proxy.newProxyInstance(context.getClassLoader(), interfaces, handler);
            Log.d(TAG, "newAmProxy proxyAm: " + proxyAm);
            return proxyAm;
        } catch (Throwable e) {
            Log.e(TAG, "newAmProxy newAmProxy: error", e);
            return null;
        }
    }

    public static Object newAtmProxy(final Context context, final InvocationHandler handler) {
        try {
            final Class<?>[] interfaces = {Reflections.refer("android.app.IActivityTaskManager")};
            final Object proxyAtm = Proxy.newProxyInstance(context.getClassLoader(), interfaces, handler);
            Log.d(TAG, "newAtmProxy() proxy: " + proxyAtm);
            return proxyAtm;
        } catch (Throwable e) {
            Log.e(TAG, "newAtmProxy() error", e);
            return null;
        }
    }
}
