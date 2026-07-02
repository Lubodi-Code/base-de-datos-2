# =============================================================================
#  GUÍA DEL USUARIO ANTE FALLAS DE SIGEC-PJ
#  Dirigida a usuarios finales (no técnicos)
# =============================================================================

## 1. Mensajes de Error Comunes

### 1.1. "Error de conexión con el servidor"

**Qué significa:** El sistema temporalmente no puede comunicarse con los servidores.

**Qué hacer:**
1. Espere 30 segundos y recargue la página (F5 o Ctrl+R)
2. Si el error persiste después de 2-3 minutos, cierre y abra el navegador
3. Si continúa después de 10 minutos, llame al soporte técnico

**No significa que:** 
- ❌ Se perdieron sus datos
- ❌ El sistema está caído permanentemente
- ❌ Alguien borró su trabajo

### 1.2. "Tiempo de espera agotado"

**Qué significa:** La operación tardó más de lo esperado (generalmente por lentitud de red).

**Qué hacer:**
1. Espere. La operación probablemente se completó en el servidor
2. Verifique sus datos antes de volver a intentar
3. Si recibe confirmación por correo electrónico, no vuelva a intentar

**No significa que:**
- ❌ La operación falló
- ❌ Necesita volver a enviar el formulario

### 1.3. "Servicio no disponible" (503)

**Qué significa:** Los servidores están en mantenimiento o cambiando automáticamente.

**Qué hacer:**
1. Espere 2-3 minutos y recargue
2. Si dura más de 30 minutos, llame al soporte

**Qué NO hacer:**
- ❌ No envíe el formulario múltiples veces
- ❌ No cierre la ventana inmediatamente

## 2. Qué Hacer en Diferentes Situaciones

### 2.1. Si estaba agregando un equipo

**Si recibe error después de hacer clic en "Guardar":**
1. No cierre la ventana
2. Espere 1 minuto y recargue
3. Verifique en la lista de equipos si el equipo aparece
4. Si aparece, el registro fue exitoso
5. Si no aparece, intente nuevamente

### 2.2. Si estaba registrando un incidente

**Si recibe error:**
1. Espere 1 minuto y recargue
2. Verifique en la lista de incidentes
3. Si aparece, el registro fue exitoso
4. Si no aparece, tome nota de la información y llame al soporte

### 2.3. Si estaba actualizando un movimiento

**Si recibe error:**
1. Espere 1 minuto y recargue
2. Verifique en el historial de movimientos
3. Si el movimiento aparece actualizado, todo está bien
4. Si no aparece, intente nuevamente

## 3. Conceptos Importantes de Alta Disponibilidad

### 3.1. ¿Qué significa que el sistema es "altamente disponible"?

**Analogía:** Imagine un restaurante con dos cocinas. Si una cocina se daña, la otra cocina sigue preparando sus pedidos sin que usted se dé cuenta.

**En SIGEC-PJ:** Tenemos servidores redundantes. Si uno falla, otro toma el control automáticamente.

### 3.2. ¿Por qué a veces veo errores breves?

**Analogía:** Como cuando cambia de cocina, el restaurante tarda 30 segundos en empezar a usar la otra cocina.

**En SIGEC-PJ:** Cuando un servidor falla, el sistema tarda menos de 1 minuto en cambiar al otro servidor automáticamente.

### 3.3. ¿Mis datos están seguros?

**Sí, absolutamente.** Todos los cambios se guardan en múltiples servidores simultáneamente. Si uno falla, sus datos están a salvo en los otros.

### 3.4. ¿Puedo perder trabajo?

**En el 99.5% de los casos, NO.** Sus datos se guardan en el momento que hace clic en "Guardar". Los errores breves no significan pérdida de datos.

## 4. Tiempos Esperados de Recuperación

| Situación | Tiempo de Espera | Acción Recomendada |
|-----------|------------------|-------------------|
| Error de conexión breve | 30-60 segundos | Espere y recargue |
| Cambio automático de servidor | 45-90 segundos | Espere y recargue |
| Mantenimiento programado | 5-10 minutos | Espere y recargue |
| Falla completa (raro) | 15-30 minutos | Llame al soporte |

## 5. Señales de que el Sistema Está Funcionando

Aunque vea errores momentáneos, el sistema está funcionando si:

- ✅ Puede acceder al login después de recargar
- ✅ Puede ver sus equipos e incidentes registrados
- ✅ Puede realizar búsquedas
- ✅ Puede agregar nuevos registros (aunque demoren un poco)

## 6. Cuándo Llamar al Soporte Técnico

Llame al 2295-3000 (infopj@poderjudicial.go.cr) si:

1. **El error dura más de 30 minutos** y no puede acceder al sistema
2. **Después de un error, sus datos desaparecieron** (esto es muy raro)
3. **Recibe un mensaje completamente diferente** a los mencionados aquí
4. **El sistema está excesivamente lento** (más de 2 minutos para cargar una página)

## 7. Mitos Comunes

### Mito: "Si me sale error, perdí mis datos"
**Realidad:** Los errores momentáneos NO borran datos. Sus datos están seguros en múltiples servidores.

### Mito: "Debo enviar el formulario muchas veces para asegurar"
**Realidad:** Enviando múltiples veces puede crear registros duplicados. Una vez es suficiente.

### Mito: "El sistema está caído para siempre"
**Realidad:** La alta disponibilidad garantiza que el sistema se recupera en menos de 1 minuto automáticamente.

### Mito: "Si recargo la página, pierdo mi trabajo"
**Realidad:** Los cambios se guardan inmediatamente en el servidor. Recargar es seguro.

## 8. Preguntas Frecuentes

**P: ¿Por qué a veces el sistema está lento?**
R: Puede ser por mantenimiento momentáneo o tráfico pesado. Espere 1-2 minutos.

**P: ¿Mis contraseñas están seguras?**
R: Sí, están cifradas con los estándares más altos. Ni siquiera los administradores pueden verlas.

**P: ¿Qué pasa si olvido mi contraseña?**
R: Llame al soporte técnico. Verificaremos su identidad y la reiniciaremos.

**P: ¿Puedo confiar en el historial de movimientos?**
R: Absolutamente. Cada movimiento queda registrado con quién lo hizo, cuándo y por qué. Es inalterable.

**P: ¿El sistema respalda automáticamente?**
R: Sí, hay respaldos continuos cada 6 horas y respaldos completos diarios. Su información nunca se pierde.

## 9. Recomendaciones de Buen Uso

1. **Guarde sus cambios frecuente** (no deje formularios abiertos por horas)
2. **Use el navegador recomendado** (Chrome, Firefox o Edge; actualizado)
3. **No abra múltiples pestañas** del mismo sistema simultáneamente
4. **Anote códigos de equipo** en lugar de confiar en la memoria
5. **Use la opción de exportar** para tener respaldo local de reportes importantes

## 10. Contacto de Emergencia

**Soporte TI Poder Judicial:** 2295-3000  
**Horario:** Lunes a viernes, 8:00 AM - 4:00 PM  
**Emergencias fuera de horario:** 2295-3001 (pager de guardia)

---
**Versión:** 1.0  
**Fecha:** 26/08/2026  
**Aprobado por:** Unidad de Informática - Poder Judicial