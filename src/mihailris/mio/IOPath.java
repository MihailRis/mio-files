package mihailris.mio;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

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

    public String getPrefix(){
        if (!hasPrefix())
            return "";
        return path.substring(0, path.indexOf(':'));
    }

    public int getDepth(){
        int slashes = path.length() - path.replace("/", "").length();
        return slashes + (getPath().isEmpty() ? 0 : 1);
    }

    public String getPath() {
        return path.substring(path.indexOf(':')+1);
    }

    public IOPath child(String node){
        String path = this.path;
        if (!path.endsWith("/"))
            path += "/";
        path += node;
        return IOPath.get(path);
    }

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

    public String name() {
        String name = getPath();
        int index = name.lastIndexOf('/');
        if (index != -1){
            name = name.substring(index+1);
        }
        return name;
    }

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

    public void emptyDirectory() throws IOException {
        IOPath[] paths = Disk.iopathsList(this);
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

    public boolean delete() throws IOException {
        if (!isExists())
            return false;
        return Disk.delete(this);
    }

    public long length() {
        return Disk.length(this);
    }

    public boolean mkdirs() {
        return Disk.mkdirs(this);
    }

    public void deleteTree() throws IOException {
        emptyDirectory();
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

    public byte[] readBytes() throws IOException {
        return Disk.readBytes(this);
    }

    public String readString() throws IOException {
        return Disk.readString(this);
    }

    public String readString(String charset) throws IOException {
        return Disk.readString(this, charset);
    }

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
}
