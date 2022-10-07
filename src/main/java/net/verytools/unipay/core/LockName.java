package net.verytools.unipay.core;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class LockName {

    private final byte[] name;

    public LockName(String name) {
        this.name = name.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LockName lockName = (LockName) o;

        return Arrays.equals(name, lockName.name);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(name);
    }
}
