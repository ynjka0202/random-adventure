package mn.adventure.service;

/** Хэрэглэгчид харуулах ойлгомжтой алдааны мессежтэй exception. */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) { super(message); }
}
