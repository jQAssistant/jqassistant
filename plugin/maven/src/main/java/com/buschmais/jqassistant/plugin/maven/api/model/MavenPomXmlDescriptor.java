package com.buschmais.jqassistant.plugin.maven.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.ValidDescriptor;
import com.buschmais.jqassistant.plugin.maven.api.report.Maven;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;

import static com.buschmais.jqassistant.plugin.maven.api.report.Maven.MavenLanguageElement.Pom;

@Maven(Pom)
public interface MavenPomXmlDescriptor extends MavenPomDescriptor, XmlFileDescriptor, ValidDescriptor {
}
