package com.praveen2gemini.binder;

import android.app.Activity;

import com.praveen2gemini.lib.annotations.internal.BindingSuffix;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Praveen Kumar Sugumaran
 */
public class DXViewBinding {

    private static Object targetRefClass = null;

    private DXViewBinding() {
        // not to be instantiated in public
    }

    private static <T extends Activity> void initiateBinder(T target, String suffix) {
        Class<?> targetClass = target.getClass();
        String className = targetClass.getName();
        try {
            Class<?> bindingClass = targetClass
                    .getClassLoader()
                    .loadClass(className + suffix);
            Constructor<?> classConstructor = bindingClass.getConstructor(targetClass);
            try {
                targetRefClass = classConstructor.newInstance(target);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to invoke " + classConstructor, e);
            } catch (InstantiationException e) {
                throw new RuntimeException("Unable to invoke " + classConstructor, e);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                if (cause instanceof Error) {
                    throw (Error) cause;
                }
                throw new RuntimeException("Unable to create instance.", cause);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to find Class for " + className + suffix, e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to find constructor for " + className + suffix, e);
        }
    }

    public static <T extends Activity> void bind(T activity) {
        initiateBinder(activity, BindingSuffix.GENERATED_CLASS_SUFFIX);
    }

    public static <T extends Activity> void unbind(T activity) {

        Class<?> targetClass = activity.getClass();
        String className = targetClass.getName();
        try {
            Class<?> bindingClass = targetClass
                    .getClassLoader()
                    .loadClass(className + BindingSuffix.GENERATED_CLASS_SUFFIX);
            if (activity instanceof Activity) {
                Method privateStringMethod = bindingClass.
                        getDeclaredMethod("unbindAll", targetClass);
                privateStringMethod.setAccessible(true);
                privateStringMethod.invoke(targetRefClass, activity);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to find Class for " + className + BindingSuffix.GENERATED_CLASS_SUFFIX, e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to find method for " + targetRefClass.getClass().getSimpleName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to invoke " + targetRefClass.getClass().getSimpleName(), e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new RuntimeException("Unable to create instance.", cause);
        }
    }
}
