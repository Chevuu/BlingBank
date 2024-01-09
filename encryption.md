Encryption - Decryption flow
1) Server startup: generate keypair (rsa) of server if they don't already exist and store them
2) Client account creation:
- client generates keypair (rsa), username and password and sends then the username, password and its public key to the server. The public key gets encrypted using the already exchanged symmetric key.
- the server stores the username and password, and also the users public key and responds by sending its own public key to the user. The public key gets encrypted using the already exchanged symmetric key for the transfer.
3) User login: the user logs in and encrypts its login data using the server's public key and then sends the encrypted data to the server.
- The server decrypts the login data of the user with its own private key.
- Then, the server generates a session key, encrypts the session key with the client's public key and sends the encrypted key to the client.
- The client receives the encrypted session key and decrypts it and stores it for later use.
4) Data transfer:
    - The data gets encrypted and decrypted with the exchanged session key.
    - The initialization vector that is used with the session key for encrypting the data gets encrypted with the receiver's public key. Then the encrypted IV gets send alongside the encrypted data.
    - The receiver first decrypts the IV with its own private key and then uses the session key and the IV to decrypt the data.