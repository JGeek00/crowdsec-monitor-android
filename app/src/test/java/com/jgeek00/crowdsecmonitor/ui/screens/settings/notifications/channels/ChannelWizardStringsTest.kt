package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications.channels

import android.content.Context
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.utils.backendStringResId
import com.jgeek00.crowdsecmonitor.utils.resolveBackendString
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ChannelWizardStringsTest {

    private val context: Context
        get() = RuntimeEnvironment.getApplication()

    private fun string(resId: Int): String = context.getString(resId)

    private fun string(resId: Int, vararg args: Any?): String =
        context.getString(resId, *args)

    private fun setLocale(qualifiers: String) {
        RuntimeEnvironment.setQualifiers(qualifiers)
    }

    @Test
    fun `wizard strings resolve in default locale`() {
        setLocale("+en")

        assertEquals("New channel", string(R.string.channel_wizard_new_title))
        assertEquals("Edit channel", string(R.string.channel_wizard_edit_title))
        assertEquals(
            "The channel could not be saved. Try again later.",
            string(R.string.error_save_channel)
        )
        assertEquals(
            "Provider configuration could not be loaded.",
            string(R.string.error_load_providers)
        )

        assertEquals(
            "A notification channel defines how the backend delivers a notification: " +
                "through ntfy or by email. Configure the provider access once here, " +
                "then pick one or more channels in each notification.",
            string(R.string.channel_wizard_info_box)
        )

        assertEquals(
            "Choose the provider for this channel.",
            string(R.string.choose_provider_section)
        )
        assertEquals("No providers available", string(R.string.no_providers_available))

        assertEquals(
            "Go back and choose a provider first.",
            string(R.string.provider_form_no_provider)
        )

        assertEquals(
            "Give the channel a name, optionally send a test notification, " +
                "then finish to save it.",
            string(R.string.channel_final_step_info)
        )
        assertEquals("Send test notification", string(R.string.send_test_notification))
        assertEquals("Test", string(R.string.test))
        assertEquals("Test notification sent", string(R.string.test_notification_sent))
        assertEquals("Test failed: timeout", string(R.string.test_failed_fmt, "timeout"))
    }

    @Test
    fun `wizard strings resolve in Spanish`() {
        setLocale("+es")

        assertEquals("Nuevo canal", string(R.string.channel_wizard_new_title))
        assertEquals("Editar canal", string(R.string.channel_wizard_edit_title))
        assertEquals(
            "No se pudo guardar el canal. Inténtalo más tarde.",
            string(R.string.error_save_channel)
        )
        assertEquals(
            "No se pudo cargar la configuración del proveedor.",
            string(R.string.error_load_providers)
        )
        assertEquals(
            "Un canal define cómo envía el backend una notificación: por ntfy o por email. " +
                "Configura el acceso al proveedor una vez aquí y luego elige uno o varios " +
                "canales en cada notificación.",
            string(R.string.channel_wizard_info_box)
        )
        assertEquals(
            "Elige el proveedor de este canal.",
            string(R.string.choose_provider_section)
        )
        assertEquals("No hay proveedores disponibles", string(R.string.no_providers_available))
        assertEquals(
            "Vuelve atrás y elige primero un proveedor.",
            string(R.string.provider_form_no_provider)
        )
        assertEquals(
            "Ponle un nombre al canal, opcionalmente envía una notificación de prueba " +
                "y finaliza para guardarlo.",
            string(R.string.channel_final_step_info)
        )
        assertEquals("Enviar notificación de prueba", string(R.string.send_test_notification))
        assertEquals("Prueba", string(R.string.test))
        assertEquals(
            "Notificación de prueba enviada",
            string(R.string.test_notification_sent)
        )
        assertEquals("La prueba falló: timeout", string(R.string.test_failed_fmt, "timeout"))
    }

    @Test
    fun `wizard strings resolve in German`() {
        setLocale("+de")

        assertEquals("Neuer Kanal", string(R.string.channel_wizard_new_title))
        assertEquals("Kanal bearbeiten", string(R.string.channel_wizard_edit_title))
        assertEquals(
            "Der Kanal konnte nicht gespeichert werden. Versuche es später erneut.",
            string(R.string.error_save_channel)
        )
        assertEquals(
            "Die Anbieterkonfiguration konnte nicht geladen werden.",
            string(R.string.error_load_providers)
        )
        assertEquals(
            "Ein Kanal legt fest, wie das Backend eine Benachrichtigung zustellt: über ntfy " +
                "oder per E-Mail. Konfiguriere den Anbieterzugang hier einmalig und wähle " +
                "dann in jeder Benachrichtigung einen oder mehrere Kanäle.",
            string(R.string.channel_wizard_info_box)
        )
        assertEquals(
            "Wähle den Anbieter für diesen Kanal.",
            string(R.string.choose_provider_section)
        )
        assertEquals("Keine Anbieter verfügbar", string(R.string.no_providers_available))
        assertEquals(
            "Gehe zurück und wähle zuerst einen Anbieter.",
            string(R.string.provider_form_no_provider)
        )
        assertEquals(
            "Gib dem Kanal einen Namen, sende optional eine Testbenachrichtigung und " +
                "schließe ab, um ihn zu speichern.",
            string(R.string.channel_final_step_info)
        )
        assertEquals("Testbenachrichtigung senden", string(R.string.send_test_notification))
        assertEquals("Test", string(R.string.test))
        assertEquals("Testbenachrichtigung gesendet", string(R.string.test_notification_sent))
        assertEquals("Test fehlgeschlagen: timeout", string(R.string.test_failed_fmt, "timeout"))
    }

    @Test
    fun `backend provider keys resolve in default locale`() {
        setLocale("+en")

        assertEquals("ntfy", resolveBackendString(context, "provider_ntfy"))
        assertEquals("Email", resolveBackendString(context, "provider_email"))
        assertEquals(
            "Messages are sent through your SMTP server.",
            resolveBackendString(context, "provider_email_description")
        )

        assertEquals("Topic", resolveBackendString(context, "section_topic"))
        assertEquals("Authentication", resolveBackendString(context, "section_authentication"))
        assertEquals("Appearance", resolveBackendString(context, "section_appearance"))
        assertEquals("Server", resolveBackendString(context, "section_server"))
        assertEquals("Addresses", resolveBackendString(context, "section_addresses"))

        assertEquals("Topic", resolveBackendString(context, "field_topic"))
        assertEquals("Server", resolveBackendString(context, "field_server"))
        assertEquals("Username", resolveBackendString(context, "field_username"))
        assertEquals("Password", resolveBackendString(context, "field_password"))
        assertEquals("Access token", resolveBackendString(context, "field_token"))
        assertEquals("Priority", resolveBackendString(context, "field_priority"))
        assertEquals("Tags", resolveBackendString(context, "field_tags"))
        assertEquals("Host", resolveBackendString(context, "field_host"))
        assertEquals("Port", resolveBackendString(context, "field_port"))
        assertEquals("Use implicit TLS", resolveBackendString(context, "field_secure"))
        assertEquals("From", resolveBackendString(context, "field_from"))
        assertEquals("To", resolveBackendString(context, "field_to"))

        // Select options
        assertEquals("Min", resolveBackendString(context, "priority_min"))
        assertEquals("Low", resolveBackendString(context, "priority_low"))
        assertEquals("Default", resolveBackendString(context, "priority_default"))
        assertEquals("High", resolveBackendString(context, "priority_high"))
        assertEquals("Urgent", resolveBackendString(context, "priority_urgent"))
        assertEquals("Max", resolveBackendString(context, "priority_max"))
    }

    @Test
    fun `backend provider keys resolve in Spanish`() {
        setLocale("+es")

        assertEquals("ntfy", resolveBackendString(context, "provider_ntfy"))
        assertEquals("Email", resolveBackendString(context, "provider_email"))
        assertEquals(
            "Los mensajes se envían a través de tu servidor SMTP.",
            resolveBackendString(context, "provider_email_description")
        )
        assertEquals("Tema", resolveBackendString(context, "section_topic"))
        assertEquals("Autenticación", resolveBackendString(context, "section_authentication"))
        assertEquals("Apariencia", resolveBackendString(context, "section_appearance"))
        assertEquals("Servidor", resolveBackendString(context, "section_server"))
        assertEquals("Direcciones", resolveBackendString(context, "section_addresses"))
        assertEquals("Contraseña", resolveBackendString(context, "field_password"))
        assertEquals("Token de acceso", resolveBackendString(context, "field_token"))
        assertEquals("Puerto", resolveBackendString(context, "field_port"))
        assertEquals("Usar TLS implícito", resolveBackendString(context, "field_secure"))
        assertEquals("Remitente", resolveBackendString(context, "field_from"))
        assertEquals("Destinatarios", resolveBackendString(context, "field_to"))
        assertEquals("Mínima", resolveBackendString(context, "priority_min"))
        assertEquals("Por defecto", resolveBackendString(context, "priority_default"))
        assertEquals("Máxima", resolveBackendString(context, "priority_max"))
    }

    @Test
    fun `backend provider keys resolve in German`() {
        setLocale("+de")

        assertEquals("ntfy", resolveBackendString(context, "provider_ntfy"))
        assertEquals("E-Mail", resolveBackendString(context, "provider_email"))
        assertEquals(
            "Nachrichten werden über deinen SMTP-Server gesendet.",
            resolveBackendString(context, "provider_email_description")
        )
        assertEquals("Thema", resolveBackendString(context, "section_topic"))
        assertEquals("Authentifizierung", resolveBackendString(context, "section_authentication"))
        assertEquals("Erscheinungsbild", resolveBackendString(context, "section_appearance"))
        assertEquals("Benutzername", resolveBackendString(context, "field_username"))
        assertEquals("Passwort", resolveBackendString(context, "field_password"))
        assertEquals("Zugriffstoken", resolveBackendString(context, "field_token"))
        assertEquals("Implizites TLS verwenden", resolveBackendString(context, "field_secure"))
        assertEquals("Von", resolveBackendString(context, "field_from"))
        assertEquals("An", resolveBackendString(context, "field_to"))
        assertEquals("Minimal", resolveBackendString(context, "priority_min"))
        assertEquals("Standard", resolveBackendString(context, "priority_default"))
        assertEquals("Dringend", resolveBackendString(context, "priority_urgent"))
        assertEquals("Maximal", resolveBackendString(context, "priority_max"))
    }

    @Test
    fun `unknown backend key falls back to the raw key`() {
        setLocale("+en")

        // An older app talking to a newer backend may receive keys the local
        // catalog does not know; the fallback must render the raw key instead
        // of crashing or returning a blank label.
        assertEquals(
            "provider_unknown_new_field",
            resolveBackendString(context, "provider_unknown_new_field")
        )
        assertEquals("section_unknown", resolveBackendString(context, "section_unknown"))
    }

    @Test
    fun `backend res id lookup returns zero for unknown keys`() {
        setLocale("+en")

        assertEquals(
            0,
            backendStringResId(context.resources, context.packageName, "no_such_backend_key")
        )
        assertEquals(
            context.resources.getIdentifier("provider_ntfy", "string", context.packageName),
            backendStringResId(context.resources, context.packageName, "provider_ntfy")
        )
    }
}
