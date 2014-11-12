package com.buschmais.jqassistant.plugin.facelet.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.facelet.api.model.JsfFaceletDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

/**
 * Scans some jspx-files and checks nodes & relationships.
 *
 * @author peter.herklotz@buschmais.com
 */
public class JsfFaceletScannerIT extends AbstractJavaPluginIT {

    /**
     * Scans some jspx-files and checks nodes.
     *
     * @throws IOException
     *             error during scan
     */
    @Test
    public void testNodes() throws IOException {
        scanClassPathDirectory(getClassesDirectory(JsfFaceletScannerIT.class));
        store.beginTransaction();

        List<JsfFaceletDescriptor> jsfFaceletDescriptors = query("MATCH (n:File:Jsf:Facelet) RETURN n").getColumn("n");
        assertThat(jsfFaceletDescriptors.size(), equalTo(7));

        List<String> fileNames = new ArrayList<String>();
        fileNames.add("/cart/cart_widget.jspx");
        fileNames.add("/cart/cart.jspx");
        fileNames.add("/cart/item.jspx");
        fileNames.add("/shop/item.jspx");
        fileNames.add("/shop/productsite.jspx");
        fileNames.add("/shop/short_info.jspx");
        fileNames.add("/templ/template.jspx");

        containsAll(jsfFaceletDescriptors, fileNames);
        store.commitTransaction();
    }

    /**
     * Scans some jspx-files and checks relationships.
     *
     * @throws IOException
     *             error during scan
     */
    @Test
    public void testRelationships() throws IOException {
        scanClassPathDirectory(getClassesDirectory(JsfFaceletScannerIT.class));
        store.beginTransaction();

        List<JsfFaceletDescriptor> descriptors = query("MATCH (n:File:Jsf:Facelet) WHERE n.fileName='/shop/productsite.jspx' RETURN n").getColumn("n");
        assertThat(descriptors.size(), equalTo(1));

        JsfFaceletDescriptor descriptor = descriptors.iterator().next();
        Assert.assertNotNull(descriptor);
        Assert.assertEquals(descriptor.getIncludes().size(), 2);

        List<String> fileNames = new ArrayList<String>();
        fileNames.add("/shop/item.jspx");
        fileNames.add("/shop/short_info.jspx");
        containsAll(descriptor.getIncludes(), fileNames);

        Assert.assertNotNull(descriptor.getTemplate());
        Assert.assertEquals(descriptor.getTemplate().getFileName(), "/templ/template.jspx");

        store.commitTransaction();
    }

    /**
     * Expects that fileNames contains all jsfFaceletDescriptors#fileNames.
     *
     * @param jsfFaceletDescriptors
     *            the descriptors
     * @param fileNames
     *            the expected names
     */
    private void containsAll(Collection<JsfFaceletDescriptor> jsfFaceletDescriptors, List<String> fileNames) {

        for (JsfFaceletDescriptor descriptor : jsfFaceletDescriptors) {
            Assert.assertTrue("File not expected: " + descriptor.getFileName(), fileNames.remove(descriptor.getFileName()));
        }

        Assert.assertTrue("Not all expected files were scanned.", fileNames.isEmpty());
    }
}
