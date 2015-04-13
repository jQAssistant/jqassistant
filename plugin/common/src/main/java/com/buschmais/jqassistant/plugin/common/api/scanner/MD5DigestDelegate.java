package com.buschmais.jqassistant.plugin.common.api.scanner;

import com.buschmais.jqassistant.plugin.common.api.model.MD5Descriptor;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A delegate around reading input streams for calculating a MD5 hash sum.
 */
public class MD5DigestDelegate {

    /**
     * Defines the read operation to execute.
     * 
     * @param <D>
     *            The MD5 descriptor type.
     */
    public interface DigestOperation<D extends MD5Descriptor> {

        /**
         * Execute the operation.
         * 
         * @param inputStream
         *            The input stream to use for calculating the MD5 hash sum.
         * @return The MD5 descriptor.
         * @throws IOException
         *             If reading fails.
         */
        D execute(InputStream inputStream) throws IOException;

    }

    private static final MD5DigestDelegate instance = new MD5DigestDelegate();

    private MessageDigest md5Digest;

    /**
     * Private constructor.
     */
    private MD5DigestDelegate() {
        try {
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot create message digest for MD5", e);
        }
    }

    /**
     * Return the singleton instance.
     * 
     * @return The instance.
     */
    public static MD5DigestDelegate getInstance() {
        return instance;
    }

    /**
     * Calculate the MD5 hash sum for the given input stream using the given
     * operation.
     * 
     * @param stream
     *            The stream.
     * @param digestOperation
     *            The operation.
     * @param <D>
     *            The MD5 descriptor type.
     * @return The MD5 descriptor.
     * @throws IOException
     *             If reading the stream fails.
     */
    public <D extends MD5Descriptor> D digest(InputStream stream, DigestOperation<D> digestOperation) throws IOException {
        DigestInputStream digestInputStream = new DigestInputStream(stream, md5Digest);
        D md5Descriptor = digestOperation.execute(digestInputStream);
        String md5 = DatatypeConverter.printHexBinary(md5Digest.digest());
        md5Descriptor.setMd5(md5);
        return md5Descriptor;
    }
}
