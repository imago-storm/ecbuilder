package com.electriccloud.plugins.builder.domain

class Step extends EFEntity {
    List<ActualParameter> actualParameters = []

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
}
