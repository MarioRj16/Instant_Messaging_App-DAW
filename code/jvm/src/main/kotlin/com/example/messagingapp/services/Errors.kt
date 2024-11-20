package com.example.messagingapp.services

import com.example.messagingapp.domain.Channel
import com.example.messagingapp.domain.ChannelInvitation
import com.example.messagingapp.domain.Message
import com.example.messagingapp.domain.Token
import com.example.messagingapp.utils.Either
import kotlinx.datetime.Instant

sealed class UserCreationError {
    data object UsernameAlreadyExists : UserCreationError()

    data object UsernameIsNotValid : UserCreationError()

    data object InvitationCodeNotValid : UserCreationError()

    data object PasswordIsNotSafe : UserCreationError()
}

typealias UserCreationResult = Either<UserCreationError, Int>

data class TokenExternalData(
    val token: Token,
    val expiration: Instant,
)

sealed class TokenCreationError {
    data object UserOrPasswordIsInvalid : TokenCreationError()
}

typealias TokenCreationResult = Either<TokenCreationError, TokenExternalData>

sealed class TokenRevocationError {
    data object TokenIsNotValid : TokenRevocationError()
}
typealias TokenRevocationResult = Either<TokenRevocationError, Boolean>

sealed class RegistrationInvitationError

typealias RegistrationInvitationResult = Either<RegistrationInvitationError, String>

sealed class ChannelCreationError {
    data object NameIsNotValid : ChannelCreationError()

    data object NameAlreadyExists : ChannelCreationError()
}

typealias ChannelCreationResult = Either<ChannelCreationError, Int>

sealed class ChannelGetError {
    data object ChannelDoesNotExist : ChannelGetError()
}

typealias ChannelGetResult = Either<ChannelGetError, Channel>

sealed class GetJoinedChannelsError

typealias GetJoinedChannelsResult = Either<GetJoinedChannelsError, List<Channel>>

sealed class SearchChannelsError

typealias SearchChannelsResult = Either<SearchChannelsError, List<Channel>>

sealed class JoinChannelError {
    data object ChannelDoesNotExist : JoinChannelError()

    data object UserIsAlreadyMember : JoinChannelError()

    data object ChannelIsNotPublic : JoinChannelError()
}

typealias JoinChannelResult = Either<JoinChannelError, Unit>

sealed class GetMessagesError {
    data object ChannelDoesNotExist : GetMessagesError()

    data object UserIsNotMember : GetMessagesError()
}

typealias GetMessagesResult = Either<GetMessagesError, List<Message>>

sealed class CreateMessageError {
    data object ChannelDoesNotExist : CreateMessageError()

    data object UserIsNotAuthorizedToWrite : CreateMessageError()

    data object UserIsNotMember : CreateMessageError()
}

typealias CreateMessageResult = Either<CreateMessageError, Int>

sealed class AcceptChannelInvitationError {
    data object ChannelNotFound: AcceptChannelInvitationError()

    data object UserIsAlreadyMember: AcceptChannelInvitationError()

    data object InvitationNotFound: AcceptChannelInvitationError()
}

typealias AcceptChannelInvitationResult = Either<AcceptChannelInvitationError, Unit>

sealed class DeclineChannelInvitationError {
    data object UserIsAlreadyMember: DeclineChannelInvitationError()

    data object InvitationNotFound: DeclineChannelInvitationError()
}

typealias DeclineChannelInvitationResult = Either<DeclineChannelInvitationError, Unit>

sealed class InviteMemberError {
    data object ChannelDoesNotExist : InviteMemberError()

    data object InviteeDoesNotExist : InviteMemberError()

    data object MembershipDoesNotExist : InviteMemberError()

    data object MembershipAlreadyExists : InviteMemberError()

    data object ForbiddenRole : InviteMemberError()
}

typealias InviteMemberResult = Either<InviteMemberError, Int>

sealed class GetInvitationsError

typealias GetInvitationsResult = Either<GetInvitationsError, List<ChannelInvitation>>

sealed class DeleteMembershipError {
    data object ChannelDoesNotExist : DeleteMembershipError()

    data object UserIsNotMember : DeleteMembershipError()

    data object UserIsOwner : DeleteMembershipError()
}

typealias DeleteMembershipResult = Either<DeleteMembershipError, Unit>
