package mihailris.mio;

import java.io.IOException;
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

    public void write(String string) {
        Disk.write(this, string);
    }

    public void append(String string) {
        Disk.write(this, string, true);
    }

    public void write(byte[] bytes) {
        Disk.write(this, bytes);
    }

    public void append(byte[] bytes) {
        Disk.write(this, bytes, true);
    }

    public String readString() throws IOException {
        return Disk.readString(this);
    }

    public String readString(String charset) throws IOException {
        return Disk.readString(this, charset);
    }

    public byte[] readBytes() throws IOException {
        return Disk.readBytes(this);
    }

    public long length() {
        return Disk.length(this);
    }

    public long lastModified() {
        return Disk.lastModified(this);
    }

    public void mkdirs() {
        Disk.mkdirs(this);
    }

    public void delete() throws IOException {
        Disk.delete(this);
    }

    public void deleteTree() throws IOException {
        Disk.deleteTree(this);
    }

    public boolean isExists(){
        return Disk.isExists(this);
    }

    public boolean isFile() {
        return Disk.isFile(this);
    }

    public boolean isLink() {
        return Disk.isLink(this);
    }

    public boolean isDirectory() {
        return Disk.isDirectory(this);
    }

    public long getUsableSpace() {
        return Disk.getUsableSpace(this);
    }

    public IOPath[] list() {
        return Disk.list(this);
    }

    public boolean setLastModified(long lastModified) {
        return Disk.setLastModified(this, lastModified);
    }

    public void clearDirectory() throws IOException {
        Disk.clearDirectory(this);
    }
}

