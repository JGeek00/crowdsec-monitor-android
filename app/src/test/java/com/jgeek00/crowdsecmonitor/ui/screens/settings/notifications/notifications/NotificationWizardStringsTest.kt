package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications.notifications

import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.utils.ConditionFormatting
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class NotificationWizardStringsTest {

    private fun string(resId: Int): String =
        RuntimeEnvironment.getApplication().getString(resId)

    private fun string(resId: Int, vararg args: Any?): String =
        RuntimeEnvironment.getApplication().getString(resId, *args)

    private fun quantityString(resId: Int, count: Int): String =
        RuntimeEnvironment.getApplication().resources.getQuantityString(resId, count, count)

    @Test
    fun `wizard strings resolve in default locale`() {
        RuntimeEnvironment.setQualifiers("+en")

        assertEquals("Next", string(R.string.next))
        assertEquals("Edit", string(R.string.edit))
        assertEquals("Finish", string(R.string.finish))
        assertEquals("New notification", string(R.string.notification_wizard_new_title))
        assertEquals("Edit notification", string(R.string.notification_wizard_edit_title))
        assertEquals("Step 2 of 5", string(R.string.step_of_fmt, 2, 5))
        assertEquals(
            "The notification could not be saved. Try again later.",
            string(R.string.error_save_notification)
        )
        assertEquals(
            "Filter options could not be loaded. You can still type custom values.",
            string(R.string.error_load_filter_options)
        )

        assertEquals(
            "Notifications are sent by the backend when new alerts match the condition " +
                "you build in the next steps. Give it a name so you can recognize it later.",
            string(R.string.wizard_info_box)
        )
        assertEquals("Details", string(R.string.details))
        assertEquals("Description (optional)", string(R.string.description_optional))

        assertEquals(
            "This is the text sent through the selected channels every time the condition triggers.",
            string(R.string.message_step_info)
        )
        assertEquals("Content", string(R.string.content))
        assertEquals("Message", string(R.string.message))
        assertEquals("Include alert information", string(R.string.include_alert_info))
        assertEquals(
            "Even when enabled, alert information is only sent through channels that support it.",
            string(R.string.include_alert_info_footer)
        )
        assertEquals("Which channels support it?", string(R.string.which_channels_support))

        assertEquals(
            "Choose one or more channels used to deliver this notification. " +
                "Channels are configured in the Channels tab.",
            string(R.string.channels_step_info)
        )
        assertEquals("No channels configured yet", string(R.string.no_channels_configured))

        assertEquals(
            "Review the notification before finishing. Use Back to change anything.",
            string(R.string.review_step_info)
        )
        assertEquals("Summary", string(R.string.summary))
        assertEquals("Any alert", string(R.string.any_alert))
        assertEquals("Condition blocks", string(R.string.condition_blocks))
        assertEquals("None", string(R.string.review_none))

        assertEquals("Condition", string(R.string.condition))
        assertEquals(
            "Triggers on any alert, without filters. Combine it with the frequency below.",
            string(R.string.any_alert_explainer)
        )
        assertEquals(
            "This notification uses an advanced condition that cannot be shown as simple rules.",
            string(R.string.advanced_condition_warning)
        )
        assertEquals(
            "This notification uses an advanced condition. Replace it to edit with simple rules.",
            string(R.string.validation_advanced)
        )
        assertEquals("Replace with simple rules", string(R.string.replace_with_simple_rules))
        assertEquals("Rules", string(R.string.rules))
        assertEquals("No rules yet. Add your first rule below.", string(R.string.no_rules_yet))
        assertEquals("Add rule", string(R.string.add_rule))
        assertEquals("Delete rule", string(R.string.delete_rule))
        assertEquals("Add at least one rule.", string(R.string.validation_empty))
        assertEquals("Complete all rules.", string(R.string.validation_incomplete))
        assertEquals("Scenario", string(R.string.field_scenario))
        assertEquals("Country", string(R.string.field_country))
        assertEquals("Target", string(R.string.field_target))
        assertEquals("IP owner", string(R.string.field_ip_owner))
        assertEquals("is", string(R.string.op_is))
        assertEquals("is not", string(R.string.op_is_not))
        assertEquals("is any of", string(R.string.op_is_any_of))
        assertEquals("is none of", string(R.string.op_is_none_of))
        assertEquals("contains", string(R.string.op_contains))
        assertEquals("scenario · is", string(R.string.rule_label_fmt, "scenario", "is"))
        assertEquals("(+3 more)", string(R.string.values_more_fmt, 3))
        assertEquals("Frequency", string(R.string.frequency))
        assertEquals("Count: 3", string(R.string.count_fmt, 3))
        assertEquals("Time window (seconds)", string(R.string.time_window_seconds))
        assertEquals(
            "Notify when 3 matching alerts arrive within 10 seconds.",
            string(R.string.frequency_footer_fmt, 3, "10 seconds")
        )
        assertEquals(
            "Window must be between 10 and 86400 seconds.",
            string(R.string.window_range_error)
        )
        assertEquals("Cooldown", string(R.string.cooldown))
        assertEquals(
            "After the notification is sent, matching alerts that arrive within this time " +
                "are ignored, so a burst of alerts sends only one notification.",
            string(R.string.cooldown_footer)
        )
        assertEquals("1 minute", string(R.string.duration_one_minute))
        assertEquals("2 minutes", string(R.string.duration_two_minutes))
        assertEquals("5 minutes", string(R.string.duration_five_minutes))
        assertEquals("15 minutes", string(R.string.duration_fifteen_minutes))
        assertEquals("1 hour", string(R.string.duration_one_hour))
        assertEquals("6 hours", string(R.string.duration_six_hours))
        assertEquals("24 hours", string(R.string.duration_twentyfour_hours))

        assertEquals("Rule", string(R.string.rule_title))
        assertEquals("1. What to look at", string(R.string.rule_section_field))
        assertEquals("2. How to compare", string(R.string.rule_section_operator))
        assertEquals("3. Values", string(R.string.rule_section_values))
        assertEquals("Field", string(R.string.field_label))
        assertEquals("Operator", string(R.string.operator_label))
        assertEquals("Value", string(R.string.value_label))
        assertEquals("Custom value", string(R.string.custom_value))
        assertEquals("Remove value", string(R.string.remove_value))
        assertEquals("No options available", string(R.string.no_options_available))
        assertEquals(
            "No options available, type a custom value below",
            string(R.string.no_options_custom_hint)
        )
        assertEquals("Or type a custom value", string(R.string.or_type_custom))
        assertEquals("Text", string(R.string.rule_value_text))

        assertEquals("3 times", quantityString(R.plurals.notification_times, 3))
        assertEquals("1 time", quantityString(R.plurals.notification_times, 1))
        assertEquals("10 seconds", quantityString(R.plurals.notification_seconds, 10))
        assertEquals("1 second", quantityString(R.plurals.notification_seconds, 1))
    }

    @Test
    fun `wizard strings resolve in Spanish`() {
        RuntimeEnvironment.setQualifiers("+es")

        assertEquals("Siguiente", string(R.string.next))
        assertEquals("Editar", string(R.string.edit))
        assertEquals("Finalizar", string(R.string.finish))
        assertEquals("Nueva notificación", string(R.string.notification_wizard_new_title))
        assertEquals("Editar notificación", string(R.string.notification_wizard_edit_title))
        assertEquals("Paso 2 de 5", string(R.string.step_of_fmt, 2, 5))
        assertEquals(
            "No se pudo guardar la notificación. Inténtalo más tarde.",
            string(R.string.error_save_notification)
        )
        assertEquals(
            "No se pudieron cargar las opciones de filtrado. Aún puedes escribir valores personalizados.",
            string(R.string.error_load_filter_options)
        )
        assertEquals(
            "El backend envía las notificaciones cuando las nuevas alertas cumplen la condición " +
                "que construyas en los siguientes pasos. Ponle un nombre para reconocerla.",
            string(R.string.wizard_info_box)
        )
        assertEquals("Detalles", string(R.string.details))
        assertEquals("Descripción (opcional)", string(R.string.description_optional))
        assertEquals(
            "Este es el texto que se envía por los canales elegidos cada vez que se cumple la condición.",
            string(R.string.message_step_info)
        )
        assertEquals("Contenido", string(R.string.content))
        assertEquals("Mensaje", string(R.string.message))
        assertEquals("Incluir información de las alertas", string(R.string.include_alert_info))
        assertEquals(
            "Aunque la opción esté activada, la información de las alertas sólo se enviará " +
                "a través de los canales que lo soporten.",
            string(R.string.include_alert_info_footer)
        )
        assertEquals("¿Qué canales lo soportan?", string(R.string.which_channels_support))
        assertEquals(
            "Elige uno o varios canales para enviar esta notificación. " +
                "Los canales se configuran en la pestaña Canales.",
            string(R.string.channels_step_info)
        )
        assertEquals("Aún no hay canales configurados", string(R.string.no_channels_configured))
        assertEquals(
            "Revisa la notificación antes de finalizar. Usa Atrás para cambiar algo.",
            string(R.string.review_step_info)
        )
        assertEquals("Resumen", string(R.string.summary))
        assertEquals("Cualquier alerta", string(R.string.any_alert))
        assertEquals("Bloques de condición", string(R.string.condition_blocks))
        assertEquals("Ninguno", string(R.string.review_none))
        assertEquals("Condición", string(R.string.condition))
        assertEquals(
            "Se activa con cualquier alerta, sin filtros. Combínalo con la frecuencia de abajo.",
            string(R.string.any_alert_explainer)
        )
        assertEquals(
            "Esta notificación usa una condición avanzada que no se puede mostrar como reglas simples.",
            string(R.string.advanced_condition_warning)
        )
        assertEquals(
            "Esta notificación usa una condición avanzada. Reemplázala para editar con reglas simples.",
            string(R.string.validation_advanced)
        )
        assertEquals("Reemplazar por reglas simples", string(R.string.replace_with_simple_rules))
        assertEquals("Reglas", string(R.string.rules))
        assertEquals("Aún no hay reglas. Añade la primera abajo.", string(R.string.no_rules_yet))
        assertEquals("Añadir regla", string(R.string.add_rule))
        assertEquals("Eliminar regla", string(R.string.delete_rule))
        assertEquals("Añade al menos una regla.", string(R.string.validation_empty))
        assertEquals("Completa todas las reglas.", string(R.string.validation_incomplete))
        assertEquals("Escenario", string(R.string.field_scenario))
        assertEquals("País", string(R.string.field_country))
        assertEquals("Objetivo", string(R.string.field_target))
        assertEquals("Propietario de IP", string(R.string.field_ip_owner))
        assertEquals("es", string(R.string.op_is))
        assertEquals("no es", string(R.string.op_is_not))
        assertEquals("es cualquiera de", string(R.string.op_is_any_of))
        assertEquals("no es ninguno de", string(R.string.op_is_none_of))
        assertEquals("contiene", string(R.string.op_contains))
        assertEquals("(+3 más)", string(R.string.values_more_fmt, 3))
        assertEquals("Frecuencia", string(R.string.frequency))
        assertEquals("Cantidad: 3", string(R.string.count_fmt, 3))
        assertEquals("Ventana de tiempo (segundos)", string(R.string.time_window_seconds))
        assertEquals(
            "Notificar cuando lleguen 3 alertas coincidentes en 10 segundos.",
            string(R.string.frequency_footer_fmt, 3, "10 segundos")
        )
        assertEquals(
            "La ventana debe estar entre 10 y 86400 segundos.",
            string(R.string.window_range_error)
        )
        assertEquals("Tiempo de espera", string(R.string.cooldown))
        assertEquals(
            "Una vez enviada la notificación, las alertas coincidentes que lleguen dentro de " +
                "este tiempo se ignoran, de forma que una ráfaga de alertas solo envía una notificación.",
            string(R.string.cooldown_footer)
        )
        assertEquals("1 minuto", string(R.string.duration_one_minute))
        assertEquals("2 minutos", string(R.string.duration_two_minutes))
        assertEquals("5 minutos", string(R.string.duration_five_minutes))
        assertEquals("15 minutos", string(R.string.duration_fifteen_minutes))
        assertEquals("1 hora", string(R.string.duration_one_hour))
        assertEquals("6 horas", string(R.string.duration_six_hours))
        assertEquals("24 horas", string(R.string.duration_twentyfour_hours))
        assertEquals("Regla", string(R.string.rule_title))
        assertEquals("1. Qué observar", string(R.string.rule_section_field))
        assertEquals("2. Cómo comparar", string(R.string.rule_section_operator))
        assertEquals("3. Valores", string(R.string.rule_section_values))
        assertEquals("Campo", string(R.string.field_label))
        assertEquals("Operador", string(R.string.operator_label))
        assertEquals("Valor", string(R.string.value_label))
        assertEquals("Valor personalizado", string(R.string.custom_value))
        assertEquals("Eliminar valor", string(R.string.remove_value))
        assertEquals("No hay opciones disponibles", string(R.string.no_options_available))
        assertEquals(
            "No hay opciones disponibles, escribe un valor personalizado abajo",
            string(R.string.no_options_custom_hint)
        )
        assertEquals("O escribe un valor personalizado", string(R.string.or_type_custom))
        assertEquals("Texto", string(R.string.rule_value_text))

        assertEquals("3 veces", quantityString(R.plurals.notification_times, 3))
        assertEquals("1 vez", quantityString(R.plurals.notification_times, 1))
        assertEquals("10 segundos", quantityString(R.plurals.notification_seconds, 10))
        assertEquals("1 segundo", quantityString(R.plurals.notification_seconds, 1))
    }

    @Test
    fun `wizard strings resolve in German`() {
        RuntimeEnvironment.setQualifiers("+de")

        assertEquals("Weiter", string(R.string.next))
        assertEquals("Bearbeiten", string(R.string.edit))
        assertEquals("Fertigstellen", string(R.string.finish))
        assertEquals("Neue Benachrichtigung", string(R.string.notification_wizard_new_title))
        assertEquals("Benachrichtigung bearbeiten", string(R.string.notification_wizard_edit_title))
        assertEquals("Schritt 2 von 5", string(R.string.step_of_fmt, 2, 5))
        assertEquals(
            "Die Benachrichtigung konnte nicht gespeichert werden. Versuche es später erneut.",
            string(R.string.error_save_notification)
        )
        assertEquals(
            "Die Filteroptionen konnten nicht geladen werden. Du kannst weiterhin benutzerdefinierte Werte eingeben.",
            string(R.string.error_load_filter_options)
        )
        assertEquals(
            "Das Backend sendet Benachrichtigungen, wenn neue Alarme die Bedingung erfüllen, " +
                "die du in den folgenden Schritten erstellst. Gib ihr einen Namen, damit du sie später erkennst.",
            string(R.string.wizard_info_box)
        )
        assertEquals("Details", string(R.string.details))
        assertEquals("Beschreibung (optional)", string(R.string.description_optional))
        assertEquals(
            "Dies ist der Text, der bei jedem Auslösen der Bedingung über die ausgewählten Kanäle gesendet wird.",
            string(R.string.message_step_info)
        )
        assertEquals("Inhalt", string(R.string.content))
        assertEquals("Nachricht", string(R.string.message))
        assertEquals("Alarminformationen einschließen", string(R.string.include_alert_info))
        assertEquals(
            "Auch bei Aktivierung werden Alarminformationen nur über Kanäle gesendet, die dies unterstützen.",
            string(R.string.include_alert_info_footer)
        )
        assertEquals("Welche Kanäle unterstützen das?", string(R.string.which_channels_support))
        assertEquals(
            "Wähle einen oder mehrere Kanäle aus, über die diese Benachrichtigung zugestellt wird. " +
                "Kanäle werden im Tab „Kanäle“ konfiguriert.",
            string(R.string.channels_step_info)
        )
        assertEquals("Noch keine Kanäle konfiguriert", string(R.string.no_channels_configured))
        assertEquals(
            "Prüfe die Benachrichtigung, bevor du sie abschließt. Verwende „Zurück“, um etwas zu ändern.",
            string(R.string.review_step_info)
        )
        assertEquals("Zusammenfassung", string(R.string.summary))
        assertEquals("Jeder Alarm", string(R.string.any_alert))
        assertEquals("Bedingungsblöcke", string(R.string.condition_blocks))
        assertEquals("Keine", string(R.string.review_none))
        assertEquals("Bedingung", string(R.string.condition))
        assertEquals(
            "Löst bei jedem Alarm aus, ohne Filter. Kombiniere das mit der Häufigkeit unten.",
            string(R.string.any_alert_explainer)
        )
        assertEquals(
            "Diese Benachrichtigung verwendet eine erweiterte Bedingung, die nicht als einfache Regeln angezeigt werden kann.",
            string(R.string.advanced_condition_warning)
        )
        assertEquals(
            "Diese Benachrichtigung verwendet eine erweiterte Bedingung. Ersetze sie, um mit einfachen Regeln zu bearbeiten.",
            string(R.string.validation_advanced)
        )
        assertEquals("Durch einfache Regeln ersetzen", string(R.string.replace_with_simple_rules))
        assertEquals("Regeln", string(R.string.rules))
        assertEquals("Noch keine Regeln. Füge unten die erste hinzu.", string(R.string.no_rules_yet))
        assertEquals("Regel hinzufügen", string(R.string.add_rule))
        assertEquals("Regel löschen", string(R.string.delete_rule))
        assertEquals("Füge mindestens eine Regel hinzu.", string(R.string.validation_empty))
        assertEquals("Vervollständige alle Regeln.", string(R.string.validation_incomplete))
        assertEquals("Szenario", string(R.string.field_scenario))
        assertEquals("Land", string(R.string.field_country))
        assertEquals("Ziel", string(R.string.field_target))
        assertEquals("IP-Eigentümer", string(R.string.field_ip_owner))
        assertEquals("ist", string(R.string.op_is))
        assertEquals("ist nicht", string(R.string.op_is_not))
        assertEquals("ist einer von", string(R.string.op_is_any_of))
        assertEquals("ist keiner von", string(R.string.op_is_none_of))
        assertEquals("enthält", string(R.string.op_contains))
        assertEquals("(+3 mehr)", string(R.string.values_more_fmt, 3))
        assertEquals("Häufigkeit", string(R.string.frequency))
        assertEquals("Anzahl: 3", string(R.string.count_fmt, 3))
        assertEquals("Zeitfenster (Sekunden)", string(R.string.time_window_seconds))
        assertEquals(
            "Benachrichtigen, wenn 3 passende Alarme innerhalb von 10 Sekunden eingehen.",
            string(R.string.frequency_footer_fmt, 3, "10 Sekunden")
        )
        assertEquals(
            "Das Zeitfenster muss zwischen 10 und 86400 Sekunden liegen.",
            string(R.string.window_range_error)
        )
        assertEquals("Wartezeit", string(R.string.cooldown))
        assertEquals(
            "Nach dem Senden der Benachrichtigung werden passende Alarme, die innerhalb dieser " +
                "Zeit eingehen, ignoriert, sodass eine Häufung von Alarmen nur eine Benachrichtigung sendet.",
            string(R.string.cooldown_footer)
        )
        assertEquals("1 Minute", string(R.string.duration_one_minute))
        assertEquals("2 Minuten", string(R.string.duration_two_minutes))
        assertEquals("5 Minuten", string(R.string.duration_five_minutes))
        assertEquals("15 Minuten", string(R.string.duration_fifteen_minutes))
        assertEquals("1 Stunde", string(R.string.duration_one_hour))
        assertEquals("6 Stunden", string(R.string.duration_six_hours))
        assertEquals("24 Stunden", string(R.string.duration_twentyfour_hours))
        assertEquals("Regel", string(R.string.rule_title))
        assertEquals("1. Worauf geprüft wird", string(R.string.rule_section_field))
        assertEquals("2. Wie verglichen wird", string(R.string.rule_section_operator))
        assertEquals("3. Werte", string(R.string.rule_section_values))
        assertEquals("Feld", string(R.string.field_label))
        assertEquals("Operator", string(R.string.operator_label))
        assertEquals("Wert", string(R.string.value_label))
        assertEquals("Benutzerdefinierter Wert", string(R.string.custom_value))
        assertEquals("Wert entfernen", string(R.string.remove_value))
        assertEquals("Keine Optionen verfügbar", string(R.string.no_options_available))
        assertEquals(
            "Keine Optionen verfügbar, gib unten einen benutzerdefinierten Wert ein",
            string(R.string.no_options_custom_hint)
        )
        assertEquals("Oder gib einen benutzerdefinierten Wert ein", string(R.string.or_type_custom))
        assertEquals("Text", string(R.string.rule_value_text))

        assertEquals("3 Mal", quantityString(R.plurals.notification_times, 3))
        assertEquals("1 Mal", quantityString(R.plurals.notification_times, 1))
        assertEquals("10 Sekunden", quantityString(R.plurals.notification_seconds, 10))
        assertEquals("1 Sekunde", quantityString(R.plurals.notification_seconds, 1))
    }

    @Test
    fun `frequency label composes pluralized times with window label`() {
        RuntimeEnvironment.setQualifiers("+en")
        assertEquals(
            "3 times in 10 seconds",
            string(
                R.string.frequency_fmt,
                quantityString(R.plurals.notification_times, 3),
                ConditionFormatting.windowLabelRes(10)?.let { string(it) }
                    ?: quantityString(R.plurals.notification_seconds, 10)
            )
        )
        assertEquals(
            "1 time in 1 minute",
            string(
                R.string.frequency_fmt,
                quantityString(R.plurals.notification_times, 1),
                ConditionFormatting.windowLabelRes(60)?.let { string(it) }
                    ?: quantityString(R.plurals.notification_seconds, 60)
            )
        )
        RuntimeEnvironment.setQualifiers("+es")
        assertEquals(
            "3 veces en 10 segundos",
            string(
                R.string.frequency_fmt,
                quantityString(R.plurals.notification_times, 3),
                quantityString(R.plurals.notification_seconds, 10)
            )
        )
        RuntimeEnvironment.setQualifiers("+de")
        assertEquals(
            "3 Mal in 10 Sekunden",
            string(
                R.string.frequency_fmt,
                quantityString(R.plurals.notification_times, 3),
                quantityString(R.plurals.notification_seconds, 10)
            )
        )
    }
}
