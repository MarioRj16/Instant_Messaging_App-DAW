package com.example.messagingapp.http

import org.springframework.web.util.UriTemplate
import java.net.URI

object Uris {
    const val PREFIX = "/api"

    object Users {
        const val BASE = "$PREFIX/users"
        const val LOGIN = "$BASE/login"
        const val LOGOUT = "$BASE/logout"
        const val INVITE = "$BASE/invite"
        const val HOME = "$BASE/me"

        fun register() = URI(BASE)

        fun login() = URI(LOGIN)

        fun logout() = URI(LOGOUT)
    }

    object Channels {
        const val BASE = "$PREFIX/channel"
        const val SEARCH = "$BASE/search"
        const val JOIN = "$BASE/{id}/join"
        const val GET_BY_ID = "$BASE/{id}/settings"
        const val MESSAGES = "$BASE/{id}"
        const val INVITATIONS = "$BASE/invitations"
        const val ACCEPT_INVITATION = "$BASE/invitations/{id}/accept"
        const val DECLINE_INVITATION = "$BASE/invitations/{id}/decline"
        const val MEMBERS = "$BASE/{id}/members"
        const val LISTEN = "$BASE/listen"

        fun create() = URI(BASE)

        fun listJoinedChannels(
            page: Int,
            pageSize: Int,
        ) = URI("$BASE?page={$page}&pageSize={$pageSize}")

        fun searchChannels(
            page: Int,
            pageSize: Int,
        ) = URI("$SEARCH?page={$page}&pageSize={$pageSize}")

        fun joinChannel(id: Int) = UriTemplate(JOIN).expand(id)

        fun getById(id: Int) = UriTemplate(GET_BY_ID).expand(id)

        fun listMessages(id: Int) = UriTemplate(MESSAGES).expand(id)

        fun sendMessage(id: Int) = UriTemplate(MESSAGES).expand(id)

        fun listInvitations(
            page: Int,
            pageSize: Int,
        ) = URI("$INVITATIONS?page={$page}&pageSize={$pageSize}")

        fun acceptInvitation(id: Int) = UriTemplate(ACCEPT_INVITATION).expand(id)

        fun declineInvitation(id: Int) = UriTemplate(DECLINE_INVITATION).expand(id)

        fun inviteMember(id: Int) = UriTemplate(MEMBERS).expand(id)

        fun leaveChannel(id: Int) = UriTemplate(MEMBERS).expand(id)

        fun listenChannels() = URI(LISTEN)
    }
}
