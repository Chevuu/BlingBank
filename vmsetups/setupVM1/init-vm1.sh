#!/bin/bash

# Configure Network adapters
sudo cat /etc/network/interfaces > /etc/network/interfaces.backup
sudo mv interfaces /etc/network/interfaces

# Set IP on first launch
sudo ifconfig eth0 192.168.0.100/24 up
sudo systemctl restart NetworkManager

# Add the CA-certificate to the trust store
sudo cp cauthority.crt /usr/local/share/ca-certificates/
sudo update-ca-certificates

# install mvn and java
wget https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.deb
sudo dpkg -i jdk-21_linux-x64_bin.deb
sudo update-alternatives --list java
sudo update-java-alternatives --set  /usr/lib/jvm/jdk-21-oracle-x64
sudo apt install maven


# Build and run the client
mvn clean package -f ~/Desktop/a56-niklas-kevin-vuk/client-apk
mvn javafx:run -f ~/Desktop/a56-niklas-kevin-vuk/client-apk

# As promised in the README, shutting off Bridged Adapter
sudo ifconfig eth1 down