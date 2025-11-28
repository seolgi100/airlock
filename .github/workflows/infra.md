# 서버 환경 구성 #
- 클라우드 : AWS EC2
- OS : Ubuntu
- Public IP : 43.202.212.164 (Elastic IP)
- 보안그룹 : SSH(포트: 22), STUN/TURN(포트: 3478), TURN fallback(포트: 3478),
            TURN over TLS(포트: 5349), Relay Ports(포트: 49152-65535)

# CI/CD 구성 #
- GitHub Actions 기반 자동 배포
- 동작 방식 : main 브랜치 push 시 자동 실행 (workflow_dispatch로 수동 배포도 가능)
- 프론트(React) → EC2 업로드 후 /var/www/html로 배포
- 백엔드(Spring Boot) → JAR 파일 서버 업로드 후 systemd 서비스(spring) 재시작
- Actions Secrets : EC2_SSH_KEY(SSH키), EC2_USER(ubuntu), EC2_PUBLIC_IP(43.202.212.164)
- workflow 파일 위치 : .github/workflows/deploy.yml

# STUN/TURN 서버 구성 #
- 서비스 : coturn
- 설정 파일 : /etc/turnserver.conf
- Private IP : 172.31.7.74
- Public IP : 43.202.212.164
- Realm : 43.202.212.164
- Relay Port Range : 49152–65535
- 보안 옵션 : fingerprint, no-sslv3, no-tlsv1, no-tlsv1_1, no-ipv6
- 상태 확인 : sudo systemctl status coturn
- coturn 재시작 명령 : sudo systemctl restart coturn

# CloudWatch 로그 및 모니터링 구성
- CloudWatch Agent 활성화 (EC2 IAM Role 기반)
- 수집 메트릭 : CPU 사용률, Memory 사용률, Disk 사용량
- 로그 수집 대상 : Spring Backend Log
- Log Group : /airlock/backend
- Backend 로그 파일 위치 : /home/ubuntu/backend/logs/app.log
- 로그 확인 경로 : AWS Console → CloudWatch → Logs → /airlock/backend