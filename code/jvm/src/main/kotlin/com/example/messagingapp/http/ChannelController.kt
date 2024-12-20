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
import com.example.messagingapp.http.model.output.ChannelCreateOutputModel
import com.example.messagingapp.http.model.output.ChannelListOutputModel
import com.example.messagingapp.http.model.output.ChannelOutputModel
import com.example.messagingapp.http.model.output.InvitationsListOutputModel
import com.example.messagingapp.http.model.output.MessageListOutputModel
import com.example.messagingapp.http.model.output.MessageOutputModel
import com.example.messagingapp.http.model.output.Problem
import com.example.messagingapp.http.model.output.UserOutputModel
import com.example.messagingapp.http.model.output.problems.ChannelNameAlreadyExists
import com.example.messagingapp.http.model.output.problems.ChannelNotFound
import com.example.messagingapp.http.model.output.problems.ChannelNotPublic
import com.example.messagingapp.http.model.output.problems.ForbiddenRole
import com.example.messagingapp.http.model.output.problems.InternalServerError
import com.example.messagingapp.http.model.output.problems.InvalidChannelName
import com.example.messagingapp.http.model.output.problems.InvalidPagination
import com.example.messagingapp.http.model.output.problems.InvitationAlreadyExists
import com.example.messagingapp.http.model.output.problems.InvitationNotFound
import com.example.messagingapp.http.model.output.problems.InviteeNotFound
import com.example.messagingapp.http.model.output.problems.UserAlreadyInChannel
import com.example.messagingapp.http.model.output.problems.UserIsOwner
import com.example.messagingapp.http.model.output.problems.UserNotAuthorizedToWrite
import com.example.messagingapp.http.model.output.problems.UserNotInChannel
import com.example.messagingapp.services.AcceptChannelInvitationError
import com.example.messagingapp.services.ChannelCreationError
import com.example.messagingapp.services.ChannelGetError
import com.example.messagingapp.services.ChannelService
import com.example.messagingapp.services.CreateMessageError
import com.example.messagingapp.services.DeclineChannelInvitationError
import com.example.messagingapp.services.DeleteMembershipError
import com.example.messagingapp.services.GetMessagesError
import com.example.messagingapp.services.InviteMemberError
import com.example.messagingapp.services.JoinChannelError
import com.example.messagingapp.utils.Failure
import com.example.messagingapp.utils.Success
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.concurrent.ConcurrentHashMap
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

@RestController
@Tag(name = "Channels", description = "Operations related to channels")
class ChannelController(
    private val channelService: ChannelService,
) {
    private val channelEmitterRegistry = ConcurrentHashMap<Int, ChannelEmitter>()

    @PostMapping(Uris.Channels.BASE)
    @Operation(
        summary = "Create a new channel",
        description = "Create a new channel with the given name and visibility.",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Channel created successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ChannelCreateOutputModel::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid channel name.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = InvalidChannelName::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Channel name already exists.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = ChannelNameAlreadyExists::class,)
                    ),
                ],
            ),
        ]
    )
    fun createChannel(
        @RequestBody channel: ChannelSearchInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<*> =

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
                    ChannelCreationError.ChannelNameIsNotValid ->
                        Problem.response(
                            HttpStatus.BAD_REQUEST.value(),
                            InvalidChannelName(channel.channelName, Uris.Channels.create()),
                        )
                    ChannelCreationError.ChannelNameAlreadyExists ->
                        Problem.response(
                            HttpStatus.BAD_REQUEST.value(),
                            ChannelNameAlreadyExists(channel.channelName, Uris.Channels.create()),
                        )
                }
        }

    @GetMapping(Uris.Channels.LISTEN)
    @Operation(
        summary = "Listen to all channels",
        description = "Listen to all channels the user is part of."
    )
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

            val userEmitter = SseEmitter(Long.MAX_VALUE)
            channelEmitter.addListener(user.user.userId, userEmitter)
            val cleanup = {
                channelEmitter.removeListener(user.user.userId)
            }
            cleanupHandlers.add(cleanup)

            userEmitter.onCompletion {
                cleanup()
            }
            userEmitter.onTimeout {
                cleanup()
            }
            userEmitter.onError {
                cleanup()
            }

            channelEmitter.addListener(
                user.user.userId,
                object : SseEmitter() {
                    override fun send(event: Any) {
                        try {
                            compositeEmitter.send(event)
                        } catch (e: Exception) {
                            this.completeWithError(e)
                        }
                    }
                },
            )
        }

        compositeEmitter.onCompletion {
            cleanupHandlers.forEach { it() }
        }
        compositeEmitter.onTimeout {
            cleanupHandlers.forEach { it() }
        }
        compositeEmitter.onError {
            cleanupHandlers.forEach { it() }
        }
        return compositeEmitter
    }

    @GetMapping(Uris.Channels.GET_BY_ID)
    @Operation(
        summary = "Get a channel by ID",
        description = "Get a channel by its ID.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Channel found.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ChannelOutputModel::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Channel not found.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = ChannelNotFound::class,)
                    ),
                ],
            ),
        ]
    )
    fun getChannel(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> =
        when (val res = channelService.getChannel(id, user.user.userId)) {
            is Success -> ResponseEntity(ChannelOutputModel(res.value), HttpStatus.OK)
            is Failure ->
                when (res.value) {
                    ChannelGetError.ChannelNotFound ->
                        Problem.response(
                            HttpStatus.NOT_FOUND.value(),
                            ChannelNotFound(Uris.Channels.getById(id)),
                        )
                }
        }

    @GetMapping(Uris.Channels.BASE)
    @Operation(
        summary = "List all channels",
        description = "List all channels the user is part of.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Channels found.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ChannelListOutputModel::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid page or page size.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = InvalidPagination::class,)
                    ),
                ],
            )
        ]
    )
    fun listJoinedChannels(
        user: AuthenticatedUser,
        @RequestParam(defaultValue = DEFAULT_PAGE) page: Int,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) pageSize: Int,
    ): ResponseEntity<*> {
        validatePaginationParams(page, pageSize)
        val res = channelService.listJoinedChannels(user.user.userId, page, pageSize) as Success // It's always a success
        return ResponseEntity(ChannelListOutputModel(res.value), HttpStatus.OK)
    }

    @GetMapping(Uris.Channels.SEARCH)
    @Operation(
        summary = "Search for channels",
        description = "Search for channels by name.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Channels found.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ChannelListOutputModel::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid page or page size.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = InvalidPagination::class,)
                    ),
                ],
            )
        ]
    )
    fun searchChannels(
        user: AuthenticatedUser,
        @RequestParam(defaultValue = DEFAULT_CHANNEL_NAME) channelName: String,
        @RequestParam(defaultValue = DEFAULT_PAGE) page: Int,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) pageSize: Int,
    ): ResponseEntity<*> {
        validatePaginationParams(page, pageSize)
        val res = channelService.searchChannels(user.user.userId, channelName, page, pageSize) as Success // It's always a success
        return ResponseEntity(ChannelListOutputModel(res.value), HttpStatus.OK)
    }

    @PostMapping(Uris.Channels.JOIN)
    @Operation(
        summary = "Join a public channel",
        description = "Join a public channel by its ID.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Channel joined successfully.",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Channel not found.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = ChannelNotFound::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "409",
                description = "User is already a member of the channel.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = UserAlreadyInChannel::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Channel is not public.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = ChannelNotPublic::class,)
                    ),
                ],
            ),
        ]
    )
    fun joinChannel(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> =
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
                    JoinChannelError.ChannelNotFound ->
                        Problem.response(
                            HttpStatus.NOT_FOUND.value(),
                            ChannelNotPublic(Uris.Channels.joinChannel(id)),
                        )
                    JoinChannelError.UserIsAlreadyMember ->
                        Problem.response(
                            HttpStatus.CONFLICT.value(),
                            UserAlreadyInChannel(Uris.Channels.joinChannel(id)),
                        )
                    JoinChannelError.ChannelIsNotPublic ->
                        Problem.response(
                            HttpStatus.FORBIDDEN.value(),
                            ChannelNotPublic(Uris.Channels.joinChannel(id)),
                        )
                }
        }

    @GetMapping(Uris.Channels.MESSAGES)
    @Operation(
        summary = "List messages",
        description = "List messages in a channel.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Messages found.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = MessageListOutputModel::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Channel not found.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = ChannelNotFound::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "403",
                description = "User is not a member of the channel.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = UserNotInChannel::class,)
                    ),
                ],
            ),
        ]
    )
    fun listMessages(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        return when (val res = channelService.listMessages(id, user.user.userId)) {
            is Success -> ResponseEntity(MessageListOutputModel(res.value), HttpStatus.OK)
            is Failure ->
                when (res.value) {
                    GetMessagesError.ChannelNotFound ->
                        Problem.response(
                            HttpStatus.NOT_FOUND.value(),
                            ChannelNotFound(Uris.Channels.getById(id)),
                        )
                    GetMessagesError.UserIsNotMember ->
                        Problem.response(
                            HttpStatus.FORBIDDEN.value(),
                            UserNotInChannel(Uris.Channels.getById(id)),
                        )
                }
        }
    }

    @PostMapping(Uris.Channels.MESSAGES)
    @Operation(
        summary = "Send a message",
        description = "Send a message to a channel.",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Message sent successfully.",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Channel not found.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = ChannelNotFound::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "403",
                description = "User is not authorized to write.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = UserNotAuthorizedToWrite::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "403",
                description = "User is not a member of the channel.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = UserNotInChannel::class,)
                    ),
                ],
            ),
        ]
    )
    fun sendMessage(
        @PathVariable id: Int,
        user: AuthenticatedUser,
        @RequestBody message: MessageInputModel,
    ): ResponseEntity<*> =
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
                ResponseEntity(Unit, HttpStatus.CREATED)
            }
            is Failure ->
                when (res.value) {
                    CreateMessageError.ChannelNotFound ->
                        Problem.response(
                            HttpStatus.NOT_FOUND.value(),
                            ChannelNotFound(Uris.Channels.getById(id)),
                        )
                    CreateMessageError.UserIsNotAuthorizedToWrite ->
                        Problem.response(
                            HttpStatus.FORBIDDEN.value(),
                            UserNotAuthorizedToWrite(Uris.Channels.getById(id)),
                        )
                    CreateMessageError.UserIsNotMember ->
                        Problem.response(
                            HttpStatus.FORBIDDEN.value(),
                            UserNotInChannel(Uris.Channels.getById(id)),
                        )
                }
        }

    @PostMapping(Uris.Channels.MEMBERS)
    @Operation(
        summary = "Invite a member",
        description = "Invite a member to a channel.",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Member invited successfully.",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Invitee not found.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = InviteeNotFound::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Channel not found.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = ChannelNotFound::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "403",
                description = "User is not a member of the channel.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = UserNotInChannel::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "403",
                description = "User is not authorized to invite.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = ForbiddenRole::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "409",
                description = "User is already a member of the channel.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = UserAlreadyInChannel::class,)
                    ),
                ],
            ),
        ]
    )
    fun inviteMember(
        @PathVariable id: Int,
        user: AuthenticatedUser,
        @RequestBody invitation: InvitationInputModel,
    ): ResponseEntity<*> {
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
                        InviteMemberError.InviteeNotFound ->
                            Problem.response(
                                HttpStatus.NOT_FOUND.value(),
                                InviteeNotFound(Uris.Channels.inviteMember(id)),
                            )
                        InviteMemberError.ChannelNotFound ->
                            Problem.response(
                                HttpStatus.NOT_FOUND.value(),
                                ChannelNotFound(Uris.Channels.inviteMember(id)),
                            )
                        InviteMemberError.MembershipNotFound ->
                            Problem.response(
                                HttpStatus.UNAUTHORIZED.value(),
                                UserNotInChannel(Uris.Channels.inviteMember(id)),
                            )
                        InviteMemberError.ForbiddenRole ->
                            Problem.response(
                                HttpStatus.FORBIDDEN.value(),
                                ForbiddenRole(Uris.Channels.inviteMember(id)),
                            )
                        InviteMemberError.MembershipAlreadyExists ->
                            Problem.response(
                                HttpStatus.CONFLICT.value(),
                                UserAlreadyInChannel(Uris.Channels.inviteMember(id)),
                            )
                        InviteMemberError.InvitationAlreadyExists ->
                            Problem.response(
                                HttpStatus.CONFLICT.value(),
                                InvitationAlreadyExists(Uris.Channels.inviteMember(id)),
                            )
                    }
            }
        } catch (e: Exception) {
            Problem.response(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                InternalServerError(instance = Uris.Channels.inviteMember(id)),
            )
        }
    }

    @GetMapping(Uris.Channels.INVITATIONS)
    @Operation(
        summary = "List invitations",
        description = "List all invitations the user has received.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Invitations found.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = InvitationsListOutputModel::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid page or page size.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = InvalidPagination::class,)
                    ),
                ],
            )
        ]
    )
    fun listInvitations(
        user: AuthenticatedUser,
        @RequestParam(defaultValue = DEFAULT_PAGE) page: Int,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) pageSize: Int,
    ): ResponseEntity<InvitationsListOutputModel> {
        validatePaginationParams(page, pageSize)
        val res = channelService.listInvitations(user.user.userId, page, pageSize) as Success // It's always a success
        return ResponseEntity(InvitationsListOutputModel(res.value), HttpStatus.OK)
    }

    @PostMapping(Uris.Channels.ACCEPT_INVITATION)
    @Operation(
        summary = "Accept an invitation",
        description = "Accept an invitation to a channel.",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Invitation accepted successfully.",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Invitation not found.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = InvitationNotFound::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "403",
                description = "User is already a member of the channel.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = UserAlreadyInChannel::class,)
                    ),
                ],
            ),
        ]
    )
    fun acceptInvitation(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> =
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
                    AcceptChannelInvitationError.UserIsAlreadyMember ->
                        Problem.response(
                            HttpStatus.FORBIDDEN.value(),
                            UserAlreadyInChannel(Uris.Channels.acceptInvitation(id)),
                        )
                    AcceptChannelInvitationError.InvitationNotFound ->
                        Problem.response(
                            HttpStatus.NOT_FOUND.value(),
                            InvitationNotFound(Uris.Channels.acceptInvitation(id)),
                        )
                }
        }

    @PostMapping(Uris.Channels.DECLINE_INVITATION)
    @Operation(
        summary = "Decline an invitation",
        description = "Decline an invitation to a channel.",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Invitation declined successfully.",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Invitation not found.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = InvitationNotFound::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "403",
                description = "User is already a member of the channel.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = UserAlreadyInChannel::class,)
                    ),
                ],
            ),
        ]
    )
    fun declineInvitation(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> =
        when (val res = channelService.declineChannelInvitation(id, user.user.userId)) {
            is Success -> ResponseEntity(res.value, HttpStatus.CREATED)
            is Failure ->
                when (res.value) {
                    DeclineChannelInvitationError.UserIsAlreadyMember ->
                        Problem.response(
                            HttpStatus.FORBIDDEN.value(),
                            UserAlreadyInChannel(Uris.Channels.declineInvitation(id)),
                        )
                    DeclineChannelInvitationError.InvitationNotFound ->
                        Problem.response(
                            HttpStatus.NOT_FOUND.value(),
                            InvitationNotFound(Uris.Channels.declineInvitation(id)),
                        )
                }
        }

    @DeleteMapping(Uris.Channels.MEMBERS)
    @Operation(
        summary = "Remove a member",
        description = "Remove a member from a channel.",
        responses = [
            ApiResponse(
                responseCode = "204",
                description = "Member removed successfully.",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Channel not found.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = ChannelNotFound::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "403",
                description = "User is the owner of the channel.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = UserIsOwner::class,)
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "403",
                description = "User is not a member of the channel.",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = UserNotInChannel::class)
                    ),
                ],
            ),
        ]
    )
    fun leaveChannel(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> =
        when (val res = channelService.deleteMembership(id, user.user.userId)) {
            is Success -> {
                channelEmitterRegistry[id]?.removeListener(user.user.userId)
                ResponseEntity(res.value, HttpStatus.NO_CONTENT)
            }
            is Failure ->
                when (res.value) {
                    DeleteMembershipError.UserIsOwner ->
                        Problem.response(
                            HttpStatus.FORBIDDEN.value(),
                            UserIsOwner(Uris.Channels.leaveChannel(id)),
                        )
                    DeleteMembershipError.UserIsNotMember ->
                        Problem.response(
                            HttpStatus.FORBIDDEN.value(),
                            UserNotInChannel(Uris.Channels.leaveChannel(id)),
                        )
                    DeleteMembershipError.ChannelNotFound ->
                        Problem.response(
                            HttpStatus.NOT_FOUND.value(),
                            ChannelNotFound(Uris.Channels.leaveChannel(id)),
                        )
                }
        }
}
