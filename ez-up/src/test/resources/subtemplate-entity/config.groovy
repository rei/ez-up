param('pkg', "base package", "com.rei.test")
param('entity', "base package", "Entity")
param('includeRepo', "include repository?", true)

params.pkgPath = params.pkg.replace('.', '/')

if (!params.includeRepo) {
    excludeFiles '**/*Repository.java'
}