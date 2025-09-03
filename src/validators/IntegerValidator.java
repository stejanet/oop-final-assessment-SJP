package validators;

public class IntegerValidator extends InputValidator {
    @Override
    public boolean validate(String input) {
        try {
            Integer.parseInt(input.trim());
            return validateNext(input);
        } catch (NumberFormatException e) {
            System.out.println("Input must be a valid integer.");
            return false;
        }
    }
}