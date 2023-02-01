package mihailris.mio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IMemoryFile {
    InputStream read() throws IOException;
    long length();
    OutputStream write(MemoryDevice device, boolean append) throws IOException;
    void close();
}
