meeno
=====

A strongly typed, immutable java-8 implementation of the Betfair NG-API for use in non-interactive (aka bot) applications.

This project is a work in progress... use at your own risk.

To get started take a look at the 'live' examples: /meeno/tree/master/test/live

Certificate generation
======================

For non-interactive access to Betfair NG-API one needs to generate a certificate.

To use API-NG you require the following:

1. A Betfair account.  you can open a Betfair account [here](https://register.betfair.com/account/registration)
2. An Application Key - you can create an application by following the instructions [here](https://api.developer.betfair.com/services/webapps/docs/display/1smk3cen4v3lu3yomq5qye0ni/Application+Keys)
3. Generate a certificate (make sure your are using provided openssl.cfg or manually update your own, see official docs):
   ```shell
   openssl genrsa -out client-2048.key 2048

   openssl req -new -config openssl.cnf -key client-2048.key -out client-2048.csr

   openssl x509 -req -days 365 -in client-2048.csr -signkey client-2048.key -out client-2048.crt -extfile openssl.cnf -extensions ssl_client

   openssl pkcs12 -export -in client-2048.crt -inkey client-2048.key -out client-2048.p12
```
4. Upload .crt file to [here](https://myaccount.betfair.com/accountdetails/mysecurity?showAPI=1).
5. For actual authentication use .p12 file (for now not sure that it's secure, check it)

For exact details please consult official manual.
