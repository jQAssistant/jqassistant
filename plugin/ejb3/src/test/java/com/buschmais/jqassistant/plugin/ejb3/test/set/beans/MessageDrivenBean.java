package com.buschmais.jqassistant.plugin.jpa2.test.set.ejb3;

import javax.ejb.Local;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * A message driven EJB.
 */
@MessageDriven
@Local
public class MessageDrivenBean implements MessageListener {

    @Override
    public void onMessage(Message message) {
    }

}
