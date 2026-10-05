# MySQL REST Service

A Java 21 and Spring Boot REST API for creating, reading, updating, and deleting rows in the MySQL `t_iot` and `t_data` tables.

## Requirements

- JDK 21 or newer
- Maven 3.9 or newer
- MySQL 8 or compatible

The service uses the existing `iot26` schema by default. If it does not exist yet, create it once in MySQL:

```sql
CREATE DATABASE iot26;
```

The service maps `t_iot` and `t_data`; Hibernate updates their schemas on startup. Configure the connection with environment variables before starting the app:

```powershell
$env:MYSQL_HOST = "localhost"
$env:MYSQL_PORT = "3306"
$env:MYSQL_DATABASE = "iot26"
$env:MYSQL_USER = "your_mysql_user"
$env:MYSQL_PASSWORD = "your_mysql_password"
mvn spring-boot:run
```

The login page, web page, and API listen on `http://localhost:8081` by default. Set `SERVER_PORT` to override the port. Set `JWT_SECRET` to a private value of at least 32 bytes outside local development; the configured fallback is for development only. Set `COOKIE_SECURE=true` when using HTTPS. `MYSQL_USER` defaults to `root` and the password defaults to `root`; set both explicitly for your own database. The application does not create the database itself.

## Endpoints

| Method   | Path                      | Result                                                  |
| -------- | ------------------------- | ------------------------------------------------------- |
| `GET`    | `/api/iots`               | List all IoT devices                                    |
| `GET`    | `/api/iot?macaddress=...` | Get the IoT device matching its MAC address             |
| `GET`    | `/api/iots/{id}`          | Get one IoT device                                      |
| `POST`   | `/api/iots`               | Insert a device, returns `201 Created`                  |
| `PUT`    | `/api/iots/{id}`          | Replace the device fields                               |
| `DELETE` | `/api/iots/{id}`          | Delete a device, returns `204 No Content`               |
| `GET`    | `/api/iot/{iotId}/data`   | List readings for one IoT device                        |
| `GET`    | `/api/data`               | List all readings; optionally filter with `?iotId=1`    |
| `GET`    | `/api/data/{id}`          | Get one reading                                         |
| `POST`   | `/api/data`               | Insert a reading, returns `201 Created`                 |
| `PUT`    | `/api/data/{id}`          | Replace a reading                                       |
| `DELETE` | `/api/data/{id}`          | Delete a reading, returns `204 No Content`              |
| `POST`   | `/api/auth/register`      | Register a user with the `readonly` role                |
| `POST`   | `/api/auth/login`         | Verify credentials and return a 30-minute Bearer JWT    |
| `GET`    | `/api/auth/me`            | Return the authenticated username and role              |
| `GET`    | `/api/iot?macaddress=...` | Device-key access is limited to the configured IoT      |
| `POST`   | `/api/data`               | Device-key access can write only for its configured IoT |
| `GET`    | `/api/users`              | List users without their passwords                      |
| `GET`    | `/api/users/{id}`         | Get one user without its password                       |
| `POST`   | `/api/users`              | Create a user; returns `201 Created`                    |
| `PUT`    | `/api/users/{id}`         | Replace user fields and password                        |
| `DELETE` | `/api/users/{id}`         | Delete a user, returns `204 No Content`                 |

Create a reading for IoT device `1`:

```powershell
curl.exe -X POST http://localhost:8081/api/data `
  -H "Content-Type: application/json" `
  -d '{"idIot":1,"jsondata":"{\"temperature\":23}"}'
```

Read readings for one IoT device:

```powershell
curl.exe http://localhost:8081/api/iot/1/data
```

Create an IoT device:

```powershell
curl.exe -X POST http://localhost:8081/api/iots `
  -H "Content-Type: application/json" `
  -d '{"name":"Sensore temperatura","description":"Sensore ambiente","macaddress":"AA:BB:CC:DD:EE:FF","jsonrange":"{\"min\":0,\"max\":50}"}'
```

Read all items:

```powershell
curl.exe http://localhost:8081/api/iots
```

Find an IoT device by MAC address:

```powershell
curl.exe "http://localhost:8081/api/iot?macaddress=12:23:34:45"
```

Update device `1`:

```powershell
curl.exe -X PUT http://localhost:8081/api/iots/1 `
  -H "Content-Type: application/json" `
  -d '{"name":"Sensore temperatura","description":"Sensore aggiornato","macaddress":"AA:BB:CC:DD:EE:FF","jsonrange":"{\"min\":-10,\"max\":60}"}'
```

Delete item `1`:

```powershell
curl.exe -X DELETE http://localhost:8081/api/iots/1
```

The JSON fields are `name`, `description`, `macaddress`, and `jsonrange`; `id` and `daterev` are returned by the API, and `daterev` is refreshed on create/update. Unknown IDs return `404 Not Found`; missing required fields or values exceeding the MySQL column lengths return `400 Bad Request`.

## Arduino Device

All Arduino boards send the shared `X-Device-Key` to `GET /api/iot` and `POST /api/data`. Configure the same key used by the sketches as `DEVICE_API_KEY` in the PowerShell session before starting Spring:

```powershell
$env:DEVICE_API_KEY = "same-random-key-as-the-Arduino-sketch"
mvn spring-boot:run
```

The server accepts this key only on those two routes. Each Arduino can request configuration by MAC and write readings for any existing IoT ID; a shared key does not isolate one board from another. If the key is absent, the device-key routes are disabled. The sketch currently uses plain HTTP, so this key is visible on the local network; use HTTPS before exposing the service outside a trusted LAN.

## Users

User passwords are stored as BCrypt hashes and are never included in API responses. Existing `t_user` tables must have a 60-character password column before starting the app:

```sql
ALTER TABLE t_user MODIFY COLUMN password VARCHAR(60) NOT NULL;
```

Legacy plaintext passwords are converted to BCrypt on the first successful login; the existing password column must be expanded first. Public registration is available only at `/api/auth/register`, and the server always assigns the `readonly` role. All other `/api/**` endpoints require a valid Bearer token. `readonly` users may use GET endpoints; only `admin` users may write data or manage users. The user CRUD endpoints are admin-only.

Register at `http://localhost:8081/login.html`, or create an account through the API:

```powershell
$registration = @{ firstname = "Ada"; lastname = "Lovelace"; username = "ada"; password = "change-me-123"; email = "ada@example.com" } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/auth/register -ContentType "application/json" -Body $registration

$credentials = @{ username = "ada"; password = "change-me-123" } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/auth/login -ContentType "application/json" -Body $credentials
$headers = @{ Authorization = "Bearer $($login.accessToken)" }
Invoke-RestMethod -Uri http://localhost:8081/api/data -Headers $headers
```

The token expires after 30 minutes. Login sets a `SameSite=Strict`, HttpOnly cookie to authorize requests for `/` and `/index.html`; the browser also keeps the JWT in session storage and sends it in the `Authorization` header for API calls. The page and API are both protected server-side. Add a unique constraint to `t_user.username` if duplicate usernames already exist or concurrent registration must be prevented at the database level.

## Verify

```powershell
mvn test
```
