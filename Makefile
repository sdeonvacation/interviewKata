.PHONY: dev dev-backend dev-frontend stop db db-stop test build clean

GRADLE := $(shell command -v ./gradlew 2>/dev/null || command -v gradle)
DOCKER_HOST_VAR := unix://$(HOME)/.colima/default/docker.sock

# Start everything (DB + backend + frontend) in background
dev: db dev-backend dev-frontend
	@echo ""
	@echo "✓ InterviewKata running:"
	@echo "  Backend:  http://localhost:5050"
	@echo "  Frontend: http://localhost:3002"
	@echo "  Auth:     Authorization: Bearer dev-token"
	@echo ""
	@echo "  Stop with: make stop"
	@echo "  Logs:      tail -f /tmp/interviewkata-backend.log"

dev-backend: stop-backend
	@echo "Starting backend..."
	@$(GRADLE) bootRun > /tmp/interviewkata-backend.log 2>&1 & echo $$! > /tmp/interviewkata-backend.pid
	@for i in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15; do \
		sleep 1; \
		if curl -sf http://localhost:5050/actuator/health > /dev/null 2>&1; then \
			echo "  Backend UP (pid $$(cat /tmp/interviewkata-backend.pid))"; \
			exit 0; \
		fi; \
	done; \
	echo "  Backend FAILED - check /tmp/interviewkata-backend.log"; exit 1

dev-frontend: stop-frontend
	@echo "Starting frontend..."
	@cd frontend && npx vite --port 3002 > /tmp/interviewkata-frontend.log 2>&1 & echo $$! > /tmp/interviewkata-frontend.pid
	@sleep 3
	@echo "  Frontend UP (pid $$(cat /tmp/interviewkata-frontend.pid)) - http://localhost:3002"

stop: stop-backend stop-frontend
	@echo "Done"

stop-backend:
	@-kill $$(cat /tmp/interviewkata-backend.pid 2>/dev/null) 2>/dev/null && echo "  Backend stopped" || true
	@-rm -f /tmp/interviewkata-backend.pid

stop-frontend:
	@-kill $$(cat /tmp/interviewkata-frontend.pid 2>/dev/null) 2>/dev/null && echo "  Frontend stopped" || true
	@-rm -f /tmp/interviewkata-frontend.pid

db:
	@DOCKER_HOST=$(DOCKER_HOST_VAR) docker compose up -d 2>&1 | grep -v "^$$"

db-stop:
	@DOCKER_HOST=$(DOCKER_HOST_VAR) docker compose down

test:
	$(GRADLE) test

build:
	$(GRADLE) bootJar
	cd frontend && npm run build

clean:
	$(GRADLE) clean
	rm -rf frontend/dist
