package com.electriccloud.plugins.builder


class Main {

    public static void main(String[] args) {
        CliProcessor processor = new CliProcessor()
        processor.run(args)

    }

    static def printUsageAndExit(cli, exitCode = 0) {
        if (exitCode != 0)
            cli.writer = System.err
        cli.usage()
        System.exit(exitCode)
    }

}
