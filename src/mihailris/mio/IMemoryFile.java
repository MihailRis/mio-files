package mihailris.mio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IMemoryFile {
    InputStream readStream() throws IOException;
    long length();
    OutputStream write(MemoryDevice device, boolean append) throws IOException;
    void close();
    default IORandomAccess openRandomAccess(boolean writeable) throws IOException {
        throw new IOException("file "+this+" does not support random access");
    }
}
