@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.messagingapp.http

import Uris
import com.example.messagingapp.domain.AuthenticatedUser
import com.example.messagingapp.domain.User
import com.example.messagingapp.http.model.input.LoginInputModel
import com.example.messagingapp.http.model.input.UserCreateInputModel
import com.example.messagingapp.http.model.output.LoginOutputModel
import com.example.messagingapp.http.model.output.RegistrationInvitationCreateOutputModel
import com.example.messagingapp.http.model.output.UserCreateOuputModel
import com.example.messagingapp.http.model.output.UserProfileOutputModel
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
    ): ResponseEntity<UserCreateOuputModel> {
        return when (
            val res =
                userService.createUser(
                    input.invitationToken,
                    input.username,
                    input.password,
                    input.email,
                )
        ) {
            is Success -> ResponseEntity(UserCreateOuputModel(res.value), HttpStatus.CREATED)
            is Failure ->
                when (res.value) {
                    UserCreationError.InvitationIsNotValid -> ResponseEntity(HttpStatus.BAD_REQUEST)
                    UserCreationError.UsernameAlreadyExists -> ResponseEntity(HttpStatus.BAD_REQUEST)
                    UserCreationError.EmailAlreadyExists -> ResponseEntity(HttpStatus.BAD_REQUEST)
                    UserCreationError.UsernameIsNotValid -> ResponseEntity(HttpStatus.BAD_REQUEST)
                    UserCreationError.PasswordIsNotSafe -> ResponseEntity(HttpStatus.BAD_REQUEST)
                    UserCreationError.EmailIsNotValid -> ResponseEntity(HttpStatus.BAD_REQUEST)
                }
        }
    }

    @PostMapping(Uris.Users.LOGIN)
    fun login(
        @RequestBody user: LoginInputModel,
    ): ResponseEntity<LoginOutputModel> =
        when (val res = userService.createToken(user.username, user.password)) {
            is Success -> ResponseEntity(LoginOutputModel(res.value), HttpStatus.OK)
            is Failure ->
                when (res.value) {
                    TokenCreationError.UserOrPasswordIsInvalid -> ResponseEntity(HttpStatus.UNAUTHORIZED)
                    TokenCreationError.UserIsNotRegistered -> ResponseEntity(HttpStatus.UNAUTHORIZED)
                }
        }

    @PostMapping(Uris.Users.LOGOUT)
    fun logout(user: AuthenticatedUser): ResponseEntity<User> {
        userService.revokeToken(user.token.value.toString())
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PostMapping(Uris.Users.INVITE)
    fun invite(user: AuthenticatedUser): ResponseEntity<RegistrationInvitationCreateOutputModel> {
        val res = userService.createRegistrationInvitation(user.user.userId)
        return if (res is Success) {
            ResponseEntity(
                RegistrationInvitationCreateOutputModel(res.value.value.toString()),
                HttpStatus.CREATED,
            )
        } else {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR) // TODO: Change this to a more specific error
        }
    }

    @GetMapping(Uris.Users.HOME)
    fun home(user: AuthenticatedUser): ResponseEntity<UserProfileOutputModel> {
        val body =
            UserProfileOutputModel(
                user.user.userId,
                user.user.username,
                user.user.email,
            )
        return ResponseEntity(body, HttpStatus.OK)
    }
}
