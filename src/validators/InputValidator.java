package validators;

public abstract class InputValidator {
    protected InputValidator next;

    public InputValidator linkWith(InputValidator next) {
        this.next = next;
        return next;
    }

    public abstract boolean validate(String input);

    protected boolean validateNext(String input) {
        if (next == null) return true;
        return next.validate(input);
    }
}