package mihailris.mio;

import java.io.*;

/**
 * Absolute paths device works with whole filesystem
 */
public class AbsDevice implements IODevice {
    @Override
    public boolean isReadonly() {
        return false;
    }

    @Override
    public long getUsableSpace(String path) {
        return new File(path).getUsableSpace();
    }

    @Override
    public InputStream read(String path) throws IOException {
        return new FileInputStream(path);
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
        String[] names = new File(path.getPath()).list();
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
        return new File(path).lastModified();
    }

    @Override
    public boolean setLastModified(String path, long lastModified) {
        return new File(path).setLastModified(lastModified);
    }

    @Override
    public boolean exists(String path) {
        return new File(path).exists();
    }

    @Override
    public boolean isFile(String path) {
        return new File(path).isFile();
    }

    @Override
    public boolean isDirectory(String path) {
        return new File(path).isDirectory();
    }

    @Override
    public boolean isLink(String path) {
        File file = new File(path);
        try {
            return !file.getCanonicalPath().equals(file.getAbsolutePath());
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean mkdirs(String path) {
        return new File(path).mkdirs();
    }

    @Override
    public boolean delete(String path) {
        File file = new File(path);
        return file.delete();
    }

    @Override
    public File getFile(String path) {
        return new File(path);
    }
}
