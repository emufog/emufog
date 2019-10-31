package emufog.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UniqueIDProviderTest {

    @Test
    void checkInit() {
        UniqueIDProvider provider = new UniqueIDProvider();
        assertFalse(provider.isUsed(0));
        assertEquals(0, provider.getNextID());
        assertTrue(provider.isUsed(0));
    }

    @Test
    void checkMarking() {
        UniqueIDProvider provider = new UniqueIDProvider();
        assertFalse(provider.isUsed(42));
        provider.markIDused(42);
        assertTrue(provider.isUsed(42));
    }

    @Test
    void checkNextIdCall() {
        UniqueIDProvider provider = new UniqueIDProvider();
        provider.markIDused(0);
        assertEquals(1, provider.getNextID());
        assertTrue(provider.isUsed(1));
    }
}