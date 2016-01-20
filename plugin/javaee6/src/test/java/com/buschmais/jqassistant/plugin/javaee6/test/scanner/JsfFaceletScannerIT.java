package com.buschmais.jqassistant.plugin.javaee6.test.scanner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.javaee6.api.model.JsfFaceletDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.WebApplicationScope;

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
        store.beginTransaction();
        scanFaceletDirectory();

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
        store.beginTransaction();
        scanFaceletDirectory();

        List<JsfFaceletDescriptor> descriptors = query("MATCH (n:File:Jsf:Facelet) WHERE n.fileName='/shop/productsite.jspx' RETURN n").getColumn("n");
        assertThat(descriptors, hasSize(1));

        JsfFaceletDescriptor descriptor = descriptors.iterator().next();
        assertThat(descriptor, not(nullValue()));
        assertThat(descriptor.getIncludes(), hasSize(2));

        List<String> fileNames = new ArrayList<>();
        fileNames.add("/shop/item.jspx");
        fileNames.add("/shop/short_info.jspx");
        containsAll(descriptor.getIncludes(), fileNames);

        assertThat(descriptor.getTemplate(), not(nullValue()));
        assertThat(descriptor.getTemplate().getFileName(), equalTo("/templ/template.jspx"));

        store.commitTransaction();
    }

    /**
     * Scan the directory containing the test facelets.
     * 
     * @throws IOException
     *             If scanning fails.
     */
    private void scanFaceletDirectory() throws IOException {
        final File faceletDirectory = new File(getClassesDirectory(JsfFaceletScannerIT.class), "facelet");
        getScanner().scan(faceletDirectory, "/", WebApplicationScope.WAR);
        TestResult result = query("match (f:File) with f.fileName as fileName match (f:File) where f.fileName=fileName "
                + "with fileName, count(f) as count where count > 1 return fileName, count");
        List<Map<String, Object>> rows = result.getRows();
        assertThat("Expecting no duplicate file names: " + rows, rows.size(), equalTo(0));
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
