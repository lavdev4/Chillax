# Chillax
🎶🦉🌲🌊 Звуки природы с возможностью комбинирования и таймером

<img src="https://github.com/lavdev4/MyCalc/assets/103329075/1551789c-4a6e-4bfa-b1ec-8ea73de6da9d" width="2%" height="2%" align="center"> **Google Play:** [**Chillax**](https://play.google.com/store/apps/details?id=com.lavdevapp.chillax)

<details>
  <summary><b>🏞️Скриншоты</b></summary>
    <p align="center">
      <img width="20%" height="20%" src="https://github.com/lavdev4/Chillax/assets/103329075/6b5a2f8d-2165-4f4d-8851-5b0ef720eb3b">
    </p>
    <p align="center">
      <img width="20%" height="20%" src="https://github.com/lavdev4/Chillax/assets/103329075/778053f2-1bd1-49a2-84ce-37d8d81116ae">
    </p>
    <p align="center">
      <img width="20%" height="20%" src="https://github.com/lavdev4/Chillax/assets/103329075/47f5809a-5dc3-49a5-92ef-b6a131e59ea1">
    </p>
    <p align="center">
      <img width="20%" height="20%" src="https://github.com/lavdev4/Chillax/assets/103329075/dabefad5-c178-4c6b-8065-8385af468cf4">
    </p>
</details>

## Описание: 
**Работа выполнена в процессе изучения Android SDK и языка Kotlin.** Приложение предоставляет возможность выбирать и воспроизводить звуки природы, комбинируя любым желаемым образом. После выборки желаемых звуков воспроизведение активируется главной кнопкой, после чего приложение можно свернуть, а экран выключить. После выключения экрана, если возспроизведение звуков активно, процесс, в котором работает приложение, гарантированно не будет уничтожен. Данный принцип работы реализован при помощи Foreground Service. 
Взаимодействие между Service и Activity реализовано с помощью Bound Service.
В приложении также реализован таймер, по истечении времени которого воспроизведение звуков останавливается. Информация о воспроизведении выводится в уведомлении, из которого можно остановить таймер и воспроизведение.

## Особенности:
- Непрерывная работа при сворачивании приложения и выключении экрана
- Работа нескольких аудиодорожек одновременно
- Цикличное воспроизведение аудиодорожки без разрывов
- Управление воспроизведением и таймером из уведомления
- Статус таймера в уведомлении
- Сохранение состояния настроек после выхода из приложения
- Приоритетный порядок в списке для избранных аудиодорожек
- Таймер автоматической остановки воспроизведения
- Приостановка воспроизведения при потере аудиофокуса
- Анимация кнопок и "эффект свечения"

## Использованные инструменты:
Moshi, UI Automator, Espresso, Firebase Crashlytics, Firebase Analytics, Audio Focus, Foreground service, Bound service, BroadcastReciever, SavedStateHandle, TransitionDrawable, ObjectAnimator, ValueAnimator, Pending Intent
