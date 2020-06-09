package com.praveen2gemini.compiler.lib;

import com.praveen2gemini.lib.annotations.internal.BindingSuffix;

public final class NameStore {

    private NameStore() {
        // not to be instantiated in public
    }

    public static String getGeneratedClassName(String clsName) {
        return clsName + BindingSuffix.GENERATED_CLASS_SUFFIX;
    }

    public static class Package {
        public static final String ANDROID_VIEW = "android.view";
        public static final String ANDROID_CONTENT = "android.content";
    }

    public static class Class {
        // Android
        public static final String ANDROID_VIEW = "View";
        public static final String ANDROID_VIEW_ON_CLICK_LISTENER = "OnClickListener";
        public static final String ANDROID_CONTEXT = "Context";
        public static final String ANDROID_INTENT = "Intent";
        public static final String ANDROID_BROADCAST_RECEIVER = "BroadcastReceiver";
        public static final String ANDROID_INTENT_FILTER = "IntentFilter";
    }

    public static class Method {
        // Android
        public static final String ANDROID_VIEW_ON_CLICK = "onClick";
        public static final String ANDROID_VIEW_ON_RECEIVER = "onReceive";

        // Binder
        public static final String BIND_VIEWS = "bindViews";
        public static final String BIND_ON_CLICKS = "bindOnClicks";
        public static final String BIND_ON_RECEIVERS = "bindOnReceivers";
        public static final String UNBIND_ALL = "unbindAll";
        public static final String BIND = "bind";
    }

    public static class Variable {
        public static final String ANDROID_ACTIVITY = "activity";
        public static final String ANDROID_VIEW = "view";
        public static final String ANDROID_CONTEXT = "context";
        public static final String ANDROID_INTENT = "intent";
    }
}

