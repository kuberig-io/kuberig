# Service Account Setup

> work-in-progress

Create a service account and grant the edit role: 

```shell
$ kubectl create sa kuberig --namespace=default
$ kubectl create rolebinding kuberig-edit --clusterrole=edit --serviceaccount=default:kuberig --namespace=default
```

Retrieve the access token:
```shell
$ kubectl describe sa kuberig --namespace=default
$ kubectl describe secret <name-of-token-secret>
```

Copy the token in environments/{environment-name}/.plain.{environment-name}.access-token

Run createEncryptionKey{Environment-name}Environment if you have not done so already. 

Run encrypt{Environment-name}Environment.

You are ready to run deploy{Environment-name}Environment