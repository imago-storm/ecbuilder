package com.electriccloud.plugins.builder

import com.electriccloud.plugins.builder.domain.*
import groovy.xml.XmlUtil

class ProjectToXML {
    Project project
    Node parent = new Node(null, 'exportedData', [
        buildLabel  : 'build_3.5_30434_OPT_2010.01.13_07:32:22',
        buildVersion: '3.5.1.30434',
        version     : '39']
    )
    @Lazy
    Node projectNode = { new Node(parent, 'project') }()

    ProjectToXML(Project project) {
        this.project = project
    }

    String generateXml() {
        new Node(parent, 'exportPath', '/projects/' + project.name)
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
            Node propertySheet = new Node(procNode, 'propertySheet')
            println "Procedure ${procedure.name}"
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
            }
        }
    }

    def generateProperties(Node parentNode, EFEntity entity) {
        for (Property property in entity.listProperties()) {
            println "Property ${property.name}"
            if (property.listProperties().size() == 0) {
                Node propNode = new Node(parentNode, 'property')
                new Node(propNode, 'propertyName', property.name)
                new Node(propNode, 'value', property.value)
            } else {
                Node propNode = new Node(parentNode, 'property')
                new Node(propNode, 'propertyName', property.name)
                Node propertySheet = new Node(propNode, 'propertySheet')
                generateProperties(propertySheet, property)
            }
        }
    }
}
