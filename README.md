# Fenrir VK

За основу взят проект Phoenix (R)<br>

<b>Языки: Русский, Английский</b>

## Отказ от ответственности
Данный проект является демонстрационным вариантом и учебным пособием для разработки rest клиентов на языке Kotlin и C++ под Android,
а также интеграции современных библиотек для работы с медиа. Использовать для других целей, кроме изучения технологий, а также публиковать данную работу строго запрещено.

<b>Скриншоты:</b>

<img src="Fenrir_VK.jpg" alt=""/>

<b>Инструкция по сборке:</b><br>
Требуется:<br>
  1. Android Studio Quail 3 2026.1.3 или выше. Kotlin 2.4.*<br>
  2. Android SDK 37.1<br>
  3. Android NDK 30.0.15729638<br>
  4. CMake 4.1.2<br>
  5. Diffutils
  
<b>Компиляция:</b>

  1. Для релизных сборок вам нужен сертификат.<br>
  keytool -genkey -v -keystore Fenrir.keystore -alias fenrir -storetype PKCS12 -keyalg RSA -keysize 2048 -validity 10000<br>
  2. Вариант 1. Далее нужно собрать нативную библиотеку:<br>
  Создать папку compiled_native, раскомментировать [include ":native"] в settings.gradle<br>
  cd native<br>
  ./ffmpeg.sh<br>
  после синхронизации репозитория ffmpeg введите min sdk version<br>
  после сборки ffmpeg соберите native в Android Studio и поместите native-release.aar в compiled_native<br>
  3. Вариант 2. Далее нужно собрать нативную библиотеку:<br>
  Раскомментировать [include ":native"] в settings.gradle<br>
  Раскомментировать [implementation project(":native")] в app*/build.gradle<br>
  Удалить [implementation fileTree('../compiled_native') { include '*.aar' }] в app*/build.gradle<br>
  cd native<br>
  ./ffmpeg.sh<br>
  после синхронизации репозитория ffmpeg введите min sdk version<br>
  после сборки ffmpeg можете собирать проект<br>
  4. Выберите flavor - fenrir или kate и Debug или Release и соберите apk :)<br>

Локальный медиа сервер https://github.com/umerov1999/FenrirMediaServer/releases <br>

# FileGallery
Просмотр фото, видео, аудио, тэги<br>

<b>Языки: Русский, Английский</b>

<b>Скриншот:</b>

<img src="FileGallery.jpg" alt=""/>
