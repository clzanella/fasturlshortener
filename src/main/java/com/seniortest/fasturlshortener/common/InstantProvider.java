package com.seniortest.fasturlshortener.common;

import java.time.Instant;

public interface InstantProvider {
    default Instant now(){
        return Instant.now();
    }

    class Impl implements InstantProvider { }
}
