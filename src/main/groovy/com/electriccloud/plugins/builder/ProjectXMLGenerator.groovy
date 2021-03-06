package com.electriccloud.plugins.builder

import com.electriccloud.plugins.builder.domain.*
import groovy.util.logging.Slf4j
import groovy.xml.XmlUtil

@Slf4j
class ProjectXMLGenerator {
    Project project
    Node parent = new Node(null, 'exportedData', [
        buildLabel  : 'build_main_129921_2018.07.05_07:40:23',
        buildVersion: '8.4.0.129921',
        version     : '86']
    )

    @Lazy
    Node projectNode = { new Node(parent, 'project') }()

    ProjectXMLGenerator(Project project) {
        this.project = project
    }

    String generateXml() {
        new Node(parent, 'exportPath', '/projects/' + project.name)
        new Node(projectNode, 'projectName', project.name)
        Node propertySheet = new Node(projectNode, 'propertySheet')
        generateProperties(propertySheet, project)
        generateProcedures(projectNode, project)
        return XmlUtil.serialize(parent)
    }

    def generateProcedures(projectNode, project) {
        for (Procedure procedure in project.procedures) {
            Node procNode = new Node(projectNode, 'procedure')
            procedure.attributes.each { k, v ->
                new Node(procNode, k, v)
            }
            new Node(procNode, 'procedureName', procedure.name)
            new Node(procNode, 'description', procedure.description)
            new Node(procNode, 'projectName', this.project.name)
            Node propertySheet = new Node(procNode, 'propertySheet')
            generateProperties(propertySheet, procedure)
            for (FormalParameter formalParameter in procedure.formalParameters) {
                Node formal = new Node(procNode, 'formalParameter')
                formalParameter.attributes.each { k, v ->
                    new Node(formal, k, v)
                }
                new Node(formal, 'formalParameterName', formalParameter.name)
                new Node(formal, 'description', formalParameter.description)
            }
//            Steps
            for (Step step in procedure.steps) {
                Node stepNode = new Node(procNode, 'step')
                step.attributes.each {k, v ->
                    new Node(stepNode, k, v)
                }
                new Node(stepNode, 'stepName', step.name)
                new Node(stepNode, 'description', step.description)
                if (step.attachedParameters.size() > 0) {
                    Node attachedParameters = new Node(stepNode, 'attachedParameters')
                    step.attachedParameters.each { paramName ->
                        new Node(attachedParameters, 'formalParameterName', paramName)
                    }
                }
            }
        }
    }

    def generateProperties(Node parentNode, EFEntity entity) {
        for (Property property in entity.listProperties()) {
            if (property.listProperties().size() == 0) {
                Node propNode = new Node(parentNode, 'property')
                new Node(propNode, 'propertyName', property.name)
                new Node(propNode, 'value', property.value)
                property.attributes.each {k, v ->
                    if (k == 'credentialProtected') {
                        v = k ? '1' : '0'
                    }
                    else {

                        new Node(propNode, k, "$v")
                    }
                }
            } else {
                Node propNode = new Node(parentNode, 'property')
                new Node(propNode, 'propertyName', property.name)
                Node propertySheet = new Node(propNode, 'propertySheet')
                property.attributes.each {k, v ->
                    if (k == 'credentialProtected') {
                        v = k ? '1' : '0'
                        new Node(propertySheet, k, "$v")
                    }
                    else {
                        new Node(propNode, k, "$v")
                    }
                }
                generateProperties(propertySheet, property)
            }
        }
    }
}
