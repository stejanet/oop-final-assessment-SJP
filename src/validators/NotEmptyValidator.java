package validators;

public class NotEmptyValidator extends InputValidator {
    @Override
    public boolean validate(String input) {
        if (input == null || input.trim().isEmpty()) {
            System.out.println("Input cannot be empty.");
            return false;
        }
        return validateNext(input);
    }
}