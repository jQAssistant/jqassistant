package com.buschmais.jqassistant.plugin.cdi.test.set.beans.inject;

import javax.inject.Inject;

import com.buschmais.jqassistant.plugin.cdi.test.set.beans.Bean;

/**
 * Example bean using field injection.
 * 
 * @author Aparna Chaudhary
 */
public class BeanWithFieldInjection {

	@Inject
	private Bean bean;
	
	public void performTask(){
		bean.doSomething();
	}

}
