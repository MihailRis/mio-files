package mihailris.mio;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Disk {
    public static long totalRead;
    public static long totalWrite;
    private static final Map<String, IODevice> devices = new HashMap<>();

    /**
     * Close all added devices
     */
    public static void close(){
        for (IODevice device : devices.values()){
            try {
                device.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void addDevice(String label, IODevice device){
        addDevice(label, device, false);
    }

    public static void addDevice(String label, IODevice device, boolean makedir){
        devices.put(label, device);
        if (makedir) {
            IOPath root = IOPath.get(label+':');
            if (!isDirectory(root))
                mkdirs(root);
        }
    }

    public static void removeDevice(String label){
        devices.remove(label);
    }

    public static void createResDevice(String label, String jarDir){
        devices.put(label, new ResDevice(jarDir));
    }

    public static void createAbsDevice(){
        devices.put("abs", new AbsDevice());
    }

    public static void createDirDevice(String label, File directory){
        createDirDevice(label, directory, true);
    }

    public static void createDirDevice(String label, File directory, boolean makedir){
        if (!directory.isDirectory() && !makedir)
            throw new IllegalArgumentException(directory+" is not a directory");
        addDevice(label, new DirDevice(directory), makedir);
    }

    public static void createReadonlyDirDevice(String label, File directory){
        if (!directory.isDirectory())
            throw new IllegalArgumentException(directory+" is not a directory");
        addDevice(label, new DirDevice(directory, true));
    }

    public static void createMemoryDevice(String label){
        addDevice(label, new MemoryDevice());
    }

    public static void createMemoryDevice(String label, int capacity){
        addDevice(label, new MemoryDevice(capacity));
    }

    public static IOPath absolute(String path){
        return new IOPath("abs", path);
    }

    public static boolean isJar(Class<?> cls){
        String className = cls.getName().replace('.', '/');
        URL url = cls.getResource("/"+className+".class");
        assert (url != null);
        String classJar = url.toString();
        return classJar.startsWith("jar:");
    }

    private static OutputStream write(IOPath path, boolean append) throws IOException {
        return getDevice(path.getPrefix(), false).write(path.getPath(), append);
    }

    static InputStream read(IOPath iopath) throws IOException {
        return getDevice(iopath.getPrefix(), true).read(iopath.getPath());
    }

    public static void write(IOPath path, byte[] content){
        write(path, content, false);
    }

    public static void write(IOPath path, byte[] content, boolean append){
        OutputStream output = null;
        try {
            output = write(path, append);
            output.write(content);
            output.close();
            totalWrite += content.length;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void write(IOPath path, String content){
        write(path, content.getBytes(), false);
    }

    public static void write(IOPath path, String content, boolean append) {
        write(path, content.getBytes(), append);
    }

    public static long lastModified(IOPath iopath){
        try {
            return getDevice(iopath.getPrefix(), true).lastModified(iopath.getPath());
        } catch (IOException e) {
            return -1;
        }
    }

    public static boolean setLastModified(IOPath iopath, long lastModified){
        try {
            return getDevice(iopath.getPrefix(), true).setLastModified(iopath.getPath(), lastModified);
        } catch (IOException e) {
            return false;
        }
    }

    public static IOPath[] list(IOPath iopath) {
        try {
            return getDevice(iopath.getPrefix(), true).listDir(iopath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static IODevice getDevice(String label, boolean readonly) throws IOException {
        IODevice device = devices.get(label);
        if (device == null)
            throw new IOException("unknown I/O device '"+label+"'");
        if (!readonly && device.isReadonly())
            throw new IOException("device '"+label+"' is read-only");
        return device;
    }

    public static byte[] readBytes(IOPath iopath) throws IOException {
        try (InputStream input = read(iopath)) {
            long length = length(iopath);
            byte[] bytes = new byte[(int) length];
            IOUtil.readFully(input, bytes);
            totalRead += bytes.length;
            return bytes;
        }
    }

    public static void copy(IOPath src, IOPath dst) throws IOException {
        IODevice deviceSrc = getDevice(src.getPrefix(), true);
        IODevice deviceDst = getDevice(dst.getPrefix(), false);

        String srcPath = src.getPath();
        String dstPath = dst.getPath();

        if (deviceSrc == deviceDst) {
            deviceDst.copy(srcPath, dstPath);
        } else {
            copy(deviceSrc, deviceDst, src, dst);
        }
    }

    private static void copy(IODevice deviceSrc, IODevice deviceDst, IOPath src, IOPath dst) throws IOException {
        if (isFile(dst))
            throw new IOException("destination file is already exists");
        long length = length(src);
        try (OutputStream output = deviceDst.write(dst.getPath(), false)) {
            try (InputStream input = deviceSrc.read(src.getPath())) {
                IOUtil.transfer(input, output, length);
            }
        }
    }

    public static void move(IOPath src, IOPath dst) throws IOException {
        IODevice deviceSrc = getDevice(src.getPrefix(), true);
        IODevice deviceDst = getDevice(dst.getPrefix(), false);

        if (deviceSrc == deviceDst) {
            String srcPath = src.getPath();
            String dstPath = dst.getPath();
            deviceDst.move(srcPath, dstPath);
        } else {
            copy(deviceSrc, deviceDst, src, dst);
            delete(src);
        }
    }

    public static String readString(IOPath iopath) throws IOException {
        byte[] bytes = readBytes(iopath);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String readString(IOPath iopath, String charset) throws IOException {
        byte[] bytes = readBytes(iopath);
        return new String(bytes, charset);
    }

    public static void read(Properties properties, IOPath iopath) throws IOException {
        try (InputStream input = read(iopath)) {
            properties.load(input);
        }
    }

    public static boolean isExists(IOPath iopath) {
        try {
            return getDevice(iopath.getPrefix(), true).exists(iopath.getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isDirectory(IOPath iopath) {
        try {
            return getDevice(iopath.getPrefix(), true).isDirectory(iopath.getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isFile(IOPath iopath) {
        try {
            return getDevice(iopath.getPrefix(), true).isFile(iopath.getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param iopath path
     * @return true if iopath points to symlink
     */
    public static boolean isLink(IOPath iopath) {
        try {
            return getDevice(iopath.getPrefix(), true).isLink(iopath.getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean mkdirs(IOPath iopath) {
        try {
            return getDevice(iopath.getPrefix(), false).mkdirs(iopath.getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param iopath path of file, symlink or empty directory
     * @return true if deleted anything
     */
    public static boolean delete(IOPath iopath) throws IOException {
        return getDevice(iopath.getPrefix(), false).delete(iopath.getPath());
    }

    public static File getFile(IOPath iopath) {
        try {
            return getDevice(iopath.getPrefix(), true).getFile(iopath.getPath());
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Delete file/directory recursive
     */
    public static void deleteTree(IOPath path) throws IOException {
        clearDirectory(path);
        delete(path);
    }

    /**
     * Delete all directory content
     */
    public static void clearDirectory(IOPath iopath) throws IOException {
        IOPath[] paths = list(iopath);
        if (paths == null)
            return;
        for (IOPath path : paths){
            if (isDirectory(path)) {
                deleteTree(path);
            } else {
                delete(path);
            }
        }
    }

    public static boolean hasDevice(String label) {
        return devices.containsKey(label);
    }

    public static long length(IOPath path) {
        try {
            return getDevice(path.getPrefix(), true).length(path.getPath());
        } catch (IOException e) {
            return -1;
        }
    }

    public static Collection<String> getLabels() {
        return devices.keySet();
    }

    public static long getUsableSpace(IOPath path) {
        try {
            return getDevice(path.getPrefix(), true).getUsableSpace(path.getPath());
        } catch (IOException e) {
            return -1;
        }
    }
}
