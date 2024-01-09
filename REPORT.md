# A56 BlingBank Project Report

## 1. Introduction
BlingBank is a digital financial platform that prioritizes accessibility and convenience. The platform offers an online
banking service through a web application and a desktop application, focussing on account management, expense monitoring
and payments. Users can view their account balances, track expenses and make bill payments.

Secure Documents
- core data includes account details such as account holders, balance, currency and movements in terms of transactions
- Security Challenge: Introduced a new document for payment orders that guarantees CIA-properties and non-repudiation of each transaction

Infrastructure
- existing infrastructure supports account management, expense monitoring and payments
- in order to meet the requirements of the security challenge, the infrastructure needs to be extended to support the new document for payment orders
- a dynamic key distribution system must be devised for users

## 2. Project Development

### 2.1. Secure Document Format

#### 2.1.1. Design

To ensure confidentiality, integrity and freshness(authenticity) of the documents, we implemented the following strategies.

1. Confidentiality
    We encrypt the document with an AES SecretKey. This ensures security of the document while being efficient. This secret key is
    encrypted using the public key of the recipient of the document, i.e. we use RSA for the encryption of the secret key.
2. Integrity
    To ensure the integrity of the document, we first generate the hash of the data. Then, we use the
    private key of the sender to encrypt the document hash. This creates the document signature. To ensure the integrity
    of the document, the receiver of the document decrypts the signature with the senders public key. Then, the receiver
    computes the decrypted document he received. Finally, the received and computed hashes are compared to verify integrity.
3. Freshness
    We include in each transmitted data a timestamp to ensure freshness. The transmitted data is only accepted if the data 
    is received within one minute.

#### 2.1.2. Implementation

We chose Java as the programing language to implement this project. We make use of the Java Cryptography Architecture (JCA)
to implement the security measures. 

- Library for integrity: java.security.Signature
- Library for nonce (freshness): java.security.SecureRandom (generates a 16 byte random number)
- Library for encrypting the symmetric secret key with the public key and decrpyting with the private key: javax.crypto.Cipher
- Library for encrypting and decrypting the document with the symmetric key: javax.crypto.Cipher
- Library for generating the symmetric key: javax.crypto.KeyGenerator
- Library for reading the public key: KeyFactory, PublicKey, X509EncodedKeySpec
- Library for reading the private key: KeyFactory, PrivateKey, PKCS8EncodedKeySpec

We created our custom library "CryptographicLibrary.CryptographicLibrary" that includes several methods needed to ensure confidentiality, integrity 
and freshness. The methods "encryptDocument" and "decryptDocument" make use of all the other methods defined and implemented 
in this library. The idea behind this library is that the user only has to make use of these two methods because they 
ensure all necessary security aspects.

### 2.2. Infrastructure

#### 2.2.1. Network and Machine Setup

Our infrastructure comprises four virtual machines: VM1 hosts the JavaFX client application, 
VM2 serves as an HTTPS traffic gateway using Nginx, VM3 runs the Java server application, 
and VM4 hosts the MongoDB database.

1. VM1 (JavaFX Client): Fairly easy to implement, visually appealing user interfaces made for the desktop environment. Portability of Java convenient to run on any machine.
2. VM2 (Nginx Gateway): Nginx servers as an efficient and lightweight reverse proxy for HTTPS traffic and forwarding requests to VM3 on port 8080.
3. VM3 (Java Server): Running a Java server-side application with Nginx forwarding traffic to port 8080, secure communication with VM4's MongoDB database using TLS.
4. VM4 (MongoDB Database): Hosts MongoDB, which is a flexible NoSQL database that is easy to use and scale.

The Infrastructure prioritizes security and separation of responsibilities.

![infra.png](img%2Finfra.png)

#### 2.2.2. Server Communication Security

Encryption - Decryption flow
1) **Server startup:** 
- Generate keypair (rsa) of server if it doesn't already exist and store it.
2) **Client account creation:**
- The client generates keypair (rsa), username and password and sends then the username, password and its public key to the server. The public key gets encrypted using the already exchanged symmetric key.
- The server stores the username and password, and also the users public key and responds by sending its own public key to the user. The public key gets encrypted using the already exchanged symmetric key for the transfer.
3) **User login:** 
- The user logs in and encrypts its login data using the server's public key and then sends the encrypted data to the server.
- The server decrypts the login data of the user with its own private key.
- Then, the server generates a session key, encrypts the session key with the client's public key and sends the encrypted key to the client.
- The client receives the encrypted session key and decrypts it and stores it for later use.
4) **Data transfer:**
- The data gets encrypted and decrypted with the exchanged session key.
- The initialization vector that is used with the session key for encrypting the data gets send alongside the encrypted data.
- The receiver uses the session key and the IV to decrypt the data.
- The data between the server and the client are being sent via https.
- By using a session key for the encryption of the data, Perfect Forward Secrecy is ensured.

5) **Server-Database**
-The server and the database communicate via ssl/tls.
### 2.3. Security Challenge

#### 2.3.1. Challenge Overview
Confidentiality:            Encrypt payment orders to limit access solely to authorized entities.
Authenticity:               Utilize digital signatures to validate the source and integrity of payment orders.
Non-Repudiation:            Prevent duplicated execution of payment orders. Mandate authorization and 
                            non-repudiation from all account owners, especially for multi-owner accounts.
Dynamic Key Distribution:   Extending cryptographic library and CLI to support dynamic key distribution was essential. 
                            This involved initial usage of existing keys and the development of a secure key management 
                            and distribution system to meet security needs.

We added to each transaction that is stored in the database a digital signature.
This ensures non-repudiation because the digital signature uniquely identifies the person with the corresponding private key.

Since we already implemented the functionality for the public key exchange for the session key, we did not have
to tackle this problem again in the security challenge.
#### 2.3.2. Attacker Model
1. VM1(Javafx Client): Untrusted
- Could be manipulated by users which may attempt to send unauthorized or fake requests
- The client itself is a legitimate part of our application but regarding its external nature it could be potentially manipulated by users and therefore is less trusted

2. VM2(Nginx Gateway): Partially Trusted
- being "exposed to the internet", at least would be in a real world scenario, makes it potentially vulnerable to external attacks
- it manages incoming https traffic but still may be a target of exploitation
- configured securely, but exposure still introduces a level of risks why we consider it partially trusted

3. VM3(Java Server): Fully Trusted
- accessible only through the Nginx gateway which provides an additional layer of security
- internal components should trust each other within the encapsulated environment

4. VM4(MongoDB Database): Fully Trusted
- only being accessed by the Java server on VM3
- no direct exposure to the internet and therefore reducing the attack surface
- communication with the server is secured by using TLS

Attacker capabilities:
- may attempt to exploit vulnerabilities in the Nginx gateway
- can try to perform denial of service attacks on exposed components
- can't directly compromise the integrity of VM3 and VM4

The setup relies on the principle of the least privilege and emphasizes on restricting trust and access to the bare 
possible minimum. Security is greatly enhanced by minimizing the potential attack surface and isolating critical components
from external threats.

#### 2.3.3. Solution Design and Implementation

We added to each transaction that is stored in the database a digital signature.
This ensures non-repudiation because the digital signature uniquely identifies the person with the corresponding private key.

Since we already implemented the functionality for the public key exchange for the session key, we did not have
to tackle this problem again in the security challenge.

We added the functionality that an account can have multiple account holders. An account holder can only be added
to the list of account holders during account creation if this holder has an existing User Account.
The account creation will be rejected by the server if at least one of the account holders already has an account.

![seq_bling_bank.png](img%2Fseq_bling_bank.png)

## 3. Conclusion

BlingBank is a digital financial platform emphasizing accessibility and convenience. It provides online banking services 
via web and desktop applications, focusing on account management, expense tracking, and seamless payments. Users can 
easily access account balances, monitor expenses, and conduct bill payments.


We ensure confidentiality be encrypting the transferred data with a session key.
We ensure freshness by including a timestamp in the transferred data.
We ensure non-repudiation of a transaction by including the digital signature generated with the private key of the user.
We ensure authenticity by adding a digital signature to the data that is being transferred.

Our application offers the functionality for account management, expense tracking and payments. Therefore, these
requirements are satisfied.


The firewall can be made more robust by only allowing outgoing traffic if there has been incoming traffic right before.
Furthermore, scaling our infrastructure is difficult due to the detailed and specific setup. If we would have had
more time, we would ensure that our application can be scaled.


In this project we were able to put our theoretical knowledge gained in the lecture into practice by hands on implementation
of an application. Although the overall workload of the project was very high for each team member, we enjoyed the
project and we think that the gained experience outweighs the difficulty and time-intensivity of this project.


## 4. BibliographygatBling

Lempa, Christian. "How to create a valid self-signed SSL Certificate?" [YouTube Video]. YouTube, Feb 28, 2022.
[Watch Video](https://www.youtube.com/watch?v=VH4gXcvkmOY)
----
END OF REPORT
