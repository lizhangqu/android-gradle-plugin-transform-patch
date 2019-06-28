package io.github.lizhangqu.transform.patch

import com.android.build.gradle.internal.pipeline.TransformTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState


public class AGPTransformPatchByPreInitPlugin implements Plugin<Project> {

    @Override
    final void apply(Project project) {
        if (AGPTransformPatch.shouldApplyPatch()) {
            project.gradle.addListener(new TaskExecutionListener() {
                @Override
                void beforeExecute(Task task) {
                    //noinspection GroovyAccessibility
                    if (task instanceof TransformTask && task.outputStream != null) {
                        //noinspection GroovyAccessibility
                        task.outputStream.init()
                    }
                }

                @Override
                void afterExecute(Task task, TaskState taskState) {

                }
            })
        }
    }
}
