@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.messagingapp.http

/**
 *
 * User
 * /channel
 * Post
 * 201 created successfully
 * 400 wrong arguments
 * /channel/:id
 * Get
 * 200 read
 * 404 channel not fount
 * /channel/:id
 * Post
 * 201 sent message to channel
 * 400 wrong arguments
 * 404 channel not found
 * /channel/:id/memberships
 * Get
 * 200 gets all current members of the channel
 * 404 channel doesnt
 * /channel/:id/memberships
 * Post
 * 201 added member to channel
 * 400 wrong arguments
 * 404 channel doesnt exist
 *
 *
 * /invite
 * Post
 * 201 sent invite to reg
 * 400 wrong arg
 *
 */

import com.example.messagingapp.domain.AuthenticatedUser
import com.example.messagingapp.domain.MembershipRole
import com.example.messagingapp.domain.MessageInput
import com.example.messagingapp.domain.User
import com.example.messagingapp.http.model.input.ChannelInputModel
import com.example.messagingapp.http.model.input.InvitationInputModel
import com.example.messagingapp.http.model.input.InvitationResponseInputModel
import com.example.messagingapp.http.model.output.ChannelCreateInputModel
import com.example.messagingapp.http.model.output.ChannelInvitationOutputModel
import com.example.messagingapp.http.model.output.ChannelListOutputModel
import com.example.messagingapp.http.model.output.ChannelWithMembershipOutputModel
import com.example.messagingapp.http.model.output.MembershipOutputModel
import com.example.messagingapp.http.model.output.MessageOutputModel
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
import org.springframework.web.bind.annotation.RestController

// TODO(CHANGE RESPONSE_ENTITYS)

@RestController
class ChannelController(
    private val channelService: ChannelService,
) {
    @PostMapping(Uris.Channels.CREATE)
    fun createChannel(
        @RequestBody channel: ChannelInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<ChannelCreateInputModel> =

        when (val res = channelService.createChannel(channel.channelName, user.user.userId, channel.isPublic)) {
            is Success -> ResponseEntity(ChannelCreateInputModel(res.value), HttpStatus.CREATED)
            is Failure ->
                when (res.value) {
                    ChannelCreationError.NameIsNotValid -> ResponseEntity(HttpStatus.BAD_REQUEST)
                    ChannelCreationError.UserDoesNotExist -> ResponseEntity(HttpStatus.UNAUTHORIZED)
                }
        }

    @GetMapping(Uris.Channels.GET_BY_ID)
    fun getChannel(
        @PathVariable id: Long,
        user: AuthenticatedUser,
    ): ResponseEntity<ChannelWithMembershipOutputModel> =
        when (val res = channelService.getChannel(id, user.user.userId)) {
            is Success -> ResponseEntity(res.value, HttpStatus.OK)
            is Failure ->
                when (res.value) {
                    ChannelGetError.ChannelDoesNotExist -> ResponseEntity(HttpStatus.NOT_FOUND)
                    ChannelGetError.UserDoesNotExist -> ResponseEntity(HttpStatus.UNAUTHORIZED)
                }
        }

    @GetMapping(Uris.Channels.GET_JOINED_CHANNELS)
    fun getJoinedChannels(user: AuthenticatedUser): ResponseEntity<ChannelListOutputModel> =
        when (val res = channelService.getJoinedChannels(user.user.userId)) {
            is Success -> ResponseEntity(ChannelListOutputModel(res.value), HttpStatus.OK)
            is Failure ->
                when (res.value) {
                    GetJoinedChannelsError.UserDoesNotExist -> ResponseEntity(HttpStatus.UNAUTHORIZED)
                }
        }

    @GetMapping(Uris.Channels.SEARCH_CHANNELS)
    fun searchChannels(user: AuthenticatedUser): ResponseEntity<List<ChannelWithMembershipOutputModel>> =
        when (val res = channelService.searchChannels(user.user.userId)) {
            is Success -> ResponseEntity(res.value, HttpStatus.OK)
            is Failure ->
                when (res.value) {
                    SearchChannelsError.UserDoesNotExist -> ResponseEntity(HttpStatus.UNAUTHORIZED)
                }
        }

    @PostMapping(Uris.Channels.JOIN_CHANNEL)
    fun joinChannel(
        @PathVariable id: Long,
        user: AuthenticatedUser,
    ): ResponseEntity<Unit> =
        when (val res = channelService.joinChannel(id, user.user.userId)) {
            is Success -> ResponseEntity(res.value, HttpStatus.CREATED)
            is Failure ->
                when (res.value) {
                    JoinChannelError.ChannelDoesNotExist -> ResponseEntity(HttpStatus.NOT_FOUND)
                    JoinChannelError.UserDoesNotExist -> ResponseEntity(HttpStatus.UNAUTHORIZED)
                    JoinChannelError.UserIsAlreadyMember -> ResponseEntity(HttpStatus.FORBIDDEN)
                    JoinChannelError.ChannelIsNotPublic -> ResponseEntity(HttpStatus.FORBIDDEN)
                }
        }

    @GetMapping(Uris.Channels.GET_MESSAGES)
    fun getMessages(
        @PathVariable id: Long,
        user: AuthenticatedUser,
    ): ResponseEntity<List<MessageOutputModel>> =
        when (val res = channelService.getMessages(id, user.user.userId)) {
            is Success -> ResponseEntity(res.value, HttpStatus.OK)
            is Failure ->
                when (res.value) {
                    GetMessagesError.ChannelDoesNotExist -> ResponseEntity(HttpStatus.NOT_FOUND)
                    GetMessagesError.UserDoesNotExist -> ResponseEntity(HttpStatus.UNAUTHORIZED)
                    GetMessagesError.UserIsNotMember -> ResponseEntity(HttpStatus.FORBIDDEN)
                }
        }

    @PostMapping(Uris.Channels.SEND_MESSAGE)
    fun sendMessage(
        @PathVariable id: Long,
        user: AuthenticatedUser,
        @RequestBody message: MessageInput,
    ): ResponseEntity<Int> =
        when (val res = channelService.sendMessage(id, user.user.userId, message.content)) {
            is Success -> ResponseEntity(res.value, HttpStatus.CREATED)
            is Failure ->
                when (res.value) {
                    SendMessageError.ChannelDoesNotExist -> ResponseEntity(HttpStatus.NOT_FOUND)
                    SendMessageError.UserDoesNotExist -> ResponseEntity(HttpStatus.UNAUTHORIZED)
                    SendMessageError.UserIsNotAuthorizedToWrite -> ResponseEntity(HttpStatus.FORBIDDEN)
                    SendMessageError.UserIsNotMember -> ResponseEntity(HttpStatus.FORBIDDEN)
                }
        }

    // TODO(MUDAR COMO ISTO FUNCIONA QUE DE MOMENTO SO CONSEGUES ARRANJAR O TEU PRÓPRIO MEMBERSHIP
    // PARA FAZER ISTO BASTA ADICIONAR E SUBSITITUIR
    // @PathVariable memberId: Long)
    @GetMapping(Uris.Channels.MEMBERSHIP)
    fun getMembership(
        @PathVariable id: Long,
        user: AuthenticatedUser,
    ): ResponseEntity<MembershipOutputModel> =
        when (val res = channelService.getMembership(id, user.user.userId)) {
            is Success -> ResponseEntity(res.value, HttpStatus.OK)
            is Failure ->
                when (res.value) {
                    GetMembershipError.UserDoesNotExist -> ResponseEntity(HttpStatus.UNAUTHORIZED)
                    GetMembershipError.ChannelDoesNotExist -> ResponseEntity(HttpStatus.NOT_FOUND)
                    GetMembershipError.MembershipDoesNotExist -> ResponseEntity(HttpStatus.FORBIDDEN)
                }
        }

    @GetMapping(Uris.Channels.MEMBERSHIPS)
    fun getMemberships(
        @PathVariable id: Long,
        user: AuthenticatedUser,
    ): ResponseEntity<List<User>> =
        when (val res = channelService.getMemberships(id, user.user.userId)) {
            is Success -> ResponseEntity(HttpStatus.OK)
            is Failure ->
                when (res.value) {
                    GetMembershipsError.UserDoesNotExist -> ResponseEntity(HttpStatus.UNAUTHORIZED)
                    GetMembershipsError.ChannelDoesNotExist -> ResponseEntity(HttpStatus.NOT_FOUND)
                    GetMembershipsError.UserIsNotMember -> ResponseEntity(HttpStatus.FORBIDDEN)
                }
        }

    @PostMapping(Uris.Channels.INVITE_MEMBER)
    fun inviteMember(
        @PathVariable id: Long,
        user: AuthenticatedUser,
        @RequestBody invitedUsername: InvitationInputModel,
    ): ResponseEntity<Int> =
        when (
            val res =
                channelService.inviteMember(
                    id,
                    user.user.userId,
                    invitedUsername.username,
                    MembershipRole.fromRole(invitedUsername.role),
                )
        ) {
            is Success -> ResponseEntity(res.value, HttpStatus.CREATED)
            is Failure ->
                when (res.value) {
                    InviteMemberError.InviteeDoesNotExist -> ResponseEntity(HttpStatus.NOT_FOUND)
                    InviteMemberError.UserDoesNotExist -> ResponseEntity(HttpStatus.UNAUTHORIZED)
                    InviteMemberError.ChannelDoesNotExist -> ResponseEntity(HttpStatus.NOT_FOUND)
                    InviteMemberError.MembershipDoesNotExist -> ResponseEntity(HttpStatus.FORBIDDEN)
                    InviteMemberError.CannotMakeMemberOwner -> ResponseEntity(HttpStatus.FORBIDDEN)
                    InviteMemberError.CannotMakeInviteeHigherRole -> ResponseEntity(HttpStatus.FORBIDDEN)
                    InviteMemberError.MembershipAlreadyExists -> ResponseEntity(HttpStatus.FORBIDDEN)
                }
        }

    @GetMapping(Uris.Channels.GET_INVITATIONS)
    fun getInvitations(user: AuthenticatedUser): ResponseEntity<List<ChannelInvitationOutputModel>> =
        when (val res = channelService.getInvitations(user.user.userId)) {
            is Success -> ResponseEntity(res.value, HttpStatus.OK)
            is Failure ->
                when (res.value) {
                    GetInvitationsError.UserDoesNotExist -> ResponseEntity(HttpStatus.UNAUTHORIZED)
                }
        }

    @PostMapping(Uris.Channels.RESPOND_INVITATION)
    fun respondToInvite(
        user: AuthenticatedUser,
        @RequestBody response: InvitationResponseInputModel,
    ): ResponseEntity<Int> =
        when (val res = channelService.respondInvitation(user.user.userId, response.inviteId, response.response)) {
            is Success -> ResponseEntity(res.value, HttpStatus.CREATED)
            is Failure ->
                when (res.value) {
                    RespondInvitationError.InvitationDoesNotExist -> ResponseEntity(HttpStatus.NOT_FOUND)
                    RespondInvitationError.UserDoesNotExist -> ResponseEntity(HttpStatus.UNAUTHORIZED)
                    RespondInvitationError.InvitationIsExpired -> ResponseEntity(HttpStatus.FORBIDDEN)
                    RespondInvitationError.InvitedUserDoesNotCoincide -> ResponseEntity(HttpStatus.FORBIDDEN)
                    RespondInvitationError.InvitationIsNotPending -> ResponseEntity(HttpStatus.FORBIDDEN)
                }
        }

    @DeleteMapping(Uris.Channels.LEAVE_CHANNEL)
    fun leaveChannel(
        @PathVariable id: Long,
        user: AuthenticatedUser,
    ): ResponseEntity<Unit> =
        when (val res = channelService.leaveChannel(id, user.user.userId)) {
            is Success -> ResponseEntity(res.value, HttpStatus.NO_CONTENT)
            is Failure ->
                when (res.value) {
                    LeaveChannelError.UserIsOwner -> ResponseEntity(HttpStatus.FORBIDDEN)
                    LeaveChannelError.UserIsNotMember -> ResponseEntity(HttpStatus.FORBIDDEN)
                    LeaveChannelError.UserDoesNotExist -> ResponseEntity(HttpStatus.UNAUTHORIZED)
                    LeaveChannelError.ChannelDoesNotExist -> ResponseEntity(HttpStatus.NOT_FOUND)
                }
        }
}
