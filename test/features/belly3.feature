@storyId:ABC-123
@DataFile:resources/data.txt

Feature: Belly3

   Background:
    Given I have <cnt> cukes in my "${args[0]}"
   
 	@qaf
  #  @DataFile:${env.resources}/data.txt
    Scenario Outline: few more cukes
    Given I have <cnt> cukes in my "<args[0]>"
    And set name:
    |cnt|name|blank|space|
    |<cnt>|${remote.server}|null |" "|
    When I wait <time> hour
    Then my belly should growl
    
    @qaf
    Scenario: few more cukes without outline
    Given I have <cnt> cukes in my "${args[0]}"
    And set name:
    |cnt|name|blank|space|
    |${cnt}|${remote.server}|null |" "|
    #And set name:{'cnt':'${cnt}','name':'${remote.server}'}
    When I wait ${time} hour
    Then my belly should growl

    
    
 

