# CrazyAdvancementsAPI


## About

CrazyAdvancementsAPI is an API for creating and managing Advancements programmatically on Paper Servers.


## Compiling
Use `./gradlew build` to build the plugin. Output will be in `build/libs/`.


## Gradle
To use this API in a plugin, first publish it to your local Maven repository:
```
./gradlew publishToMavenLocal
```
Then, add it to your dependencies.
```kts
repositories {
    mavenLocal()
}
```
```kts
dependencies {
    compileOnly("eu.endercentral.crazy_advancements:CrazyAdvancementsAPI:VERSION")
}
```
Check [build.gradle.kts](https://github.com/gecko10000/CrazyAdvancementsAPI/blob/26.2/build.gradle.kts#L12) for the version to use. It will always align with the Minecraft version, and have an extra number at the end to show the revision for that version.

## Documentation

The Official Documentation can be found [here][0]

There is also Javadoc available [here][1]

[0]: https://docs.crazyadvancements.endercentral.eu "Official Documentation"
[1]: https://javadoc.crazyadvancements.endercentral.eu "Javadoc"
