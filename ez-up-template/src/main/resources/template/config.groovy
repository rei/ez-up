param('groupId', "groupId of project", "com.rei.ez-up")
param('artifactId', "artifactId of project", "ez-up-template-example")

params.pkg = params.groupId
params.pkgPath = params.pkg.replace('.', '/')
params.testClass = toCamelCase(params.artifactId) + 'Test'