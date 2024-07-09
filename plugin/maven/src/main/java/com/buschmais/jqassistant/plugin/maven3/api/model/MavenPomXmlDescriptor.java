package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.ValidDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.report.Maven;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;

import static com.buschmais.jqassistant.plugin.maven3.api.report.Maven.MavenLanguageElement.PomXmlFile;

@Maven(PomXmlFile)
public interface MavenPomXmlDescriptor extends MavenPomDescriptor, XmlFileDescriptor, ValidDescriptor {
}
