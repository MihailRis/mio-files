package mihailris.mio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IODeviceAdapter implements IODevice {
    @Override
    public boolean isReadonly() {
        return false;
    }

    @Override
    public long getUsableSpace(String path) {
        return 0;
    }

    @Override
    public OutputStream write(String path, boolean append) throws IOException {
        return null;
    }

    @Override
    public boolean setLastModified(String path, long lastModified) {
        return false;
    }

    @Override
    public boolean mkdirs(String path) {
        return false;
    }

    @Override
    public boolean delete(String path) {
        return false;
    }

    @Override
    public InputStream read(String path) throws IOException {
        return null;
    }

    @Override
    public IOPath[] listDir(IOPath path) {
        return null;
    }

    @Override
    public long length(String path) {
        return -1;
    }

    @Override
    public long lastModified(String path) {
        return -1;
    }

    @Override
    public boolean exists(String path) {
        return false;
    }

    @Override
    public boolean isFile(String path) {
        return false;
    }

    @Override
    public boolean isDirectory(String path) {
        return false;
    }

    @Override
    public boolean isLink(String path) {
        return false;
    }

    @Override
    public File getFile(String path) {
        return null;
    }

    @Override
    public void close() throws IOException {
    }
}
