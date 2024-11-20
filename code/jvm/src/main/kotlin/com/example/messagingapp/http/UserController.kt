@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.messagingapp.http

import Uris
import com.example.messagingapp.domain.AuthenticatedUser
import com.example.messagingapp.domain.User
import com.example.messagingapp.http.model.input.LoginInputModel
import com.example.messagingapp.http.model.input.UserCreateInputModel
import com.example.messagingapp.http.model.output.LoginOutputModel
import com.example.messagingapp.http.model.output.RegistrationInvitationCreateOutputModel
import com.example.messagingapp.http.model.output.UserOutputModel
import com.example.messagingapp.services.TokenCreationError
import com.example.messagingapp.services.UserCreationError
import com.example.messagingapp.services.UsersService
import com.example.messagingapp.utils.Failure
import com.example.messagingapp.utils.Success
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UsersService,
) {
    @PostMapping(Uris.Users.REGISTER)
    fun register(
        @RequestBody input: UserCreateInputModel,
    ): ResponseEntity<Int> {
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
                    UserCreationError.InvitationCodeNotValid -> ResponseEntity(HttpStatus.BAD_REQUEST)
                    UserCreationError.UsernameAlreadyExists -> ResponseEntity(HttpStatus.BAD_REQUEST)
                    UserCreationError.UsernameIsNotValid -> ResponseEntity(HttpStatus.BAD_REQUEST)
                    UserCreationError.PasswordIsNotSafe -> ResponseEntity(HttpStatus.BAD_REQUEST)
                }
        }
    }

    @PostMapping(Uris.Users.LOGIN)
    fun login(
        @RequestBody user: LoginInputModel,
    ): ResponseEntity<*> =
        when (val res = userService.createToken(user.username, user.password)) {
            is Success -> ResponseEntity(LoginOutputModel(res.value), HttpStatus.OK)
            is Failure ->
                when (res.value) {
                    TokenCreationError.UserOrPasswordIsInvalid -> ResponseEntity(res.value, HttpStatus.UNAUTHORIZED)
                }
        }

    @PostMapping(Uris.Users.LOGOUT)
    fun logout(user: AuthenticatedUser): ResponseEntity<User> {
        return try {
            userService.revokeToken(user.token.value.toString())
            ResponseEntity(HttpStatus.NO_CONTENT)
        } catch (e: Exception) {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping(Uris.Users.INVITE)
    fun invite(user: AuthenticatedUser): ResponseEntity<RegistrationInvitationCreateOutputModel> {
        return when (val res = userService.createRegistrationInvitation()) {
            is Success -> ResponseEntity(
                RegistrationInvitationCreateOutputModel(res.value),
                HttpStatus.CREATED,
            )
            is Failure -> ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR) // Never happens
        }
    }

    @GetMapping(Uris.Users.HOME)
    fun home(user: AuthenticatedUser): ResponseEntity<UserOutputModel> {
        return ResponseEntity(
            UserOutputModel(user.user), HttpStatus.OK
        )
    }
}
