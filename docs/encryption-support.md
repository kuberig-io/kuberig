# Encryption Support

> work-in-progress

KubeRig uses the [Google Tink](https://github.com/google/tink) library in order to provide encryption support. 

If you prefix all sensitive filenames with `.plain.` and configure your .gitignore file properly by using the `initGitIgnore` task
you can prevent yourself from committing sensitive information. But even then it requires a lot of rigor to not accidentally commit sensitive information. You have been warned!

So before proceeding please run the `initGitIgnore` task and remember to prefix files that contain sensitive information with the `.plain.` prefix! 

You create an encryption key for every environment by using the `createEncryptionKey{Environment-name}Environment` task. We currently generate a AeadKeyTemplates.AES256_CTR_HMAC_SHA256 keyset.

This will create a {environment-name}.keyset file in the `environment` directory. You should **NEVER EVER** commit the {environment-name}.keyset file. If you have used the `initGitIgnore` task this file will already get ignored.

You should have multiple secure backups of the .keyset files without them you can't decrypt/deploy to the environment. If you loose a .keyset file we can't help you.

When you run the `encrypt{Environment-name}Environment` task all files that have the `.plain.` prefix will be encrypted. A file prefixed with `.encrypted.` will be created and the `.plain.` file will be removed.

The `decrypt{Environment-name}Environment` works in the other direction. The only difference is that it will not delete the files with the `.encrypted.` prefix. 

