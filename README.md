# Compojure-api 2.0.0 microservice chassis

Forked from https://github.com/metosin/chassis.git

## API Microservice chassis

* Compojure-api & swagger 
  * https://github.com/metosin/compojure-api
* API validation (spec/schema) 
  * https://clojure.org/guides/spec
* component definition (mount) 
  * https://github.com/tolitius/mount
* metrics (metrics-clojure: forked) 
  * https://github.com/metrics-clojure/metrics-clojure
* auth (buddy: forked, waiting for async middleware support) 
  * https://github.com/funcool/buddy-auth
* env loading (omniconf) 
  * https://github.com/grammarly/omniconf
* db migrations (migratus & migratus-lein)
 * https://github.com/yogthos/migratus#quick-start-leiningen-2x
* tracing TODO


## Usage

### Run the application locally, with an nRepl and auto-reload

```
;; show help
> lein run -- --help


;; set the server port
> SERVER__PORT=3000 lein repl
> lein ring server --server-port 3000
```

### Run via repl

```
(in-ns 'user)

;; to reload
(do (refresh-all) (go))
```

### Create a migration

Due to dependency on `mount`, we run migratus tasks with a lein alias called `migrations` instead of `migratus`:

```
;; create a migration
> lein migrations create some_migration_name
> ls resources/migrations
```

We can also invoke the from the `chassis.db` namespace.

### Calling some endpoints

```
;; file upload
curl -XPOST  "http://localhost:3000/spec/file" -F file=@project.clj
```

### Testing

Testing is provided by `clojure.test`. There's also `eftest` runner, providing a task `lein eftest`.
Feel free to investigate the following resources:

* https://github.com/metosin/compojure-api/wiki/Testing-api-endpoints
* https://github.com/weavejester/eftest

### Packaging and running as standalone jar

```
lein do clean, ring uberjar
java -jar target/server.jar
```


### Running standalone (preferred method because of config validation)

```
lein uberjar
lein run --required-option qux --option-from-set bar
REQUIRED_OPTION=qux OPTION_FROM_SET=bar java -jar target/server.jar
```


### Packaging as war

`lein ring uberwar`

## License

Distributed under the Eclipse Public License, the same as Clojure.
