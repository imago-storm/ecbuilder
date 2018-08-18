package com.electriccloud.plugins.builder.domain

class Procedure extends EFEntity {
    List<Step> steps = []
    List<FormalParameter> formalParameters = []

    Procedure(Project project) {
        this.parent = project
    }

    def addAttribute(String name, value) {
        if (name == 'procedureName') {
            this.name = value
        } else {
            super.addAttribute(name, value)
        }
    }

    def addChild(Step step) {
        steps.add(step)
    }

    def findChild(Class className, String name) {
        if (Step == className) {
            return steps.find{ it.name == name }
        }
        if (FormalParameter == className) {
            return formalParameters.find { it.name == name }
        }
        super.findChild(className, name)
    }

    def addChild(FormalParameter parameter) {
        formalParameters.add(parameter)
    }

}
