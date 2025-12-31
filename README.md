# ☁️ Private Family Cloud (Raspberry Pi Edition)
라즈베리파이와 외부 USB 저장소를 활용하여 가족만의 안전한 개인용 클라우드 공간을 만드는 프로젝트입니다. Docker를 기반으로 설계되어 설치가 간편하며, 저사양 기기에서도 안정적으로 동작합니다.

## ✨ 주요 기능
**외부 저장소 마운트 가능**: SD 카드의 용량 제한 없이 USB/외장 하드를 메인 저장소로 사용

**파일 관리**: 가족 구성원별 독립된 공간 제공 및 파일 업로드/다운로드

**자동 복구**: 시스템 재부팅 시 Docker와 외부 저장소가 자동으로 동기화 및 실행

## 🚀 시작하기 (Quick Start)
1. 전제 조건 (Prerequisites)
OS: Raspberry Pi OS (또는 Linux 계열)

Tools: Docker, Docker-compose

Storage: 외부 USB 드라이브 (EXT4 또는 NTFS 권장)

2. 설치 및 실행 (Installation)
먼저 저장소를 클론합니다.

```Bash
git clone https://github.com/Sharon77770/private-cloud.git
cd private-cloud
```

3. USB 드라이브 설정
외부 저장소를 특정 경로에 마운트합니다. (예: /mnt/familycloud)
```Bash
# UUID 확인 후 /etc/fstab에 등록하여 자동 마운트 설정 권장
sudo mount /dev/sda1 /mnt/familycloud
```

4. 환경 변수 설정
application.yml 또는 docker-compose.yml에서 파일 업로드 용량 및 저장 경로를 설정합니다.

```YAML
spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
```

5. 서비스 가동
```Bash
sudo docker-compose up -d --build
```

## 📂 프로젝트 구조 (Architecture)
Cloud App: Spring Boot 기반의 메인 서버 (Port: 59273)

Database: MariaDB (데이터 및 계정 정보 보관)

Discord Bots: 독립된 컨테이너로 실행되는 관리용 봇

## 🔧 주요 설정 가이드 (Troubleshooting)
모바일 업로드 에러 해결
모바일에서 고화질 사진 업로드 시 413 에러가 발생한다면 max-file-size를 늘린 후 아래 명령어로 재빌드하세요.

```Bash
sudo docker-compose up -d --build
```

## 저장 경로 확인
실제 파일이 USB에 저장되고 있는지 확인하려면 다음 명령어를 사용합니다.

```Bash
docker exec -it family-cloud-app df -h /app/storage
```

## 🤝 기여하기 (Contributing)
이 프로젝트는 오픈소스입니다. 개선 사항이 있다면 언제든지 Issue를 생성하거나 Pull Request를 보내주세요!

📜 라이선스 (License)
MIT License - 상세 내용은 LICENSE 파일을 확인하세요.
