package io.github.lizhangqu.tranform.patch;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * agp transform patch when use agp 3.2.0+
 */
public class AGPTransformPatch {
    public static boolean shouldApplyPatch() {
        String version = getAndroidGradlePluginVersionCompat();
        if (version == null) {
            return false;
        }
        String[] splitVersions = version.split("\\.");
        if (splitVersions == null || splitVersions.length < 3) {
            return false;
        }
        int major = Integer.parseInt(splitVersions[0]);
        int minor = Integer.parseInt(splitVersions[1]);
        //only agp 3.2.0+ need to apply patch
        if (major < 3 || (major == 3 && minor < 2)) {
            return false;
        }
        return true;
    }

    public static void applyAGPTransformPatch(Project project, URL url) {
        try {
            if (!shouldApplyPatch()) {
                return
            }
            //why not replace classloader ?
            //because there are some problems when replace it.
            //so we add the file to ucp's loader at first.
            ClassLoader originalClassloader = AGPTransformPatch.class.getClassLoader();
            Field ucpField = URLClassLoader.class.getDeclaredField("ucp");
            ucpField.setAccessible(true);
            Object ucp = ucpField.get(originalClassloader);
            Class ucpClass = ucp.getClass();
            Field loadersField = ucpClass.getDeclaredField("loaders");
            loadersField.setAccessible(true);
            List<Object> loaders = (List<Object>) loadersField.get(ucp);
            Method getLoaderMethod = ucpClass.getDeclaredMethod("getLoader", URL.class);
            getLoaderMethod.setAccessible(true);
            Object loader = getLoaderMethod.invoke(ucp, url);
            if (!loaders.first().getBaseURL().equals(loader.getBaseURL())) {
                loaders.add(0, loader);
            } else {
                loaders.set(0, loader);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GradleException("Apply agp transform patch failed, please report it.");
        }
    }

    static String getAndroidGradlePluginVersionCompat() {
        try {
            Class versionModel = Class.forName("com.android.builder.model.Version");
            Field versionFiled = versionModel.getDeclaredField("ANDROID_GRADLE_PLUGIN_VERSION");
            versionFiled.setAccessible(true);
            return (String) versionFiled.get(null);
        } catch (Exception e) {
            //ignore
        }
        return null;
    }
}
