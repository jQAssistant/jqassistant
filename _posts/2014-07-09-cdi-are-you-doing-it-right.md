---
layout: post
title:  "Context Dependency Injection - Are you doing it right?"
author: aparnachaudhary
---

CDI specification allows Field, Property (aka setter) and constructor injection. Like many developers out there, I also habitually use field injection. I must admit I never gave enough thought about it. But recently I saw a bean with more than 10 injection points and started to ponder over Dependency Injection. 

The following blog post discusses why you should prefer constructor injection over field or setter injection. In the later part of the post, we will see how jQAssistant can be used to find beans with too many injection points or to enforce use of constructor injection.


### Field Injection:

```java
  public class DefectServiceImpl implements DefectService {
  
    // @Inject annotation on field
    @Inject
    private DefectRepository defectRepository;
    
    public DefectServiceImpl() {
    }
    ...
  }
```

**PROS:**

* Apparently intuitive to many developers

**CONS:**

* Hard to test
* Beans can silently grow fat with too many dependencies

### Setter Injection:

```java
  public class DefectServiceImpl implements DefectService {
  
    private DefectRepository defectRepository;
    
    public DefectServiceImpl() {
    }
    
    // @Inject annotation on setter
    @Inject
    public void setDefectRepository(DefectRepository defectRepository) {
        this.defectRepository = defectRepository;
    }
    ...
  }
```

**PROS:**

* Dependencies can be set after construction allowing finer control over bean state. This might be handy for injecting non-mandatory collaborators.

**CONS:**

* Bean dependencies are not communicated clearly - no clear way to see mandatory and optional dependencies


### Constructor Injection:


```java
  public class DefectServiceImpl implements DefectService {
  
    private DefectRepository defectRepository;
    
    // @Inject annotation on constructor
    @Inject
    public DefectServiceImpl(DefectRepository defectRepository) {
        this.defectRepository = defectRepository;
    }
    ...
  }
```

**PROS:**

* Ensures that when a bean instance is constructed; all mandatory collaborators are in place and bean is ready to use.
* If the number of dependencies start growing; then you get clumsy constructors which is a clear indicator for un-maintaible code. Listen to your bean; probably its saying "Refactor Me!".

**CONS:**

May be I'm bit biased by now for constructor injection; but I do not see any clear drawbacks.

Now that we have seen PROS and CONS of different approaches for Dependency Injection; next big challenge is to be consistent with our choices. jQAssistant to the rescue!


```xml
<jqa:jqassistant-rules
  xmlns:jqa="http://www.buschmais.com/jqassistant/core/analysis/rules/schema/v1.0">

    <constraint id="cdi:BeansWithoutConstructorInjection">
      <requiresConcept refId="cdi:InjectionPoint" />
      <description>All CDI beans must use constructor injection.</description>
      <cypher><![CDATA[
        MATCH
          (a:Type)-[:DECLARES]->(member:Cdi:InjectionPoint)
        WHERE
          NOT member.name = "<init>"
        RETURN
          DISTINCT a.fqn AS InvalidBean
      ]]></cypher>
    </constraint>

</jqa:jqassistant-rules>  
```

```xml
<jqa:jqassistant-rules
  xmlns:jqa="http://www.buschmais.com/jqassistant/core/analysis/rules/schema/v1.0">

    <constraint id="cdi:BeansWithFieldInjection">
      <requiresConcept refId="cdi:InjectionPoint" />
      <description>CDI beans shall not use field injection.</description>
      <cypher><![CDATA[
        MATCH
          (a:Type)-[:DECLARES]->(member:Field:Cdi:InjectionPoint)
        RETURN
          DISTINCT a.fqn AS InvalidBean
      ]]></cypher>
    </constraint>

</jqa:jqassistant-rules>  
```

Still not convinced about advantages of constructor injection? Take your time. But meanwhile you can always prevent the existing beans from getting cluttered up with too many dependencies. 

The current version of jQAssistant introduced CDI plugin with "Cdi:InjectionPoint" concept. Following query can be used to set a rule to report beans with more than 5 field injection points.

```xml
<jqa:jqassistant-rules
  xmlns:jqa="http://www.buschmais.com/jqassistant/core/analysis/rules/schema/v1.0">

    <constraint id="cdi:BeansWithTooManyDependencies">
      <requiresConcept refId="cdi:InjectionPoint" />
      <description>CDI beans shall not have more than 5 field injection points.</description>
      <cypher><![CDATA[
        MATCH
          (a:Type)-[:DECLARES]->(member:Field:Cdi:InjectionPoint)
        WITH
          DISTINCT a.fqn AS Bean, count(member.name) AS fieldInjectionCount
        WHERE
          fieldInjectionCount > 5
        RETURN
          Bean AS RefactorMe, fieldInjectionCount
        ORDER BY
          fieldInjectionCount DESC
      ]]></cypher>
    </constraint>

</jqa:jqassistant-rules>  
```


**Reference:**

* [http://docs.spring.io/spring/docs/current/spring-framework-reference/html/beans.html#beans-dependency-resolution](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/beans.html#beans-dependency-resolution)
* [http://spring.io/blog/2007/07/11/setter-injection-versus-constructor-injection-and-the-use-of-required](http://spring.io/blog/2007/07/11/setter-injection-versus-constructor-injection-and-the-use-of-required)
* [http://martinfowler.com/articles/injection.html](http://martinfowler.com/articles/injection.html)
* [http://olivergierke.de/2013/11/why-field-injection-is-evil](http://olivergierke.de/2013/11/why-field-injection-is-evil)
