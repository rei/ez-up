new File(projectDir, "src/main/resources/template").mkdirs()
new File(projectDir, "src/main/resources/template/config.groovy").text = "// configure your parameters/includes here"
new File(projectDir, "src/main/resources/template/postinstall.groovy").text = "// generate resources after the fact here"