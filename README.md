### android gradle plugin transform patch

#### 背景

在android gradle plugin 3.2.0以上版本，增量编译时如果aar发生了改变，则会出现类重复，典型的两个场景如下

A、全量构建完成后，此时更新任意一个aar，再进行增量构建，此时会出现类重复。
B、以project依赖进行全量构建后，将project发布到远程，再将project依赖修改成aar远程依赖，再进行增量构建，此时会出现类重复。


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

#### 修复方式

1、应用修复bug用的patch插件

```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath('io.github.lizhangqu:plugin-agp-transfrom-patch:1.0.0')
    }
}
apply plugin: 'agp-transform-patch'
```

2、重复A复现步骤和B复现步骤，类重复问题已经被修复