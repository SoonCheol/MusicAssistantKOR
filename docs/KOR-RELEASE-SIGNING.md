# Music Assistant KOR: 고정 서명 및 GitHub Releases 설정

이 문서는 저장소 관리자가 **한 번만** 설정하는 Android 릴리스 서명 절차입니다. 키를 Git 저장소에 직접 올리지 마세요.

## 1. 로컬 Windows PC에서 전용 키 생성

PowerShell에서 JDK의 `keytool`을 실행합니다. 개인용 비밀키 경로는 Git 저장소 밖에 두세요.

```powershell
keytool -genkeypair -v -keystore "$env:USERPROFILE\MusicAssistantKOR-release.jks" -storetype PKCS12 -alias musicassistantkor -keyalg RSA -keysize 3072 -validity 10000
```

실행 중 입력한 비밀번호와 생성된 JKS 파일은 **안전하게 별도 백업**하세요. 이 파일을 잃어버리면 같은 서명으로 앱 업데이트를 배포할 수 없습니다.

## 2. GitHub 저장소에 Actions Secrets 등록

`Settings → Secrets and variables → Actions → New repository secret`에 아래 네 항목을 등록합니다.

| 이름 | 값 |
|---|---|
| `MAK_KEYSTORE_BASE64` | JKS 파일의 Base64 인코딩 문자열 |
| `MAK_KEYSTORE_PASSWORD` | 키 저장소 비밀번호 |
| `MAK_KEY_ALIAS` | `musicassistantkor` (키 생성 시 지정한 alias) |
| `MAK_KEY_PASSWORD` | 키 비밀번호 (동일하게 설정한 경우 저장소 비밀번호와 같음) |

Base64 생성 예시(PowerShell, 화면에 출력하지 말고 필요한 곳에만 붙여 넣기):

```powershell
$bytes = [System.IO.File]::ReadAllBytes("$env:USERPROFILE\MusicAssistantKOR-release.jks")
[Convert]::ToBase64String($bytes) | Set-Clipboard
Remove-Variable bytes
```

GitHub Secrets에 넣고 클립보드를 지우세요. 비밀번호나 Base64 데이터를 채팅/이슈/README에 남기지 마세요.

## 3. 빌드 및 배포

`kor` 브랜치에서 앱 코드가 변경되면 `Build Music Assistant KOR APK` 워크플로가 실행됩니다. [Actions에서 수동 실행](https://github.com/SoonCheol/MusicAssistantKOR/actions/workflows/build-kor-apk.yml)도 가능합니다.

- 각 실행은 `versionCode=18+GitHub Actions 실행 번호`를 부여합니다.
- 버전 파일명 예시: `MusicAssistantKOR-0.17.0+25-arm64.apk`.
- 네 Secrets가 모두 설정되면 Android arm64용 서명 릴리스 APK를 빌드한 후 [Releases](https://github.com/SoonCheol/MusicAssistantKOR/releases)에 게시합니다.
- Secrets가 부족하면 디버그 APK만 Artifacts에 저장되며 Releases는 게시하지 않습니다.
- 처음 릴리스 전에 로그에서 실제 서명/빌드 성공을 확인하세요. 설치 및 기존 버전 업데이트는 별도 실기기 검증이 필요합니다.

**주의:** 서명이 서로 다른 기존 APK는 업데이트 설치할 수 없습니다. 현재 설치된 앱을 먼저 삭제하지 말고, 설정 백업 가능 여부를 확인하세요.
