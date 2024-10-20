package com.example.messagingapp.services

import com.example.messagingapp.domain.Token
import com.example.messagingapp.http.model.output.ChannelInvitationOutputModel
import com.example.messagingapp.http.model.output.ChannelWithMembershipOutputModel
import com.example.messagingapp.http.model.output.MembershipOutputModel
import com.example.messagingapp.http.model.output.MessageOutputModel
import com.example.messagingapp.utils.Either
import kotlinx.datetime.Instant

sealed class UserCreationError {
    data object UsernameAlreadyExists : UserCreationError()

    data object EmailAlreadyExists : UserCreationError()

    data object UsernameIsNotValid : UserCreationError()

    data object EmailIsNotValid : UserCreationError()

    data object InvitationIsNotValid : UserCreationError()

    data object PasswordIsNotSafe : UserCreationError()
}

typealias UserCreationResult = Either<UserCreationError, Int>

data class TokenExternalData(
    val token: Token,
    val expiration: Instant,
)

sealed class TokenCreationError {
    data object UserOrPasswordIsInvalid : TokenCreationError()

    data object UserIsNotRegistered : TokenCreationError()
}
typealias TokenCreationResult = Either<TokenCreationError, TokenExternalData>

sealed class TokenRevocationError {
    data object TokenIsNotValid : TokenRevocationError()
}
typealias TokenRevocationResult = Either<TokenRevocationError, Boolean>

sealed class RegistrationInvitationError {
    data object InviteeEmailIsInvalid : RegistrationInvitationError()

    data object InviterDoesNotExist : RegistrationInvitationError()
}

typealias RegistrationInvitationResult = Either<RegistrationInvitationError, Token>

sealed class ChannelCreationError {
    data object NameIsNotValid : ChannelCreationError()

    data object UserDoesNotExist : ChannelCreationError()
}

typealias ChannelCreationResult = Either<ChannelCreationError, Int>

sealed class ChannelGetError {
    data object ChannelDoesNotExist : ChannelGetError()

    data object UserDoesNotExist : ChannelGetError()
}

typealias ChannelGetResult = Either<ChannelGetError, ChannelWithMembershipOutputModel>

sealed class GetJoinedChannelsError {
    data object UserDoesNotExist : GetJoinedChannelsError()
}

typealias GetJoinedChannelsResult = Either<GetJoinedChannelsError, List<ChannelWithMembershipOutputModel>>

sealed class SearchChannelsError {
    data object UserDoesNotExist : SearchChannelsError()
}

typealias SearchChannelsResult = Either<SearchChannelsError, List<ChannelWithMembershipOutputModel>>

sealed class JoinChannelError {
    data object ChannelDoesNotExist : JoinChannelError()

    data object UserDoesNotExist : JoinChannelError()

    data object UserIsAlreadyMember : JoinChannelError()

    data object ChannelIsNotPublic : JoinChannelError()
}

typealias JoinChannelResult = Either<JoinChannelError, Unit>

sealed class GetMessagesError {
    data object ChannelDoesNotExist : GetMessagesError()

    data object UserDoesNotExist : GetMessagesError()

    data object UserIsNotMember : GetMessagesError()
}

typealias GetMessagesResult = Either<GetMessagesError, List<MessageOutputModel>>

sealed class SendMessageError {
    data object ChannelDoesNotExist : SendMessageError()

    data object UserDoesNotExist : SendMessageError()

    data object UserIsNotAuthorizedToWrite : SendMessageError()

    data object UserIsNotMember : SendMessageError()
}

typealias SendMessageResult = Either<SendMessageError, Int>

sealed class GetMembershipError {
    data object ChannelDoesNotExist : GetMembershipError()

    data object UserDoesNotExist : GetMembershipError()

    data object MembershipDoesNotExist : GetMembershipError()
}

typealias GetMembershipResult = Either<GetMembershipError, MembershipOutputModel>

sealed class GetMembershipsError {
    data object ChannelDoesNotExist : GetMembershipsError()

    data object UserDoesNotExist : GetMembershipsError()

    data object UserIsNotMember : GetMembershipsError()
}

typealias GetMembershipsResult = Either<GetMembershipsError, List<MembershipOutputModel>>

sealed class InviteMemberError {
    data object ChannelDoesNotExist : InviteMemberError()

    data object UserDoesNotExist : InviteMemberError()

    data object InviteeDoesNotExist : InviteMemberError()

    data object MembershipDoesNotExist : InviteMemberError()

    data object MembershipAlreadyExists : InviteMemberError()

    data object CannotMakeMemberOwner : InviteMemberError()

    data object CannotMakeInviteeHigherRole : InviteMemberError()
}

typealias InviteMemberResult = Either<InviteMemberError, Int>

sealed class GetInvitationsError {
    data object UserDoesNotExist : GetInvitationsError()
}

typealias GetInvitationsResult = Either<GetInvitationsError, List<ChannelInvitationOutputModel>>

sealed class RespondInvitationError {
    data object UserDoesNotExist : RespondInvitationError()

    data object InvitationDoesNotExist : RespondInvitationError()

    data object InvitedUserDoesNotCoincide : RespondInvitationError()

    data object InvitationIsExpired : RespondInvitationError()

    data object InvitationIsNotPending : RespondInvitationError()
}

typealias RespondInvitationResult = Either<RespondInvitationError, Int>

sealed class LeaveChannelError {
    data object UserDoesNotExist : LeaveChannelError()

    data object ChannelDoesNotExist : LeaveChannelError()

    data object UserIsNotMember : LeaveChannelError()

    data object UserIsOwner : LeaveChannelError()
}

typealias LeaveChannelResult = Either<LeaveChannelError, Unit>
