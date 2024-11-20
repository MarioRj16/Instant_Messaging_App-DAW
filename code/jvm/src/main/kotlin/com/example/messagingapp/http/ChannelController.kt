@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.messagingapp.http

import com.example.messagingapp.domain.AuthenticatedUser
import com.example.messagingapp.domain.MembershipRole
import com.example.messagingapp.http.model.input.ChannelSearchInputModel
import com.example.messagingapp.http.model.output.ChannelCreateOutputModel
import com.example.messagingapp.http.model.input.InvitationInputModel
import com.example.messagingapp.http.model.input.MessageInputModel
import com.example.messagingapp.http.model.output.*
import com.example.messagingapp.services.*
import com.example.messagingapp.utils.Failure
import com.example.messagingapp.utils.Success
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ChannelController(
    private val channelService: ChannelService,
) {
    @PostMapping(Uris.Channels.CREATE)
    fun createChannel(
        @RequestBody channel: ChannelSearchInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<ChannelCreateOutputModel> =

        when (val res = channelService.createChannel(channel.channelName, user.user.userId, channel.isPublic)) {
            is Success -> ResponseEntity(ChannelCreateOutputModel(res.value), HttpStatus.CREATED)
            is Failure ->
                when (res.value) {
                    ChannelCreationError.NameIsNotValid -> ResponseEntity(HttpStatus.BAD_REQUEST)
                    ChannelCreationError.NameAlreadyExists -> ResponseEntity(HttpStatus.BAD_REQUEST)
                }
        }

    @GetMapping(Uris.Channels.GET_BY_ID)
    fun getChannel(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<ChannelOutputModel> =
        when (val res = channelService.getChannel(id, user.user.userId)) {
            is Success -> ResponseEntity(ChannelOutputModel(res.value), HttpStatus.OK)
            is Failure ->
                when (res.value) {
                    ChannelGetError.ChannelDoesNotExist -> ResponseEntity(HttpStatus.NOT_FOUND)
                }
        }

    @GetMapping(Uris.Channels.GET_JOINED_CHANNELS)
    fun getJoinedChannels(user: AuthenticatedUser): ResponseEntity<ChannelListOutputModel> =
        when (val res = channelService.getJoinedChannels(user.user.userId)) {
            is Success -> ResponseEntity(ChannelListOutputModel(res.value), HttpStatus.OK)
            is Failure -> ResponseEntity(HttpStatus.UNAUTHORIZED) // never happens
        }

    @GetMapping(Uris.Channels.SEARCH_CHANNELS)
    fun searchChannels(
        user: AuthenticatedUser,
        @RequestParam(required = false) channelName: String? = null,
    ): ResponseEntity<ChannelListOutputModel> =
        when (val res = channelService.searchChannels(user.user.userId, channelName ?: "")) {
            is Success -> ResponseEntity(ChannelListOutputModel(res.value), HttpStatus.OK)
            is Failure -> ResponseEntity(HttpStatus.UNAUTHORIZED) // never happens
        }

    @PostMapping(Uris.Channels.JOIN_CHANNEL)
    fun joinChannel(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<Unit> =
        when (val res = channelService.joinPublicChannel(id, user.user.userId)) {
            is Success -> ResponseEntity(res.value, HttpStatus.OK)
            is Failure ->
                when (res.value) {
                    JoinChannelError.ChannelDoesNotExist -> ResponseEntity(HttpStatus.NOT_FOUND)
                    JoinChannelError.UserIsAlreadyMember -> ResponseEntity(HttpStatus.CONFLICT)
                    JoinChannelError.ChannelIsNotPublic -> ResponseEntity(HttpStatus.FORBIDDEN)
                }
        }

    @GetMapping(Uris.Channels.LIST_MESSAGES)
    fun listMessages(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<MessageListOutputModel> {
        return when (val res = channelService.listMessages(id, user.user.userId)) {
            is Success -> ResponseEntity(MessageListOutputModel(res.value), HttpStatus.OK)
            is Failure ->
                when (res.value) {
                    GetMessagesError.ChannelDoesNotExist -> ResponseEntity(HttpStatus.NOT_FOUND)
                    GetMessagesError.UserIsNotMember -> ResponseEntity(HttpStatus.FORBIDDEN)
                }
        }
    }

    @PostMapping(Uris.Channels.SEND_MESSAGE)
    fun sendMessage(
        @PathVariable id: Int,
        user: AuthenticatedUser,
        @RequestBody message: MessageInputModel,
    ): ResponseEntity<Unit> =
        when (val res = channelService.createMessage(id, user.user.userId, message.content)) {
            is Success -> ResponseEntity(HttpStatus.CREATED)
            is Failure ->
                when (res.value) {
                    CreateMessageError.ChannelDoesNotExist -> ResponseEntity(HttpStatus.NOT_FOUND)
                    CreateMessageError.UserIsNotAuthorizedToWrite -> ResponseEntity(HttpStatus.FORBIDDEN)
                    CreateMessageError.UserIsNotMember -> ResponseEntity(HttpStatus.FORBIDDEN)
                }
        }

    @PostMapping(Uris.Channels.INVITE_MEMBER)
    fun inviteMember(
        @PathVariable id: Int,
        user: AuthenticatedUser,
        @RequestBody invitation: InvitationInputModel,
    ): ResponseEntity<Unit> {
        return try {
            when (
                val res =
                    channelService.createChannelInvitation(
                        id, user.user.userId, invitation.username, MembershipRole.fromRole(invitation.role),
                    )
            ) {
                is Success -> ResponseEntity(HttpStatus.CREATED)
                is Failure ->
                    when (res.value) { // TODO: Add error for situation when user has already been invited
                        InviteMemberError.InviteeDoesNotExist -> ResponseEntity(HttpStatus.NOT_FOUND)
                        InviteMemberError.ChannelDoesNotExist -> ResponseEntity(HttpStatus.NOT_FOUND)
                        InviteMemberError.MembershipDoesNotExist -> ResponseEntity(HttpStatus.UNAUTHORIZED)
                        InviteMemberError.ForbiddenRole -> ResponseEntity(HttpStatus.FORBIDDEN)
                        InviteMemberError.MembershipAlreadyExists -> ResponseEntity(HttpStatus.CONFLICT)
                    }
            }
        } catch (e: Exception) {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping(Uris.Channels.LIST_INVITATIONS)
    fun listInvitations(
        user: AuthenticatedUser,
    ): ResponseEntity<ListInvitationsOutputModel> =
        when (val res = channelService.listInvitations(user.user.userId)) {
            is Success -> ResponseEntity(ListInvitationsOutputModel(res.value), HttpStatus.OK)
            is Failure -> ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR) // never happens
        }

    @GetMapping(Uris.Channels.ACCEPT_INVITATION)
    fun acceptInvitation(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<Unit> =
        when (val res = channelService.acceptChannelInvitation(id, user.user.userId)) {
            is Success -> ResponseEntity(res.value, HttpStatus.CREATED)
            is Failure ->
                when (res.value) {
                    AcceptChannelInvitationError.ChannelNotFound -> ResponseEntity(HttpStatus.NOT_FOUND)
                    AcceptChannelInvitationError.UserIsAlreadyMember -> ResponseEntity(HttpStatus.FORBIDDEN)
                    AcceptChannelInvitationError.InvitationNotFound -> ResponseEntity(HttpStatus.NOT_FOUND)
                }
        }

    @GetMapping(Uris.Channels.DECLINE_INVITATION)
    fun declineInvitation(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<Unit> =
        when (val res = channelService.declineChannelInvitation(id, user.user.userId)) {
            is Success -> ResponseEntity(res.value, HttpStatus.CREATED)
            is Failure ->
                when (res.value) {
                    DeclineChannelInvitationError.UserIsAlreadyMember -> ResponseEntity(HttpStatus.FORBIDDEN)
                    DeclineChannelInvitationError.InvitationNotFound -> ResponseEntity(HttpStatus.NOT_FOUND)
                }
        }

    @DeleteMapping(Uris.Channels.LEAVE_CHANNEL)
    fun leaveChannel(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<Unit> =
        when (val res = channelService.deleteMembership(id, user.user.userId)) {
            is Success -> ResponseEntity(res.value, HttpStatus.NO_CONTENT)
            is Failure ->
                when (res.value) {
                    DeleteMembershipError.UserIsOwner -> ResponseEntity(HttpStatus.FORBIDDEN)
                    DeleteMembershipError.UserIsNotMember -> ResponseEntity(HttpStatus.FORBIDDEN)
                    DeleteMembershipError.ChannelDoesNotExist -> ResponseEntity(HttpStatus.NOT_FOUND)
                }
        }
}
