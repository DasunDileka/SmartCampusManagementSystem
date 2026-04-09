import java.time.LocalDate;
import java.util.Date;

public final class LegacyDateSpinnerAdapter implements SpinnerDateAdapter {

    @Override
    public LocalDate toLocalDate(Object spinnerValue) {
        return DateTimeUtil.toLocalDate((Date) spinnerValue);
    }
}
