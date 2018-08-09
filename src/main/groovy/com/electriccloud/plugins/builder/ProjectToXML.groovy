package com.electriccloud.plugins.builder

import com.electriccloud.plugins.builder.domain.*

class ProjectToXML {
    Project project
    Node parent = new Node(null, 'exportedData', [
        buildLabel  : 'build_3.5_30434_OPT_2010.01.13_07:32:22',
        buildVersion: '3.5.1.30434',
        version     : '39']
    )
    @Lazy
    Node projectNode = { new Node(parent, 'project')}()

    String generateXml() {
        generateProperties(projectNode, project)

    }

    def generateProperties(Node parentNode, EFEntity entity) {
        if (entity.properties.size() == 0) {
            return
        }
        Node propertySheet = new Node(parentNode, 'propertySheet')
        for(Property property in entity.properties) {

        }
    }
}
