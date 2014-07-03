---
layout: post
title:  "Find JPA Entities without Field Access"
---

The JPA specification allows field vs property based access for entities. Following query can be used to find entities using property access.


```
MATCH
  (entity:Jpa:Entity)-[:DECLARES]->(m:Method)-[:ANNOTATED_BY]-()-[:OF_TYPE]->(a:Type)
WHERE
  a.fqn IN ["javax.persistence.Id", "javax.persistence.EmbeddedId"]
RETURN
  entity.name AS EntityWithPropertyAccess
```

JPA field based access is quite commonly used because of following reasons.

PROS:

* Only interesting fields are exposed to the outside world.
* The state is well encapsulated.
* You can declare getters and setters in interfaces, abstract classes or mapped superclasses, and override them in the concrete subclasses.
* It is easier to define transient contract; cleaner to mark fields transient than the getters.

CONS:

* No debugging possibility.

Reference:

* http://www.oracle.com/technetwork/articles/marx-jpa-087268.html
* http://www.adam-bien.com/roller/abien/entry/field_vs_property_based_access

The above cypher query can easily be modelled as a constraint (i.e. jQAssistant rule):

```xml
<jqa:jqassistant-rules
	xmlns:jqa="http://www.buschmais.com/jqassistant/core/analysis/rules/schema/v1.0">

	<constraint id="my-rules:EntitiesMustUseFieldAccess">
		<requiresConcept refId="jpa2:Entity" />
		<description>Verifies that entities prefer field access over property access.</description>
		<cypher><![CDATA[
            MATCH
               	(entity:Jpa:Entity)-[:DECLARES]->(m:Method)-[:ANNOTATED_BY]-()-[:OF_TYPE]->(a:Type)
            WHERE 
               	a.fqn IN ["javax.persistence.Id", "javax.persistence.EmbeddedId"]
            RETURN
               	entity.name AS EntityWithoutFieldAccess
        ]]></cypher>
	</constraint>

	<group id="default">
		<includeConcept refId="jpa2:Entity" />
		<includeConstraint refId="my-rules:EntitiesMustUseFieldAccess" />
	</group>
</jqa:jqassistant-rules>
```
