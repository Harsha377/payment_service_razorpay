package com.streamhub.payment.common.util;

import com.github.f4b6a3.ulid.UlidCreator;

public final class UlidGenerator {
    private UlidGenerator(){

    }

    public static String generate(){
        return UlidCreator.getUlid().toString();
    }
}
