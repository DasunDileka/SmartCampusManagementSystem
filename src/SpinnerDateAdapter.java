import java.time.LocalDate;

/**
 * Structural — Adapter (target role): domain code wants {@link LocalDate}; Swing uses legacy {@link java.util.Date}.
 * Implementations adapt spinner (or other UI) values into {@code LocalDate}.
 */
@FunctionalInterface
public interface SpinnerDateAdapter {

    LocalDate toLocalDate(Object spinnerValue);
}
