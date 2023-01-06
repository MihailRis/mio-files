package mihailris.mio;

import java.io.*;
import java.net.URL;

/**
 * IODevice that use Java Resources
 * Usefull for internal application resources (read only)
 * <br><br>
 * <i>Create _files.txt in directories to use listdir feature here</i>
 */
public class ResDevice extends IODeviceAdapter {
    private final String jarDir;

    public ResDevice(String jarDir) {
        if (!(jarDir.isEmpty() || jarDir.endsWith("/")))
            jarDir += "/";
        this.jarDir = jarDir;
    }

    @Override
    public boolean isReadonly() {
        return true;
    }

    @Override
    public InputStream read(String path) throws IOException {
        InputStream is = Disk.class.getResourceAsStream(jarDir+path);
        if (is == null)
            throw new IOException("could not to read internal file "+path);
        return is;
    }

    @Override
    public long length(String path) {
        URL url = Disk.class.getResource(jarDir+path);
        if (url == null)
            return -1;
        try {
            return url.openConnection().getContentLengthLong();
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public IOPath[] listDir(IOPath path) {
        IOPath[] paths;
        try {
            String[] lines = path.child("_files.txt").readString().split("\n");
            int count = 0;
            for (int i = 0; i < lines.length; i++) {
                lines[i] = lines[i].trim();
                if (!lines[i].isEmpty())
                    count++;
            }
            paths = new IOPath[count];
            int index = 0;
            for (String line : lines) {
                if (line.isEmpty())
                    continue;
                paths[index++] = path.child(line);
            }
        } catch (Exception e){
            System.err.println("could not to read _files.txt");
            return null;
        }
        return paths;
    }

    @Override
    public long lastModified(String path) {
        URL url = Disk.class.getResource(jarDir+path);
        if (url == null)
            return -1;
        try {
            return url.openConnection().getLastModified();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public boolean setLastModified(String path, long lastModified) {
        return false;
    }

    @Override
    public boolean exists(String path) {
        return Disk.class.getResource(jarDir+path) != null;
    }

    @Override
    public boolean isFile(String path) {
        try {
            InputStream input = Disk.class.getResourceAsStream(jarDir+path);
            if (input == null)
                return false;
            boolean isfile = input.available() > 0;
            input.close();
            return isfile;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean isDirectory(String path) {
        try {
            InputStream input = Disk.class.getResourceAsStream(jarDir+path);
            if (input == null)
                return false;
            boolean isdir = input.available() == 0;
            input.close();
            return isdir;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean isLink(String path) {
        return false;
    }
}
