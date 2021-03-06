package io.github.lizhangqu.transform.patch

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency


public class AGPTransformPatchPlugin implements Plugin<Project> {

    @Override
    final void apply(Project project) {
        if (AGPTransformPatch.shouldApplyPatch()) {
            File patchFile
            try {
                Dependency dependency = project.getDependencies().create("io.github.lizhangqu:android-gradle-plugin-transform-patch:1.0.0")
                Configuration configuration = project.getConfigurations().detachedConfiguration(dependency)
                configuration.setTransitive(false)
                configuration.resolutionStrategy.cacheDynamicVersionsFor(5, 'minutes')
                configuration.resolutionStrategy.cacheChangingModulesFor(0, 'seconds')
                patchFile = configuration.getSingleFile()
            } catch (Exception e) {

            }

            if (patchFile == null) {
                project.logger.error("can't get transform patch's patchFile")
                return
            }
            AGPTransformPatch.applyAGPTransformPatch(project, patchFile.toURI().toURL())
        }
    }
}
