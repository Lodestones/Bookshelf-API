# Bookshelf API
Bookshelf API allows you to hook into the Bookshelf Plugin to access quality of life managers that you can integrate into your plugin.<br>

The Bookshelf API also provides utility classes that you can use such as:
- [Creating Custom Menus](https://github.com/Lodestones/Bookshelf/tree/master/docs/menu_builder.md)
- [Utility Classes](https://github.com/Lodestones/Bookshelf/tree/master/src/main/java/to/lodestone/bookshelfapi/api/util)
  - [ArrayUtil](https://github.com/Lodestones/Bookshelf/blob/master/src/main/java/to/lodestone/bookshelfapi/api/util/ArrayUtil.java)
  - [ByteUtil](https://github.com/Lodestones/Bookshelf/blob/master/src/main/java/to/lodestone/bookshelfapi/api/util/ByteUtil.java)
  - [EnumUtil](https://github.com/Lodestones/Bookshelf/blob/master/src/main/java/to/lodestone/bookshelfapi/api/util/EnumUtil.java)
  - [LocationUtil](https://github.com/Lodestones/Bookshelf/blob/master/src/main/java/to/lodestone/bookshelfapi/api/util/LocationUtil.java)
  - [MiniMessageUtil](https://github.com/Lodestones/Bookshelf/blob/master/src/main/java/to/lodestone/bookshelfapi/api/util/MiniMessageUtil.java)
    - Kyori Adventure Utility
    - Centering Messages
    - Converting MiniMessage to Components
    - Persisting Component Styles
  - [PackUtil](https://github.com/Lodestones/Bookshelf/blob/master/src/main/java/to/lodestone/bookshelfapi/api/util/PackUtil.java)
  - [StringUtil](https://github.com/Lodestones/Bookshelf/blob/master/src/main/java/to/lodestone/bookshelfapi/api/util/StringUtil.java)
  - [WorldUtil](https://github.com/Lodestones/Bookshelf/blob/master/src/main/java/to/lodestone/bookshelfapi/api/util/WorldUtil.java)
- [Cooldown Manager](https://github.com/Lodestones/Bookshelf/blob/master/src/main/java/to/lodestone/bookshelfapi/api/manager/ICooldownManager.java)
- Events
  - [Creating Events with BaseEvent](https://github.com/Lodestones/Bookshelf/blob/master/docs/creating_events.md)
  - [PlayerChatEvent](https://github.com/Lodestones/Bookshelf/blob/master/src/main/java/to/lodestone/bookshelfapi/api/event/PlayerChatEvent.java)
- [Creating Custom Config Files]((https://github.com/Lodestones/Bookshelf/blob/master/docs/creating_configs.md))
- [Task Schedulers](https://github.com/Lodestones/Bookshelf/blob/master/src/main/java/to/lodestone/bookshelfapi/api/Task.java)
- [Modrinth Version Updater](https://github.com/Lodestones/Bookshelf/blob/master/src/main/java/to/lodestone/bookshelfapi/api/VersionUpdater.java)
- [Fetching Players with Mojang API](https://github.com/Lodestones/Bookshelf/blob/master/src/main/java/to/lodestone/bookshelfapi/api/mojang/MojangProfile.java)
- [Another CommandAPI Builder](https://github.com/Lodestones/Bookshelf/blob/master/src/main/java/to/lodestone/bookshelfapi/api/command/Command.java)

## Table of Contents
1. [Installing the Bookshelf API](#installation)
2. [What's in the Bookshelf API](#api)
3. [Using the Bookshelf API](#usage)
4. [Contributing to the Bookshelf API](#contributing)
5. [Bookshelf API's License](#license)
6. [Donating to help support the Bookshelf API](#donating)

---

## Donating
Bookshelf among other various plugins I have worked on are free to use!
Please consider donating to my ko-fi! It helps fund other projects that I am passionate about.

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/E1E6RSLWV)

https://ko-fi.com/apollo30

---

# Installation
Below shows you how to install Bookshelf API into your project.
## Gradle
Add it in your root build.gradle at the end of repositories:
```java
    dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
        repositories {
		mavenCentral()
		maven { url 'https://jitpack.io' }
        }
    }
```
Add the dependency
```java
    dependencies {
        implementation 'com.github.Lodestones:Bookshelf:beta-v1.0.0'
    }
```
## Maven
Add the JitPack repository to your build file
```xml
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
```
Add the dependency
```xml
    <dependency>
        <groupId>com.github.Lodestones</groupId>
        <artifactId>Bookshelf</artifactId>
        <version>beta-v1.0.0</version>
    </dependency>
```

# Documentation
## API
TODO - Documentation Description

## Usage
Hooking into the Bookshelf API.

```java
import to.lodestone.bookshelfapi.IBookshelfAPI;

public class MainPlugin extends JavaPlugin {
    private BookshelfHook bookshelfHook; // Declare Bookshelf Hook Variable.

    @Override
    public void onEnable() {
        bookshelfHook = new BookshelfHook(this); // Hook into the Bookshelf API.       
    }

    public IBookshelfAPI bookshelf() { // use this method to access the Bookshelf API.
        return bookshelfHook.api(); // Retrieve the Bookshelf API from the Hook.       
    }
}
```

---

# Contributing
See something Bookshelf doesn't support, a bug or something that may be useful? We welcome contributions to improve the Bookshelf API. Open an issue [here](https://github.com/Lodestones/Bookshelf/issues).

---

# License
Bookshelf is protected by [Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)](https://creativecommons.org/licenses/by-nc/4.0/legalcode.txt). Please view the license [here](https://github.com/Lodestones/Bookshelf/blob/master/LICENSE).