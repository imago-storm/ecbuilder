import com.electriccloud.client.groovy.ElectricFlow
import groovy.json.JsonOutput

$[/myProject/scripts/EFPluginHelper]

def efPlugin = new EFPluginHelper()
def config = efPlugin.getConfigValues('$[config]')
println "Configuration:"
println JsonOutput.toJson(config)

ElectricFlow ef = new ElectricFlow()
ef.setProperty(propertyName: '/myJob/configuration', value: JsonOutput.toJson(config))