package net.alterorb.launcher;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class HashingVoidStream extends BufferedInputStream {

    private final MessageDigest digest;

    public HashingVoidStream(MessageDigest digest, InputStream stream) {
        super(stream);
        this.digest = digest;
    }

    public static HashingVoidStream create(InputStream stream) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return new HashingVoidStream(digest, stream);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized int read() throws IOException {
        var read = super.read();

        if (read != -1) {
            digest.update((byte) read);
        }
        return read;
    }

    @Override
    public synchronized int read(byte[] buffer, int offset, int length) throws IOException {
        var read = super.read(buffer, offset, length);

        if (read != -1) {
            digest.update(buffer, offset, length);
        }
        return read;
    }

    public void consume() throws IOException {
        skipNBytes(Integer.MAX_VALUE);
    }

    public String hash() {
        var digest = this.digest.digest();
        return HexFormat.of().formatHex(digest);
    }
}
