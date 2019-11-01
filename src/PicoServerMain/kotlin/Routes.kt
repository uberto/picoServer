package com.ubertob.picoserver

fun handler(request: Request): Response =

        when (request.path) {
            "/hello" -> Response(
                    status = "200 OK",
                    contentType = "text/html;charset=UTF-8",
                    body = "<html><body><h1>Hello Greetings from Kotlin Native</h1></body></html>")
            else ->
                Response(
                        status = "404 NOT_FOUND",
                        contentType = "text/html;charset=UTF-8",
                        body = "<html><body><h1>Try /hello</h1></body></html>")
        }
