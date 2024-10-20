package com.example.messagingapp

import com.example.messagingapp.domain.UUIDTokenEncoder
import com.example.messagingapp.domain.UserDomainConfig
import com.example.messagingapp.http.pipeline.AuthenticatedUserArgumentResolver
import com.example.messagingapp.http.pipeline.AuthenticationInterceptor
import com.example.messagingapp.repository.jdbi.configureWithAppRequirements
import kotlinx.datetime.Clock
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.Locale
import kotlin.time.Duration.Companion.hours

@SpringBootApplication
class MessagingAppApplication {
    @Bean
    fun jdbi() =
        Jdbi
            .create(
                PGSimpleDataSource().apply {
                    setURL(Environment.getDbUrl())
                },
            ).configureWithAppRequirements()

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun tokenEncoder() = UUIDTokenEncoder()

    @Bean
    fun clock() = Clock.System

    @Bean
    fun userDomainConfig() =
        UserDomainConfig(
            tokenTTL = 24.hours,
            tokenRollingTTL = 1.hours,
            maxTokensPerUser = 3,
            registrationInvitationTTL = 24.hours,
        )
}

@Configuration
class PipelineConfigurer(
    val authenticationInterceptor: AuthenticationInterceptor,
    val authenticatedUserArgumentResolver: AuthenticatedUserArgumentResolver,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticatedUserArgumentResolver)
    }
}

fun main(args: Array<String>) {
    Locale.setDefault(Locale.ENGLISH)
    runApplication<MessagingAppApplication>(*args)
}
