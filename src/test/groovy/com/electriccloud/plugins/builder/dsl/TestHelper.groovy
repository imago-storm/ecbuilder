package com.electriccloud.plugins.builder.dsl

import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.plugins.builder.PluginBuilder
import com.electriccloud.plugins.builder.PluginMetadata

class TestHelper {

    static def getPlugins() {
        String pluginsTestFolder = System.getenv('EC_BUILDER_TEST_FOLDER')
        assert pluginsTestFolder: "EC_BUILDER_TEST_FOLDER variable must be provided"
        def plugins = []
        new File(pluginsTestFolder).eachFile {
            if (it.name.startsWith('EC-') && isPluginWizard(it)) {

                plugins.add(it.absolutePath)
            }
        }
        return ['/Users/imago/Documents/ecloud/plugins/containers/EC-Kubernetes']
        return plugins
    }

    static def isPluginWizard(File folder) {
        return new File(folder, 'dsl/promote.groovy').exists()
    }


    def buildEF() {
        ElectricFlow ef = new ElectricFlow()
        ef.login(commanderServer, 'admin', 'changeme')
        return ef
    }


    def getCommanderServer() {
        String commanderServer = System.getenv('COMMANDER_SERVER')
        assert commanderServer: "COMMANDER_SERVER environment variable is not provided"
        return commanderServer
    }


    def installPlugin(File archive) {
        runCommand("ectool --server $commanderServer installPlugin ${archive.absolutePath}")
    }

    def uninstallPlugin(pluginName) {
        runCommand("ectool --server $commanderServer uninstallPlugin $pluginName")
    }

    def promotePlugin(pluginName) {
        runCommand("ectool --server $commanderServer promotePlugin  $pluginName")
    }

    def login() {
        String username = System.getenv('COMMANDER_USERNAME') ?: 'admin'
        String password = System.getenv('COMMANDER_PASSWORD') ?: 'changeme'
        runCommand("ectool --server $commanderServer login  $username $password")
    }

    def buildPlugin(pluginName) {
        PluginBuilder builder = new PluginBuilder(getPluginFolder(pluginName))
        return builder.build()
    }

    def getPluginFolder(pluginName) {
        File pluginFolder = new File(this.class.getResource("/$pluginName").toURI())
        return pluginFolder
    }

    def getPluginMetadata(pluginName) {
        PluginBuilder builder = new PluginBuilder(getPluginFolder(pluginName))
        return builder.readPluginMetadata()
    }

    def runCommand(command) {
        def stdout = new StringBuilder()
        def stderr = new StringBuilder()
        def process = command.execute()
        process.consumeProcessOutput(stdout, stderr)
        process.waitForOrKill(20 * 1000)
        println "STDOUT: $stdout"
        println "STDERR: $stderr"
        println "Exit Code: ${process.exitValue()}"
        def text = "$stdout\n$stderr"
        assert process.exitValue() == 0 : "Command $command failed to execute: exit code is ${process.exitValue()}"
        text
    }

}
