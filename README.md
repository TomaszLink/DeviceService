# Device Service

## Technologie

Projekt został zaimplementowany z wykorzystaniem następujących technologii:

* Java 17
* Gradle 9.5.1
* Spring Boot 4.1.0
* Openapi
* Spring Security
* JWT
* RabbitMQ
* PostgreSQL
* JPA/Hibernate
* Docker / Docker Compose

## Wymagania wstępne

Do zbudowania i uruchomienia projektu lokalnie wymagane są:

* JDK 17 (do zbudowania aplikacji),
* Docker oraz Docker Compose (do uruchomienia środowiska aplikacji).

Projekt zawiera Gradle Wrapper (`./gradlew`), więc lokalna instalacja Gradle nie jest konieczna.

## Architektura rozwiązania

Aplikacja służy do rejestracji urządzeń mobilnych oraz obsługi przesyłanych przez nie danych lokalizacyjnych GPS.

Część odpowiedzialna za zarządzanie urządzeniami została zaimplementowana w klasycznym podejściu CRUD z wykorzystaniem Spring Boot, JPA oraz Hibernate. Umożliwia ona rejestrację nowych urządzeń, pobieranie listy urządzeń, pobieranie szczegółów pojedynczego urządzenia oraz modyfikację danych urządzenia.

W celu zapewnienia wysokiej wydajności przy dużej liczbie żądań zawierających dane GPS zastosowano architekturę asynchroniczną. Po otrzymaniu żądania z lokalizacją system najpierw waliduje, czy urządzenie istnieje, a następnie publikuje komunikat do RabbitMQ. Dzięki temu zapis danych lokalizacyjnych może być obsługiwany asynchronicznie, co ogranicza ryzyko przeciążenia bazy danych w przypadku jednoczesnego wysyłania lokalizacji przez dużą liczbę urządzeń.

Takie podejście pozwala oddzielić przyjęcie żądania HTTP od faktycznego zapisu danych w bazie. Endpoint przyjmujący lokalizację może szybko zwrócić odpowiedź 202 Accepted, a dalsze przetwarzanie odbywa się po stronie konsumenta wiadomości.

## Struktura bazy danych

W bazie danych utworzono trzy główne tabele:

* devices - przechowuje dane zarejestrowanych urządzeń,
* device_locations - przechowuje pełną historię lokalizacji przesłanych przez urządzenia,
* device_last_locations - tabela pomocnicza przechowująca ostatnią znaną lokalizację urządzenia.

Tabela device_last_locations została wprowadzona w celu optymalizacji odczytu aktualnej lokalizacji urządzenia. Dzięki niej system może zwrócić ostatnią pozycję bez konieczności każdorazowego wyszukiwania najnowszego rekordu w tabeli historycznej device_locations.

Schemat bazy danych jest tworzony jednorazowo przez skrypt docker/init/init.sql, montowany do kontenera PostgreSQL jako skrypt inicjalizujący (docker-entrypoint-initdb.d). Skrypt ten wykonuje się tylko przy pierwszym starcie kontenera na pustym wolumenie - nie jest to narzędzie migracji (np. Flyway czy Liquibase), więc zmiana schematu na już istniejącej bazie wymaga ręcznej interwencji lub usunięcia wolumenu postgres_data i ponownego uruchomienia środowiska.

## Bezpieczeństwo i autoryzacja

Aplikacja wykorzystuje mechanizm autoryzacji oparty o token JWT. Token przekazywany w żądaniu zawiera informacje identyfikujące użytkownika lub urządzenie oraz listę przysługujących mu uprawnień.

Dostęp do poszczególnych endpointów jest zabezpieczony na poziomie kontrolerów za pomocą adnotacji @PreAuthorizeHasPrivilege. Oznacza to, że wykonanie danej operacji jest możliwe tylko wtedy, gdy token JWT zawiera wymagane uprawnienie.

Przykładowe uprawnienia wykorzystywane w systemie:

* DEVICE_READ - pozwala na pobieranie listy urządzeń oraz szczegółów pojedynczego urządzenia,
* DEVICE_CREATE - pozwala na rejestrację nowego urządzenia,
* DEVICE_UPDATE - pozwala na modyfikację danych urządzenia,
* DEVICE_LOCATION_SEND - pozwala na wysyłanie lokalizacji urządzenia,
* DEVICE_LOCATION_READ - pozwala na odczyt historii lokalizacji oraz ostatniej lokalizacji urządzenia.

W przypadku wysyłania lokalizacji urządzenia identyfikator urządzenia nie jest przekazywany bezpośrednio w adresie endpointu. Jest on pobierany z kontekstu bezpieczeństwa na podstawie danych zawartych w tokenie JWT. Dzięki temu urządzenie może wysyłać lokalizację wyłącznie we własnym imieniu, a system ogranicza ryzyko podszycia się pod inne urządzenie.

## Sposób uruchomienia

Z poziomu katalogu projektu należy wykonać następujące polecenia:

./gradlew clean build
docker compose up -d --build

Pierwsze polecenie buduje aplikację i tworzy plik .jar. Drugie polecenie uruchamia środowisko aplikacji przy użyciu Docker Compose.

Zgodnie z konfiguracją znajdującą się w pliku docker-compose.yml, uruchamiane są następujące kontenery:

* kontener aplikacji Device Service,
* kontener bazy danych PostgreSQL,
* kontener brokera wiadomości RabbitMQ.

Po uruchomieniu kontenerów aplikacja jest gotowa do obsługi żądań HTTP oraz asynchronicznego przetwarzania komunikatów z lokalizacjami urządzeń.

Dokumentacja API dostępna jest pod adresem http://localhost:8080/swagger-ui.html. Dostęp do Swagger UI jest zabezpieczony osobnym mechanizmem uwierzytelniania (Basic Auth), niezależnym od JWT używanego przez właściwe API - dane logowania: swagger / swagger.

## Testy jednostkowe

Testy jednostkowe (Spock) można uruchomić poleceniem:

./gradlew test

Raport z wynikami testów zostanie wygenerowany w build/reports/tests/test/index.html, a raport pokrycia kodu (Jacoco) w build/reports/jacoco/test/html/index.html.

## Testowanie
W katalogu manual_testing umieszczono pliki pomocnicze ułatwiające ręczne testowanie aplikacji.

Katalog zawiera gotowe tokeny JWT podpisane właściwym kluczem prywatnym wykorzystywanym przez aplikację. Tokeny zostały przygotowane wyłącznie na potrzeby testów manualnych i nie posiadają terminu ważności. W środowisku produkcyjnym tokeny JWT powinny zawsze posiadać określony czas ważności, na przykład za pomocą claimu exp.

Dostępne tokeny testowe:

* admin - token posiadający wszystkie uprawnienia administracyjne z wyjątkiem DEVICE_LOCATION_SEND. Reprezentuje użytkownika administracyjnego, który może zarządzać urządzeniami oraz odczytywać dane lokalizacyjne, ale nie może wysyłać lokalizacji jako urządzenie.
* user with device - token posiadający wyłącznie uprawnienie DEVICE_LOCATION_SEND. Reprezentuje użytkownika lub urządzenie, które może jedynie wysłać własną lokalizację do systemu.

Takie rozdzielenie uprawnień pozwala przetestować zarówno scenariusze administracyjne, jak i scenariusz wysyłania lokalizacji przez urządzenie.

W katalogu manual_testing znajduje się również kolekcja Postmana: DeviceService.postman_collection.json.

Kolekcja zawiera przygotowane requesty umożliwiające przetestowanie najważniejszych endpointów aplikacji, w tym rejestracji urządzeń, pobierania danych urządzeń, modyfikacji urządzeń, wysyłania lokalizacji oraz pobierania historii i ostatniej lokalizacji urządzenia.
