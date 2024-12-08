@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.messagingapp.http

import com.example.messagingapp.domain.AuthenticatedUser
import com.example.messagingapp.http.model.input.LoginInputModel
import com.example.messagingapp.http.model.input.UserCreateInputModel
import com.example.messagingapp.http.model.output.LoginOutputModel
import com.example.messagingapp.http.model.output.Problem
import com.example.messagingapp.http.model.output.RegistrationInvitationCreateOutputModel
import com.example.messagingapp.http.model.output.UserOutputModel
import com.example.messagingapp.services.TokenCreationError
import com.example.messagingapp.services.UserCreationError
import com.example.messagingapp.services.UsersService
import com.example.messagingapp.utils.Failure
import com.example.messagingapp.utils.Success
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Users", description = "Operations related to users")
class UserController(
    private val userService: UsersService,
) {
    @PostMapping(Uris.Users.BASE)
    @Operation(
        summary = "Create a new user",
        description = "Provide user details to create a new user",
    )
    fun register(
        @RequestBody input: UserCreateInputModel,
    ): ResponseEntity<*> {
        return when (
            val res =
                userService.createUser(
                    input.invitationCode,
                    input.username,
                    input.password,
                )
        ) {
            is Success -> ResponseEntity(res.value, HttpStatus.CREATED)
            is Failure ->
                when (res.value) {
                    UserCreationError.InvitationCodeNotValid ->
                        Problem.response(
                            HttpStatus.BAD_REQUEST.value(),
                            Problem.invitationCodeNotValid(input.invitationCode, Uris.Users.register()),
                        )
                    UserCreationError.UsernameAlreadyExists ->
                        Problem.response(
                            HttpStatus.BAD_REQUEST.value(),
                            Problem.usernameAlreadyExists(input.username, Uris.Users.register()),
                        )
                    UserCreationError.UsernameIsNotValid ->
                        Problem.response(
                            HttpStatus.BAD_REQUEST.value(),
                            Problem.usernameNotValid(input.username, Uris.Users.register()),
                        )
                    UserCreationError.PasswordIsNotSafe ->
                        Problem.response(
                            HttpStatus.BAD_REQUEST.value(),
                            Problem.passwordNotSafe(Uris.Users.register()),
                        )
                }
        }
    }

    @PostMapping(Uris.Users.LOGIN)
    @Operation(
        summary = "Login",
        description = "Provide user credentials to login",
    )
    fun login(
        @RequestBody user: LoginInputModel,
    ): ResponseEntity<*> =
        when (val res = userService.createToken(user.username, user.password)) {
            is Success -> ResponseEntity(LoginOutputModel(res.value), HttpStatus.OK)
            is Failure ->
                when (res.value) {
                    TokenCreationError.UserOrPasswordIsInvalid ->
                        Problem.response(
                            HttpStatus.UNAUTHORIZED.value(),
                            Problem.userOrPasswordInvalid(Uris.Users.login()),
                        )
                }
        }

    @PostMapping(Uris.Users.LOGOUT)
    @Operation(
        summary = "Logout",
        description = "Logout the user",
    )
    fun logout(user: AuthenticatedUser): ResponseEntity<*> {
        return try {
            userService.revokeToken(user.token.value.toString())
            ResponseEntity(Unit, HttpStatus.NO_CONTENT)
        } catch (e: Exception) {
            Problem.response(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                Problem.internalServerError(e.message ?: "Unknown Error", Uris.Users.logout()),
            )
        }
    }

    @PostMapping(Uris.Users.INVITE)
    @Operation(
        summary = "Invite",
        description = "Invite a new user",
    )
    fun invite(user: AuthenticatedUser): ResponseEntity<RegistrationInvitationCreateOutputModel> {
        val res = userService.createRegistrationInvitation() as Success // It's always a success
        return ResponseEntity(
            RegistrationInvitationCreateOutputModel(res.value),
            HttpStatus.CREATED,
        )
    }

    @GetMapping(Uris.Users.HOME)
    @Operation(
        summary = "Home",
        description = "Get user details",
    )
    fun home(user: AuthenticatedUser): ResponseEntity<UserOutputModel> {
        return ResponseEntity(
            UserOutputModel(user.user),
            HttpStatus.OK,
        )
    }
}
