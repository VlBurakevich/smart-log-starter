# Smart log starter

Spring boot starter для асинхронной пакетной отправки логов во внешние системы.

Поднимается на уровне ApplicationContextInitializer

### Параметры:

    boolean enabled = true;
    String apiKey;
    String serverUrl;
    String applicationName;
    int scheduledDelay = 5;
    int batchSize = 50;
    int shutdownTimeoutSec = 20;
    int maxStackTraceLines = 5;

### Формат сетевого взаимодействия:

Стартер отправляет данные на указанный `serferUrl` методом POST

##### Заголовки
- `Content-Type` - `application/json`
- `X-Api-Key` - `...ваш ключ`
- `Service-Name` - `...имя вашего приложения(микросервиса)`

##### Тело запроса
Json-массив объектов размер определяется `batchSize`

```json
[
  {
    "level": "Error",
    "message": "Connection timeout\n\tat com.example.DbService.connect(DbService.java:23)...",
    "loggerName": "com.example.DbService",
    "timestamp": 1706713200000
  }
]
```
    
### Зависимости:

- spring-boot-starter
- spring-boot-autoconfigure
- spring-boot-starter-json
- spring-web
- spring-boot-configuration-processor (optional = true)

### Использование:

1. добавить в pom.xml:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

2. добавить зависимость
```xml
<dependency>
    <groupId>com.github.VlBurakevich</groupId>
    <artifactId>smart-log-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```