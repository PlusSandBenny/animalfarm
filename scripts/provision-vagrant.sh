#!/usr/bin/env bash
set -euo pipefail

export DEBIAN_FRONTEND=noninteractive
REPO_URL="${REPO_URL:-https://github.com/PlusSandBenny/animalfarm.git}"
APP_DIR="${APP_DIR:-/opt/animalfarm}"
APP_BRANCH="${APP_BRANCH:-dev}"

apt-get update -y
apt-get install -y ca-certificates curl gnupg lsb-release git

if ! command -v docker >/dev/null 2>&1; then
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  chmod a+r /etc/apt/keyrings/docker.gpg

  ARCH="$(dpkg --print-architecture)"
  CODENAME="$(. /etc/os-release && echo "$VERSION_CODENAME")"
  echo \
    "deb [arch=${ARCH} signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu ${CODENAME} stable" \
    > /etc/apt/sources.list.d/docker.list

  apt-get update -y
  apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
fi

systemctl enable docker
systemctl start docker
usermod -aG docker vagrant || true

if [ -d "${APP_DIR}/.git" ]; then
  git -C "${APP_DIR}" fetch --all --prune
  if git -C "${APP_DIR}" show-ref --verify --quiet "refs/heads/${APP_BRANCH}"; then
    git -C "${APP_DIR}" checkout "${APP_BRANCH}"
  else
    git -C "${APP_DIR}" checkout -b "${APP_BRANCH}" --track "origin/${APP_BRANCH}"
  fi
  git -C "${APP_DIR}" pull --ff-only origin "${APP_BRANCH}"
else
  rm -rf "${APP_DIR}"
  git clone --branch "${APP_BRANCH}" --single-branch "${REPO_URL}" "${APP_DIR}"
fi

cd "${APP_DIR}"
docker compose up -d --build

echo "Animal Farm stack deployed in VM."
echo "Branch: ${APP_BRANCH}"
echo "Frontend: http://localhost:5173"
echo "Backend API: http://localhost:8080/api"
