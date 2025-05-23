pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        // 加上这行，让插件也能从 JitPack 拿到
        maven("https://jitpack.io")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 加上这行，让你的 app module 能从 JitPack 拿到 PersistentCookieJar
        maven("https://jitpack.io")
    }
}

rootProject.name = "ArtsyFrontend"
include(":app")
