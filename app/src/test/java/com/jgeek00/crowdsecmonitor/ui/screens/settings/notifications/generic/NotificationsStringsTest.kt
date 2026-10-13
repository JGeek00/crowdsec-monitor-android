package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications.generic

import com.jgeek00.crowdsecmonitor.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class NotificationsStringsTest {

    private fun string(resId: Int): String =
        RuntimeEnvironment.getApplication().getString(resId)

    private fun string(resId: Int, vararg args: Any?): String =
        RuntimeEnvironment.getApplication().getString(resId, *args)

    @Test
    fun `notification strings resolve in default locale`() {
        RuntimeEnvironment.setQualifiers("+en")
        assertEquals("Notifications", string(R.string.notifications))
        assertEquals("Channels", string(R.string.channels))
        assertEquals("Add notification", string(R.string.add_notification))
        assertEquals("Add channel", string(R.string.add_channel))
        assertEquals("No notifications", string(R.string.no_notifications_title))
        assertEquals("No channels", string(R.string.no_channels_title))
        assertEquals("Enable notification", string(R.string.enable_notification))
        assertEquals("Disable notification", string(R.string.disable_notification))
        assertEquals("Delete notification", string(R.string.delete_notification))
        assertEquals("Delete channel", string(R.string.delete_channel))
        assertEquals(
            "The notification could not be enabled or disabled. Try again later.",
            string(R.string.error_toggle_notification)
        )
        assertEquals(
            "The notification could not be deleted. Try again later.",
            string(R.string.error_delete_notification)
        )
        assertEquals(
            "Notification deleted successfully",
            string(R.string.notification_deleted)
        )
        assertEquals(
            "The channel could not be deleted. Try again later.",
            string(R.string.error_delete_channel)
        )
        assertEquals(
            "This channel is used by one or more notifications. Remove it from them before deleting it.",
            string(R.string.channel_in_use)
        )
        assertEquals("Channel deleted successfully", string(R.string.channel_deleted))
        assertEquals("ntfy", string(R.string.channel_type_ntfy))
        assertEquals("Email", string(R.string.channel_type_email))
        assertEquals("Enabled", string(R.string.notification_state_enabled))
        assertEquals("Disabled", string(R.string.notification_state_disabled))
        assertEquals("Notification #7", string(R.string.notification_details_title, 7))
        assertEquals("Channel #9", string(R.string.channel_details_title, 9))
        assertEquals("Coming soon", string(R.string.coming_soon_placeholder))
    }

    @Test
    fun `notification strings resolve in Spanish`() {
        RuntimeEnvironment.setQualifiers("+es")
        assertEquals("Notificaciones", string(R.string.notifications))
        assertEquals("Canales", string(R.string.channels))
        assertEquals("Añadir notificación", string(R.string.add_notification))
        assertEquals("Añadir canal", string(R.string.add_channel))
        assertEquals("Sin notificaciones", string(R.string.no_notifications_title))
        assertEquals("Sin canales", string(R.string.no_channels_title))
        assertEquals("Activar notificación", string(R.string.enable_notification))
        assertEquals("Desactivar notificación", string(R.string.disable_notification))
        assertEquals("Eliminar notificación", string(R.string.delete_notification))
        assertEquals("Eliminar canal", string(R.string.delete_channel))
        assertEquals(
            "No se pudo activar o desactivar la notificación. Prueba de nuevo más tarde.",
            string(R.string.error_toggle_notification)
        )
        assertEquals(
            "No se pudo eliminar la notificación. Prueba de nuevo más tarde.",
            string(R.string.error_delete_notification)
        )
        assertEquals(
            "Notificación eliminada correctamente",
            string(R.string.notification_deleted)
        )
        assertEquals(
            "No se pudo eliminar el canal. Prueba de nuevo más tarde.",
            string(R.string.error_delete_channel)
        )
        assertEquals(
            "Este canal lo utilizan una o más notificaciones. Elimínalo de ellas antes de eliminarlo.",
            string(R.string.channel_in_use)
        )
        assertEquals("Canal eliminado correctamente", string(R.string.channel_deleted))
        assertEquals("ntfy", string(R.string.channel_type_ntfy))
        assertEquals("Email", string(R.string.channel_type_email))
        assertEquals("Activada", string(R.string.notification_state_enabled))
        assertEquals("Desactivada", string(R.string.notification_state_disabled))
        assertEquals("Notificación #7", string(R.string.notification_details_title, 7))
        assertEquals("Canal #9", string(R.string.channel_details_title, 9))
        assertEquals("Próximamente", string(R.string.coming_soon_placeholder))
    }

    @Test
    fun `notification strings resolve in German`() {
        RuntimeEnvironment.setQualifiers("+de")
        assertEquals("Benachrichtigungen", string(R.string.notifications))
        assertEquals("Kanäle", string(R.string.channels))
        assertEquals("Benachrichtigung hinzufügen", string(R.string.add_notification))
        assertEquals("Kanal hinzufügen", string(R.string.add_channel))
        assertEquals("Keine Benachrichtigungen", string(R.string.no_notifications_title))
        assertEquals("Keine Kanäle", string(R.string.no_channels_title))
        assertEquals("Benachrichtigung aktivieren", string(R.string.enable_notification))
        assertEquals("Benachrichtigung deaktivieren", string(R.string.disable_notification))
        assertEquals("Benachrichtigung löschen", string(R.string.delete_notification))
        assertEquals("Kanal löschen", string(R.string.delete_channel))
        assertEquals(
            "Die Benachrichtigung konnte nicht aktiviert oder deaktiviert werden. Versuche es später erneut.",
            string(R.string.error_toggle_notification)
        )
        assertEquals(
            "Die Benachrichtigung konnte nicht gelöscht werden. Versuche es später erneut.",
            string(R.string.error_delete_notification)
        )
        assertEquals(
            "Benachrichtigung erfolgreich gelöscht",
            string(R.string.notification_deleted)
        )
        assertEquals(
            "Der Kanal konnte nicht gelöscht werden. Versuche es später erneut.",
            string(R.string.error_delete_channel)
        )
        assertEquals(
            "Dieser Kanal wird von einer oder mehreren Benachrichtigungen verwendet. Entferne ihn daraus, bevor du ihn löschst.",
            string(R.string.channel_in_use)
        )
        assertEquals("Kanal erfolgreich gelöscht", string(R.string.channel_deleted))
        assertEquals("ntfy", string(R.string.channel_type_ntfy))
        assertEquals("E-Mail", string(R.string.channel_type_email))
        assertEquals("Aktiviert", string(R.string.notification_state_enabled))
        assertEquals("Deaktiviert", string(R.string.notification_state_disabled))
        assertEquals("Benachrichtigung #7", string(R.string.notification_details_title, 7))
        assertEquals("Kanal #9", string(R.string.channel_details_title, 9))
        assertEquals("Demnächst verfügbar", string(R.string.coming_soon_placeholder))
    }
}
