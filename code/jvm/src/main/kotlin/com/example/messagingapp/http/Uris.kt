object Uris {
    const val PREFIX = "/api"

    const val HOME = "/"

    object Users {
        const val REGISTER = "$PREFIX/users"
        const val LOGIN = "$PREFIX/login"
        const val LOGOUT = "$PREFIX/logout"
        const val INVITE = "$PREFIX/invite"
        const val HOME = "$PREFIX/me"
    }

    object Channels {
        const val CREATE = "$PREFIX/channel"
        const val GET_JOINED_CHANNELS = "$PREFIX/channel"
        const val SEARCH_CHANNELS = "$PREFIX/channel/search"
        const val JOIN_CHANNEL = "$PREFIX/channel/{id}/join"
        const val GET_BY_ID = "$PREFIX/channel/{id}/settings"
        const val GET_MESSAGES = "$PREFIX/channel/{id}"
        const val SEND_MESSAGE = "$PREFIX/channel/{id}"
        const val GET_INVITATIONS = "$PREFIX/channel/invitations"
        const val RESPOND_INVITATION = "$PREFIX/channel/invitations"
        const val MEMBERSHIPS = "$PREFIX/channel/{id}/memberships"
        const val MEMBERSHIP = "$PREFIX/channel/{id}/memberships/{memberId}"
        const val INVITE_MEMBER = "$PREFIX/channel/{id}/memberships"
        const val KICK_MEMBERS = "$PREFIX/channel/{id}/memberships"
        const val LEAVE_CHANNEL = "$PREFIX/channel/{id}/memberships"
    }
}
