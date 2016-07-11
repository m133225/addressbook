package address.keybindings;

import address.events.BaseEvent;
import javafx.scene.input.KeyCombination;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Represents a shortcut that is a key sequence e.g. G followed by P
 */
public class KeySequence extends KeyBinding{

    /** Max delay (in milliseconds) allowed between key presses of a key sequence */
    protected static final int KEY_SEQUENCE_MAX_MILLISECONDS_BETWEEN_KEYS = 1000;

    protected KeyCombination secondKeyCombination;

    @Deprecated
    public KeySequence(String name, KeyCombination firstKeyCombination,  BaseEvent eventToRaise) {
        super(name, firstKeyCombination, eventToRaise);
        assert false : "Invalid constructor called";
    }

    public KeySequence(String name, KeyCombination firstKeyCombination,
                       KeyCombination secondKeyCombination, BaseEvent eventToRaise) {
        super(name, firstKeyCombination, eventToRaise);
        assert secondKeyCombination != null: "Second key combination cannot be null";
        this.secondKeyCombination = secondKeyCombination;
    }

    public KeyCombination getSecondKeyCombination(){
        return secondKeyCombination;
    }

    @Override
    public String toString(){
        return "Key sequence " + getDisplayText() + ", " + secondKeyCombination.getDisplayText();
    }

    /**
     * Returns true of the {@code otherKeyCombination} is exactly same as one of the key combinations
     *     in this key sequence.
     */
    public boolean isIncluded(KeyCombination otherKeyCombination) {

        if (otherKeyCombination == null) {return false; }

        return keyCombination.toString().equals(otherKeyCombination.toString())
                || secondKeyCombination.toString().equals(otherKeyCombination.toString());

        //TODO: make the comparison smarter so that it can detect a match between CTRL+X and SHORTCUT+X
    }

    /**
     * Returns true if {@code (timeOfSecondKeyEvent - timeOfFirstKeyEvent)} is within the permissible delay between
     *     two key events for them to be considered a key sequence.<br>
     * Assumption: {@code timeOfFirstKeyEvent =< timeOfSecondKeyEvent}
     * @param timeOfFirstKeyEvent {@code System.nanoTime() when the first key event occurred}
     * @param timeOfSecondKeyEvent {@code System.nanoTime() when the second key event occurred}
     */
    public static boolean isElapsedTimePermissibile(long timeOfFirstKeyEvent, long timeOfSecondKeyEvent) {
        assert timeOfFirstKeyEvent >= 0 : "times cannot be negative";
        assert timeOfSecondKeyEvent >= 0 : "times cannot be negative";

        long durationInNanoSeconds = timeOfSecondKeyEvent - timeOfFirstKeyEvent;
        assert durationInNanoSeconds >= 0 : "second key event cannot happen before the first one";

        return durationInNanoSeconds <= NANOSECONDS.convert(KEY_SEQUENCE_MAX_MILLISECONDS_BETWEEN_KEYS, MILLISECONDS);
    }

}
