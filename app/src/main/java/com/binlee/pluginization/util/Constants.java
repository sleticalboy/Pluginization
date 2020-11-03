package com.binlee.pluginization.util;

import android.content.ComponentName;

public final class Constants {

    public static final ComponentName PROXY_ACTIVITY = new ComponentName(
            "com.binlee.pluginization", "com.binlee.pluginization.ProxyActivity");
    public static final ComponentName PROXY_SERVICE = new ComponentName(
            "com.binlee.pluginization", "com.binlee.pluginization.ProxyService");
    public static final String REAL_COMPONENT = "real_component";

    public static final int LAUNCH_ACTIVITY = 100;
    public static final int PAUSE_ACTIVITY = 101;
    public static final int PAUSE_ACTIVITY_FINISHING = 102;
    public static final int STOP_ACTIVITY_SHOW = 103;
    public static final int STOP_ACTIVITY_HIDE = 104;
    public static final int SHOW_WINDOW = 105;
    public static final int HIDE_WINDOW = 106;
    public static final int RESUME_ACTIVITY = 107;
    public static final int SEND_RESULT = 108;
    public static final int DESTROY_ACTIVITY = 109;
    public static final int BIND_APPLICATION = 110;
    public static final int EXIT_APPLICATION = 111;
    public static final int NEW_INTENT = 112;
    public static final int RECEIVER = 113;
    public static final int CREATE_SERVICE = 114;
    public static final int SERVICE_ARGS = 115;
    public static final int STOP_SERVICE = 116;
    public static final int CONFIGURATION_CHANGED = 118;
    public static final int CLEAN_UP_CONTEXT = 119;
    public static final int GC_WHEN_IDLE = 120;
    public static final int BIND_SERVICE = 121;
    public static final int UNBIND_SERVICE = 122;
    public static final int DUMP_SERVICE = 123;
    public static final int LOW_MEMORY = 124;
    public static final int ACTIVITY_CONFIGURATION_CHANGED = 125;
    public static final int RELAUNCH_ACTIVITY = 126;
    public static final int PROFILER_CONTROL = 127;
    public static final int CREATE_BACKUP_AGENT = 128;
    public static final int DESTROY_BACKUP_AGENT = 129;
    public static final int SUICIDE = 130;
    public static final int REMOVE_PROVIDER = 131;
    public static final int ENABLE_JIT = 132;
    public static final int DISPATCH_PACKAGE_BROADCAST = 133;
    public static final int SCHEDULE_CRASH = 134;
    public static final int DUMP_HEAP = 135;
    public static final int DUMP_ACTIVITY = 136;
    public static final int SLEEPING = 137;
    public static final int SET_CORE_SETTINGS = 138;
    public static final int UPDATE_PACKAGE_COMPATIBILITY_INFO = 139;
    public static final int TRIM_MEMORY = 140;
    public static final int DUMP_PROVIDER = 141;
    public static final int UNSTABLE_PROVIDER_DIED = 142;
    public static final int REQUEST_ASSIST_CONTEXT_EXTRAS = 143;
    public static final int TRANSLUCENT_CONVERSION_COMPLETE = 144;
    public static final int INSTALL_PROVIDER = 145;
    public static final int ON_NEW_ACTIVITY_OPTIONS = 146;
    public static final int ENTER_ANIMATION_COMPLETE = 149;
    public static final int MULTI_WINDOW_MODE_CHANGED = 152;
    public static final int PICTURE_IN_PICTURE_MODE_CHANGED = 153;
    public static final int LOCAL_VOICE_INTERACTION_STARTED = 154;
    public static final int ATTACH_AGENT = 155;
    public static final int APPLICATION_INFO_CHANGED = 156;
    public static final int ACTIVITY_MOVED_TO_DISPLAY = 157;
    public static final int EXECUTE_TRANSACTION = 159;

    public static String codeToString(int code) {
        final String result;
        switch (code) {
            case LAUNCH_ACTIVITY:
                result = "LAUNCH_ACTIVITY";
                break;
            case PAUSE_ACTIVITY:
                result = "PAUSE_ACTIVITY";
                break;
            case PAUSE_ACTIVITY_FINISHING:
                result = "PAUSE_ACTIVITY_FINISHING";
                break;
            case STOP_ACTIVITY_SHOW:
                result = "STOP_ACTIVITY_SHOW";
                break;
            case STOP_ACTIVITY_HIDE:
                result = "STOP_ACTIVITY_HIDE";
                break;
            case SHOW_WINDOW:
                result = "SHOW_WINDOW";
                break;
            case HIDE_WINDOW:
                result = "HIDE_WINDOW";
                break;
            case RESUME_ACTIVITY:
                result = "RESUME_ACTIVITY";
                break;
            case SEND_RESULT:
                result = "SEND_RESULT";
                break;
            case DESTROY_ACTIVITY:
                result = "DESTROY_ACTIVITY";
                break;
            case BIND_APPLICATION:
                result = "BIND_APPLICATION";
                break;
            case EXIT_APPLICATION:
                result = "EXIT_APPLICATION";
                break;
            case NEW_INTENT:
                result = "NEW_INTENT";
                break;
            case RECEIVER:
                result = "RECEIVER";
                break;
            case CREATE_SERVICE:
                result = "CREATE_SERVICE";
                break;
            case SERVICE_ARGS:
                result = "SERVICE_ARGS";
                break;
            case STOP_SERVICE:
                result = "STOP_SERVICE";
                break;
            case CONFIGURATION_CHANGED:
                result = "CONFIGURATION_CHANGED";
                break;
            case CLEAN_UP_CONTEXT:
                result = "CLEAN_UP_CONTEXT";
                break;
            case GC_WHEN_IDLE:
                result = "GC_WHEN_IDLE";
                break;
            case BIND_SERVICE:
                result = "BIND_SERVICE";
                break;
            case UNBIND_SERVICE:
                result = "UNBIND_SERVICE";
                break;
            case DUMP_SERVICE:
                result = "DUMP_SERVICE";
                break;
            case LOW_MEMORY:
                result = "LOW_MEMORY";
                break;
            case ACTIVITY_CONFIGURATION_CHANGED:
                result = "ACTIVITY_CONFIGURATION_CHANGED";
                break;
            case ACTIVITY_MOVED_TO_DISPLAY:
                result = "ACTIVITY_MOVED_TO_DISPLAY";
                break;
            case RELAUNCH_ACTIVITY:
                result = "RELAUNCH_ACTIVITY";
                break;
            case PROFILER_CONTROL:
                result = "PROFILER_CONTROL";
                break;
            case CREATE_BACKUP_AGENT:
                result = "CREATE_BACKUP_AGENT";
                break;
            case DESTROY_BACKUP_AGENT:
                result = "DESTROY_BACKUP_AGENT";
                break;
            case SUICIDE:
                result = "SUICIDE";
                break;
            case REMOVE_PROVIDER:
                result = "REMOVE_PROVIDER";
                break;
            case ENABLE_JIT:
                result = "ENABLE_JIT";
                break;
            case DISPATCH_PACKAGE_BROADCAST:
                result = "DISPATCH_PACKAGE_BROADCAST";
                break;
            case SCHEDULE_CRASH:
                result = "SCHEDULE_CRASH";
                break;
            case DUMP_HEAP:
                result = "DUMP_HEAP";
                break;
            case DUMP_ACTIVITY:
                result = "DUMP_ACTIVITY";
                break;
            case SLEEPING:
                result = "SLEEPING";
                break;
            case SET_CORE_SETTINGS:
                result = "SET_CORE_SETTINGS";
                break;
            case UPDATE_PACKAGE_COMPATIBILITY_INFO:
                result = "UPDATE_PACKAGE_COMPATIBILITY_INFO";
                break;
            case TRIM_MEMORY:
                result = "TRIM_MEMORY";
                break;
            case DUMP_PROVIDER:
                result = "DUMP_PROVIDER";
                break;
            case UNSTABLE_PROVIDER_DIED:
                result = "UNSTABLE_PROVIDER_DIED";
                break;
            case REQUEST_ASSIST_CONTEXT_EXTRAS:
                result = "REQUEST_ASSIST_CONTEXT_EXTRAS";
                break;
            case TRANSLUCENT_CONVERSION_COMPLETE:
                result = "TRANSLUCENT_CONVERSION_COMPLETE";
                break;
            case INSTALL_PROVIDER:
                result = "INSTALL_PROVIDER";
                break;
            case ON_NEW_ACTIVITY_OPTIONS:
                result = "ON_NEW_ACTIVITY_OPTIONS";
                break;
            case ENTER_ANIMATION_COMPLETE:
                result = "ENTER_ANIMATION_COMPLETE";
                break;
            case MULTI_WINDOW_MODE_CHANGED:
                result = "MULTI_WINDOW_MODE_CHANGED";
                break;
            case PICTURE_IN_PICTURE_MODE_CHANGED:
                result = "PICTURE_IN_PICTURE_MODE_CHANGED";
                break;
            case LOCAL_VOICE_INTERACTION_STARTED:
                result = "LOCAL_VOICE_INTERACTION_STARTED";
                break;
            case ATTACH_AGENT:
                result = "ATTACH_AGENT";
                break;
            case APPLICATION_INFO_CHANGED:
                result = "APPLICATION_INFO_CHANGED";
                break;
            case EXECUTE_TRANSACTION:
                result = "EXECUTE_TRANSACTION";
                break;
            default:
                result = Integer.toString(code);
                break;
        }
        return code + ": " + result;
    }

    private Constants() {
        //no instance
    }
}
