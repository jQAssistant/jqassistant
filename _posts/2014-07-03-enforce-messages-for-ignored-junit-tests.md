---
layout: post
title:  "Enforce messages for ignored JUnit tests"
author: dirkmahler
---

During the last days two posts ([Find JUnit assertions without a message]({{ site.baseurl }}{% post_url 2014-06-28-find-junit-assertions-without-a-message %})) and [Find JUnit tests without assertions]({{ site.baseurl }}{% post_url 2014-07-21-find-junit-tests-without-assertions %})) have been published here about useful rules regarding proper usage of JUnit.

Now a colleague came up with the hint that it would be very useful to ensure that every occurrence of the annotation @org.junit.Ignore shall provide a message why a particular test class or method is ignored:

```java
  @Ignore // N
  public void test1() {
    ...
  }

  @Ignore("Requires fix for ticket XYZ.")
  public void test2() {
    ...
  }
```

The method "test1" does not provide any context information why it has been disabled whereas "test2" does. Thus for setting up a rule we need to find all annotations of type @org.junit.Ignore which do not have an attribute value named ["value"](http://junit.sourceforge.net/javadoc/org/junit/Ignore.html#value):

```xml
<jqa:jqassistant-rules
  xmlns:jqa="http://www.buschmais.com/jqassistant/core/analysis/rules/schema/v1.0">

    <constraint id="junit4:IgnoreWithoutMessage">
        <description>All @Ignore annotations must provide a message.</description>
        <cypher><![CDATA[
            match
              (e)-[:ANNOTATED_BY]->(ignore:Annotation)-[:OF_TYPE]->(ignoreType:Type)
            where
              ignoreType.fqn= "org.junit.Ignore"
              and not (ignore)-[:HAS]->(:Value{name:"value"})
            return
              e as IgnoreWithoutMessage
        ]]></cypher>
    </constraint>

</jqa:jqassistant-rules>	
```

So this is another candidate for the jQAssistant JUnit4 plugin.