package mihailris.mio;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public abstract class IODeviceAdapter implements IODevice {
    @Override
    public long getUsableSpace(String path) {
        return 0;
    }

    @Override
    public OutputStream write(String path, boolean append) throws IOException {
        throw new IOException("device is read-only");
    }

    @Override
    public boolean setLastModified(String path, long lastModified) {
        throw new IllegalStateException("device is read-only");
    }

    @Override
    public boolean mkdirs(String path) {
        throw new IllegalStateException("device is read-only");
    }

    @Override
    public boolean delete(String path) {
        throw new IllegalStateException("device is read-only");
    }

    @Override
    public File getFile(String path) {
        return null;
    }

    @Override
    public void close() throws IOException {
    }
}
