package com.electriccloud.plugins.builder.gradle

import org.gradle.api.Project

class PluginWizardPluginExtension {
    final File pluginFolder

    PluginWizardPluginExtension(Project project) {
        pluginFolder = project.layout.projectDirectory
    }

}
