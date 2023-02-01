package mihailris.mio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReadonlyDeviceWrapper implements IODevice {
    private final ReadDevice device;

    public ReadonlyDeviceWrapper(ReadDevice device) {
        this.device = device;
    }

    public ReadDevice getReadDevice() {
        return device;
    }

    @Override
    public boolean isReadonly() {
        return true;
    }

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
        return false;
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
        device.close();
    }

    @Override
    public InputStream read(String path) throws IOException {
        return device.read(path);
    }

    @Override
    public IOPath[] listDir(IOPath path) {
        return device.listDir(path);
    }

    @Override
    public long length(String path) {
        return device.length(path);
    }

    @Override
    public long lastModified(String path) {
        return device.lastModified(path);
    }

    @Override
    public boolean exists(String path) {
        return device.exists(path);
    }

    @Override
    public boolean isFile(String path) {
        return device.isFile(path);
    }

    @Override
    public boolean isDirectory(String path) {
        return device.isDirectory(path);
    }

    @Override
    public boolean isLink(String path) {
        return device.isLink(path);
    }
}
