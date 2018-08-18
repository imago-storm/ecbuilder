package com.electriccloud.plugins.builder

class PluginMetadata {
    Node node

    PluginMetadata(String meta) {
        node = new XmlParser().parseText(meta)
    }

    PluginMetadata(File metaFile) {
        node = new XmlParser().parse(metaFile)
    }

    def getKey() {
        return node.key.text()
    }

    def methodMissing(String methodName, args) {
        if (methodName.startsWith('get')) {
            String property = methodName.replaceAll('key', '').uncapitalize()
            return node.plugin[property]
        }
        throw new MissingMethodException()
    }

    def getBuildNumber() {
        int retval = 0
        String buildNumber = System.getenv('BUILD_NUMBER')
        try {
            retval = Integer.parseInt(buildNumber)
        } catch (NumberFormatException e) {
        }
        return retval
    }

    def getVersion() {
        String basicVersion = node.version.text()
        def parts = basicVersion.split(/\./)
        def major = parts[0]
        def minor = "0"
        def patch = "0"
        if (parts.size() >= 2) {
            minor = parts[1]
        }
        if (parts.size() >= 3) {
            patch = parts[2]
        }
        return "${major}.${minor}.${patch}.${buildNumber}"
    }

}
