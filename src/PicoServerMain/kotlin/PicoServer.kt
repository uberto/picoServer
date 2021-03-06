package com.ubertob.picoserver

import kotlinx.cinterop.*
import platform.posix.*

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: echoserver.kexe <port>")
        return
    }

    val port = args[0].toShort()

    // Initialize sockets in platform-dependent way.
    init_sockets()


    memScoped {

        val buffer = ByteArray(1024)
        val serverAddr = alloc<sockaddr_in>()

        val listenFd = socket(AF_INET, SOCK_STREAM, 0)
                .ensureUnixCallResult("socket") { !it.isMinusOne() }

        with(serverAddr) {
            memset(this.ptr, 0, sockaddr_in.size.convert())
            sin_family = AF_INET.convert()
            sin_port = posix_htons(port).convert()
        }

        bind(listenFd, serverAddr.ptr.reinterpret(), sockaddr_in.size.convert())
                .ensureUnixCallResult("bind") { it == 0 }


        listen(listenFd, 10)
                .ensureUnixCallResult("listen") { it == 0 }

        while (true) {

            val commFd = accept(listenFd, null, null)
                    .ensureUnixCallResult("accept") { !it.isMinusOne() }

            buffer.usePinned { pinned ->
                val length = recv(commFd, pinned.addressOf(0), buffer.size.convert(), 0).toInt()
                        .ensureUnixCallResult("read") { it >= 0 }


                val request = readRawRequest(buffer, length)
//                println("received req $request")
                val response = handler(request)

                val rawResp = writeRawResponse(response)


                send(commFd, rawResp.refTo(0), rawResp.size.convert(), 0)
                        .ensureUnixCallResult("write") { it >= 0 }


            }
            close(commFd)
        }
    }
}


inline fun Int.ensureUnixCallResult(op: String, predicate: (Int) -> Boolean): Int {
    if (!predicate(this)) {
        throw Error("$op: ${strerror(posix_errno())!!.toKString()}")
    }
    return this
}

inline fun Long.ensureUnixCallResult(op: String, predicate: (Long) -> Boolean): Long {
    if (!predicate(this)) {
        throw Error("$op: ${strerror(posix_errno())!!.toKString()}")
    }
    return this
}

private fun Int.isMinusOne() = (this == -1)
