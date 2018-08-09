package com.electriccloud.plugins.builder.dsl.listeners

import com.electriccloud.plugins.builder.domain.*

class ProjectBuilder implements EventListener {

    Project project = new Project()
    Closure callback
    EFEntity current

    ProjectBuilder(Closure callback) {
        this.callback = callback
    }

    @Override
    def startEvent(String name) {
        if (name == 'aclEntry') {
            return
        }
        switch(name) {
            case 'project':
                current = project
                break
            case 'procedure':
                current = new Procedure(current)
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
        }
        else {
            println "Attribute $name, $value"
        }

    }

    @Override
    def endEvent(String name) {
        if (name == 'aclEntry') {
            return
        }
        switch (name) {
            case ~/project|procedure|property|step|formalParameter|actualParameter/:
                if (current.parent){
                    current.parent.addChild(current)
                }
                current = current.parent
                break
        }
    }

    @Override
    def done() {
        callback.call(this.project)
    }
}
