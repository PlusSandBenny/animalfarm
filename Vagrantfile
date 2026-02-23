Vagrant.configure("2") do |config|
  config.vm.define "animalfarm" do |animalfarm|
    app_branch = ENV.fetch("APP_BRANCH", "main")

    animalfarm.vm.box = "ubuntu/jammy64"
    animalfarm.vm.hostname = "animalfarm-dvm"
    animalfarm.vm.synced_folder ".", "/vagrant", disabled: true

    animalfarm.vm.network "forwarded_port", guest: 5173, host: 5173, auto_correct: true
    animalfarm.vm.network "forwarded_port", guest: 8080, host: 8080, auto_correct: true
    animalfarm.vm.network "forwarded_port", guest: 3307, host: 3307, auto_correct: true

    animalfarm.vm.provider "virtualbox" do |vb|
      vb.name = "animalfarm-dvm"
      vb.memory = 4096
      vb.cpus = 2
    end

    animalfarm.vm.provision "shell",
      inline: <<-SHELL,
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
          echo "deb [arch=${ARCH} signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu ${CODENAME} stable" > /etc/apt/sources.list.d/docker.list

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

        COMPOSE_PATH="$(find "${APP_DIR}" -maxdepth 4 -type f \\( -name 'docker-compose.yml' -o -name 'docker-compose.yaml' -o -name 'compose.yml' -o -name 'compose.yaml' \\) | head -n1)"
        if [ -z "${COMPOSE_PATH}" ]; then
          echo "No Docker Compose file found in ${APP_DIR} (branch: ${APP_BRANCH})."
          echo "Directory contents:"
          ls -la "${APP_DIR}" || true
          exit 1
        fi

        COMPOSE_DIR="$(dirname "${COMPOSE_PATH}")"
        cd "${COMPOSE_DIR}"

        ATTEMPTS=3
        for i in $(seq 1 ${ATTEMPTS}); do
          if docker compose -f "${COMPOSE_PATH}" up -d --build; then
            break
          fi
          if [ "${i}" -eq "${ATTEMPTS}" ]; then
            echo "docker compose build/deploy failed after ${ATTEMPTS} attempts."
            exit 1
          fi
          echo "docker compose failed (attempt ${i}/${ATTEMPTS}). Retrying in 20s..."
          sleep 20
        done

        echo "Animal Farm stack deployed in VM."
        echo "Branch: ${APP_BRANCH}"
        echo "Compose file: ${COMPOSE_PATH}"
        echo "Frontend: http://localhost:5173"
        echo "Backend API: http://localhost:8080/api"
      SHELL
      env: {
        "REPO_URL" => "https://github.com/PlusSandBenny/animalfarm.git",
        "APP_DIR" => "/opt/animalfarm",
        "APP_BRANCH" => app_branch
      }
  end
end
