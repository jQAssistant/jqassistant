---
layout: post
title:  "Find JUnit tests without assertions."
author: dirkmahler
---

As a follow up of the last post ([Find JUnit assertions without a message]({{ site.baseurl }}{% post_url 2014-06-28-find-junit-assertions-without-a-message %})) another useful constraint can be created using a set of cypher queries.

How can we find all test implementations which do not perform any assertion?

Here's an example Java class:

```java
  @Test
  public void test() {
    // prepare data
    int a = 1;
	int b = 2;
	// call the method which shall be tested
	int result = myService.add(a, b);
	// no verification if the result is correct
  }
```

The following set of jQAssistant rules report such kind of test implementations, they consist of two concepts and the constraint:

```xml
    <concept id="junit4:TestClassOrMethod">
        <description>Finds test methods (i.e. annotated with
		"@org.junit.Test") and labels them and their containing
		classes with "Test" and "Junit4".</description>
        <cypher><![CDATA[
            match
              (c:Type:Class)-[:DECLARES]->(m:Method),
			  (m)-[:ANNOTATED_BY]-()-[:OF_TYPE]->(a:Type)
            where
              a.fqn="org.junit.Test"
            set
              c:Test:Junit4, m:Test:Junit4
            return
              c as TestClass, collect(m) as TestMethods
        ]]></cypher>
    </concept>

    <concept id="junit4:AssertMethod">
        <description>Labels all assertion methods declared
  		  by org.junit.Assert with "Assert".</description>
        <cypher><![CDATA[
            match
				(assertType:Type)-[:DECLARES]->(assertMethod)
            where
				assertType.fqn = 'org.junit.Assert'
				and assertMethod.signature =~ 'void assert.*'
            set
				assertMethod:Assert
            return
				assertMethod
            ]]></cypher>
    </concept>

    <constraint id="junit4:TestMethodWithoutAssertion">
        <requiresConcept refId="junit4:TestClassOrMethod"/>
        <requiresConcept refId="junit4:AssertMethod"/>
        <description>All test methods must perform assertions.</description>
        <cypher><![CDATA[
			match
			  (testType:Type)-[:DECLARES]->(testMethod:Test:Method)
			where
			  not (testMethod)-[:INVOKES*]->(:Method:Assert)
			return
			  testType as DeclaringType,
			  testMethod as Method
        ]]></cypher>
    </constraint>
```	

The concept "junit4:TestClassOrMethod" adds a label "Test" to all test methods annotated with @org.junit.Test, the concept "junit4:AssertMethod" adds a label "Assert" to all assert methods provided by org.junit.Assert.

Both are required by the constraint "junit4:TestMethodWithoutAssertion" which does nothing more than checking if within the call graph starting at a test method (i.e. traversal over all outgoing INVOKE relations) at least one "Assert" labeled method can be found.

The constraint has been added to the jQAssistant JUnit4 plugin and thus will be officially available with the next release.
