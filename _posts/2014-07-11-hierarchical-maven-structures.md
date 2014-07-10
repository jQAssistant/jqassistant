---
layout: post
title:  "Hierarchical Maven Structures"
author: dirkmahler
---

A Java project does not only consist of packages or classes. If the code base grows it normally gets separated into modules managed by the build system. It can be assumed that currently the most popular one in the Java world is [Maven](http://maven.apache.org). There are lots of people who actually don't like it too much because it makes strong assumptions on how a project is to be organized and built - this reduced flexibility can be seen as both advantage and disadvantage. But there are still lot of possibilities to create projects structures which are hard to maintain because they are lacking clear rules and structures.

So it seems useful to see if there's room for jQAssistant providing a little help here. Let's have a look at a common use case which is organizing a project in a hierarchical way:

```
parent
     |-module1
	 |       |-submodule1
	 |       |-submodule2
     |
     |-module2
             |-submodule2
	         |-submodule2
```
	
Maven provides two concepts reflecting these structures: modules and parent relations. 

```xml
<project ...>
	
	<!-- the parent relation -->
    <parent>
        <groupId>com.buschmais.maven</groupId>
        <artifactId>parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>module1</artifactId>

	<!-- the modules -->
	<modules>
		<module>submodule1</module>
		<module>submodule2</module>
	</modules>
</project>	
```

Both concepts have different purposes: specifying a "module" tells Maven to include it in the build reactor whereas the parent relation indicates that settings like repositories, properties, dependencies or plugin configurations shall be inherited from the referenced project. They can be used independently of each other: the pom.xml file of a Maven project not necessarily needs to specify that project as parent whose pom.xml includes it as a module. Looking from the other direction a project may define a parent relation without the referenced project defining it as a module.

In reality both concepts make a good fit if used in combination: a hierarchy is created where projects define their modules and at the same time the modules define direct back-references as parent relations. This way everything can be built from the upper-most parent project and it's possible to move all common settings to it for reducing overall redundancy in the build descriptors.

Such a hierarchy may break if new modules are introduced or existing modules are moved during refactorings. Using jQAssistant it is quite easy to verify that the described hierarchy stays consistent. The only thing we need is the following rule:

```xml
    <constraint id="maven3:HierarchicalParentModuleRelation">
        <description>If a parent Maven project declares a module then the
			parent project must also be declared as the parent of the module 
			(i.e. to keep the project hierarchy consistent).
        </description>
        <cypher><![CDATA[
			match
			  (parent:Maven:Project)-[:HAS_MODULE]->(module:Maven:Project)
			where
			  not (module)-[:HAS_PARENT]->(parent)
			return
			  module as InvalidModule
		]]></cypher>
    </constraint>
```
