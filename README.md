# Music Assistant KOR

**Music Assistant 공식 모바일 앱을 기반으로 한 비공식 Android 한국어 개선판**입니다.

원본: [music-assistant/mobile-app](https://github.com/music-assistant/mobile-app) · 라이선스: Apache-2.0 (원본 라이선스 유지)

> 공식 Music Assistant 개발팀의 공식 배포판이 아닌 개인 포크(Fork)입니다.

## 공식 앱과 다른 점

| 항목 | Music Assistant KOR 변경 사항 |
| --- | --- |
| 한국어 | 공통 리소스 문자열 565개 한국어 번역 추가 |
| 앱 구분 | 앱 이름 Music Assistant KOR, 패키지 io.music_assistant.client.kor (공식 앱과 별도 설치) |
| 아이콘 | 배경 진보라색, 로고 흰색으로 변경 |
| 폴더 탐색 | Filesystem 소스가 하나로 확인되면 소스 선택 화면을 건너뛰고 폴더 목록 표시 |
| 가사 | A− / A+ 글꼴 크기 16~32sp 조절, 기본 21sp, 선택값 저장, 가사 간격 축소 |

**Android 앱 우선 개발:** 공통 코드 변경은 iOS에도 영향을 줄 수 있으나 iOS 빌드·검증은 하지 않았습니다.

## APK 다운로드 및 설치

개발 브랜치: [kor](https://github.com/SoonCheol/MusicAssistantKOR/tree/kor)

1. [GitHub Actions: Build Music Assistant KOR APK](https://github.com/SoonCheol/MusicAssistantKOR/actions/workflows/build-kor-apk.yml)로 이동합니다.
2. 성공한 빌드를 열어 Artifacts의 MusicAssistantKOR-debug ZIP을 다운로드합니다 (GitHub 로그인 필요할 수 있음).
3. ZIP을 압축 해제하고 Android 기기에 APK를 설치합니다.

현재는 **개발용 Debug APK**이며 정식 GitHub Releases APK는 게시하지 않았습니다. 업데이트마다 같은 서명 키가 유지되도록 서명 정책을 별도로 설정해야 합니다.

## 확인된 상태

- [첫 Android Debug APK 빌드 성공](https://github.com/SoonCheol/MusicAssistantKOR/actions/runs/42880089520)
- Android 실기기에서 한국어 서버 연결 설정 화면 표시 확인
- 폴더 자동 진입, 가사 조절 및 전체 재생 동작은 실기기에서 별도 확인 필요
- 원본 음악 파일을 직접 변경하는 기능은 추가하지 않음

## 업데이트 원칙

공식 저장소의 새 소스를 받아 한국어 번역과 위 편의 기능을 병합합니다. 원본 변경에 따라 충돌 해결이나 재검증이 필요할 수 있습니다.

---

## 원본 프로젝트 안내 (English)
# Music Assistant Mobile app

*Please note: that this project is still under (heavy) development and not yet in a production state or published to any of the app stores. Development work is in progress to allow this project to become the official mobile app for the Music Assistant project.*

The (official) Music Assistant Mobile app is a cross-platform client application designed for Android and iOS. Developed using Kotlin Multiplatform (KMP) and Compose Multiplatform frameworks, this project aims to provide a unified codebase for seamless music management across mobile platforms.

## Features

*Disclaimer: This app is not intended to provide offline playback*

- All platforms:
  - managing MA players queues and playback;
  - managing dynamic and static groups (no static group creation);
  - local playback on device from MA library (Sendspin streaming protocol over WebRTC or WebSocket);
  - comprehensive Settings screen with section-based UI for server connection, authentication (builtin/OAuth), and local player configuration.
- Android-specific:
  - requires Android 9 or later; 
  - media service (background playback) and media notification in system area for quick access to players controls;
  - Android Auto support for the built-in player, plus voice playback ("Hey Google, play *X* on Music Assistant") via Google Assistant and Gemini App Actions — works in the car *and* on the phone, single symmetric code path. Full phrase reference, architecture notes and testing in [ANDROID-AUTO.md](docs/ANDROID-AUTO.md).
- iOS-specific:
  - requires iOS 18.5 or later; 
  - native audio playback via AudioQueue (CoreAudio) with support for FLAC, Opus, and PCM;
  - Lock Screen and Control Center integration (Now Playing info, play/pause/next/prev remote commands);
  - background audio playback with automatic resume after phone call or Siri interruptions;
  - WebRTC data channel transport for low-latency Sendspin streaming;
  - Apple CarPlay support for built-in player (very early state, a lot of bugs are expected);
  - *known issue:* artwork may be missing in certain setups, for example peer-to-peer mesh VPN setups such as Tailscale. We're aware of this and looking into it, but we don't have an ETA on a fix yet.
    - Why: iOS App Transport Security (ATS) blocks image requests to insecure hosts, with an exception for local networks. On your LAN, artwork loads fine, but over Tailscale and similar VPNs the server appears to the phone as a remote host, so ATS kicks in and blocks the requests.

### Server compatibility

Releases of the Music Assistant Mobile App are intended to support the latest [Music Assistant Server](https://github.com/music-assistant/server) stable versions.

### Design goals

The goal of this app is to provide an iOS and Android native feeling experience for playing and controlling audio via Music Assistant on both Android/iOS phones and tablets. This means that as well as making it easy to navigate the MA library and sources, this shouldn't come at the expense of being able to quickly control players. Specifically, these actions should be available as quickly as possible when opening the app in pretty much any state:

- See what's playing on the app's current player
- Change the current player
- Play/pause current player
- Transfer playback to another player
- Group current player with another player
- Navigate to "global" (across all sources) search
- Return to home screen
- Skip to next track
- Start browsing library
- Open settings

The app's built-in Local player is designed to be a private player for a personal music experience. It plays music on your device, for you, while you're using it. This is different from the many other players Music Assistant supports, like speakers, that stay on and available all the time. The app's Local player isn't meant to be one of those, and by default you can't find it or control it from other apps.

Compatibility with Apple CarPlay and Android Auto is a core focus, ensuring you can enjoy your music easily and safely through your car’s infotainment system. These in-car experiences are dedicated exclusively to the local player; managing external players is out of scope.

In addition, there's some general design directions that are being observed:

- Optimize for searching/filtering/drilling-down to smaller lists of items over large list navigation
- Keep customization settings as close to the place they affect over maintaining a "centralized" or "global" set of customization settings

## Want to try it?

Android users can download the APK [for the latest release](https://github.com/music-assistant/mobile-app/releases).

iOS users can join [TestFlight](https://testflight.apple.com/join/AkvSDf2z).

### Configuring and using the app

For information on configuring and using the Music Assistant Mobile app, see the [App Documentation](docs/app-documentation/index.md).

## Android Auto & Google Assistant

See [ANDROID-AUTO](docs/ANDROID-AUTO.md) for:

- enabling Android Auto with sideloaded debug/self-signed builds (Unknown sources flow);
- the library browsing layout on the head unit;
- the full list of supported Google Assistant voice phrases (works in the car *and* on the phone);
- shell-based testing of `MEDIA_PLAY_FROM_SEARCH` intents;
- known limitations.

---

[![A project from the Open Home Foundation](https://www.openhomefoundation.org/badges/ohf-project.png)](https://www.openhomefoundation.org/)
