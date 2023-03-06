package mihailris.mio;

import java.io.*;

/**
 * IODevice that use directory as paths root
 * Writeable by default, but may be created as readonly
 */
public class DirDevice extends IODeviceAdapter {
    final File directory;
    final boolean readonly;

    public DirDevice(File directory) {
        this.directory = directory;
        this.readonly = false;
    }

    public DirDevice(File directory, boolean readonly) {
        this.directory = directory;
        this.readonly = readonly;
    }

    @Override
    public boolean isReadonly() {
        return readonly;
    }

    @Override
    public long getUsableSpace(String path) {
        return new File(directory, path).getUsableSpace();
    }

    @Override
    public InputStream read(String path) throws IOException {
        return new FileInputStream(new File(directory, path));
    }

    @Override
    public OutputStream write(String path, boolean append) throws IOException {
        File file = getFile(path);
        if (!file.isFile()) {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        }
        return new FileOutputStream(file, append);
    }

    @Override
    public long length(String path) {
        return getFile(path).length();
    }

    @Override
    public IOPath[] listDir(IOPath path) {
        String[] names = new File(directory, path.getPath()).list();
        if (names == null)
            return null;
        IOPath[]paths = new IOPath[names.length];
        for (int i = 0; i < names.length; i++) {
            IOPath newpath = path.cpy().child(names[i]);
            paths[i] = newpath;
        }
        return paths;
    }

    @Override
    public long lastModified(String path) {
        return new File(directory, path).lastModified();
    }

    @Override
    public boolean setLastModified(String path, long lastModified) {
        return new File(directory, path).setLastModified(lastModified);
    }

    @Override
    public boolean exists(String path) {
        return new File(directory, path).exists();
    }

    @Override
    public boolean isFile(String path) {
        return new File(directory, path).isFile();
    }

    @Override
    public boolean isDirectory(String path) {
        return new File(directory, path).isDirectory();
    }

    @Override
    public boolean isLink(String path) {
        File file = new File(directory, path);
        try {
            return !file.getCanonicalPath().equals(file.getAbsolutePath());
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean mkdirs(String path) {
        return new File(directory, path).mkdirs();
    }

    @Override
    public boolean delete(String path) {
        File file = new File(directory, path);
        return file.delete();
    }

    @Override
    public File getFile(String path) {
        return new File(directory, path);
    }

    @Override
    public IORandomAccess openRandomAccess(String path, boolean writeable) throws IOException {
        return new IORandomAccessFile(new File(directory, path), "rw");
    }
}
