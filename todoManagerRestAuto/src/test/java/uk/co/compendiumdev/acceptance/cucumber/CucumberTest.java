package uk.co.compendiumdev.acceptance.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty"},
        features = {"src/test/resources"}
        //glue = "uk.co.compendiumdev.acceptance.features"
)
public class CucumberTests {
}
