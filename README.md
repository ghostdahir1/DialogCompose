# DialogCompose - AlertDialog con Estados, Funciones Lambda y Google Maps (UPP)

Práctica de desarrollo de aplicaciones móviles en **Android Studio** utilizando **Kotlin** y **Jetpack Compose (Material Design 3)**. Implementa un diálogo modal (`AlertDialog`) reutilizable controlado mediante estados observables (`rememberSaveable`), elevación de estado (*State Hoisting*) con funciones lambda (`onDismiss`, `onConfirm`), diseño institucional con el escudo de la Universidad Politécnica de Pachuca y visualización de ubicaciones en **Google Maps**.

## Datos académicos
- **Institución:** Universidad Politécnica de Pachuca (UPP)
- **Programa Educativo:** Ing. en Tecnologías De La Información Y La Innovación Digital
- **Asignatura:** Desarrollo de aplicaciones móviles
- **Alumno:** Lorenzana Hernández Aarón Dahir
- **Matrícula:** 2531126341
- **Grupo:** TIID 4-02
- **Profesor:** MIGUEL ANGEL MONTOYA CERRO

## Archivos principales
- `app/src/main/java/com/example/dialogcompose/MainActivity.kt` — Pantalla principal (`DialogMapsScreen`), mapa interactivo (`GoogleMapCardView`) y componente reutilizable `MyDialog`.
- `app/src/main/AndroidManifest.xml` — Permisos de red/ubicación y metadato `com.google.android.geo.API_KEY`.
- `app/src/main/res/drawable/logo_upp.jpg` — Escudo oficial de la Universidad Politécnica de Pachuca.
- `app/src/main/res/values/strings.xml` — Recurso `google_maps_key` para configurar la API Key de Google Maps.
- `Reporte_Practica_DialogCompose_Aaron_Lorenzana.docx` — Reporte completo de la práctica en formato Word.
