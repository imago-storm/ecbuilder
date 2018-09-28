package com.electriccloud.plugins.builder

import groovy.cli.picocli.CliBuilder

class CliProcessor {
    static final String EXECUTABLE_NAME = 'builder'

    def options
    def cli

    def run(String[] args) {
        if (args.size() < 1) {
            printUsageAndExit(createCli(""), -1)
        }
        def action = args.first()
        args = args.drop(1)
        cli = createCli(action)
        if (!action) {
            printUsageAndExit( -1)
        }
        options = cli.parse(args)
        if (this.metaClass.respondsTo(this, action)) {
            try {
                this."$action"()
            } catch (Throwable e) {
                System.err.println(e.message)
                System.exit(1)
            }
        }
        else {
            System.err.println("Wrong action $action")
            printUsageAndExit(-1)
        }
    }

    def build() {
        def path = options['plugin-folder']
        def version = options['version']

        if (!path) {
            path = System.getProperty("user.dir")
        }
        File pluginFolder = new File(path)
        if (!pluginFolder.exists()) {
            System.err.println("Folder $path does not exist")
            System.exit(-1)
        }
        PluginBuilder builder = new PluginBuilder(pluginFolder)
        if (version) {
            println "Version is $version"
            builder.setVersion(version)
        }
        builder.build()
    }

    def printUsageAndExit(cli = this.cli, exitCode = 0) {
        cli.usage()
        System.exit(exitCode)
    }


    def createCli(String action) {
        if (action == 'build') {
            def cli = new CliBuilder(name: EXECUTABLE_NAME + ' ' + action)
            cli.p(longOpt: 'plugin-folder', type: String, required: false, 'path to plugin folder')
            cli.v(
                longOpt: 'version',
                type: String,
                required: false,
                'plugin version (will be taken form plugin.xml by default')
            cli.h(longOpt: 'help', type: String, required: false, 'show cli help')
            return cli
        } else {
            def cli = new CliBuilder(name: EXECUTABLE_NAME + ' <action>')
            cli.h(longOpt: 'help', 'show cli help')
            cli.usageMessage.with {
                description("available actions are: build")
            }
            return cli
        }
    }
}
