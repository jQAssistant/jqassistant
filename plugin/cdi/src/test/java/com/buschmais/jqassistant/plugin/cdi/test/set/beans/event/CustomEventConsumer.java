package com.buschmais.jqassistant.plugin.cdi.test.set.beans.event;

import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;

/**
 * Custom CDI Event consumer.
 * 
 * @author Aparna Chaudhary
 */
public class CustomEventConsumer {

	/**
	 * Observer for CDI Event of type {@link TestEvent}
	 * 
	 * @param testEvent
	 *            test event
	 */
	public void onTestEvent(@Observes(notifyObserver = Reception.IF_EXISTS) final TestEvent testEvent) {
		//do something with test event
	}

}
