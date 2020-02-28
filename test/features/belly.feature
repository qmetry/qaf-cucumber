@storyId:ABC-123
Feature: Belly

  Scenario: a few cukes
    Given I have 10 cukes in my "${remote.server}"
    When I wait 1 hour
    And set name:
    |name|
    |Chirag|
    And can't wait more 10 minutes for more "cucks"
    Then my belly should growl
   
 	@qaf
    @DataFile:resources/data.txt
    Scenario Outline: few more cukes
    Given I have <cnt> cukes in my "${args[0]}"
    And set name:
    |cnt|name|blank|space|
    |<cnt>|<recId>|null |" "|
    When I wait <time> hour
    Then my belly should growl

     Scenario: and few more cukes
    Given I have 10 cukes in my "${remote.server}"
    When I wait 1 hour
    Then my belly should growl
    And I have 1 {what} cucumber in my stomach (amazing!)
    And I have 9 {what} cucumbers in my belly (amazing!)
    
    
    Scenario: cukes with qaf steps
    Given I have 10 cukes in my "${remote.server}"
    When I wait 1 hour
    Then my belly should growl
    And user request "request.call" with data:
    |a|b|
    |a|0|
    And user request "request.call.withdata" with map "{'a':'a','b':0}"
    And test "PASSED" during execution
    And create new report entry:
    |name|dir|startTime|
    |test|results/temp/new.txt|123456789|
    
    
    Scenario: cukes with qaf steps and datatable
    Given I have 10 cukes in my "${remote.server}"
    When I wait 1 hour
    Then my belly should growl

    And user request "request.call" with list:
    |a|b|
    |1|0|
    |2|3| 
     And user request "request.call" with list:
    |a|
    |1|
    |2|
        And user request "request.call" with data:
    |a|b|
    |a|x| 
     
   