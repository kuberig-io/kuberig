# Environment Tasks

> work-in-progress

For each environment the following actions are available. Task names are {action}{Environment-name}Environment.

|action|description|
|----|-----------|
|createEncryptionKey|Generates an encryption key for the environment|
|encrypt|Will encrypt all files that have a filename prefixed with .plain. and create/overwrite the .encrypted. file for all files in the environments/{environment-name} directory.|
|encryptFile|Will encrypt the file specified via the --file option|
|encryptConfig|Will encrypt the value of the property in the environment config properties file specified via the --key option|
|decrypt|Will decrypt all files that have a filename prefixed with .encrypted. and create the .plain. decrypted file for all files in the environments/{environment-name} directory.|
|decryptFile|Will decrypt the file specified via the --file option|
|decryptConfig|Will decrypt the value of the property in the environment config properties file specified via the --key option|
|deploy|Will create/update all resources applicable for the environment|
|generateYaml|Will generate yaml files for all resources applicable for the environment in the build/generated-yaml/{environment-name} directory|