package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import com.buschmais.jqassistant.plugin.common.api.model.MD5Descriptor;

public class MD5Reader {

    public interface DigestOperation<D extends MD5Descriptor> {

        D execute(InputStream inputStream) throws IOException;

    }

    private static final MD5Reader instance = new MD5Reader();

    private MessageDigest md5Digest;

    private MD5Reader() {
        try {
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot create message digest for MD5", e);
        }
    }

    public static MD5Reader getInstance() {
        return instance;
    }

    public <D extends MD5Descriptor> D digest(InputStream stream, DigestOperation<D> digestOperation) throws IOException {
        DigestInputStream digestInputStream = new DigestInputStream(stream, md5Digest);
        D md5Descriptor = digestOperation.execute(digestInputStream);
        String md5 = DatatypeConverter.printHexBinary(md5Digest.digest());
        md5Descriptor.setMD5(md5);
        return md5Descriptor;
    }
}
