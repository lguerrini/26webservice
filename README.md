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

The web page and API listen on `http://localhost:8088` by default. The page displays records from `/api/data`; filter by device ID or refresh the list from the controls. Set `SERVER_PORT` to override the port. `MYSQL_USER` defaults to `root` and the password defaults to empty for local development; set both explicitly for your own database. The application does not create the database itself.

## Endpoints

| Method   | Path                      | Result                                               |
| -------- | ------------------------- | ---------------------------------------------------- |
| `GET`    | `/api/iots`               | List all IoT devices                                 |
| `GET`    | `/api/iot?macaddress=...` | Get the IoT device matching its MAC address          |
| `GET`    | `/api/iots/{id}`          | Get one IoT device                                   |
| `POST`   | `/api/iots`               | Insert a device, returns `201 Created`               |
| `PUT`    | `/api/iots/{id}`          | Replace the device fields                            |
| `DELETE` | `/api/iots/{id}`          | Delete a device, returns `204 No Content`            |
| `GET`    | `/api/iot/{iotId}/data`   | List readings for one IoT device                     |
| `GET`    | `/api/data`               | List all readings; optionally filter with `?iotId=1` |
| `GET`    | `/api/data/{id}`          | Get one reading                                      |
| `POST`   | `/api/data`               | Insert a reading, returns `201 Created`              |
| `PUT`    | `/api/data/{id}`          | Replace a reading                                    |
| `DELETE` | `/api/data/{id}`          | Delete a reading, returns `204 No Content`           |

Create a reading for IoT device `1`:

```powershell
curl.exe -X POST http://localhost:8088/api/data `
  -H "Content-Type: application/json" `
  -d '{"idIot":1,"jsondata":"{\"temperature\":23}"}'
```

Read readings for one IoT device:

```powershell
curl.exe http://localhost:8088/api/iot/1/data
```

Create an IoT device:

```powershell
curl.exe -X POST http://localhost:8088/api/iots `
  -H "Content-Type: application/json" `
  -d '{"name":"Sensore temperatura","description":"Sensore ambiente","macaddress":"AA:BB:CC:DD:EE:FF","jsonrange":"{\"min\":0,\"max\":50}"}'
```

Read all items:

```powershell
curl.exe http://localhost:8088/api/iots
```

Find an IoT device by MAC address:

```powershell
curl.exe "http://localhost:8088/api/iot?macaddress=12:23:34:45"
```

Update device `1`:

```powershell
curl.exe -X PUT http://localhost:8088/api/iots/1 `
  -H "Content-Type: application/json" `
  -d '{"name":"Sensore temperatura","description":"Sensore aggiornato","macaddress":"AA:BB:CC:DD:EE:FF","jsonrange":"{\"min\":-10,\"max\":60}"}'
```

Delete item `1`:

```powershell
curl.exe -X DELETE http://localhost:8088/api/iots/1
```

The JSON fields are `name`, `description`, `macaddress`, and `jsonrange`; `id` and `daterev` are returned by the API, and `daterev` is refreshed on create/update. Unknown IDs return `404 Not Found`; missing required fields or values exceeding the MySQL column lengths return `400 Bad Request`.

## Verify

```powershell
mvn test
```
