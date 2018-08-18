package com.electriccloud.plugins.builder.domain

class Step extends EFEntity {
    List<ActualParameter> actualParameters = []
    List<String> attachedParameters = []

    Step(Procedure procedure) {
        this.parent = procedure
    }

    def addChild(ActualParameter parameter) {
        actualParameters.add(parameter)
    }

    def addAttribute(String name, String value) {
        if (name == 'stepName') {
            this.name = value
        }
        super.addAttribute(name, value)
    }

    @Override
    def findChild(Class entityClass, String name) {
        if (ActualParameter == entityClass) {
            return actualParameters.find { it.name == name }
        }
        return super.findChild(entityClass, name)
    }

    def attachParameter(String formalParameterName) {
        attachedParameters << formalParameterName
    }
}
