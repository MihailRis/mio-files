package mihailris.mio;

import java.io.*;

public interface IODevice extends Closeable {
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
        if (isFile(dst))
            throw new IOException("destination file is already exists");
        if (src.equals(dst))
            return;
        long length = length(src);
        try (OutputStream output = write(dst, false)) {
            try (InputStream input = read(src)) {
                IOUtil.transfer(input, output, length);
            }
        }
    }

    // Read methods
    InputStream read(String path) throws IOException;
    IOPath[] listDir(IOPath path);
    long length(String path);
    long lastModified(String path);
    boolean exists(String path);
    boolean isFile(String path);
    boolean isDirectory(String path);
    boolean isLink(String path);

    // Special
    File getFile(String path);
}
