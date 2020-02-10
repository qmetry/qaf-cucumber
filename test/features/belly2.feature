Feature: Belly2


   Scenario: a few cukes again

    Given I have "items" datatable:
    |cnt|time|recId|
    |10 |2   |10 belly|
    |20 |1   |20 belly|
    
    Given I have ${remote.port} cukes in my "belly"
    When I wait 1 hour
    Then my belly should growl
  
   
    
   @Smoke
   @Author:chirag
    Scenario Outline: few more cukes again
    Given I have <cnt> cukes in my "${remote.server}"
    When I wait <time> hour
    Then my belly should growl
   Examples:
    |cnt|time|recId|
    |10 |2   |10 belly|
    |20 |1   |20 belly|
    
    
     Scenario: and few more cukes
     Given I have datatable:
    |cnt|time|recId|
    |10 |2   |10 belly|
    |20 |1   |20 belly|
    Given I have 10 cukes in my "${remote.server}"
    When I wait 1 hour
    Then my belly should growl
    
     Scenario: and more cukes
    Given I have 10 cukes in my "${remote.server}"
    When I wait 1 hour
    Then my belly should growl