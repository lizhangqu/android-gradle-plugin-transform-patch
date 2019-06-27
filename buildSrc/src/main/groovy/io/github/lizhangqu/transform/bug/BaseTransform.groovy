package io.github.lizhangqu.transform.bug

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.collect.ImmutableSet
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.util.GFileUtils


class BaseTransform extends Transform {

    protected Project project

    BaseTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return 'base'
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        if (project.getPlugins().hasPlugin("com.android.application")) {
            return TransformManager.SCOPE_FULL_PROJECT
        } else {
            return ImmutableSet.of(QualifiedContent.Scope.PROJECT)
        }
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        if (!transformInvocation.isIncremental()) {
            transformInvocation.getOutputProvider().deleteAll();
        }

        transformInvocation.inputs.each {
            it.jarInputs.each {
                File dest = transformInvocation.outputProvider.getContentLocation(it.getName(), it.contentTypes, it.scopes, Format.JAR)
                if (transformInvocation.isIncremental()) {
                    switch (it.getStatus()) {
                        case Status.NOTCHANGED:
                            project.logger.error "NOTCHANGED file is: ${it.file} dest: ${dest}"
                            break;
                        case Status.CHANGED:
                        case Status.ADDED:
                            project.logger.error "${it.getStatus()} file is: ${it.file} dest: ${dest}"
                            GFileUtils.copyFile(it.file, dest)
                            break;
                        case Status.REMOVED:
                            project.logger.error "REMOVED file is: ${it.file} dest: ${dest}"
                            GFileUtils.deleteQuietly(dest)
                            break;
                    }
                } else {
                    project.logger.error "no incremental: ${it.file}  dest: ${dest}"
                    GFileUtils.copyFile(it.file, dest)
                }
            }
            it.directoryInputs.each {
                File inputDir = it.getFile()
                File outputDir = transformInvocation.outputProvider.getContentLocation(it.getName(), it.contentTypes, it.scopes, Format.DIRECTORY)
                if (transformInvocation.isIncremental()) {
                    for (Map.Entry<File, Status> changedInput : it.getChangedFiles().entrySet()) {
                        File inputFile = changedInput.getKey()
                        String relativePath = com.android.utils.FileUtils.relativePossiblyNonExistingPath(inputFile, inputDir)
                        File outputFile = new File(outputDir, relativePath)
                        switch (changedInput.getValue()) {
                            case Status.NOTCHANGED:
                                project.logger.error "NOTCHANGED file is: ${inputFile} dest: ${outputFile}"
                                break;
                            case Status.REMOVED:
                                project.logger.error "REMOVED file is: ${inputFile} dest: ${outputFile}"
                                GFileUtils.deleteQuietly(outputFile)
                                break
                            case Status.ADDED:
                            case Status.CHANGED:
                                project.logger.error "${changedInput.getValue()} file is: ${inputFile} dest: ${outputFile}"
                                if (inputFile.isFile() && !inputFile.isDirectory()) {
                                    GFileUtils.deleteQuietly(outputFile)
                                    FileUtils.copyFile(inputFile, outputFile)
                                }
                        }
                    }
                } else {
                    project.logger.error "no incremental: ${inputDir}"
                    GFileUtils.deleteQuietly(outputDir)
                    FileUtils.copyDirectory(inputDir, outputDir)
                }
            }
        }
    }
}
