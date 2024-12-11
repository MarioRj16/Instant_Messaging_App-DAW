package com.example.messagingapp.services

import com.example.messagingapp.domain.Channel
import com.example.messagingapp.domain.ChannelInvitation
import com.example.messagingapp.domain.Message
import com.example.messagingapp.domain.Token
import com.example.messagingapp.utils.Either
import com.example.messagingapp.utils.PaginatedResponse
import kotlinx.datetime.Instant

sealed class UserCreationError(val message: String) {
    data object UsernameAlreadyExists : UserCreationError("Username already exists")

    data object UsernameIsNotValid : UserCreationError("Username validation failed")

    data object InvitationCodeNotValid : UserCreationError("Invitation is not valid")

    data object PasswordIsNotSafe : UserCreationError("Password validation failed")
}

typealias UserCreationResult = Either<UserCreationError, Int>

data class TokenExternalData(
    val token: Token,
    val expiration: Instant,
)

sealed class TokenCreationError(val message: String) {
    data object UserOrPasswordIsInvalid : TokenCreationError("Invalid username or password")
}

typealias TokenCreationResult = Either<TokenCreationError, TokenExternalData>

sealed class TokenRevocationError(val message: String) {
    data object TokenIsNotValid : TokenRevocationError("Token is not valid")
}
typealias TokenRevocationResult = Either<TokenRevocationError, Boolean>

sealed class RegistrationInvitationError

typealias RegistrationInvitationResult = Either<RegistrationInvitationError, String>

sealed class ChannelCreationError(val message: String) {
    data object ChannelNameIsNotValid : ChannelCreationError("Channel name is not valid")

    data object ChannelNameAlreadyExists : ChannelCreationError("Channel name already exists")
}

typealias ChannelCreationResult = Either<ChannelCreationError, Int>

sealed class ChannelGetError(val message: String) {
    data object ChannelNotFound : ChannelGetError("Channel not found")
}

typealias ChannelGetResult = Either<ChannelGetError, Channel>

sealed class GetJoinedChannelsError

typealias GetJoinedChannelsResult = Either<GetJoinedChannelsError, PaginatedResponse<Channel>>

sealed class SearchChannelsError

typealias SearchChannelsResult = Either<SearchChannelsError, PaginatedResponse<Channel>>

sealed class JoinChannelError(val message: String) {
    data object ChannelNotFound : JoinChannelError("Channel not found")

    data object UserIsAlreadyMember : JoinChannelError("User is already a member of the channel")

    data object ChannelIsNotPublic : JoinChannelError("Channel is not public")
}

typealias JoinChannelResult = Either<JoinChannelError, Unit>

sealed class GetMessagesError(val message: String) {
    data object ChannelNotFound : GetMessagesError("Channel not found")

    data object UserIsNotMember : GetMessagesError("User is not a member of the channel")
}

typealias GetMessagesResult = Either<GetMessagesError, List<Message>>

sealed class CreateMessageError(val message: String) {
    data object ChannelNotFound : CreateMessageError("Channel not found")

    data object UserIsNotAuthorizedToWrite : CreateMessageError("User is not authorized to write in the channel")

    data object UserIsNotMember : CreateMessageError("User is not a member of the channel")
}

typealias CreateMessageResult = Either<CreateMessageError, Int>

sealed class AcceptChannelInvitationError(val message: String) {
    data object UserIsAlreadyMember : AcceptChannelInvitationError("User is already a member of the channel")

    data object InvitationNotFound : AcceptChannelInvitationError("Invitation not found for the channel")
}

typealias AcceptChannelInvitationResult = Either<AcceptChannelInvitationError, Unit>

sealed class DeclineChannelInvitationError(val message: String) {
    data object UserIsAlreadyMember : DeclineChannelInvitationError("User is already a member of the channel")

    data object InvitationNotFound : DeclineChannelInvitationError("Invitation not found for the channel")
}

typealias DeclineChannelInvitationResult = Either<DeclineChannelInvitationError, Unit>

sealed class InviteMemberError(val message: String) {
    data object ChannelNotFound : InviteMemberError("Channel not found")

    data object InviteeNotFound : InviteMemberError("Invitee not found")

    data object MembershipNotFound : InviteMemberError("Membership not found for channel and user")

    data object MembershipAlreadyExists : InviteMemberError("Membership already exists for the invitee")

    data object ForbiddenRole : InviteMemberError("User is not allowed to invite a user with a higher role")

    data object InvitationAlreadyExists : InviteMemberError("Invitation already exists for the invitee")
}

typealias InviteMemberResult = Either<InviteMemberError, Int>

sealed class GetInvitationsError

typealias GetInvitationsResult = Either<GetInvitationsError, PaginatedResponse<ChannelInvitation>>

sealed class DeleteMembershipError(val message: String) {
    data object ChannelNotFound : DeleteMembershipError("Channel not found")

    data object UserIsNotMember : DeleteMembershipError("User is not a member of the channel")

    data object UserIsOwner : DeleteMembershipError("User is the owner of the channel")
}

typealias DeleteMembershipResult = Either<DeleteMembershipError, Unit>
