import java.io.File

def procName = 'Sample Procedure'
procedure procName, description: 'Sample procedure', {
    step 'sample step',
        command: new File(pluginDir, "dsl/procedures/sampleProcedure/steps/step1.groovy").text,
        shell: 'ec-groovy'

}

