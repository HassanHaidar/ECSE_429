package uk.co.compendiumdev.acceptance.cucumber;

import cucumber.api.java.en.Then;
import org.apache.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class StatusCodesStepDefinitions {
    public static boolean override = false;

    @Then("^I should receive a confirmation that my operation was successful$")
    public void iShouldReceiveAConfirmationThatMyOperationWasSuccessful() {
        if(override){
            override = false;
        } else {
            assertTrue(AppRunningStepDefinition.lastResponse.getFirst().statusCode() >= 200,
                    "Expected success status code!");
        }
    }

    @Then("^I should receive an error informing me that the requested resource was not found$")
    public void iShouldReceiveAnErrorInformingMeThatTheRequestedResourceWasNotFound() {
        if(override){
            override = false;
        } else {
            assertEquals(HttpStatus.SC_NOT_FOUND, AppRunningStepDefinition.lastResponse.getFirst().statusCode());
        }
    }

    @Then("^I should receive an error informing me that the passed information was invalid$")
    public void iShouldReceiveAnErrorInformingMeThatThePassedInformationWasInvalid() {
        if(override){
            override = false;
        } else {
            assertEquals(HttpStatus.SC_BAD_REQUEST, AppRunningStepDefinition.lastResponse.getFirst().statusCode());
        }
    }
}
