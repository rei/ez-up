# EZ Up 

EZ-Up is a simple project templating tool. It was born out of frustration with the [Maven Archetype Plugin](http://maven.apache.org/archetype/maven-archetype-plugin/). 
It borrows several ideas from [Lazybones](https://github.com/pledbrook/lazybones) but is a much better fit for the Maven ecosystem. 

EZ up template projects are standard jar projects with some special folders in `src/main/resources/`.

The main template must go in the `template/` sub-folder. Sub-templates must go in `subtemplate-$name/`.

Each template or sub-template **MUST** contain `config.groovy` and may optionally contain `postinstall.groovy`. 

## config.groovy

The `config.groovy` file contains the configuration for the template. In it you can configure parameters with the 
`param('paramName', 'description/prompt text', defaultValue)` method. You can access parameters with the `params` map.
Parameters can be dynamically created like this: `params.pkgPath = params.pkg.replace('.', '/')`. 

`config.groovy` also controls which files in the template are included and which are processes as templates
   * includeFiles(String... patterns) - `includeFiles '**/Foo.java', '**/blah.properties'` [Default: `**/*`]
   * excludeFiles(String... patterns) - `excludeFiles '**/Foo.java'` [Default: none]
   * processFiles(String... patterns) - `processFiles '**/blah.properties'` [Default: `**/*`]
   * passthroughFiles(String... patterns) - `passthroughFiles '**/blah.properties'` [Default: none]

## postinstall.groovy 

The `postinstall.groovy` file is run after the template is generated which allows generating further derived resources.
This script also has access to the same methods as `config.groovy`.

## Template Processing

By default all resources in the template directory are processed as with the groovy `SimpleTemplateEngine` which has 
very similar syntax to jsps. Directory/file names can also be parameterized with `__paramName__` syntax, this allows for 
paths and filenames to use parameters. `/`'s in the paths will be expand to directories and missing parent dirs will be created.

## Using Classes and Libraries 

Because ez-up templates are just regular maven jar projects they can include custom classes in `src/main/java` or even other languages
like groovy (as long as they compile to java classes). They also can have dependencies that will be included in the classpath for the script runs.  

## Testing your template

It's recommended to test your template by including the chairlift-testing dependency:

    <dependency>
        <groupId>com.rei.ez-up</groupId>
        <artifactId>ez-up-testing</artifactId>
        <version>\${chairliftVersion}</version>
    </dependency>

Since Ez-up templates are just regular jar projects the simplest way to test them is to just write a unit test!

	public class EzUpTemplateTest {
	
	    @Rule
	    public TemplateTester tester = TemplateTester.forCurrentProject().deleteOnFailure(false).offline(false);
	    
	    @Test
	    public void canGenerateTemplateAndSubTemplate() throws Exception {
	        tester.forTemplate().withParam("groupId", "com.rei.ezup.testing")
	              .generatesFile("src/main/resources/template/config.groovy")
	              .generatesFile("src/main/resources/template/postinstall.groovy")
	              .generatesFileContaining("README.md", "# Writing an EZ Up Template")
	              .runsMavenPackage()
	              .generateAndValidate();
	        
	        tester.forSubTemplate("subtemplate")
	              .withParam("name", "my-sub")
	              .generatesFile("src/main/resources/subtemplate-my-sub/config.groovy")
	              .generatesFile("src/main/resources/subtemplate-my-sub/postinstall.groovy")
	              .generateAndValidate();        
	    }
	    
	}
 
 
## Sub-templates 

Sub-templates are templates that are meant to be applied into an existing project. They are packaged in folders `subtemplate-$name`.
A template project may contain as many subtemplates as desired and could even only contain sub-templates. Subtemplates otherwise behave the 
same as regular templates. 