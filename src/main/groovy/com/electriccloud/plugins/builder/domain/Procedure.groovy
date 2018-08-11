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

    def addChild(FormalParameter parameter) {
        formalParameters.add(parameter)
    }

}
