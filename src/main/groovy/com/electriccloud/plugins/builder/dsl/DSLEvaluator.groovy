package com.electriccloud.plugins.builder.dsl

import org.codehaus.groovy.control.CompilerConfiguration
import com.electriccloud.plugins.builder.dsl.listeners.EventListener

class DSLEvaluator {
    Map dummyProperty = [
        value: 'dummy'
    ]

    static final String START = 'start'
    static final String END = 'end'
    static final String ATTRIBUTE = 'attribute'

    String pluginFolder
    String pluginKey
    @Lazy
    String pluginName = { pluginKey + '-' + version }()
    String version

    DSLEvaluator(String pluginFolder, String pluginKey, String version, List<EventListener> listeners) {
        this.pluginFolder = pluginFolder
        this.pluginKey = pluginKey
        this.version = version
        this.listeners = listeners
    }

    List<EventListener> listeners = []

    def done() {
        listeners.each {
            it.done()
        }
    }

    def sendEvent(String eventType, String eventName, properties = null) {
        listeners.each {
            if (eventType == 'start') {
                it.startEvent(eventName)
            }
            else if (eventType == 'end') {
                it.endEvent(eventName)
            }
            else {
                it.attribute(eventName, properties)
            }
        }
    }

    def methodMissing(String methodName, args) {
        if (methodName.startsWith("get")) {
            return get(methodName, args)
        }

        sendEvent(START, methodName)
        String entityName = getEntityName(methodName, args)
        String nameAttribute = methodName + 'Name'
        sendEvent(ATTRIBUTE, nameAttribute, entityName)

        Map params = getParams(args)
        Closure closure = getClosure(args)
        if (closure) {

            Map filler = [pluginDir: pluginFolder]
            closure.setResolveStrategy(Closure.DELEGATE_FIRST)
            closure.setDelegate(filler)
            closure.run()
            filler.each { k, v ->
                if (!(k in ['pluginDir'])) {
                    params[k] = v
                }
            }

        }
//        TODO deal with partial declaration
        params.each { String k, v ->
            if (k in objectProperties(methodName)) {
                sendEvent(ATTRIBUTE, k, v)
            } else {
                sendEvent(START, 'property')
                sendEvent(ATTRIBUTE, 'propertyName', k)
                sendEvent(ATTRIBUTE, 'value', v)
                sendEvent(END, 'property')
            }
        }
        sendEvent(END, methodName)
    }

//    TODO
    List objectProperties(String name) {
        Map properties = [procedure: ['description', 'jobNameTemplate', 'projectName',
                                      'resourceName', 'timeLimit', 'timeLimitUnits', 'workspaceName'],
                          step     : ['description', 'alwaysRun', 'broadcast',
                                      'command', 'condition', 'errorHandling', 'exclusiveMode',
                                      'logFileName', 'parallel', 'postProcessor', 'precondition',
                                      'projectName', 'releaseMode', 'resourceName', 'shell',
                                      'subprocedure', 'subproject', 'timeLimit', 'timeLimitUnits',
                                      'workingDirectory', 'workspaceName'],
                          property : ['value', 'expandable']]
        return properties[name]
    }

    def get(methodName, args) {
        if (methodName == 'getProject') {
            if (args.size() == 1) {
                return [pluginKey: pluginKey]
            }
        } else if (methodName == 'getProperty_1') {
            if (args.size() == 1 && args.getAt(0) =~ /pluginDir/) {
                return [value: pluginFolder]
            }
            return dummyProperty
        } else {
            return [:]
        }
    }

    def getEntityName(String methodName, args) {
        if (args?.getAt(0) instanceof String) {
            return args?.getAt(0)
        } else if (args?.getAt(0) instanceof Map) {
            if (args.size() > 1) {
                return args?.getAt(1)
            } else {
                if (methodName == 'aclEntry') {
                    return args[0].systemObjectName
                } else {
                    return 'unknown'
                }
            }

        } else {
            return 'unknown'
        }
    }

    def getClosure(args) {
        if (args.size() > 1 && args?.getAt(1) && args?.getAt(1) instanceof Closure) {
            return args?.getAt(1)
        }
        if (args.size() > 2 && args[2] instanceof Closure) {
            return args[2]
        }
        return null
    }

    def getParams(args) {
        if (args?.getAt(0) instanceof Map) {
            return args?.getAt(0)
        }
        return [:]
    }

    def loadPluginProperties(pluginDir, pluginName) {
        println "Load plugin properties"
    }

    def loadProcedures(pluginDir, pluginKey, pluginName, pluginCategory) {
        File proceduresFolder = new File(pluginFolder, 'dsl/procedures')
        if (!proceduresFolder.exists()) {
            throw new RuntimeException("Folder dsl/procedures does not exist")
        }
        proceduresFolder.listFiles().each { File proc ->
            loadProcedure(proc)
        }
    }

    def loadProcedure(File procedureFolder) {
        File procedureDsl = new File(procedureFolder, 'procedure.dsl')
        File formXml = new File(procedureFolder, 'form.xml')
//        TODO formal parameters
//        TODO form properties

        CompilerConfiguration cc = new CompilerConfiguration()
        cc.setScriptBaseClass(DelegatingScript.class.getName())
        Map bindings = [
            pluginKey : pluginKey,
            pluginName: pluginName,
            pluginDir : pluginFolder
        ]
        GroovyShell sh = new GroovyShell(this.class.classLoader, new Binding(bindings), cc)
        DelegatingScript script = (DelegatingScript) sh.parse(procedureDsl)
        script.setDelegate(this)
        script.run()
    }

    def upgrade(action, pluginName, otherPluginName, stepsWithAttachedCredentials) {
        print "Upgrade: $action"
    }
}