package org.helioviewer.jhv.platform;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.helioviewer.jhv.ExitHooks;

import java.awt.Desktop;

public class OSXHandler {

    public static void aboutHandler(Object action) {
        try {
            Method m = action.getClass().getDeclaredMethod("show", (Class[]) null);
            if (OSXAdapter.JAVA9) {
                try {
                    OSXAdapter adapter = new OSXAdapter("handleAbout", action, m);
                    Class<?> handlerClass = Class.forName("java.awt.desktop.AboutHandler");
                    Method addHandlerMethod = Desktop.class.getDeclaredMethod("setAboutHandler", new Class<?>[] { handlerClass });
                    Object adapterProxy = Proxy.newProxyInstance(OSXHandler.class.getClassLoader(), new Class<?>[] { handlerClass }, adapter);
                    addHandlerMethod.invoke(Desktop.getDesktop(), new Object[] { adapterProxy });
                 } catch (Exception e) {
                    e.printStackTrace();
                 }
            } else
                OSXAdapter.setAboutHandler(action, m);
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
    }

    public static void preferencesHandler(Object action) {
        try {
            Method m = action.getClass().getDeclaredMethod("show", (Class[]) null);
            if (OSXAdapter.JAVA9) {
                try {
                    OSXAdapter adapter = new OSXAdapter("handlePreferences", action, m);
                    Class<?> handlerClass = Class.forName("java.awt.desktop.PreferencesHandler");
                    Method addHandlerMethod = Desktop.class.getDeclaredMethod("setPreferencesHandler", new Class<?>[] { handlerClass });
                    Object adapterProxy = Proxy.newProxyInstance(OSXHandler.class.getClassLoader(), new Class<?>[] { handlerClass }, adapter);
                    addHandlerMethod.invoke(Desktop.getDesktop(), new Object[] { adapterProxy });
                 } catch (Exception e) {
                    e.printStackTrace();
                 }
            } else
                OSXAdapter.setPreferencesHandler(action, m);
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
    }

    public static void quitHandler() {
        try {
            Method m = ExitHooks.class.getDeclaredMethod("exitProgram", (Class[]) null);
            if (OSXAdapter.JAVA9) {
                try {
                    OSXAdapter adapter = new OSXAdapter("handleQuitRequestWith", "", m) {
                        @Override
                        public boolean callTarget(Object appleEvent, Object response) throws InvocationTargetException, IllegalAccessException {
                            Object result = targetMethod.invoke(targetObject, (Object[]) null);
                            boolean res = result == null || Boolean.parseBoolean(result.toString());

                            String sign = res ? "performQuit" : "cancelQuit";
                            try {
                                Method meth = response.getClass().getDeclaredMethod(sign);
                                meth.invoke(response, (Object[]) null);
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }

                            return res;
                        }
                    };
                    Class<?> handlerClass = Class.forName("java.awt.desktop.QuitHandler");
                    Method addHandlerMethod = Desktop.class.getDeclaredMethod("setQuitHandler", new Class<?>[] { handlerClass });
                    Object adapterProxy = Proxy.newProxyInstance(OSXHandler.class.getClassLoader(), new Class<?>[] { handlerClass }, adapter);
                    addHandlerMethod.invoke(Desktop.getDesktop(), new Object[] { adapterProxy });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                OSXAdapter.setQuitHandler("", m);
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
    }

}
