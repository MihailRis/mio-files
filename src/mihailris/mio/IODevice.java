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
