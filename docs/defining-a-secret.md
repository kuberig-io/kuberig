# Defining a Secret

```kotlin
@EnvResource
fun basicSecret(): SecretDsl {
    return secret {
        metadata {
            name("basic-secret")
        }
        // use an encrypted file (environment specific)
        data("app-secrets.properties", environmentFileBytes("files/.encrypted.custom-app-secrets.properties"))
        // use an encrypted config value (environment specific)
        data("some.password", environmentConfig("some.password").toByteArray())
    }
}
```