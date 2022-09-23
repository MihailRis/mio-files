package mihailris.mio;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class IOPath {
    private String path;
    IOPath(String path){
        this.path = path;
    }

    IOPath(String prefix, String path){
        this.path = prefix+":"+path;
    }

    public IOPath set(String path){
        this.path = path;
        return this;
    }

    public IOPath set(IOPath path) {
        this.path = path.path;
        return this;
    }

    public boolean hasPrefix(){
        return path.indexOf(':') != -1;
    }

    /**
     * @return label part of iopath (all before ':')
     */
    public String getPrefix(){
        if (!hasPrefix())
            return "";
        return path.substring(0, path.indexOf(':'));
    }

    /**
     * @return depth of iopath where root is 0
     */
    public int getDepth(){
        int slashes = path.length() - path.replace("/", "").length();
        return slashes + (getPath().isEmpty() ? 0 : 1);
    }

    public String getPath() {
        return path.substring(path.indexOf(':')+1);
    }

    /**
     * @param node subnode name
     * @return new subnode iopath
     */
    public IOPath child(String node){
        String path = this.path;
        if (!path.endsWith("/") && !path.endsWith(":"))
            path += "/";
        path += node;
        return IOPath.get(path);
    }

    /**
     * @return new parent node iopath
     */
    public IOPath parent(){
        String path = this.path;
        int index = path.lastIndexOf('/');
        if (index == -1){
            path = getPrefix();
        } else {
            path = path.substring(0, index);
        }
        return IOPath.get(path);
    }

    public boolean isExists(){
        return Disk.isExist(this);
    }

    public long getLastModification(){
        return Disk.lastModified(this);
    }

    public IOPath cpy(){
        return new IOPath(path);
    }

    public static IOPath get(String path) {
        IOPath iopath = new IOPath(path);
        if ("abs".equals(iopath.getPrefix())) {
            throw new IllegalArgumentException("absolute paths are not allowed, use Disk.absolute(..)");
        }
        return iopath;
    }

    /**
     * @return name of iopath node (with extension)
     */
    public String name() {
        String name = getPath();
        int index = name.lastIndexOf('/');
        if (index != -1){
            name = name.substring(index+1);
        }
        return name;
    }

    /**
     * @return name of iopath node (with no extension)
     */
    public String nameNoExt() {
        String name = getPath();
        int index = name.lastIndexOf('/');
        if (index != -1){
            name = name.substring(index+1);
        }
        index = name.lastIndexOf('.');
        if (index == -1)
            return name;
        return name.substring(0, index);
    }

    /**
     * @return iopath path part without extension
     */
    public String pathNoExt() {
        String path = getPath();
        int index = path.lastIndexOf('.');
        if (index == -1)
            return path;
        return path.substring(0, index);
    }

    public String createResourceName(boolean removeExtension){
        IOPath iopath = cpy();
        iopath.removeRoot();
        if (removeExtension)
            return iopath.pathNoExt();
        else
            return iopath.getPath();
    }

    private IOPath removeRoot() {
        String prefix = getPrefix();
        String path = getPath();
        int index = path.indexOf("/");
        if (index == -1 || index + 1 == path.length()){
            set(prefix);
        } else {
            set(prefix+":"+path.substring(index+1));
        }
        return this;
    }

    /**
     * @return removes extension and returns itself
     */
    public IOPath removeExt() {
        int index = path.lastIndexOf('.');
        if (index == -1)
            return this;
        path = path.substring(index+1);
        return this;
    }

    @Override
    public String toString() {
        return path;
    }

    public void addToPath(String ext) {
        path += ext;
    }

    public boolean isFile() {
        return Disk.isFile(this);
    }

    public static void sortByModificationDate(IOPath[] paths){
        Arrays.sort(paths, Comparator.comparingLong(IOPath::getLastModification).reversed());
    }

    /**
     * Delete all directory content
     */
    public void clearDirectory() throws IOException {
        IOPath[] paths = Disk.list(this);
        if (paths == null)
            return;
        for (IOPath path : paths){
            if (path.isDirectory()) {
                path.deleteTree();
            } else {
                path.delete();
            }
        }
    }

    /**
     * Delete file or empty directory
     * @return true if deleted something
     */
    public boolean delete() throws IOException {
        if (!isExists())
            return false;
        return Disk.delete(this);
    }

    /**
     * @return length of file (bytes)
     */
    public long length() {
        return Disk.length(this);
    }

    public boolean mkdirs() {
        return Disk.mkdirs(this);
    }

    public void deleteTree() throws IOException {
        clearDirectory();
        delete();
    }

    public boolean isDirectory() {
        return Disk.isDirectory(this);
    }

    public void writeString(String string) {
        Disk.writeString(this, string);
    }

    public void writeString(String string, boolean append) {
        Disk.writeString(this, string, append);
    }

    public File file() {
        return Disk.getFile(this);
    }

    /**
     * Read all file bytes
     */
    public byte[] readBytes() throws IOException {
        return Disk.readBytes(this);
    }

    public String readString() throws IOException {
        return Disk.readString(this);
    }

    public String readString(String charset) throws IOException {
        return Disk.readString(this, charset);
    }

    /**
     * @return node name extension (.png, .jpg, ...)
     */
    public String extension() {
        String name = name();
        int index = name.lastIndexOf(".");
        if (index == -1 || index + 1 == name.length())
            return "";
        return name.substring(index+1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IOPath path1 = (IOPath) o;
        return Objects.equals(path, path1.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    public IOPath extended(String ext) {
        return IOPath.get(path+ext);
    }

    public IOPath[] listDir() {
        return Disk.list(this);
    }
}
