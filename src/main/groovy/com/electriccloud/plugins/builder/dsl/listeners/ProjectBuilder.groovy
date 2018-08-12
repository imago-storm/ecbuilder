package com.electriccloud.plugins.builder.dsl.listeners

import com.electriccloud.plugins.builder.domain.*
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

    def method(String methodName, properties) {
        if (methodName == 'attachParameter') {
            log.debug "Found attach parameter: ${properties}"
            attachedCredentials << properties[0]
        }
        log.debug "Found method $methodName"
    }

    @Override
    def startEvent(String name, entityName) {
        switch (name) {
            case 'project':
                current = project
                break
            case 'procedure':
                Procedure existingProcedure = this.project.procedures.find { it.name == entityName }
                if (existingProcedure) {
                    current = existingProcedure
                } else {
                    current = new Procedure(current)
                }
                break
            case 'step':
                current = new Step(current)
                break
            case 'property':
                current = new Property(current)
                break
            case 'formalParameter':
                current = new FormalParameter(current)
                break
            case 'actualParameter':
                current = new ActualParameter(current)
                break
                defaut:
                println "Event $name"
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
        switch (name) {
            case ~/project|procedure|property|step|formalParameter|actualParameter/:
                if (current.parent) {
                    current.parent.addChild(current)
                }
                current = current.parent
                break
        }
    }

    @Override
    def done() {
        attachParameters()
        squishDoubles()
        callback.call(this.project)
    }


    def squishDoubles() {
        List<Procedure> procedures = []
        for (Procedure procedure in project.procedures) {
            List<Procedure> doubles = project.procedures.findAll {
                it.name == procedure.name
            }
            if (doubles.size() == 1) {
                procedures.add(doubles[0])
            } else {
                Procedure squished = squishProcedures(doubles)
                if (!procedures.find { it.name == squished.name })
                    procedures.add(squished)
            }
        }
        project.procedures = procedures
    }

    def attachParameters() {
        attachedCredentials.each { cred ->
            Procedure procedure = project.procedures.find { it.name == cred.procedureName}
            Step step = procedure.steps.find { it.name == cred.stepName }
            step.attachParameter(cred.formalParameterName)
        }
    }

    def squishProcedures(List<Procedure> doubles) {
        Procedure result = new Procedure(this.project)
        for (Procedure proc in doubles) {
            result.name = proc.name
            result.attributes += proc.attributes
            for (Property prop in proc.listProperties()) {
                if (!result.listProperties().find { it.name == prop.name})
                    result.addProperty(prop)
            }
        }

        return result
    }

}
