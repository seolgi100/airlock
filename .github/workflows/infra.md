# 서버 환경 구성 #
- 클라우드 : AWS EC2
- OS : Ubuntu
- Public IP : 15.165.2.31 (Elastic IP)
- 보안그룹 : SSH(포트: 22), STUN/TURN(포트: 3478), TURN fallback(포트: 3478),
            TURN over TLS(포트: 5349), Relay Ports(포트: 49152-65535)

# CI/CD 구성 #
- github Actions 사용
- 트리거 조건 : push시 자동 실행([front], [back]의 커밋 메세지 기준으로 분기)
- Actions Secrets : EC2_SSH_KEY(SSH키), EC2_USER(ubuntu), EC2_PUBLIC_IP(15.165.2.31)
- workflow 파일 위치 : .github/workflows/deploy.yml

# STUN/TURN 서버 구성 #
- 서비스 : coturn
- 설정 파일 : /etc/turnserver.conf
- Private IP : 172.31.1.202
- Public IP : 15.165.2.31
- Realm : 15.165.2.31
- Relay Port Range : 49152–65535
- 보안 옵션 : fingerprint, no-sslv3, no-tlsv1, no-tlsv1_1, no-ipv6
- 상태 확인 : sudo systemctl status coturn
- coturn 재시작 명령 : sudo systemctl restart coturn
