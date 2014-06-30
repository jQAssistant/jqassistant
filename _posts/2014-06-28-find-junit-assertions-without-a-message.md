---
layout: post
title:  "Find JUnit assertions without a message."
---

During our last Neo4j tutorial in Leipzig one of the participants pointed out that there could be some useful constraints regarding test implementations based on JUnit. A quite useful and easy thing would be finding all assertions without a human readable message:

```java
  @Test
  public void test() {
	assertEquals(3, 1 + 2);
  }
```

If such a test fails within a build containing hundreds of other tests it is quite helpful to provide some context
information. Therefore each of the assert methods provided by the class org.junit.Assert is overloaded with another one allowing to specify a message which is included in the report if the assertion fails:

```java
  @Test
  public void test() {
	assertEquals("The sum of 1 and 2 must be 3.", 3, 1 + 2);
  }
```

Now let's find all the invocations which do not provide a message. First we need to scan the project and start the server:

```raw
cd <project_root>
mvn jqassistant:scan
// or mvn com.buschmais.jqassistant.scm:jqassistant-maven-plugin:scan
mvn jqassistant:server
// or mvn com.buschmais.jqassistant.scm:jqassistant-maven-plugin:server
```

Using the Neoj browser (http://localhost:7474) it is quite easy to find all assert methods which are actually used by the project (as they are not part of the project and only created as referenced methods):

```
match
  (assertType:Type)-[:DECLARES]->(assertMethod)
where
  assertType.fqn = 'org.junit.Assert'
  and assertMethod.signature =~ 'void assert.*'
return
  assertMethod.signature
```

The statement can now be extended to find all invocations which use an assert method that has a signature which does not declare a first parameter of type "java.lang.String" (i.e. the message parameter):

```
match
  (assertType:Type)-[:DECLARES]->(assertMethod)
where
  assertType.fqn = 'org.junit.Assert'
  and assertMethod.signature =~ 'void assert.*'
with
  assertMethod
match
  (testType:Type)-[:DECLARES]->(testMethod:Method),
  (testMethod)-[invocation:INVOKES]->(assertMethod)
where
  not assertMethod.signature =~ 'void assert.*\\(java.lang.String,.*\\)'
return
  testType.fqn + "#" + testMethod.name as TestMethod,
  invocation.lineNumber as LineNumber
order by
  LineNumber
```

This statement with a slightly modified return clause can also be used as a constraint in a jQAssistant rules file:

```xml
<jqa:jqassistant-rules 
  xmlns:jqa="http://www.buschmais.com/jqassistant/core/analysis/rules/schema/v1.0">

	<group id="default">
		<includeConstraint refId="my-rules:AssertionMustProvideMessage" />
	</group>

    <constraint id="my-rules:AssertionMustProvideMessage">
        <description>All assertions must provide a message.</description>
        <cypher><![CDATA[
			match
			  (assertType:Type)-[:DECLARES]->(assertMethod)
			where
			  assertType.fqn = 'org.junit.Assert'
			  and assertMethod.signature =~ 'void assert.*'
			with
			  assertMethod
			match
			  (testType:Type)-[:DECLARES]->(testMethod:Method),
			  (testMethod)-[invocation:INVOKES]->(assertMethod)
			where
			  not assertMethod.signature =~ 'void assert.*\\(java.lang.String,.*\\)'
			return
			  invocation as Invocation,
			  testType as DeclaringType,
			  testMethod as Method
        ]]></cypher>
    </constraint>

</jqa:jqassistant-rules>
```

The described rule will be part of the next jQAssistant release.
