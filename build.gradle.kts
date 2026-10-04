// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.spotless)
}

spotless {
    kotlin {
        target("app/src/**/*.kt")
        ktfmt("0.54").googleStyle().configure {
            it.setBlockIndent(4)
            it.setContinuationIndent(4)
            it.setMaxWidth(100)
        }
    }
    kotlinGradle {
        target("*.gradle.kts", "*/build.gradle.kts")
        ktfmt("0.54").googleStyle().configure {
            it.setBlockIndent(4)
            it.setContinuationIndent(4)
            it.setMaxWidth(100)
        }
    }
    format("text") {
        target(
            "*.md",
            "docs/**/*.md",
            ".gitignore",
            ".gitattributes",
            "gradle/*.toml",
            "scripts/**/*.ps1",
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
}
