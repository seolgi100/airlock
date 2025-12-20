# Infrastructure & Deployment Overview – Airlock

본 문서는 Airlock 채팅 웹 애플리케이션의 **서버 환경, CI/CD 파이프라인,
STUN/TURN 서버 구성, CloudWatch 로그 및 모니터링 환경**을 하나의 페이지로
정리한 인프라 정의 문서이다.  
실제 서비스 운영 환경을 기준으로 설계 및 구성되었다.

---

## Infrastructure Summary

### 서버 환경 구성
- **클라우드**: AWS EC2
- **OS**: Ubuntu
- **Public IP**: 43.202.212.164 (Elastic IP)
- **Private IP**: 172.31.7.74
- **보안 그룹**
  - SSH: TCP 22
  - STUN / TURN: UDP 3478
  - TURN (TCP fallback): TCP 3478
  - TURN over TLS: TCP 5349
  - TURN Relay Ports: UDP 49152–65535

### CI/CD 구성
- GitHub Actions 기반 자동 배포
- `main` 브랜치 push 시 자동 실행
- `workflow_dispatch`를 통한 수동 배포 지원
- Frontend(React): 빌드 후 `/var/www/html` 배포
- Backend(Spring Boot): JAR 업로드 후 `spring` systemd 서비스 재시작
- GitHub Actions Secrets
  - `EC2_SSH_KEY`
  - `EC2_USER` (`ubuntu`)
  - `EC2_PUBLIC_IP` (`43.202.212.164`)
- Workflow 파일 위치: `.github/workflows/deploy.yml`

### STUN / TURN 서버
- 서비스: coturn
- 설정 파일: `/etc/turnserver.conf`
- Realm: `43.202.212.164`
- Relay Port Range: `49152–65535`
- 보안 옵션: `fingerprint`, `no-sslv3`, `no-tlsv1`, `no-tlsv1_1`, `no-ipv6`

### 로그 및 모니터링
- CloudWatch Agent (EC2 IAM Role 기반)
- 수집 메트릭: CPU / Memory / Disk
- Backend Log Group: `/airlock/backend`
- Backend Log File: `/home/ubuntu/backend/logs/app.log`

---

## 0. Initial Server Provisioning (초기 서버 구성)

### Server Base
- **Cloud Provider**: AWS EC2
- **Operating System**: Ubuntu
- **Instance Type**: 운영 당시 사용 인스턴스 기준
- **Public IP**: Elastic IP (`43.202.212.164`)
- **Private IP**: `172.31.7.74`
- **Timezone**: Asia/Seoul

### Base Package Installation
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y nginx
sudo apt install -y openjdk-21-jdk
sudo apt install -y coturn
```

## 1. Server Environment

- **Cloud Provider**: AWS EC2
- **Operating System**: Ubuntu
- **Elastic Public IP**: EC2_PUBLIC_IP (GitHub Actions Secrets)
- **Private IP**: `172.31.7.74`

### Security Group Configuration

| Purpose | Protocol | Port | Source |
|------|--------|------|------|
| SSH | TCP | 22 | 0.0.0.0/0 |
| HTTP | TCP | 80 | 0.0.0.0/0 |
| HTTPS | TCP | 443 | 0.0.0.0/0 |
| Backend API | TCP | 8080 | 0.0.0.0/0 |
| STUN / TURN | UDP | 3478 | 0.0.0.0/0 |
| TURN (TCP fallback) | TCP | 3478 | 0.0.0.0/0 |
| TURN over TLS | TCP | 5349 | 0.0.0.0/0 |
| TURN Relay | UDP | 49152–65535 | 0.0.0.0/0 |

---

## 2. CI/CD Pipeline

- **CI/CD Tool**: GitHub Actions
- **Workflow File**: `.github/workflows/deploy.yml`

### Trigger
- `main` 브랜치 push 시 자동 실행
- `workflow_dispatch` 수동 실행 지원

### GitHub Actions Secrets
- `EC2_SSH_KEY`: EC2 접속용 SSH Private Key
- `EC2_USER`: `ubuntu`
- `EC2_PUBLIC_IP`: Elastic IP (`43.202.212.164`)

---

## 3. Deployment Configuration

### Frontend (React)

- CI 환경에서 React 애플리케이션 빌드
- 빌드 결과물 EC2 서버 업로드
- 임시 디렉토리를 거쳐 서비스 경로에 반영

#### Deployment Paths
- **Temp**: `/home/ubuntu/frontend-build-temp`
- **Live**: `/var/www/html`

```bash
sudo rm -rf /var/www/html/*
sudo cp -r /home/ubuntu/frontend-build-temp/* /var/www/html/
sudo systemctl restart nginx
```

## 4. Backend (Spring Boot)

### Build & Runtime Environment
- **Language / Runtime**: Java 21 (Temurin)
- **Build Tool**: Gradle
- **Build Options**: `-x test`
- **Deployment Artifact**: `app.jar`

### Service Management (systemd)
- **Service Name**: `spring`

```bash
sudo systemctl stop spring
sudo systemctl start spring
sudo systemctl status spring
```

## 5. STUN / TURN Server Configuration

- **Service**: coturn
- **Config File**: `/etc/turnserver.conf`
- **Realm**: `43.202.212.164`
- **Private IP**: `172.31.7.74`
- **Public IP**: `43.202.212.164`
- **Relay Port Range**: `49152–65535`

### Security Options
```conf
fingerprint
no-sslv3
no-tlsv1
no-tlsv1_1
no-ipv6
```

### Service Management
```bash
sudo systemctl status coturn
sudo systemctl restart coturn
```

## 6. Logging & Monitoring (CloudWatch)

### CloudWatch Agent
- EC2 IAM Role 기반 활성화
- 서버 메트릭 및 애플리케이션 로그 수집

### Collected Metrics
- CPU Usage
- Memory Usage
- Disk Usage

### Backend Log Configuration
- **Log Group**: `/airlock/backend`
- **Log File**: `/home/ubuntu/backend/logs/app.log`

### Log Access Path
- AWS Console → CloudWatch → Logs → `/airlock/backend`

## 7. Web Server Configuration (Nginx)

Airlock 서비스의 웹 서버는 **Nginx**를 사용하며,  
Frontend(React) 정적 리소스 서빙과 Backend(Spring Boot) Reverse Proxy 역할을 동시에 수행한다.  
또한 **HTTP → HTTPS 리다이렉트** 및 **Let’s Encrypt SSL 인증서**를 적용하여 보안을 강화하였다.

---

### Nginx Configuration Overview

- **Web Server**: Nginx
- **Config Path**: `/etc/nginx/sites-available/default`
- **Protocol**: HTTPS (HTTP/2 지원)
- **SSL Provider**: Let’s Encrypt
- **Domain**: `mychatapp.mooo.com`

---

### Purpose

- 모든 HTTP 요청을 HTTPS로 강제 리다이렉트
- React SPA 정적 파일 제공
- `/back/` 경로를 Backend(Spring Boot) API 서버로 프록시
- WebSocket 통신 지원

---

### HTTP → HTTPS Redirect Configuration

```nginx
server {
    listen 80;
    server_name _;
    return 301 https://$host$request_uri;
}

```

- 모든 HTTP 요청을 HTTPS로 **영구 리다이렉트 (301)**
- 보안 통신 강제
- 검색 엔진 최적화(SEO) 측면에서도 유리

---

### HTTPS Server Configuration (Production)

```nginx
server {
    listen 443 ssl http2;
    server_name mychatapp.mooo.com;

    ssl_certificate /etc/letsencrypt/live/mychatapp.mooo.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/mychatapp.mooo.com/privkey.pem;
}

```

- Protocol: HTTPS + HTTP/2
- SSL 인증서: Let’s Encrypt
- 서비스 도메인: `mychatapp.mooo.com`

---

### Backend Reverse Proxy (Spring Boot)

```nginx
location /back/ {
    proxy_pass http://localhost:8080/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection 'upgrade';
    proxy_cache_bypass $http_upgrade;
}

```

- `/back/` 경로 요청을 Spring Boot Backend (8080)로 전달
-  WebSocket 통신을 위한 Upgrade / Connection 헤더 설정
-  실제 클라이언트 IP 전달 (X-Forwarded-For)

### Frontend Static File Serving (React)

```nginx
location /static/ {
    root /var/www/html;
}

location / {
    root /var/www/html;
    try_files $uri /index.html;
}
```

- React 빌드 결과물 제공 경로: /var/www/html
- SPA 특성을 고려하여 존재하지 않는 경로는 index.html로 fallback
- 새로고침 시 404 오류 방지

### Nginx Service Management

```bash
sudo systemctl restart nginx
sudo systemctl status nginx
```

- 설정 변경 후 Nginx 재시작
- 서비스 상태 확인


## Architecture Overview
```text
[User Browser]
      ↓ HTTPS
   [Nginx]
   ├─ /        → React (Static)
   └─ /back/   → Spring Boot (8080)
                      ↓
                  [DB / TURN / CloudWatch]
```

# 본 문서는 Airlock 서비스의 운영 재현성과 인프라 구조 이해를 목적으로 작성되었다.