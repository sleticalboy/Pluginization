package com.binlee.pluginization.hook;

/**
 * Created on 20-11-27.
 *
 * @author binli
 */
public interface HookListener {

    void before(Object rawCaller, String method, Object... args);

    Object after(Object rawResult);
}
