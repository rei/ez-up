param('appName', "name of application", "my-app")
param('groupId', "groupId of project", "test")
param('includeFoo', "include Foo.java", false)

params.AppName = toCamelCase(params.appName)

includeFiles '**/*' //default

if (params.includeFoo) {
    excludeFiles '**/Foo.java'
}

processFiles '**/*.java', '**/*.xml', '**/*.md'
passthroughFiles '**/*.jpg'