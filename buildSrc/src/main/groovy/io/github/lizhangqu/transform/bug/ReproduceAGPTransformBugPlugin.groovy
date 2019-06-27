package io.github.lizhangqu.transform.bug

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

public class ReproduceAGPTransformBugPlugin implements Plugin<Project> {

    @Override
    final void apply(Project project) {
        if (!project.getPlugins().hasPlugin('com.android.application') && !project.getPlugins().hasPlugin('com.android.library')) {
            throw new GradleException('apply plugin: \'com.android.application\' or apply plugin: \'com.android.library\' is required')
        }
        project.android.registerTransform(new FirstTransform(project))
        project.android.registerTransform(new SecondTransform(project))
    }
}
