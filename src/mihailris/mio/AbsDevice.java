package mihailris.mio;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Absolute paths device works with whole filesystem
 */
public class AbsDevice extends IODeviceAdapter {
    @Override
    public boolean isReadonly() {
        return false;
    }

    @Override
    public long getUsableSpace(String path) {
        path = toPath(path);
        return new File(path).getUsableSpace();
    }

    @Override
    public InputStream read(String path) throws IOException {
        path = toPath(path);
        return Files.newInputStream(Paths.get(path));
    }

    @Override
    public OutputStream write(String path, boolean append) throws IOException {
        path = toPath(path);
        File file = getFile(path);
        if (!file.isFile()) {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        }
        return new FileOutputStream(file, append);
    }

    @Override
    public long length(String path) {
        path = toPath(path);
        return getFile(path).length();
    }

    @Override
    public IOPath[] listDir(IOPath iopath) {
        String path = toPath(iopath);
        String[] names = new File(path).list();
        if (names == null)
            return null;
        IOPath[] paths = new IOPath[names.length];
        for (int i = 0; i < names.length; i++) {
            IOPath newpath = iopath.cpy().child(names[i]);
            paths[i] = newpath;
        }
        return paths;
    }

    private String toPath(IOPath iopath) {
        String path = iopath.getPath();
        return toPath(path);
    }

    private String toPath(String path) {
        if (!path.startsWith("/"))
            return "/"+path;
        return path;
    }

    @Override
    public long lastModified(String path) {
        path = toPath(path);
        return new File(path).lastModified();
    }

    @Override
    public boolean setLastModified(String path, long lastModified) {
        path = toPath(path);
        return new File(path).setLastModified(lastModified);
    }

    @Override
    public boolean exists(String path) {
        path = toPath(path);
        return new File(path).exists();
    }

    @Override
    public boolean isFile(String path) {
        path = toPath(path);
        return new File(path).isFile();
    }

    @Override
    public boolean isDirectory(String path) {
        path = toPath(path);
        return new File(path).isDirectory();
    }

    @Override
    public boolean isLink(String path) {
        path = toPath(path);
        File file = new File(path);
        try {
            return !file.getCanonicalPath().equals(file.getAbsolutePath());
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean mkdirs(String path) {
        path = toPath(path);
        return new File(path).mkdirs();
    }

    @Override
    public boolean delete(String path) {
        path = toPath(path);
        File file = new File(path);
        return file.delete();
    }

    @Override
    public File getFile(String path) {
        path = toPath(path);
        path = path.isEmpty() ? "/" : path;
        return new File(path);
    }

    @Override
    public IORandomAccess openRandomAccess(String path, boolean writeable) throws IOException {
        path = toPath(path);
        return new IORandomAccessFile(new File(path), "rw");
    }
}
