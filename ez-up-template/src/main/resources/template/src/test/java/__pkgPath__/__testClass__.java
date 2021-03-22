package ${pkg};

import org.junit.Rule;
import org.junit.Test;

import com.rei.ezup.testing.TemplateTester;

public class ${testClass} {

    @Rule
    public TemplateTester tester = TemplateTester.forCurrentProject()
                                                 //.deleteOnFailure(false) // will leave generated project in-tact if test fails
                                                 //.debugLogging() // turns on more logging 
                                                 ;
    
    @Test
    public void canGenerateTemplate() throws Exception {
        tester.forTemplate()
              .runsMavenPackage()
              .generateAndValidate();
    }
    
}
