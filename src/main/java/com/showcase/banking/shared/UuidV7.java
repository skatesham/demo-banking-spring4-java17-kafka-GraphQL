package com.showcase.banking.shared;

import java.security.SecureRandom;
import java.util.UUID;

/** Generates RFC 9562 UUID version 7 identifiers (time ordered, millisecond precision). */
public final class UuidV7 {
    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidV7() { }

    public static UUID next() {
        long timestamp = System.currentTimeMillis();
        long mostSignificantBits = (timestamp << 16) | 0x7000L | RANDOM.nextInt(1 << 12);
        long leastSignificantBits = (RANDOM.nextLong() & 0x3fff_ffff_ffff_ffffL) | 0x8000_0000_0000_0000L;
        return new UUID(mostSignificantBits, leastSignificantBits);
    }
}
