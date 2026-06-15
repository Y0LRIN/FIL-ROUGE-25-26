#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

if [[ "${1:-}" == "stop" ]]; then
  echo "Stopping backend and frontend..."
  [[ -f "$ROOT/backend.pid" ]] && kill "$(cat "$ROOT/backend.pid")" 2>/dev/null || true
  [[ -f "$ROOT/frontend.pid" ]] && kill "$(cat "$ROOT/frontend.pid")" 2>/dev/null || true  kill_port 8080  rm -f "$ROOT/backend.pid" "$ROOT/frontend.pid"
  echo "Stopped."
  exit 0
fi

CLASSES_DIR="$ROOT/backend/classes"
mkdir -p "$CLASSES_DIR"

echo "Compiling backend Java sources..."
find backend -name '*.java' > /tmp/filrouge-java-sources.txt
javac -d "$CLASSES_DIR" -cp backend/lib/sqlite-jdbc.jar $(cat /tmp/filrouge-java-sources.txt)
rm -f /tmp/filrouge-java-sources.txt

DB_FILE="$ROOT/filrouge.db"
if [[ ! -f "$DB_FILE" ]]; then
  touch "$DB_FILE"
fi

check_port() {
  local port=$1
  if command -v lsof >/dev/null 2>&1; then
    if lsof -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
      return 0
    fi
  elif command -v ss >/dev/null 2>&1; then
    if ss -ltn "sport = :$port" | grep -q LISTEN; then
      return 0
    fi
  fi
  return 1
}

kill_port() {
  local port=$1
  if ! check_port "$port"; then
    return
  fi
  if command -v lsof >/dev/null 2>&1; then
    lsof -tiTCP:"$port" -sTCP:LISTEN | xargs -r kill 2>/dev/null || true
  elif command -v ss >/dev/null 2>&1; then
    ss -ltnp "sport = :$port" 2>/dev/null | awk '/LISTEN/ {print $NF}' | sed 's#/.*##' | xargs -r kill 2>/dev/null || true
  fi
}

if check_port 8080; then
  echo "ERROR: Port 8080 is already in use. Stop the process or choose a different port."
  exit 1
fi

FRONTEND_PORT=0
for port in {5500..5510}; do
  if ! check_port "$port"; then
    FRONTEND_PORT=$port
    break
  fi
done
if [[ "$FRONTEND_PORT" -eq 0 ]]; then
  echo "ERROR: Aucun port libre trouvé entre 5500 et 5510. Arrêt."
  exit 1
fi

echo "Starting backend on http://localhost:8080 ..."
nohup java -cp "$CLASSES_DIR:backend/lib/sqlite-jdbc.jar" Main > "$ROOT/backend.log" 2>&1 &
BACKEND_PID=$!
echo "$BACKEND_PID" > "$ROOT/backend.pid"

printf "Waiting for backend to become available..."
for i in {1..20}; do
  if curl -s http://127.0.0.1:8080/api/properties >/dev/null 2>&1; then
    echo " ok"
    break
  fi
  printf "."
  sleep 0.5
  if [[ $i -eq 20 ]]; then
    echo
    echo "ERROR: automation failed waiting for backend."
    exit 1
  fi
done

cat > "$ROOT/populate_test_data.py" <<'PY'
import hashlib
import os
import sqlite3

root = os.getenv('PWD')
db_path = os.path.join(root, 'filrouge.db')
conn = sqlite3.connect(db_path)
cur = conn.cursor()
cur.execute('PRAGMA foreign_keys = ON')

# Helpers

def sha256(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()

def stored_password(password):
    client_hash = sha256(password)
    salt = hashlib.sha256((password + 'seed').encode('utf-8')).hexdigest()[:32]
    return f"{salt}${sha256(salt + client_hash)}"

# Agents
admin_email = 'admin@ymmo.com'
if not cur.execute('SELECT id FROM agents WHERE email = ?', (admin_email,)).fetchone():
    cur.execute(
        'INSERT INTO agents (name, email, phone, is_admin, password_hash, created_at) VALUES (?, ?, ?, ?, ?, ?)',
        ('Admin Ymmo', admin_email, '0600000000', 1, stored_password('admin123'), '2026-06-15')
    )

agent_email = 'agent1@ymmo.com'
if not cur.execute('SELECT id FROM agents WHERE email = ?', (agent_email,)).fetchone():
    cur.execute(
        'INSERT INTO agents (name, email, phone, is_admin, password_hash, created_at) VALUES (?, ?, ?, ?, ?, ?)',
        ('Agent One', agent_email, '0611111111', 0, stored_password('agent123'), '2026-06-15')
    )

# Addresses
address_id = cur.execute('SELECT id FROM addresses WHERE street = ? AND city = ?', ('10 Rue Principale', 'Paris')).fetchone()
if address_id is None:
    cur.execute(
        'INSERT INTO addresses (street, city, postal_code, country) VALUES (?, ?, ?, ?)',
        ('10 Rue Principale', 'Paris', '75001', 'France')
    )
    address_id = cur.lastrowid
else:
    address_id = address_id[0]

# Properties
agent1_id = cur.execute('SELECT id FROM agents WHERE email = ?', (agent_email,)).fetchone()[0]
property_exists = cur.execute('SELECT id FROM properties WHERE title = ?', ('Maison test Ymmo',)).fetchone()
if not property_exists:
    cur.execute(
        'INSERT INTO properties (title, description, price, surface, rooms, type, status, agent_id, address_id, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
        ('Maison test Ymmo', 'Belle maison de test dans le coeur de Paris.', 450000, 120.0, 5, 'HOUSE', 'AVAILABLE', agent1_id, address_id, '2026-06-15')
    )

# Client
client_email = 'client@test.local'
if not cur.execute('SELECT id FROM clients WHERE email = ?', (client_email,)).fetchone():
    cur.execute(
        'INSERT INTO clients (first_name, last_name, email, phone, type) VALUES (?, ?, ?, ?, ?)',
        ('Client', 'Testeur', client_email, '0622222222', 'BUYER')
    )

conn.commit()
conn.close()
PY
python3 "$ROOT/populate_test_data.py"
rm -f "$ROOT/populate_test_data.py"

cd "$ROOT/frontend"
echo "Starting frontend on http://localhost:$FRONTEND_PORT ..."
nohup python3 -m http.server "$FRONTEND_PORT" > "$ROOT/frontend.log" 2>&1 &
FRONTEND_PID=$!
echo "$FRONTEND_PID" > "$ROOT/frontend.pid"

echo "Start complete."
echo "Backend: http://localhost:8080"
echo "Frontend: http://localhost:$FRONTEND_PORT"
echo "Login admin with admin@ymmo.com / admin123"
echo "Login agent with agent1@ymmo.com / agent123"
echo "To stop the services, run: $ROOT/start-all.sh stop"
