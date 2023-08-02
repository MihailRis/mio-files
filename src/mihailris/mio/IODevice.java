package mihailris.mio;

import java.io.*;

public interface IODevice extends ReadDevice, Closeable {
    boolean isReadonly();
    long getUsableSpace(String path);

    // Modification methods
    OutputStream write(String path, boolean append) throws IOException;
    boolean setLastModified(String path, long lastModified);
    boolean mkdirs(String path);
    boolean delete(String path);

    /**
     * Move file. Default implementation is based on copy->delete
     * @param src source file
     * @param dst desination file
     * @throws IOException if device is readonly or destination file is already exists or I/O error ocurred
     */
    default void move(String src, String dst) throws IOException {
        copy(src, dst);
        delete(src);
    }

    default void copy(String src, String dst) throws IOException {
        if (isReadonly())
            throw new IOException("device is readonly");
        if (src.equals(dst))
            return;
        long length = length(src);
        try (OutputStream output = write(dst, false)) {
            try (InputStream input = read(src)) {
                IOUtil.transfer(input, output, length);
            }
        }
        setLastModified(dst, lastModified(src));
    }

    // Special
    File getFile(String path);

    default IORandomAccess openRandomAccess(String path, boolean writeable) throws IOException {
        throw new IOException("device "+getClass()+" does not support random access");
    }
}
