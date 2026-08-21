# Release 0.2.0 — Google Play Console

## Cambios incluidos

- Bloqueo de tráfico HTTP claro en producción.
- Logging HTTP desactivado en release.
- Cookies filtradas por dominio y ruta.
- Backup de datos de la aplicación desactivado.
- Minificación/R8 activada para release.
- Firma configurada mediante variables externas, fuera del repositorio.

## Antes de compilar

Configurar las variables únicamente en la sesión local, sin escribirlas en el
repositorio ni pegarlas en el chat:

```powershell
$env:KS_KEYSTORE_PATH = 'E:\Afinavila-private\deploy-tools\artifacts\afinavila-release.jks'
$env:KS_STORE_PASSWORD = '***'
$env:KS_KEY_ALIAS = '***'
$env:KS_KEY_PASSWORD = '***'
```

Comprobar solo la existencia de las variables, nunca imprimir sus valores:

```powershell
($env:KS_KEYSTORE_PATH, $env:KS_STORE_PASSWORD, $env:KS_KEY_ALIAS, $env:KS_KEY_PASSWORD).ForEach({ $_.Length -gt 0 })
```

## Gates locales

```powershell
./gradlew.bat clean
./gradlew.bat test
./gradlew.bat bundleRelease
```

El artefacto esperado es:

```text
app/build/outputs/bundle/release/app-release.aab
```

Verificar que el bundle está firmado antes de subirlo:

```powershell
 jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
```

No subir un bundle `unsigned`.

## Google Play Console

1. Abrir **Testing → Internal testing** y crear una nueva release.
2. Subir `app-release.aab`.
3. Revisar el `versionCode` 2 y `versionName` 0.2.0.
4. Añadir testers internos y guardar primero como borrador.
5. Revisar **App content**, privacidad, anuncios, acceso a la aplicación y
   clasificación de contenido.
6. Añadir notas de versión:

   > Mejoras de seguridad y privacidad, conexiones HTTPS reforzadas,
   > optimización de rendimiento y correcciones en la gestión de documentos.

7. Publicar primero en testing interno.
8. Probar login, carga de documentos, apertura de PDF, logout, rotación y
   recuperación ante sesión expirada.
9. Promover a **Closed testing** o producción solo después de validar el canal
   interno.

## Reglas de seguridad

- No subir el keystore, passwords, `.env` ni APKs al repositorio.
- No cambiar el `applicationId`.
- No reutilizar el `versionCode` 2.
- Conservar el mismo keystore de firma de la aplicación publicada.
- Si el keystore no corresponde a la aplicación existente, detenerse antes de
  subir el bundle.
