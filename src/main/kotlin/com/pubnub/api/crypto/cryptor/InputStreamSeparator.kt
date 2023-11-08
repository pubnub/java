package com.pubnub.api.crypto.cryptor

import java.io.InputStream

/** This class is used to separate the inputStream from the CipherInputStream.
 * We might want to separate the inputStream from the CipherInputStream because we want to be able to close the
 * CipherInputStream without closing the inputStream.
 * */
internal class InputStreamSeparator(private val inputStream: InputStream) : InputStream() {
    override fun read(): Int {
        return inputStream.read()
    }

    override fun read(b: ByteArray): Int {
        return inputStream.read(b)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return inputStream.read(b, off, len)
    }

    override fun skip(n: Long): Long {
        return inputStream.skip(n)
    }

    override fun available(): Int {
        return inputStream.available()
    }

    override fun mark(readlimit: Int) {
        inputStream.mark(readlimit)
    }

    override fun reset() {
        inputStream.reset()
    }

    override fun markSupported(): Boolean {
        return inputStream.markSupported()
    }
}
