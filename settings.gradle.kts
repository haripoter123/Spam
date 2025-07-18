pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Добавляем репозиторий JitPack для библиотеки Tdlibx
        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "MyTelegramApp"
include(":app")