### reproduced the bug

1, changed the aar dependency to project dependency in app module's build.gradle


change from

```
dependencies {
    implementation "io.github.lizhangqu:library:1.0.0-SNAPSHOT"
}
```

to

```

dependencies {
    implementation project(path: ':library')
}
```


2, clean and build app module and it will build successfully.

```
./gradlew :app:clean :app:assembleDebug
```

3, publish library module and the aar will publish to root project's dir named repo

```
./gradlew :library:clean :library:uploadSnapshot
```

4, changed the project dependency to aar dependency in app module's build.gradle


change from

```
dependencies {
    implementation project(path: ':library')
}
```

to

```
dependencies {
    implementation "io.github.lizhangqu:library:1.0.0-SNAPSHOT"
}
```

5, build app with incremental build and an error will happened with android gradle plugin 3.2.0+

```
./gradlew :app:assembleDebug
```