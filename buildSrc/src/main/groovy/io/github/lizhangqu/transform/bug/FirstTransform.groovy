package io.github.lizhangqu.transform.bug


import org.gradle.api.Project

class FirstTransform extends BaseTransform {

    FirstTransform(Project project) {
       super(project)
    }

    @Override
    String getName() {
        return 'first'
    }

}
