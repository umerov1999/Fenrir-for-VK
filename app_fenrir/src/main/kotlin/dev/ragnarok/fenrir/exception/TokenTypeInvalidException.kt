package dev.ragnarok.fenrir.exception

class TokenTypeInvalidException(val accountId: Long, val tokenPolicy: String) :
    Exception("Unsupported account $accountId with token policy: $tokenPolicy")