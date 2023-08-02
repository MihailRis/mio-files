package mihailris.mio;

/**
 * Copy or move files collision solution function
 */
public interface CollisionSolver {
    CollisionSolver ERROR = (prev, next) -> {
        throw new RuntimeException("destination file '"+prev+"' does already exists");
    };
    CollisionSolver SKIP_FILE = (prev, next) -> false;
    CollisionSolver REPLACE_FILE = (prev, next) -> false;
    CollisionSolver CHOOSE_NEWER = (prev, next) -> next.lastModified() > prev.lastModified();
    CollisionSolver CHOOSE_BIGGEST = (prev, next) -> next.length() > prev.length();

    /**
     * @param prev file will be replaced
     * @param next replacement file
     * @return true is replace, false is skip
     */
    boolean solve(IOPath prev, IOPath next);
}
