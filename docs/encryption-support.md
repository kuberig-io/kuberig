# Encryption Support

KubeRig uses the [Google Tink](https://github.com/google/tink) library in order to provide encryption support. 
We are currently using AeadKeyTemplates.AES256_CTR_HMAC_SHA256 keysets. 
A different keyset file is generated for every environment in the `{environment-name}.keyset.json` in the environment directory.

You should **NEVER EVER** commit the {environment-name}.keyset file. If you have used the `initEnvironment` or `initGitIgnore` task this file will already get ignored.
You should have multiple secure backups of the .keyset files without them you can't decrypt/deploy to the environment. 

> If you loose a keyset file we can't help you!

In case you use the `initEnvironment` task encryption setup is setup already and you don't need to create an encryption key manually.
The `initEnvironment` task will also make sure that your .gitignore file is updated.

If you have not used the `initEnvironment` task you can initialize the .gitignore file using the `initGitIgnore` task.

## Conventions
KubeRig provides some basic conventions to deal with sensitive information in your environment repositories.
> But even then it requires a lot of rigor to not accidentally commit sensitive information. You have been warned!

### Sensitive Configuration Values
All configuration values for environments go in the {environment-name}-configs.properties file. Sensitive values can be encrypted.

Add your sensitive property and execute the `encryptConfig` task for the environment with the `--key` parameter passing in the property key.

The value of the property will be encrypted. You can use the `decryptConfig` task to update the value back to the plain value.

> Remember to encrypt it back before you commit!

### Sensitive Files
All configuration files for environments should go in the environments/{environment-name} directory.

Add your sensitive file and prefix it with `.plain.`. If you have used the `initEnvironment` task you will notice that the file is already ignored by git.

There are 2 tasks that you can use to encrypt/decrypt files.
The `encryptFile` task that encrypts only the file that you specify with the `--file` parameter.
The `encrypt` task that encrypts all `.plain.` files for the environment.
