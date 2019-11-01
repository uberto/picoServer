package com.ubertob.picoserver

typealias HttpHandler = (Request) -> Response

data class Request (val verb: String, val path: String, val headers: List<String>)

data class Response (val status: String, val contentType: String, val body: String)

val emptyRequest = Request("GET", "/", emptyList())


fun readRawRequest(array: ByteArray, length: Int): Request {
    val raw: String = array.decodeToString(0, length)
    //                GET /helloG HTTP/1.1

    val lines = raw.lines()
    if (lines.size == 0)
        return emptyRequest

    val parts = lines[0].split(" ")

    if (parts.size < 2)
        return emptyRequest

    return Request(parts[0], parts[1], emptyList())
}

fun writeRawResponse(response: Response): ByteArray =
        """
        HTTP/1.1 ${response.status}
        Content-Length: ${response.body.length}
        Content-Type: ${response.contentType}

        ${response.body}
    """.trimIndent().encodeToByteArray()
