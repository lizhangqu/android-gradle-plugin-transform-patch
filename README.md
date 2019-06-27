### android gradle plugin transform patch

#### 背景

在android gradle plugin 3.2.0以上版本，增量编译时如果aar发生了改变，则会出现类重复，典型的两个场景如下


A、以project依赖进行全量构建后，将project发布到远程，再将project依赖修改成aar远程依赖，再进行增量构建，此时会出现类重复。
B、全量构建完成后，此时更新任意一个aar，再进行增量构建，此时会出现类重复。



#### A复现步骤

1, 将app模块中对library模块的依赖修改为project依赖

将

```
dependencies {
    implementation "io.github.lizhangqu:library:1.0.0-SNAPSHOT"
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
    implementation "io.github.lizhangqu:library:1.0.0-SNAPSHOT"
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
    implementation "io.github.lizhangqu:library:1.0.0-SNAPSHOT"
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

5, 重新发布library模块，此时aar会被发布到工程根目录的repo目录下

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