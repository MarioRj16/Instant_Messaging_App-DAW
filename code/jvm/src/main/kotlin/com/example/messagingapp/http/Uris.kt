object Uris {
    const val PREFIX = "/api"

    object Users {
        const val REGISTER = "$PREFIX/users"
        const val LOGIN = "$PREFIX/login"
        const val LOGOUT = "$PREFIX/logout"
        const val INVITE = "$PREFIX/invite"
        const val HOME = "$PREFIX/me"
    }

    object Channels {
        const val CREATE = "$PREFIX/channel"
        const val LIST_JOINED_CHANNELS = "$PREFIX/channel"
        const val SEARCH_CHANNELS = "$PREFIX/channel/search"
        const val JOIN_CHANNEL = "$PREFIX/channel/{id}/join"
        const val GET_BY_ID = "$PREFIX/channel/{id}/settings"
        const val LIST_MESSAGES = "$PREFIX/channel/{id}"
        const val SEND_MESSAGE = "$PREFIX/channel/{id}"
        const val LIST_INVITATIONS = "$PREFIX/channel/invitations"
        const val ACCEPT_INVITATION = "$PREFIX/channel/invitations/{id}/accept"
        const val DECLINE_INVITATION = "$PREFIX/channel/invitations/{id}/decline"
        const val INVITE_MEMBER = "$PREFIX/channel/{id}/memberships"
        const val LEAVE_CHANNEL = "$PREFIX/channel/{id}/memberships"
        const val LISTEN_CHANNELS = "$PREFIX/channel/listen"
    }
}
