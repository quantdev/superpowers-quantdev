// Complete implementation of condition-based waiting utilities for JUnit 5 tests.
// Plain JDK — no external dependency required (see superpowers:java-development
// dependency gate before reaching for Awaitility).
//
// Drop into src/test/java as a test utility class.

import java.time.Duration;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class ConditionBasedWaiting {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
    private static final long POLL_INTERVAL_MS = 10; // 10ms balances latency vs CPU

    private ConditionBasedWaiting() {}

    /**
     * Wait until a condition becomes true.
     *
     * Example:
     *   waitFor(() -> listener.received("TOOL_RESULT"), "TOOL_RESULT event");
     */
    public static void waitFor(BooleanSupplier condition, String description) {
        waitFor(condition, description, DEFAULT_TIMEOUT);
    }

    public static void waitFor(BooleanSupplier condition, String description, Duration timeout) {
        waitForValue(() -> condition.getAsBoolean() ? Boolean.TRUE : null, description, timeout);
    }

    /**
     * Wait until a supplier produces a non-null value, and return it.
     * Re-evaluates the supplier on every poll — never caches stale state.
     *
     * Example:
     *   Event event = waitForValue(
     *       () -> events.stream().filter(e -> e.type() == TOOL_RESULT).findFirst().orElse(null),
     *       "TOOL_RESULT event");
     */
    public static <T> T waitForValue(Supplier<T> supplier, String description) {
        return waitForValue(supplier, description, DEFAULT_TIMEOUT);
    }

    public static <T> T waitForValue(Supplier<T> supplier, String description, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (true) {
            T result = supplier.get();
            if (result != null) {
                return result;
            }
            if (System.nanoTime() > deadline) {
                throw new AssertionError(
                    "Timeout waiting for " + description + " after " + timeout.toMillis() + "ms");
            }
            sleep(description);
        }
    }

    /**
     * Wait until at least {@code count} elements of a live list match the predicate.
     * The list must be the live collection (e.g. a ConcurrentLinkedQueue snapshot getter),
     * not a copy taken before the wait.
     *
     * Example:
     *   // Wait for 2 AGENT_MESSAGE events (initial response + continuation)
     *   waitForCount(recorder.events(), e -> e.type() == AGENT_MESSAGE, 2, "AGENT_MESSAGE x2");
     */
    public static <T> List<T> waitForCount(
            List<T> liveList, Predicate<T> matches, int count, String description) {
        return waitForValue(
            () -> {
                List<T> matching = liveList.stream().filter(matches).toList();
                return matching.size() >= count ? matching : null;
            },
            description + " (need " + count + ")");
    }

    private static void sleep(String description) {
        try {
            Thread.sleep(POLL_INTERVAL_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted waiting for " + description, e);
        }
    }
}

// Usage example from an actual debugging session:
//
// BEFORE (flaky):
// ---------------
// var future = agent.sendMessage("Execute tools");
// Thread.sleep(300);                       // Hope tools start in 300ms
// agent.abort();
// future.join();
// Thread.sleep(50);                        // Hope results arrive in 50ms
// assertEquals(2, toolResults.size());     // Fails randomly under load
//
// AFTER (reliable):
// ----------------
// var future = agent.sendMessage("Execute tools");
// waitForCount(recorder.events(), e -> e.type() == TOOL_CALL, 2, "tools started");
// agent.abort();
// future.join();
// waitForCount(recorder.events(), e -> e.type() == TOOL_RESULT, 2, "tool results");
// assertEquals(2, toolResults.size());     // Always succeeds
//
// Result: 60% pass rate → 100%, 40% faster execution
