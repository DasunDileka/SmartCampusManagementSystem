import java.util.concurrent.atomic.AtomicLong;

public final class IdGenerator {
    private final AtomicLong counter = new AtomicLong(1);
    private final String prefix;

    public IdGenerator(String prefix) {
        this.prefix = prefix;
    }

    public String next() {
        return prefix + counter.getAndIncrement();
    }
}
