package com.binlee.pluginization.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 20-11-27.
 *
 * @author binli
 */
public final class ProviderParser {

    private static final String TAG = "ProviderParser";

    private ProviderParser() {
    }

    public static void parse(Context context, String[] files) {
        for (String file : files) {
            parseProviders(context, file);
        }
    }

    public static void parseProviders(Context context, String plugin) {
        // 可以在宿主 apk 的 Application#attachBaseContent() 方法中执行此方法
        // 在此之前，需要先将插件中的 dex 合并到宿主中
        File file;
        try {
            file = new File(plugin);
        } catch (Throwable e) {
            throw new IllegalArgumentException();
        }

        // android.content.pm.PackageParser
        // public Package parsePackage(File packageFile, int flags)
        Object parser = Reflecter.on("android.content.pm.PackageParser").create();
        // parse plugin package info
        Class<?>[] parameters = {File.class, int.class};
        Object[] args = {file, PackageManager.GET_PROVIDERS};
        Object packageInfo = Reflecter.on(parser).call("parsePackage", parameters, args);

        // get providers form Package#providers -> ArrayList<Provider>
        List<?> providers = (List<?>) Reflecter.on(packageInfo).get("providers");

        // prepare params for android.content.pm.PackageParser
        // static generateProviderInfo(Provider p, int flags, PackageUserState state, int userId)
        Object state = Reflecter.on("android.content.pm.PackageUserState").create();
        int userId = (int) Reflecter.on("android.os.UserHandle").call("getCallingUserId");
        parameters = new Class[]{providers.get(0).getClass(), int.class, state.getClass(), int.class};
        args = new Object[]{null/*provider*/, 0, state, userId};

        List<ProviderInfo> infoList = new ArrayList<>(providers.size());
        for (Object p : providers) {
            args[0] = p;
            ProviderInfo info = (ProviderInfo) Reflecter.on(parser.getClass())
                    .call("generateProviderInfo", parameters, args);
            // 将插件的 packageName 替换为宿主的 packageName
            info.applicationInfo.packageName = context.getPackageName();
            infoList.add(info);
        }
        Log.d(TAG, "infoList: " + infoList);

        // install providers
        // ActivityThread -> private void installContentProviders(
        //         Context context, List<ProviderInfo> providers)
        parameters = new Class[]{Context.class, List.class};
        args = new Object[]{context, infoList};
        Reflecter.on(Hooks.getActivityThread()).call("installContentProviders", parameters, args);
    }
}
