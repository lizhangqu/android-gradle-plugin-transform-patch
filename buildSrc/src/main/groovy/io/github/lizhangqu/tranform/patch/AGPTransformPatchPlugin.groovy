package io.github.lizhangqu.tranform.patch

import org.gradle.api.Plugin
import org.gradle.api.Project


public class AGPTransformPatchPlugin implements Plugin<Project> {

    @Override
    final void apply(Project project) {
        AGPTransformPatch.applyAGPTransformPatch(project, new URL("file:/Users/lizhangqu/.gradle/caches/jars-3/14178045e3ebd34f2544774b98384ccf/android-gradle-plugin-transfrom-patch-1.0.0-20190627.011233-1.jar"))
    }
}
