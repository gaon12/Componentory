import java.io.ByteArrayInputStream
import java.util.jar.JarInputStream
import java.util.zip.ZipFile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "xyz.gaon.componentory"
    compileSdk { version = release(37) }

    defaultConfig {
        applicationId = "xyz.gaon.componentory"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "AUTOFILL_VERSION", "\"${libs.versions.autofill.get()}\"")
        testInstrumentationRunner = "xyz.gaon.componentory.testing.ComponentoryTestRunner"
        buildConfigField(
            "String",
            "MATERIAL2_VERSION",
            "\"${libs.versions.composeMaterial2.get()}\"",
        )
        buildConfigField(
            "String",
            "MATERIAL3_VERSION",
            "\"${libs.versions.composeMaterial3.get()}\"",
        )
        buildConfigField(
            "String",
            "MATERIAL_ICONS_VERSION",
            "\"${libs.versions.composeMaterialIcons.get()}\"",
        )
    }

    buildTypes { release { optimization { enable = false } } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    // Keep every supported language available when switching without a network connection.
    bundle { language { enableSplit = false } }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.autofill) { version { strictly(libs.versions.autofill.get()) } }
    // Pin the sample libraries so the displayed version cannot drift through BOM resolution.
    implementation(libs.androidx.compose.material) {
        version { strictly(libs.versions.composeMaterial2.get()) }
    }
    implementation(libs.androidx.compose.material3) {
        version { strictly(libs.versions.composeMaterial3.get()) }
    }
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material.icons) {
        version { strictly(libs.versions.composeMaterialIcons.get()) }
    }
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

abstract class GenerateIconCatalog : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val archives: ConfigurableFileCollection

    @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val iconClass =
            Regex(
                "androidx/compose/material/icons/(automirrored/)?(filled|outlined|rounded|sharp|twotone)/[^/$]+Kt\\.class"
            )
        val names = sortedSetOf<String>()
        archives.files.forEach { archive ->
            ZipFile(archive).use { aar ->
                val bytes = aar.getInputStream(aar.getEntry("classes.jar")).use { it.readBytes() }
                JarInputStream(ByteArrayInputStream(bytes)).use { jar ->
                    var entry = jar.nextJarEntry
                    while (entry != null) {
                        if (iconClass.matches(entry.name))
                            names += entry.name.removeSuffix(".class").replace('/', '.')
                        entry = jar.nextJarEntry
                    }
                }
            }
        }
        check(names.isNotEmpty()) { "The pinned icon libraries must contain public icons." }
        outputDirectory.file("material-icons.txt").get().asFile.apply {
            parentFile.mkdirs()
            writeText(names.joinToString("\n", postfix = "\n"))
        }
    }
}

val iconCatalogArchives =
    configurations.create("iconCatalogArchives") {
        isCanBeConsumed = false
        isTransitive = false
    }

dependencies {
    listOf("material-icons-core-android", "material-icons-extended-android").forEach { artifact ->
        add(
            iconCatalogArchives.name,
            "androidx.compose.material:$artifact:${libs.versions.composeMaterialIcons.get()}@aar",
        )
    }
}

val generateIconCatalog =
    tasks.register<GenerateIconCatalog>("generateIconCatalog") {
        archives.from(iconCatalogArchives)
        outputDirectory.set(layout.buildDirectory.dir("generated/iconCatalog"))
    }

abstract class PackageComponentInventory : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val sourceFile: RegularFileProperty

    @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun packageInventory() {
        // Package the audited source inventory used by coverage checks.
        val destination = outputDirectory.file("component-inventory.csv").get().asFile
        destination.parentFile.mkdirs()
        sourceFile.get().asFile.copyTo(destination, overwrite = true)
    }
}

val packageComponentInventory =
    tasks.register<PackageComponentInventory>("packageComponentInventory") {
        sourceFile.set(rootProject.layout.projectDirectory.file("docs/component-inventory.csv"))
        outputDirectory.set(layout.buildDirectory.dir("generated/componentInventory"))
    }

abstract class PackageAndroidHistory : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectory: DirectoryProperty
    @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun packageHistory() {
        listOf("public-ui.csv", "provenance.json").forEach { name ->
            val destination = outputDirectory.file("history/$name").get().asFile
            destination.parentFile.mkdirs()
            sourceDirectory.file(name).get().asFile.copyTo(destination, overwrite = true)
        }
    }
}

val packageAndroidHistory =
    tasks.register<PackageAndroidHistory>("packageAndroidHistory") {
        sourceDirectory.set(rootProject.layout.projectDirectory.dir("data/history"))
        outputDirectory.set(layout.buildDirectory.dir("generated/androidHistory"))
    }

abstract class PackageSourceNotices : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectory: DirectoryProperty
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val applicationLicense: RegularFileProperty
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val applicationNotice: RegularFileProperty
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val privacyPolicy: RegularFileProperty
    @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun packageNotices() {
        val sources =
            sourceDirectory
                .get()
                .asFile
                .walkTopDown()
                .filter { it.isFile }
                .map { it.relativeTo(sourceDirectory.get().asFile).invariantSeparatorsPath to it }
                .toList() +
                listOf(
                    "Componentory-MIT.txt" to applicationLicense.get().asFile,
                    "NOTICE.txt" to applicationNotice.get().asFile,
                    "PrivacyPolicy.txt" to privacyPolicy.get().asFile,
                )
        sources.forEach { (name, source) ->
            val destination = outputDirectory.file("legal/$name").get().asFile
            destination.parentFile.mkdirs()
            source.copyTo(destination, overwrite = true)
        }
    }
}

val packageSourceNotices =
    tasks.register<PackageSourceNotices>("packageSourceNotices") {
        sourceDirectory.set(rootProject.layout.projectDirectory.dir("licenses"))
        applicationLicense.set(rootProject.layout.projectDirectory.file("LICENSE"))
        applicationNotice.set(rootProject.layout.projectDirectory.file("NOTICE"))
        privacyPolicy.set(rootProject.layout.projectDirectory.file("docs/privacy-policy.txt"))
        outputDirectory.set(layout.buildDirectory.dir("generated/sourceNotices"))
    }

androidComponents.onVariants { variant ->
    variant.sources.assets?.addGeneratedSourceDirectory(
        packageSourceNotices,
        PackageSourceNotices::outputDirectory,
    )
    variant.sources.assets?.addGeneratedSourceDirectory(
        packageAndroidHistory,
        PackageAndroidHistory::outputDirectory,
    )
    variant.sources.assets?.addGeneratedSourceDirectory(
        generateIconCatalog,
        GenerateIconCatalog::outputDirectory,
    )
    variant.sources.assets?.addGeneratedSourceDirectory(
        packageComponentInventory,
        PackageComponentInventory::outputDirectory,
    )
}
