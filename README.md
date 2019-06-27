### android gradle plugin transform patch

#### 背景

在android gradle plugin 3.2.0以上版本，增量编译时如果aar发生了改变，则会出现类重复，典型的两个场景如下


 - A、以project依赖进行全量构建后，将project发布到远程，再将project依赖修改成aar远程依赖，再进行增量构建，此时会出现类重复。
 - B、全量构建完成后，此时更新任意一个aar，再进行增量构建，此时会出现类重复。

#### 问题产生原因

```
@NonNull
TransformOutputProvider asOutput(boolean isIncremental) throws IOException {
    if (!isIncremental) {
        FileUtils.deleteIfExists(new File(getRootLocation(), SubStream.FN_FOLDER_CONTENT));
    }
    init();
    return new TransformOutputProviderImpl(folderUtils);
}
```

在android gradle plugin 3.2.0及以上版本中，如果第一个transform不是增量构建的，则会删除该transform目录下的__content__.json文件，触发文件名命名规则归0递增。当触发背景中的两个场景的时候，导致原先该被remove的文件没有触发removed事件，而是变成了changed事件，出现类重复。

但是在3.2.0以下版本，该文件不会被删除，文件名命名规则从该文件中最大的index开始递增，当触发背景中的两个场景的时候，由于文件名递增没有被清零所以新增文件触发added事件，原先该被remove的文件触发removed事件，不会出现类重复

#### 如何使用修复插件

```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath('io.github.lizhangqu:plugin-agp-transform-patch:1.0.0')
    }
}
apply plugin: 'agp-transform-patch'
```


#### A复现步骤

1, 将app模块中对library模块的依赖修改为project依赖

将

```
dependencies {
    implementation("io.github.lizhangqu:library:1.0.0-SNAPSHOT") {
        changing = true
    }
}
```


修改为
```

dependencies {
    implementation project(path: ':library')
}
```

2，应用相应的复现bug用的插件

```
apply plugin: 'reproduce-agp-transform-bug'
```


3, clean并且进行app模块的全量构建

```
./gradlew :app:clean :app:assembleDebug
```

4, 将library模块clean后发布成aar，此时aar会被发布到工程根目录的repo目录下

```
./gradlew :library:clean :library:uploadSnapshot
```

5, 将app模块中对library模块的依赖修改成aar依赖


将

```
dependencies {
    implementation project(path: ':library')
}
```

修改为

```
dependencies {
    implementation("io.github.lizhangqu:library:1.0.0-SNAPSHOT") {
        changing = true
    }
}
```

6, 不进行clean再构建app模块，此时会进行增量构建，并出现类重复的问题

```
./gradlew :app:assembleDebug
```

#### B复现步骤

1, 将app模块中对library模块的依赖修改为aar依赖

将

```
dependencies {
    implementation project(path: ':library')
}
```

修改为

```
dependencies {
    implementation("io.github.lizhangqu:library:1.0.0-SNAPSHOT") {
        changing = true
    }
}
```

2，应用相应的复现bug用的插件

```
apply plugin: 'reproduce-agp-transform-bug'
```

3, 将library模块clean后发布成aar，此时aar会被发布到工程根目录的repo目录下

```
./gradlew :library:clean :library:uploadSnapshot
```

4, clean并且进行app模块的全量构建

```
./gradlew :app:clean :app:assembleDebug
```

5, 修改library模块中任意代码后，重新发布library模块，此时aar会被发布到工程根目录的repo目录下

```
./gradlew :library:clean :library:uploadSnapshot
```

6, 不进行clean再构建app模块，此时会进行增量构建，并出现类重复的问题

```
./gradlew :app:assembleDebug
```


#### A修复方案

1、应用修复bug用的patch插件

```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath('io.github.lizhangqu:plugin-agp-transform-patch:1.0.0')
    }
}
apply plugin: 'agp-transform-patch'
```

2、重复A复现步骤，类重复问题已经被修复

#### B修复方案1

1、将buildSrc工程下的BaseTransform中的getContentLocation调用第一个入参进行修改

由
```
transformInvocation.outputProvider.getContentLocation(it.getName(), it.contentTypes, it.scopes, Format.JAR)
transformInvocation.outputProvider.getContentLocation(it.getName(), it.contentTypes, it.scopes, Format.DIRECTORY)
```

修改为

```
transformInvocation.outputProvider.getContentLocation(it.getFile().toString(), it.contentTypes, it.scopes, Format.JAR)
transformInvocation.outputProvider.getContentLocation(it.getFile().toString(), it.contentTypes, it.scopes, Format.DIRECTORY)
```

2、重复B复现步骤，类重复问题已经被修复

但是此修复方式对第三方插件无效，只对自己写的transform可以修改，因此完美的解决方案请看B修复方案2

#### B修复方案2

1、应用修复bug用的patch插件

```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath('io.github.lizhangqu:plugin-agp-transform-patch:1.0.0')
    }
}
apply plugin: 'agp-transform-patch'
```

2、重复B复现步骤，类重复问题已经被修复