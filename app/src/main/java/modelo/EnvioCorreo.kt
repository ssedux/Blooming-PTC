package modelo

import java.util.Properties
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EnvioCorreo {

    fun EnvioDeCorreo(destinatario: String, asunto: String, cuerpo: String) {
        val username = "bloomingservicee@gmail.com"
        val password = "sbqjrcgljkkmdlsa"

        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.port"] = "587"

        val session = Session.getInstance(props,
            object : javax.mail.Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })

        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(username))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario))
            message.subject = asunto
            message.setText(cuerpo)

            Transport.send(message)

        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}