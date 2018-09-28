package com.electriccloud.plugins.builder.dsl.listeners

import com.electriccloud.plugins.builder.domain.*
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j

@Slf4j
class ProjectBuilder implements EventListener {

    Project project = new Project()
    Closure callback
    EFEntity current

    ProjectBuilder(Closure<Project> callback) {
        this.callback = callback
    }

    List attachedCredentials = []
    List stepsWithAttachedCredentials = []

    def method(String methodName, properties) {
        if (methodName == 'attachParameter') {
            attachedCredentials << properties[0]
        }
        if (methodName == 'loadProcedures') {
            stepsWithAttachedCredentials = properties.stepsWithAttachedCredentials
        }
    }

    def startEntity(String name, Class entityClass) {
        EFEntity existing = current.findChild(entityClass, name)
        if (!existing) {
            existing = entityClass.newInstance(current)
            existing.name = name
            current.addChild(existing)
        }
        return existing
    }

    @Override
    def startEvent(String name, entityName) {
        switch (name) {
            case 'project':
                current = project
                break
            case 'procedure':
                current = startEntity(entityName, Procedure)
                break
            case 'step':
                current = startEntity(entityName, Step)
                break
            case 'property':
                current = startEntity(entityName, Property)
                break
            case 'formalParameter':
                current = startEntity(entityName, FormalParameter)
                break
            case 'actualParameter':
                current = startEntity(entityName, ActualParameter)
                break
            defaut:
                log.debug("Event: $name")
        }
    }


    @Override
    def attribute(String name, Object value) {
        if (current) {
            current.addAttribute(name, value)
        } else {
            log.debug "Found attribute $name, $value"
        }
    }

    @Override
    def endEvent(String name, entityName) {
        if (name =~ /project|procedure|property|step|formalParameter|actualParameter/) {
            current = current.parent
        }
    }

    @Override
    def done() {
        attachParameters()
        encodeSteps()
        callback.call(this.project)
    }

    def encodeSteps() {
        Procedure createConfiguration = this.project.procedures.find { it.name == 'CreateConfiguration' }
        if (!createConfiguration) {
            log.debug "No CreateConfiguration procedure found"
            return
        }

        String json = JsonOutput.toJson(stepsWithAttachedCredentials)
        Property property = new Property(createConfiguration)
        property.setValue(json)
        property.setName('ec_stepsWithAttachedCredentials')
        createConfiguration.addProperty(property)
    }


    def attachParameters() {
        attachedCredentials.each { cred ->
            Procedure procedure = project.procedures.find { it.name == cred.procedureName }
            Step step = procedure.steps.find { it.name == cred.stepName }
            step.attachParameter(cred.formalParameterName)
        }
    }


}
