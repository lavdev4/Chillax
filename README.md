# Chillax
🎶🦉🌲🌊 Звуки природы с возможностью комбинирования и таймером

<img src="https://github.com/lavdev4/MyCalc/assets/103329075/1551789c-4a6e-4bfa-b1ec-8ea73de6da9d" width="2%" height="2%" align="center"> **Google Play:** [**Chillax**](https://play.google.com/store/apps/details?id=com.lavdevapp.chillax)

<details>
  <summary><b>🏞️Скриншоты</b></summary>
    <p align="center">
      <img width="30%" height="30%" src="https://github.com/lavdev4/Chillax/assets/103329075/c69353d3-9919-4a39-8e02-f4f1b97b0133">
    </p>
    <p align="center">
      <img width="30%" height="30%" src="https://github.com/lavdev4/Chillax/assets/103329075/4d8e16d5-9a0f-4540-93fb-3f23d6078f0b">
    </p>
    <p align="center">
      <img width="30%" height="30%" src="https://github.com/lavdev4/Chillax/assets/103329075/f66faa60-7386-43b1-abd0-9ab335755f5d">
    </p>
    <p align="center">
      <img width="30%" height="30%" src="https://github.com/lavdev4/Chillax/assets/103329075/b84a2ec6-5693-499f-a3c0-e2afe517be0e">
    </p>
    <p align="center">
      <img width="30%" height="30%" src="https://github.com/lavdev4/Chillax/assets/103329075/cfea37a1-7400-4c2a-a0a3-2a45d70253b7">
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
