package org.ymkm.pwchart;

import android.support.annotation.NonNull;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class MD5 {

    public static long seedHash(@NonNull final String seed)  {
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.reset();
            final byte[] fullHash = digest.digest(seed.getBytes(Charset.forName("UTF-8")));
            final String strHash = String.format("%032X", new BigInteger(1, fullHash));
            // hash the seed phrase and use the first 4 bytes as the random number seed.
            // The Mersenne Twister masks off everything above the first 4 bytes.
            return Long.decode("0x" + strHash.substring(0, 8));
        }
        catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }



    private MD5() {
    }
}
