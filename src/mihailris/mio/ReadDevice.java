package mihailris.mio;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface ReadDevice extends Closeable {
    InputStream read(String path) throws IOException;
    IOPath[] listDir(IOPath path);
    long length(String path);
    long lastModified(String path);
    boolean exists(String path);
    boolean isFile(String path);
    boolean isDirectory(String path);
    boolean isLink(String path);
}
