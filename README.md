# qaf-cucumber
This plug-in can be used for any of the following purpose:
 
 * Use cucumber steps with QAF
 * Use cucumber runner and all QAF BDD2 features
 
This plug-in will work with cucumber 5+. 
### Why BDD2 syntax
`BDD2` is super set of `Gherkin` syntax. Following are **additional features** in `BDD2` in addition to `Gherkin`:
 * Custom Meta-Data
 * Parameter support in step argument
 * Examples from external source (`CSV`, `XML`, `JSON`, `EXCEL`, `DB`) with filter options

### Using qaf-cucumber with QAF BDD runner
When you are using QAF BDD runner you can use Cucumber 5 steps with QAF BDD or BDD2 or Gherkin. 

##### Why QAF BDD runner
 * Native TestNG support (All features of testNG)
 * Multiple syntax support (QAF BDD, BDD2, Gherkin)
 * Step Meta-data support
 * Step retry support
 * Supports step defined in BDD (non Java steps)
 * Verification (also known as soft-assert) support
 * Detailed live reporting
 * TestNG Execution configuration
 
### Using qaf-cucumber with Cucumber runner
When you want to use cucumber or cucumber runner you can get support of `BDD2` and QAF reporting. 

##### QAF Over Cucumber 
 Following are **additional features** when used QAF with Cucumber:
 * `BDD2` Support
 * Verification support
 * QAF Detailed reporting
 * Integration with third party tools
 * QAF backend (like cucumber-java and cucumber-java8 backend)
 * Inbuilt Web/MObile/Web-service functional test automation support
 
Required to add cucumber plugin `com.qmetry.qaf.automation.cucumber.QAFCucumberPlugin` to get above features with cucumber.
 
