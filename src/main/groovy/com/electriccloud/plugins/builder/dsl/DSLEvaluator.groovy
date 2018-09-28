package com.electriccloud.plugins.builder.dsl

import com.electriccloud.plugins.builder.exceptions.RequiredFileDoesNotExist
import com.electriccloud.plugins.builder.exceptions.UnsupportedDSLException
import org.codehaus.groovy.control.CompilerConfiguration
import com.electriccloud.plugins.builder.dsl.listeners.EventListener

class DSLEvaluator {
    Map dummyProperty = [
        value: 'dummy'
    ]

    static final String START = 'start'
    static final String END = 'end'
    static final String ATTRIBUTE = 'attribute'
    static final String METHOD = 'method'

    static
    final String OLD_LOAD_PROCEDURES = "4-th parameter for the loadProcedures() function should be a list 'stepsWithAttachedCredentials'"

    File procedureFolder

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
                it.startEvent(eventName, properties)
            } else if (eventType == 'end') {
                it.endEvent(eventName, properties)
            } else if (eventType == METHOD) {
                it.method(eventName, properties)
            } else {
                it.attribute(eventName, properties)
            }
        }
    }

    def methodMissing(String methodName, args) {
        if (methodName.startsWith("get")) {
            return get(methodName, args)
        }
//        Actions which do not declare entities
        if (methodName in ['aclEntry', 'attachParameter']) {
            sendEvent(METHOD, methodName, args)
            return
        }

        String entityName = getEntityName(methodName, args)
        sendEvent(START, methodName, entityName)
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

        if (methodName == 'procedure') {
            loadProcedureForm(this.procedureFolder, entityName)
        }

//        TODO deal with partial declaration
        params.each { String k, v ->
            if (k in objectProperties(methodName)) {
                sendEvent(ATTRIBUTE, k, v)
            } else {
                sendEvent(START, 'property', k)
                sendEvent(ATTRIBUTE, 'propertyName', k)
                sendEvent(ATTRIBUTE, 'value', v)
                sendEvent(END, 'property', k)
            }
        }

        sendEvent(END, methodName, entityName)
    }

    def readECSetup() {
        File ecSetup = new File(pluginFolder, 'ec_setup.pl')
        return ecSetup.text
    }

    def generateDescription(formElement) {
        if (formElement.documentation) {
            return formElement.documentation.toString()
        } else {
            return formElement.htmlDocumentation.toString()
        }
    }

    def loadProcedureForm(File folder, String procedureName) {
//        Somewhere between start and end procedure events
        File formXml = new File(folder, "form.xml")
        def formElements = new XmlSlurper().parse(formXml)

        formElements.formElement.each { formElement ->
            formalParameter("${formElement.property}",
                            defaultValue: "${formElement.value}",
                            required: "${formElement.required}",
                            type: "${formElement.type}",
                            description: generateDescription(formElement),
                            label: "${formElement.label}")

            if (formElement['attachedAsParameterToStep'] && formElement['attachedAsParameterToStep'] != '') {
                formElement['attachedAsParameterToStep'].toString().split(/\s*,\s*/).each {
                    attachParameter projectName: pluginName,
                                    procedureName: procedureName,
                                    stepName: it,
                                    formalParameterName: "${formElement.property}"
                }
            }

        }
        property 'ec_parameterForm', value: formXml.text
        property 'ec_customEditorData', {
            property 'parameters', {
                formElements.formElement.each { formElement ->
                    property "${formElement.property}", {
                        formType = 'standard'
                        if ('checkbox' == formElement.type.toString()) {
                            checkedValue = formElement.checkedValue ?: 'true'
                            uncheckedValue = formElement.uncheckedValue ?: 'false'
                            initiallyChecked = formElement.initiallyChecked ?: '0'
                        } else if ('select' == formElement.type.toString() || 'radio' == formElement.type.toString()) {
                            int count = 0
                            property 'options', {
                                formElement.option.each { option ->
                                    count++
                                    property "option${count}", {
                                        property 'text', value: "${option.name}"
                                        property 'value', value: "${option.value}"
                                    }
                                }
                                type = 'list'
                                optionCount = count
                            }
                        }
                    }
                }
            }
        }
    }

//    TODO
    List objectProperties(String name) {
        Map properties = [procedure      : ['description', 'jobNameTemplate', 'projectName',
                                            'resourceName', 'timeLimit', 'timeLimitUnits', 'workspaceName'],
                          step           : ['description', 'alwaysRun', 'broadcast',
                                            'command', 'condition', 'errorHandling', 'exclusiveMode',
                                            'logFileName', 'parallel', 'postProcessor', 'precondition',
                                            'projectName', 'releaseMode', 'resourceName', 'shell',
                                            'subprocedure', 'subproject', 'timeLimit', 'timeLimitUnits',
                                            'workingDirectory', 'workspaceName'],
                          property       : ['value', 'expandable', 'credentialProtected'],
                          project        : ['description', 'name'],
                          formalParameter: ['property', 'defaultValue', 'type',
                                            'label', 'required', 'formalParameterName', 'description']]
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
        if (args?.getAt(0) instanceof String || args?.getAt(0) instanceof GString) {
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
        loadNestedProperties(new File(pluginDir, 'dsl/properties'))
    }

    def loadNestedProperties(File propsDir) {
        propsDir.eachFile { dir ->
            int extension = dir.name.lastIndexOf('.')
            int endIndex = extension > -1 ? extension : dir.name.length()
            String propName = dir.name.substring(0, endIndex)

            if (dir.directory) {
                property propName, {
                    loadNestedProperties(dir)
                }
            } else {
                property propName, value: dir.text
            }
        }
    }

    def loadProcedures(pluginDir, pluginKey, pluginName, stepsWithAttachedCredentials) {
        File proceduresFolder = new File(pluginFolder, 'dsl/procedures')
        if (!proceduresFolder.exists()) {
            throw new RuntimeException("Folder dsl/procedures does not exist")
        }
        proceduresFolder.listFiles().each { File proc ->
            if (!proc.name.endsWith('_ignore')) {
                loadProcedure(proc)
            }
        }
        if (stepsWithAttachedCredentials instanceof List) {
            sendEvent(
                METHOD,
                'loadProcedures',
                [pluginKey: pluginKey, pluginName: pluginName, stepsWithAttachedCredentials: stepsWithAttachedCredentials])
        } else {
//            In some plugins there is a category name, we do not support this case
            throw new UnsupportedDSLException(OLD_LOAD_PROCEDURES)
        }
    }

    def loadProcedure(File procedureFolder) {
        File procedureDsl = new File(procedureFolder, 'procedure.dsl')
        if (!procedureDsl.exists()) {
            throw new RequiredFileDoesNotExist("procedure.dsl does not exist in the folder $procedureFolder")
        }
        this.procedureFolder = procedureFolder
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


//    Multiple implementations of upgrade method
    def upgrade(action, pluginName, otherPluginName, stepsWithAttachedCredentials) {
        if (!stepsWithAttachedCredentials instanceof List) {
            throw new UnsupportedDSLException(OLD_LOAD_PROCEDURES)
        }
        sendEvent(METHOD, 'upgrade', [stepsWithAttachedCredentials: stepsWithAttachedCredentials])
    }

    def upgrade(action, pluginName, otherPluginName, stepsWithAttachedCredentials, configPropertySheet, cloneProperties) {
        if (!stepsWithAttachedCredentials instanceof List) {
            throw new UnsupportedDSLException(OLD_LOAD_PROCEDURES)
        }
        sendEvent(
            METHOD,
            'upgrade',
            [stepsWithAttachedCredentials: stepsWithAttachedCredentials, cloneProperties: cloneProperties, configPropertySheet: configPropertySheet])
    }
}
