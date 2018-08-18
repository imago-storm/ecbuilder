package com.electriccloud.plugins.builder

import groovy.cli.picocli.CliBuilder

class Main {
    public static void main(String[] args) {
        def cli = new CliBuilder()

        cli.p(longOpt: 'plugin-folder', type: String, required: false, 'path to plugin folder')
        cli.v(longOpt: 'version', type: String, required: false, 'plugin version (will be taken form plugin.xml by default')
//        TODO other options

        def options = cli.parse(args)
//        TODO show help
        def path = options['plugin-folder']
        def version = options['version']

        if (!path) {
            path = System.getProperty("user.dir")
        }
        def arguments = options.arguments()
        if (arguments.size() < 1) {
            cli.usage()
            System.exit(-1)
        }
        String action = arguments[0]
        if ('build' == action) {
            PluginBuilder builder = new PluginBuilder(path)
            builder.build()
        }
        else {
            cli.usage()
            System.exit(-1)
        }
    }


}
