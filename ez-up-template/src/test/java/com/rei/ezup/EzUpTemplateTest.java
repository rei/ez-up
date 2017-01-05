package com.rei.ezup;

import org.junit.Rule;
import org.junit.Test;

import com.rei.ezup.testing.TemplateTester;

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
