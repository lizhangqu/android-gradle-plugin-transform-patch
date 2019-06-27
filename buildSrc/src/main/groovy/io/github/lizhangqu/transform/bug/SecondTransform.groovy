package io.github.lizhangqu.transform.bug


import org.gradle.api.Project

class SecondTransform extends BaseTransform {

    SecondTransform(Project project) {
       super(project)
    }

    @Override
    String getName() {
        return 'second'
    }


}
