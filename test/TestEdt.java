import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

/**
 * {@link NotificationBus} delivers on the EDT via {@code invokeLater}; tests flush the queue so
 * assertions can run on the test thread after {@code publish}.
 */
final class TestEdt {

    private TestEdt() {
    }

    static void flushEdt() {
        try {
            if (EventQueue.isDispatchThread()) {
                return;
            }
            EventQueue.invokeAndWait(() -> {
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            Throwable c = e.getCause();
            if (c instanceof RuntimeException) {
                throw (RuntimeException) c;
            }
            if (c instanceof Error) {
                throw (Error) c;
            }
            throw new IllegalStateException(c);
        }
    }
}
