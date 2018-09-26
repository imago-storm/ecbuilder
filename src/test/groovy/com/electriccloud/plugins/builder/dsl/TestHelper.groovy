package com.electriccloud.plugins.builder.dsl

class TestHelper {
    static def getPlugins() {
        String pluginsTestFolder = System.getenv('EC_BUILDER_TEST_FOLDER')
        assert pluginsTestFolder : "EC_BUILDER_TEST_FOLDER variable must be provided"
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
}
