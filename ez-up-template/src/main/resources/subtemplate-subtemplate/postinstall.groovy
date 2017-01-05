new File(projectDir, "src/main/resources/subtemplate-${params.name}").mkdirs()
new File(projectDir, "src/main/resources/subtemplate-${params.name}/config.groovy").text = "// configure your parameters/includes here"
new File(projectDir, "src/main/resources/subtemplate-${params.name}/postinstall.groovy").text = "// generate resources after the fact here"