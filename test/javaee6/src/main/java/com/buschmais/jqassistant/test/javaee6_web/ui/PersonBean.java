package com.buschmais.jqassistant.test.javaee6_web.ui;

import com.buschmais.jqassistant.test.javaee6_web.logic.api.PersonService;
import com.buschmais.jqassistant.test.javaee6_web.persistence.api.model.Person;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dirk.mahler
 * Date: 24.06.13
 * Time: 14:08
 * To change this template use File | Settings | File Templates.
 */
@Named
@ConversationScoped
public class PersonBean implements Serializable {

    @Inject
    private PersonService personService;

    @Inject
    private Conversation conversation;

    private Person person;

    public List<Person> getPersonList() {
        return personService.getPersons();
    }

    public String onCreate() {
        this.person = new Person();
        this.conversation.begin();
        return "/edit";
    }

    public String onEdit(Person person) {
        this.person = person;
        this.conversation.begin();
        return "/edit";
    }

    public String onSave() {
        this.conversation.end();
        if (this.person.getId() == null) {
            this.personService.create(this.person);
        } else {
            this.personService.update(this.person);
        }
        return "/list";
    }

    public String onDelete(Person person) {
        this.personService.delete(person);
        return "/list";
    }

}
