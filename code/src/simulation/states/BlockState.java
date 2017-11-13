package simulation.states;

/**
 * Possible states that our data blocks may have.
 */
public enum BlockState {
    UNCACHED,
    INVALID,
    SHARED,
    MODIFIED
}
