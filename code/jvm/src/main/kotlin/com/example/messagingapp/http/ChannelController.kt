@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.messagingapp.http

import ChannelEmitter
import com.example.messagingapp.DEFAULT_CHANNEL_NAME
import com.example.messagingapp.DEFAULT_PAGE
import com.example.messagingapp.DEFAULT_PAGE_SIZE
import com.example.messagingapp.domain.AuthenticatedUser
import com.example.messagingapp.domain.MembershipRole
import com.example.messagingapp.http.model.input.ChannelSearchInputModel
import com.example.messagingapp.http.model.input.InvitationInputModel
import com.example.messagingapp.http.model.input.MessageInputModel
import com.example.messagingapp.http.model.output.*
import com.example.messagingapp.http.model.output.ChannelCreateOutputModel
import com.example.messagingapp.services.*
import com.example.messagingapp.utils.Failure
import com.example.messagingapp.utils.Success
import kotlinx.datetime.Clock
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

@RestController
class ChannelController(
    private val channelService: ChannelService,
) {
    private val channelEmitterRegistry = ConcurrentHashMap<Int, ChannelEmitter>()

    @PostMapping(Uris.Channels.CREATE)
    fun createChannel(
        @RequestBody channel: ChannelSearchInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<ChannelCreateOutputModel> =

        when (val res = channelService.createChannel(channel.channelName, user.user.userId, channel.isPublic)) {
            is Success -> {
                val channelEmitter = ChannelEmitter()
                channelEmitterRegistry[res.value] = channelEmitter

                // Add the creator as a listener
                val emitter = SseEmitter(Long.MAX_VALUE)
                channelEmitter.addListener(user.user.userId, emitter)
                ResponseEntity(ChannelCreateOutputModel(res.value), HttpStatus.CREATED)
            }
            is Failure ->
                when (res.value) {
                    ChannelCreationError.ChannelNameIsNotValid -> ResponseEntity(HttpStatus.BAD_REQUEST)
                    ChannelCreationError.ChannelNameAlreadyExists -> ResponseEntity(HttpStatus.BAD_REQUEST)
                }
        }

    @GetMapping(Uris.Channels.LISTEN_CHANNELS)
    fun listenToAllChannels(user: AuthenticatedUser): SseEmitter {
        // Create a new composite SseEmitter for the user
        val compositeEmitter = SseEmitter(Long.MAX_VALUE)

        // Get all channel IDs the user is participating in
        val userChannels = channelService.listJoinedChannels(user.user.userId, 1, Int.MAX_VALUE)

        if (userChannels is Failure) {
            throw IllegalStateException("User is not part of any channels.")
        }

        // Get the list of channel IDs the user is part of
        val channels = (userChannels as Success).value

        // Track cleanup logic for all channels
        val cleanupHandlers = mutableListOf<() -> Unit>()

        channels.data.forEach { channel ->
            channelEmitterRegistry.computeIfAbsent(channel.channelId) { ChannelEmitter() }
            val channelEmitter =
                channelEmitterRegistry[channel.channelId]
                    ?: throw IllegalArgumentException("Channel not found: ${channel.channelId}")

            // Create an individual SseEmitter for the channel
            val userEmitter = SseEmitter(Long.MAX_VALUE)

            // Add the userEmitter to the channel
            channelEmitter.addListener(user.user.userId, userEmitter)

            // Cleanup logic for this channel
            val cleanup = {
                channelEmitter.removeListener(user.user.userId)
                // logger.info("Cleaned up listener for user ${user.user.userId} in channel ${channel.channelId}")
            }
            cleanupHandlers.add(cleanup)

            // Handle completion, timeout, and errors for the individual emitter
            userEmitter.onCompletion {
                cleanup()
                // logger.info("User ${user.user.userId} completed for channel ${channel.channelId}")
            }
            userEmitter.onTimeout {
                cleanup()
                // logger.warn("User ${user.user.userId} timed out for channel ${channel.channelId}")
            }
            userEmitter.onError { ex ->
                cleanup()
                // logger.error("User ${user.user.userId} encountered an error in channel ${channel.channelId}: ${ex.message}")
            }

            // Forward events from the individual emitter to the composite emitter
            channelEmitter.addListener(
                user.user.userId,
                object : SseEmitter() {
                    override fun send(event: Any) {
                        try {
                            compositeEmitter.send(event)
                        } catch (e: Exception) {
                            // logger.error("Error forwarding event to composite emitter for user ${user.user.userId}", e)
                            this.completeWithError(e)
                        }
                    }
                },
            )
        }

        // Global cleanup if the composite emitter is closed
        compositeEmitter.onCompletion {
            cleanupHandlers.forEach { it() }
            // logger.info("Composite emitter completed for user ${user.user.userId}")
        }
        compositeEmitter.onTimeout {
            cleanupHandlers.forEach { it() }
            // logger.warn("Composite emitter timed out for user ${user.user.userId}")
        }
        compositeEmitter.onError { ex ->
            cleanupHandlers.forEach { it() }
            // logger.error("Composite emitter encountered an error for user ${user.user.userId}: ${ex.message}")
        }

        return compositeEmitter
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
                    ChannelGetError.ChannelNotFound -> ResponseEntity(HttpStatus.NOT_FOUND)
                }
        }

    @GetMapping(Uris.Channels.LIST_JOINED_CHANNELS)
    fun listJoinedChannels(
        user: AuthenticatedUser,
        @RequestParam(defaultValue = DEFAULT_PAGE) page: Int,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) pageSize: Int,
    ): ResponseEntity<ChannelListOutputModel> {
        validatePaginationParams(page, pageSize)
        return when (val res = channelService.listJoinedChannels(user.user.userId, page, pageSize)) {
            is Success -> ResponseEntity(ChannelListOutputModel(res.value), HttpStatus.OK)
            is Failure -> ResponseEntity(HttpStatus.UNAUTHORIZED) // never happens
        }
    }

    @GetMapping(Uris.Channels.SEARCH_CHANNELS)
    fun searchChannels(
        user: AuthenticatedUser,
        @RequestParam(defaultValue = DEFAULT_CHANNEL_NAME) channelName: String,
        @RequestParam(defaultValue = DEFAULT_PAGE) page: Int,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) pageSize: Int,
    ): ResponseEntity<ChannelListOutputModel> {
        validatePaginationParams(page, pageSize)
        return when (val res = channelService.searchChannels(user.user.userId, channelName, page, pageSize)) {
            is Success -> ResponseEntity(ChannelListOutputModel(res.value), HttpStatus.OK)
            is Failure -> ResponseEntity(HttpStatus.UNAUTHORIZED) // never happens
        }
    }

    @PostMapping(Uris.Channels.JOIN_CHANNEL)
    fun joinChannel(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<Unit> =
        when (val res = channelService.joinPublicChannel(id, user.user.userId)) {
            is Success -> {
                channelEmitterRegistry.computeIfAbsent(id) { ChannelEmitter() }

                // Add the user as a listener
                val emitter = SseEmitter(Long.MAX_VALUE)
                channelEmitterRegistry[id]?.addListener(user.user.userId, emitter)

                ResponseEntity(res.value, HttpStatus.OK)
            }
            is Failure ->
                when (res.value) {
                    JoinChannelError.ChannelNotFound -> ResponseEntity(HttpStatus.NOT_FOUND)
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
                    GetMessagesError.ChannelNotFound -> ResponseEntity(HttpStatus.NOT_FOUND)
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
            is Success -> {
                val messageOutput =
                    MessageOutputModel(
                        res.value,
                        UserOutputModel(user.user),
                        id,
                        message.content,
                        Clock.System.now().toString(),
                    )
                channelEmitterRegistry[id]?.broadcast(messageOutput)
                ResponseEntity(HttpStatus.CREATED)
            }
            is Failure ->
                when (res.value) {
                    CreateMessageError.ChannelNotFound -> ResponseEntity(HttpStatus.NOT_FOUND)
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
                        id,
                        user.user.userId,
                        invitation.username,
                        MembershipRole.fromRole(invitation.role),
                    )
            ) {
                is Success -> ResponseEntity(HttpStatus.CREATED)
                is Failure ->
                    when (res.value) {
                        InviteMemberError.InviteeNotFound -> ResponseEntity(HttpStatus.NOT_FOUND)
                        InviteMemberError.ChannelNotFound -> ResponseEntity(HttpStatus.NOT_FOUND)
                        InviteMemberError.MembershipNotFound -> ResponseEntity(HttpStatus.UNAUTHORIZED)
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
        @RequestParam(defaultValue = DEFAULT_PAGE) page: Int,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) pageSize: Int,
    ): ResponseEntity<InvitationsListOutputModel> {
        validatePaginationParams(page, pageSize)
        return when (val res = channelService.listInvitations(user.user.userId, page, pageSize)) {
            is Success -> ResponseEntity(InvitationsListOutputModel(res.value), HttpStatus.OK)
            is Failure -> ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR) // never happens
        }
    }

    @PostMapping(Uris.Channels.ACCEPT_INVITATION)
    fun acceptInvitation(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<Unit> =
        when (val res = channelService.acceptChannelInvitation(id, user.user.userId)) {
            is Success -> {
                channelEmitterRegistry.computeIfAbsent(id) { ChannelEmitter() }

                // Add the user as a listener
                val emitter = SseEmitter(Long.MAX_VALUE)
                channelEmitterRegistry[id]?.addListener(user.user.userId, emitter)

                ResponseEntity(res.value, HttpStatus.CREATED)
            }
            is Failure ->
                when (res.value) {
                    AcceptChannelInvitationError.UserIsAlreadyMember -> ResponseEntity(HttpStatus.FORBIDDEN)
                    AcceptChannelInvitationError.InvitationNotFound -> ResponseEntity(HttpStatus.NOT_FOUND)
                }
        }

    @PostMapping(Uris.Channels.DECLINE_INVITATION)
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
            is Success -> {
                channelEmitterRegistry[id]?.removeListener(user.user.userId)
                ResponseEntity(res.value, HttpStatus.NO_CONTENT)
            }
            is Failure ->
                when (res.value) {
                    DeleteMembershipError.UserIsOwner -> ResponseEntity(HttpStatus.FORBIDDEN)
                    DeleteMembershipError.UserIsNotMember -> ResponseEntity(HttpStatus.FORBIDDEN)
                    DeleteMembershipError.ChannelNotFound -> ResponseEntity(HttpStatus.NOT_FOUND)
                }
        }
}
