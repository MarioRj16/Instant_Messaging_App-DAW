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
        const val CREATE = "/channel"
        const val GET_JOINED_CHANNELS = "/channel"
        const val SEARCH_CHANNELS = "/channel/search"
        const val JOIN_CHANNEL = "channel/{id}/join"
        const val GET_BY_ID = "/channel/{id}/settings"
        const val GET_MESSAGES = "/channel/{id}"
        const val SEND_MESSAGE = "/channel/{id}"
        const val GET_INVITATIONS = "/channel/invitations"
        const val RESPOND_INVITATION = "/channel/invitations"
        const val MEMBERSHIPS = "/channel/{id}/memberships"
        const val MEMBERSHIP = "/channel/{id}/memberships/{memberId}"
        const val INVITE_MEMBER = "/channel/{id}/memberships"
        const val KICK_MEMBERS = "/channel/{id}/memberships"
        const val LEAVE_CHANNEL = "/channel/{id}/memberships"
    }
}
