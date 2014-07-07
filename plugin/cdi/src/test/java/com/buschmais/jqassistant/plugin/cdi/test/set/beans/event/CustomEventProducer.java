package com.buschmais.jqassistant.plugin.cdi.test.set.beans.event;

import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Custom CDI Event Producer.
 * 
 * @author Aparna Chaudhary
 */
public class CustomEventProducer {

	@Inject
	Event<TestEvent> todolistEvent;

	/**
	 * Fires CDI Event of type {@link TestEvent}
	 * 
	 * @param testEvent
	 *            test event
	 * @return test event
	 */
	public TestEvent create(TestEvent testEvent) {
		todolistEvent.fire(testEvent);
		return testEvent;
	}

}
