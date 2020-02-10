package com.qmetry.qaf.automation.cucumber.test;

import java.util.Map;

import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.qmetry.qaf.automation.core.ConfigurationManager;
import com.qmetry.qaf.automation.step.QAFTestStep;
import com.qmetry.qaf.automation.util.Reporter;
import com.qmetry.qaf.automation.util.Validator;

import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;


@ContextConfiguration(classes = { Config.class })
public class StepDefinitions implements En {
	@Autowired
	public SampleBean p;
    //private static StepDefinitions lastInstance;
	 String hi = Thread.currentThread().getName();

	public StepDefinitions() {
		
		Given("I have {int} cukes in my {string}s", (Integer int1, String s) -> {
			// Write code here that turns the phrase above into concrete actions
			System.out.println(s);
			ConfigurationManager.getBundle().setProperty("remote.server", "updated Server");
			p.setFirst(Thread.currentThread().getName());
			hi = Thread.currentThread().getName();
			Reporter.log(hi + int1 + Thread.currentThread().getName());
			Validator.verifyTrue(int1<251, "Cukes less than 251", "Cukes less than 251");
			
			//hi = "somethig else";
		});

		When("I wait {int} hour", (Integer int1) -> {
			// Write code here that turns the phrase above into concrete actions
			Reporter.log(hi + "=" + Thread.currentThread().getName());
			System.out.println(hi + p.getFirst());
			Validator.assertThat(p.getFirst(), Matchers.equalToIgnoringCase(Thread.currentThread().getName()));
			Validator.verifyTrue(int1<10, "Hour less than 10", "Hour less than 10");
		});
		
		When("I have {string}, {string}", (String s1, String s2) -> {
			// Write code here that turns the phrase above into concrete actions
			System.out.println(s1 + s2);
		});

		Then("my belly should growl", () -> {
			// Write code here that turns the phrase above into concrete actions
			// throw new io.cucumber.java8.PendingException();
		});
		/*		
		And("I have list:{list}", (List<Object> items) -> {
			System.out.println("list items::"+items);
		});
		
		And("I have list map:", (List<Map<String, Object>> items) -> {
			System.out.println("list map items::"+items);
		});
		
		And("I have map:", (Map<String, Object> items) -> {
			System.out.println("map items::"+items);
		});
		*/
		
		And("I have datatable:", (DataTable items) -> {
			System.out.println("items::"+items);
			System.out.println("items::"+items.asList());
			System.out.println("items::"+items.asMaps());
		});
		
		And("I have {string} datatable:", (String s, DataTable items) -> {
			System.out.println("String::"+s);

			System.out.println("items::"+items);
			System.out.println("items::"+items.asList());
			System.out.println("items::"+items.asMaps());
		});
		
		Given("A constructor reference with an argument {string}", Contact::new);
        Given("A method reference to an arbitrary object of a particular type {string}", Contact::call);
        Given("A method reference to an arbitrary object of a particular type {string} with argument {string}", Contact::update);
	}
	

	@io.cucumber.java.en.Given("I have {int} cukes in my {string}")
	public void mstep(Integer int1, String s) {
		// Write code here that turns the phrase above into concrete actions
		System.out.println(s);
		ConfigurationManager.getBundle().setProperty("remote.server", "updated Server");
		p.setFirst(Thread.currentThread().getName());
		hi = Thread.currentThread().getName();
		Reporter.log(hi + int1 + Thread.currentThread().getName());
		Validator.verifyTrue(int1<251, "Cukes less than 251", "Cukes less than 251");
	}
	
   @QAFTestStep(description="set name:{name}")
    public void _setName(Map<String,Object> first) {
			System.out.println("name set to:: " +first);
		}
    
    //@QAFTestStep(description="set name:{string}")
    public void setFirst(DataTable first) {
			System.out.println("name set to:: " +first);
		}
	 public static class Person {
	        String first;
	        String last;
	        
	        public Person() {
				// TODO Auto-generated constructor stub
			}
	        
	        public String getFirst() {
				return first;
			}
	        public void setFirst(String first) {
				this.first = first;
			}
	    }

	    public static class Contact {

	        private final String number;

	        public Contact(String number) {
	            this.number = number;
	            System.out.println("Contact" +number);
	            //assertEquals("42", number);
	        }

	        public void call() {
	            //System.out.println("call" +number);

	        	//assertEquals("42", number);
	        }

	        public void update(String number) {
	        	//assertEquals("42", this.number);
	        	//assertEquals("314", number);
	            System.out.println("update" +this.number);
	            System.out.println("update" +number);

	        }
	    }
}
