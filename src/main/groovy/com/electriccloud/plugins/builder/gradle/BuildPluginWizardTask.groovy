package com.electriccloud.plugins.builder.gradle

import com.electriccloud.plugins.builder.PluginBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class BuildPluginWizardTask extends DefaultTask {
    final File pluginFolder = project.layout.projectDirectory

    @TaskAction
    void build() {
        PluginBuilder builder = new PluginBuilder(pluginFolder)
        builder.build()
    }
}
