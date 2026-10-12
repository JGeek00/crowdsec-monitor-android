package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications

import com.jgeek00.crowdsecmonitor.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Localization checks for the details screens' new strings (A10) across the
 * three catalogs (en/es/de).
 */
@RunWith(RobolectricTestRunner::class)
class NotificationDetailsStringsTest {

    private fun string(resId: Int): String =
        RuntimeEnvironment.getApplication().getString(resId)

    // MARK: - en

    @Test
    fun `details strings resolve in default locale`() {
        RuntimeEnvironment.setQualifiers("+en")

        assertEquals("Notification saved", string(R.string.notification_saved))
        assertEquals("Channel saved", string(R.string.channel_saved))
        assertEquals("Not configured", string(R.string.not_configured))
        assertEquals("Provider", string(R.string.provider))
        assertEquals("Channel", string(R.string.channel_section))
        assertEquals("No configuration values", string(R.string.no_configuration_values))
        assertEquals("Yes", string(R.string.common_yes))
        assertEquals("No", string(R.string.common_no))

        // Reused keys the details screens rely on.
        assertEquals("Cancel", string(R.string.cancel))
        assertEquals("Save", string(R.string.save))
        assertEquals("Edit", string(R.string.edit))
        assertEquals("Edit notification", string(R.string.notification_wizard_edit_title))
        assertEquals("Edit channel", string(R.string.channel_wizard_edit_title))
        assertEquals(
            "The notification could not be saved. Try again later.",
            string(R.string.error_save_notification)
        )
        assertEquals(
            "The channel could not be saved. Try again later.",
            string(R.string.error_save_channel)
        )
        assertEquals(
            "Provider configuration could not be loaded.",
            string(R.string.error_load_providers)
        )
        assertEquals(
            "An error occurred when fetching the data",
            string(R.string.error_fetching_data)
        )
    }

    // MARK: - es

    @Test
    fun `details strings resolve in Spanish locale`() {
        RuntimeEnvironment.setQualifiers("+es")

        assertEquals("Notificación guardada", string(R.string.notification_saved))
        assertEquals("Canal guardado", string(R.string.channel_saved))
        assertEquals("No configurado", string(R.string.not_configured))
        assertEquals("Proveedor", string(R.string.provider))
        assertEquals("Canal", string(R.string.channel_section))
        assertEquals("Sin valores de configuración", string(R.string.no_configuration_values))
        assertEquals("Sí", string(R.string.common_yes))
        assertEquals("No", string(R.string.common_no))

        assertEquals("Cancelar", string(R.string.cancel))
        assertEquals("Guardar", string(R.string.save))
        assertEquals("Editar", string(R.string.edit))
        assertEquals("Editar notificación", string(R.string.notification_wizard_edit_title))
        assertEquals("Editar canal", string(R.string.channel_wizard_edit_title))
        assertEquals(
            "No se pudo guardar la notificación. Inténtalo más tarde.",
            string(R.string.error_save_notification)
        )
        assertEquals(
            "No se pudo guardar el canal. Inténtalo más tarde.",
            string(R.string.error_save_channel)
        )
        assertEquals(
            "No se pudo cargar la configuración del proveedor.",
            string(R.string.error_load_providers)
        )
    }

    // MARK: - de

    @Test
    fun `details strings resolve in German locale`() {
        RuntimeEnvironment.setQualifiers("+de")

        assertEquals("Benachrichtigung gespeichert", string(R.string.notification_saved))
        assertEquals("Kanal gespeichert", string(R.string.channel_saved))
        assertEquals("Nicht konfiguriert", string(R.string.not_configured))
        assertEquals("Anbieter", string(R.string.provider))
        assertEquals("Kanal", string(R.string.channel_section))
        assertEquals("Keine Konfigurationswerte", string(R.string.no_configuration_values))
        assertEquals("Ja", string(R.string.common_yes))
        assertEquals("Nein", string(R.string.common_no))

        assertEquals("Abbrechen", string(R.string.cancel))
        assertEquals("Speichern", string(R.string.save))
        assertEquals("Bearbeiten", string(R.string.edit))
        assertEquals("Benachrichtigung bearbeiten", string(R.string.notification_wizard_edit_title))
        assertEquals("Kanal bearbeiten", string(R.string.channel_wizard_edit_title))
        assertEquals(
            "Die Benachrichtigung konnte nicht gespeichert werden. Versuche es später erneut.",
            string(R.string.error_save_notification)
        )
        assertEquals(
            "Der Kanal konnte nicht gespeichert werden. Versuche es später erneut.",
            string(R.string.error_save_channel)
        )
        assertEquals(
            "Die Anbieterkonfiguration konnte nicht geladen werden.",
            string(R.string.error_load_providers)
        )
    }
}
