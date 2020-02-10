/**
 * 
 */
package com.qmetry.qaf.automation.cucumber.test;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.cucumber.spring.CucumberTestContext;

/**
 * @author chirag
 *
 */
@Component
@Scope(CucumberTestContext.SCOPE_CUCUMBER_GLUE)
public class SampleBean {
	   String first;
       String last;
       
       public SampleBean() {
			// TODO Auto-generated constructor stub
		}
       
       public String getFirst() {
			return first;
		}
       
       public void setFirst(String first) {
			this.first = first;
		}
       
       
}
