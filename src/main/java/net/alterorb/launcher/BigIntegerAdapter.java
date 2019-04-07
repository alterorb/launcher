package net.alterorb.launcher;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.math.BigInteger;

public class BigIntegerAdapter {

    @ToJson
    public String toJson(BigInteger bigInteger) {
        return bigInteger.toString();
    }

    @FromJson
    public BigInteger fromJson(String json) {
        return new BigInteger(json);
    }
}
