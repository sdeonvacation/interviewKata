.PHONY: dev dev-backend dev-frontend stop stop-backend stop-frontend db db-stop db-backup db-restore test build clean

MVN := $(shell command -v ./mvnw 2>/dev/null || command -v mvn)
DOCKER_HOST_VAR := unix://$(HOME)/.colima/default/docker.sock

# Start everything (DB + backend + frontend) in background.
# Runs stop first to guarantee no orphaned processes from a previous run.
dev: stop db dev-backend dev-frontend
	@echo ""
	@echo "✓ InterviewKata running:"
	@echo "  Backend:  http://localhost:5050"
	@echo "  Frontend: http://localhost:3002"
	@echo "  Auth:     Authorization: Bearer dev-token"
	@echo ""
	@echo "  Stop with: make stop"
	@echo "  Logs:      tail -f /tmp/interviewkata-backend.log"

dev-backend:
	@echo "Starting backend..."
	@$(MVN) -q -DskipTests spring-boot:run > /tmp/interviewkata-backend.log 2>&1 & echo $$! > /tmp/interviewkata-backend.pid
	@for i in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20; do \
		sleep 1; \
		if curl -sf http://localhost:5050/actuator/health > /dev/null 2>&1; then \
			echo "  Backend UP on :5050"; \
			exit 0; \
		fi; \
	done; \
	echo "  Backend FAILED - check /tmp/interviewkata-backend.log"; exit 1

dev-frontend:
	@echo "Starting frontend..."
	@cd frontend && npx vite --port 3002 > /tmp/interviewkata-frontend.log 2>&1 & echo $$! > /tmp/interviewkata-frontend.pid
	@sleep 3
	@echo "  Frontend UP on :3002 - http://localhost:3002"

stop: stop-backend stop-frontend
	@echo "Done"

# Kill by PORT (authoritative) to catch orphaned child JVMs that the build tool forks,
# then clean up any tracked PID and its process group.
stop-backend:
	@-lsof -ti tcp:5050 2>/dev/null | xargs kill -9 2>/dev/null || true
	@-kill $$(cat /tmp/interviewkata-backend.pid 2>/dev/null) 2>/dev/null || true
	@-rm -f /tmp/interviewkata-backend.pid
	@echo "  Backend stopped (:5050 clear)"

stop-frontend:
	@-lsof -ti tcp:3002 2>/dev/null | xargs kill -9 2>/dev/null || true
	@-kill $$(cat /tmp/interviewkata-frontend.pid 2>/dev/null) 2>/dev/null || true
	@-rm -f /tmp/interviewkata-frontend.pid
	@echo "  Frontend stopped (:3002 clear)"

db:
	@DOCKER_HOST=$(DOCKER_HOST_VAR) docker compose up -d 2>&1 | grep -v "^$$"

db-stop:
	@DOCKER_HOST=$(DOCKER_HOST_VAR) docker compose down

# Dump current DB content to the committed snapshot (schema + data + liquibase state).
db-backup:
	@DOCKER_HOST=$(DOCKER_HOST_VAR) docker exec interviewkata-db \
		pg_dump -U interviewkata -d interviewkata --clean --if-exists --no-owner --no-privileges \
		> db/backup/interviewkata.sql
	@echo "✓ Wrote db/backup/interviewkata.sql"

# Load the committed snapshot into the local DB (safe on empty or existing DB).
db-restore:
	@./db/restore.sh

test:
	$(MVN) test

build:
	$(MVN) -DskipTests package
	cd frontend && npm run build

clean:
	$(MVN) clean
	rm -rf frontend/dist
