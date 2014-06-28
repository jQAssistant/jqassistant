---
layout: post
title:  "Find JUnit assertions without a message."
categories: Rules
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

First let's scan the project and start the server:

```raw
cd <project_root>
mvn jqassistant:scan
mvn jqassistant:server
```

Using the Neoj browser (http://localhost:7474) it is quite easy to find all assert methods which are actually used by the project:

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
