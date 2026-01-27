## Descripción
**Moneymaster** es una aplicación Android nativa desarrollada en Java que permite a los usuarios llevar un control detallado de sus finanzas personales y comparitdas. La app funciona completamente **offline**
utilizando almacenamiento local, sin necesidad de servidores externos, garantizando la privacidad total de los datos del usuario.

### Problemas que resuelve esta aplicación
- **Personal**: Dificil seguimiento de gastos diarios y categorización de ingresos.
- **Grupal**: Complicado dividir gastos entre compañeros de piso, viajes o proyectos.
- **Visual**: Falta de claridad en dónde va el dinero cada mes.
- **Privacidad**: Preocupación por compartir datos financieros en servidores de terceros.

## Características

### Control Personal
- Registro de gastos e ingresos con fecha y descripción
- 15+ categorías predefinidas de gastos
- 3 categorías de ingresos (Salario, Ahorros, Depósitos)
- Creación de categorías personalizadas
- Adjuntar fotos de recibos y tickes
- Gráficos y estadísticas mensuales/anuales
- Exportación de datos (CSV, Excell, PDF)

### Gestión de Grupos
- Creación de grupos locales (piso, viajes, proyectos)
- Gestión de múltiples miembros por grupo
- Registro de gastos compartidos con división automática
- Cálculo inteligente de balances (quién debe a quién)
- **Generacion de imágenes resumen** para compartir por WhatsApp.
- Sistema de liquidación de deudas

### Tablón de Fotos
- Galería de fotos de recibos por categoría
- Asociación de fotos a gastos específicos
- **Compartir múltiples fotos** del grupo por WhatsApp/Email
- Organización por grupo y fecha
- Visualización en cuadrícula y detalle

### Onboarding y Acceso
- Tutorial Inicial con 3 pantallas explicativas
- Diseño atractivo e intuitivo para primera experiencia
- Sistema de login/registro local (sin servidor)
- Protección con PIN
- Recuperación de cuenta con pregunta de seguridad

### Privacidad y Seguridad
- **100% offline** - todos los datos en memoria interna
- Sin recopilación de datos personales
- Sin anuncios ni rastreadores
- Cumplimiento GDPR por diseño

## Tecnologías
- **Lenguaje** Java 11+
- **IDE** Android Studio Hedgehog+
- **Min SDK** Android 7.0 (API 24)
- **Target SDK** Android 14 (API 34)
- **Base de datos** SQLite 3
- **Arquitectura** MVVM (Model-View-ViewModel)
- **UI** Material Design 3
- **Gráficos** MPAndroidChart
- **Imágenes** Glide/Picasso
- **Exportación** Apache POI (Excel), iText (PDF)

## Instalación
### Requisitos Previos
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 11 o superior
- Android SDK API 24+
- Gradle 8.0+

### Compilar y ejecutar
1. Abre el proyecto en Android Studio
2. Sincroniza Gradle
3. Conecta un dispositivo Android o inicia un emulador
4. Click en ´Run´o presiona ´Shift + F10´

## Contribución
¡Las contribuciones son bienvenidas! Si quieres colaborar:
1. Fork el proyecto
2. Crea una rama para tu feature
3. Commit tus cambios
4. Push a la rama
5. Abre un Pull Request

## Licencia
Este proyecto esta bhajo la Licencia MIT. 




