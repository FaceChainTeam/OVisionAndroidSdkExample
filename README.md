# Пример интеграции `com.ovision.camera.FaceCaptureActivity`

Проект демонстрирует минимальную настройку Android-приложения для запуска `FaceCaptureActivity` из библиотеки `com.ovision.camera` и обработки результата съёмки лица в Jetpack Compose.

## Требования
- Android Studio Koala или новее с подключённым Android SDK 36.
- Устройство или эмулятор под управлением Android 10 (API 29) и выше (минимальная версия библиотеки).
- Локальный JDK 17+, необходимый Gradle установит автоматически.

## Быстрый старт
1. Скопируйте файл `camera-v1.X.Xaar` и поместите его в каталог `app/libs`.
2. Убедитесь, что в `app/build.gradle.kts` подключена папка `libs`:
   ```kotlin
   dependencies {
       implementation(fileTree("libs") { include("*.jar", "*.aar") })
       // ...
   }
   ```
3. Синхронизируйте проект в Android Studio и запустите модуль `app` на устройстве.

### Дополнительные зависимости
Библиотека использует CameraX и ML Kit. В демонстрационном проекте уже добавлены необходимые зависимости:
```kotlin
implementation("androidx.exifinterface:exifinterface:1.3.6")
implementation("androidx.camera:camera-core:1.4.2")
implementation("androidx.camera:camera-camera2:1.4.2")
implementation("androidx.camera:camera-lifecycle:1.4.2")
implementation("androidx.camera:camera-view:1.4.2")
implementation("androidx.camera:camera-mlkit-vision:1.4.2")
implementation("com.google.mlkit:face-detection:16.1.7")
```
При интеграции в своё приложение перенесите эти зависимости в соответствующий модуль.

## Запуск `FaceCaptureActivity`
Используйте метод `FaceCaptureActivity.newIntent(...)`, передавая требуемую камеру:
```kotlin
val intent = FaceCaptureActivity.newIntent(
    context = context,
    lensFacing = CameraSelector.LENS_FACING_FRONT // или CameraSelector.LENS_FACING_BACK
)
launcher.launch(intent)
```
В примере запуск реализован через `rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult())`.

## Обработка результата
- При успешной съёмке `FaceCaptureActivity` возвращает `RESULT_OK`, а в `intent.data` лежит `Uri` сохранённого фото. Используйте `ContentResolver` для чтения файла, как показано в `MainActivity`.
- В случае отмены съёмки `result.resultCode` не будет равен `RESULT_OK`. Дополнительно из `intent` можно получить строковый extra `result_reason`. Библиотека возвращает значение `timeout`, если пользователь не успел сделать фото.
- Полученный `Uri` указывает на файл внутри `FileProvider` (`${applicationId}.fileprovider`). Для временного доступа другим компонентам передавайте `FLAG_GRANT_READ_URI_PERMISSION`.

## Разрешения и манифест
- AAR объявляет разрешение `android.permission.CAMERA` и `FileProvider` с authorities `${applicationId}.fileprovider`. Дополнительных записей в манифест не требуется.
- Перед запуском активности убедитесь, что приложению выдано разрешение на камеру. В примере проверка опущена: если вы встраиваете библиотеку в продуктивное приложение, добавьте runtime-запрос разрешения.

## Структура примера
- `app/src/main/java/com/ovision/cameraexample/MainActivity.kt` — основной экран с кнопкой «Сделать фото» и отображением результата.
- `app/libs/camera-v1.X.X.aar` — поставляемая библиотека с `FaceCaptureActivity` и её ресурсами.

При необходимости расширяйте интерфейс и обработку результатов, используя этот проект как точку входа.
