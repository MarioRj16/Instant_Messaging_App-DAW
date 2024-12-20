package com.example.messagingapp.http.pipeline

import com.example.messagingapp.domain.AuthenticatedUser
import com.example.messagingapp.http.model.output.Problem
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.net.URI
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthenticationInterceptor(
    private val authorizationHeaderProcessor: RequestTokenProcessor,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (handler is HandlerMethod &&
            handler.methodParameters.any {
                it.parameterType == AuthenticatedUser::class.java
            }
        ) {
            var user =
                authorizationHeaderProcessor
                    .processAuthorizationHeaderValue(request.getHeader(NAME_AUTHORIZATION_HEADER))
            if (user == null) {
                user = getUserFromCookies(request)
            }
            return if (user == null) {
                response.status = 401
                response.addHeader(NAME_WWW_AUTHENTICATE_HEADER, RequestTokenProcessor.SCHEME)
                response.contentType = "application/problem+json"
                response.writer.write(
                    Problem(
                        type = Problem.INVALID_TOKEN,
                        title = "You are not authorized to access this resource",
                        detail = "Please provide a valid token",
                        instance = URI(request.requestURI),
                    ).toJson()
                )
                false
            } else {
                AuthenticatedUserArgumentResolver.addUserTo(user, request)
                true
            }
        }
        return true
    }

    private fun getUserFromCookies(request: HttpServletRequest): AuthenticatedUser? {
        val cookies: Array<Cookie>? = request.cookies
        cookies?.forEach { cookie ->
            if (cookie.name == COOKIE_NAME) {
                return authorizationHeaderProcessor.processAuthorizationHeaderValue("Bearer ${cookie.value}")
            }
        }
        return null
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthenticationInterceptor::class.java)
        const val NAME_AUTHORIZATION_HEADER = "Authorization"
        private const val COOKIE_NAME = "authToken"
        private const val NAME_WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"
    }
}
