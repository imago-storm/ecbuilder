import com.electriccloud.client.groovy.ElectricFlow

class EFPluginHelper {

    @Lazy
    ElectricFlow ef = { new ElectricFlow() }()

    def getConfigValues(String configName) {
        assert configName : "No config name is provided"
        def path = "/plugins/@PLUGIN_KEY@/project/ec_plugin_cfgs/${configName}"
        def properties = ef.getProperties(path: path)?.propertySheet?.property

        def loadConfigCredential = { credName ->
            def cred = ef.getFullCredential(credentialName: credName)?.credential
            assert cred.userName : "Username is not found in the credential $credName: $cred"
            return [userName: cred.userName, password: cred.password]
        }

        def configValues = [:]
        properties.each {
            configValues[it.propertyName] = it.value
            if (it.propertyName =~ /credential/) {
                def cred = loadConfigCredential.call(it.value)
                configValues[it.propertyName + 'UserName'] = cred.userName
                configValues[it.propertyName + 'Password'] = cred.password
            }
        }

        return configValues
    }

}
