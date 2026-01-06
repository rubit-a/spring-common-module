package rubit.coresecurityoauth2.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.Serializable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Base64

object CookieUtils {

    fun getCookie(request: HttpServletRequest, name: String): Cookie? {
        return request.cookies?.firstOrNull { it.name == name }
    }

    fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Int) {
        val cookie = Cookie(name, value)
        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.maxAge = maxAge
        response.addCookie(cookie)
    }

    fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String) {
        val cookie = getCookie(request, name) ?: return
        cookie.value = ""
        cookie.path = "/"
        cookie.maxAge = 0
        response.addCookie(cookie)
    }

    fun serialize(obj: Serializable): String {
        val bytes = ByteArrayOutputStream().use { outputStream ->
            ObjectOutputStream(outputStream).use { it.writeObject(obj) }
            outputStream.toByteArray()
        }
        return Base64.getUrlEncoder().encodeToString(bytes)
    }

    fun <T> deserialize(cookie: Cookie, clazz: Class<T>): T {
        val bytes = Base64.getUrlDecoder().decode(cookie.value)
        val deserialized = ByteArrayInputStream(bytes).use { inputStream ->
            ObjectInputStream(inputStream).use { it.readObject() }
        }
        return clazz.cast(deserialized)
    }
}
