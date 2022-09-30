package mihailris.mio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MixedResDevice extends IODeviceAdapter {
    private final String localDir;
    private final String jarDir;

    public MixedResDevice(String localDir, String jarDir) {
        this.localDir = localDir;
        this.jarDir = jarDir;
    }

    @Override
    public boolean isReadonly() {
        return true;
    }

    @Override
    public InputStream read(String path) throws IOException {
        if (Disk.isJar()){
            InputStream is = Disk.class.getResourceAsStream("/"+jarDir+"/"+path);
            if (is == null)
                throw new IOException("could not to read internal file "+path);
            return is;
        } else {
            return new FileInputStream(new File(localDir, path));
        }
    }

    @Override
    public long length(String path) {
        if (Disk.isJar()){
            URL url = Disk.class.getResource(jarDir+"/"+path);
            if (url == null)
                return -1;
            try {
                return url.openConnection().getContentLengthLong();
            } catch (IOException e) {
                return -1;
            }
        } else {
            return new File(localDir, path).length();
        }
    }

    @Override
    public IOPath[] listDir(IOPath path) {
        String[] names;
        IOPath[] paths;
        if (Disk.isJar()){
            try {
                String[] lines = Disk.readString(path.child("_files.txt")).split("\n");
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
        } else {
            names = new File(localDir, path.getPath()).list();
            if (names == null)
                return null;
            paths = new IOPath[names.length];
            for (int i = 0; i < names.length; i++) {
                IOPath newpath = path.cpy().child(names[i]);
                paths[i] = newpath;
            }
        }
        return paths;
    }

    @Override
    public long lastModified(String path) {
        if (Disk.isJar()) {
            URL url = Disk.class.getResource(jarDir+"/"+path);
            if (url == null)
                return -1;
            try {
                return url.openConnection().getLastModified();
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return new File(localDir, path).lastModified();
    }

    @Override
    public boolean setLastModified(String path, long lastModified) {
        if (Disk.isJar()) {
            return false;
        }
        return new File(localDir, path).setLastModified(lastModified);
    }

    @Override
    public boolean exists(String path) {
        if (Disk.isJar()){
            return Disk.class.getResource("/res/"+path) != null;
        } else {
            return new File(localDir, path).exists();
        }
    }

    @Override
    public boolean isFile(String path) {
        if (Disk.isJar()){
            try {
                InputStream input = Disk.class.getResourceAsStream("/res/"+path);
                if (input == null)
                    return false;
                boolean isfile = input.available() > 0;
                input.close();
                return isfile;
            } catch (IOException e) {
                return false;
            }
        } else {
            return new File(localDir, path).isFile();
        }
    }

    @Override
    public boolean isDirectory(String path) {
        if (Disk.isJar()){
            try {
                InputStream input = Disk.class.getResourceAsStream("/res/"+path);
                if (input == null)
                    return false;
                boolean isdir = input.available() == 0;
                input.close();
                return isdir;
            } catch (IOException e) {
                return false;
            }
        } else {
            return new File(localDir, path).isDirectory();
        }
    }

    @Override
    public boolean isLink(String path) {
        if (Disk.isJar()){
            return false;
        }
        File file = new File(localDir, path);
        try {
            return !file.getCanonicalPath().equals(file.getAbsolutePath());
        } catch (IOException e) {
            return false;
        }
    }
}
