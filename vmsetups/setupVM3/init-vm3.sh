#!/bin/bash

# Configure Network Adapters
sudo cat /etc/network/interfaces > /etc/network/interfaces.backup
sudo mv interfaces /etc/network/interfaces

# Static IPs on first launch
sudo ifconfig eth0 192.168.1.1/24 up
sudo ifconfig eth1 192.168.2.1/24 up
sudo systemctl restart NetworkManager

# install mvn and java
wget https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.deb
sudo dpkg -i jdk-21_linux-x64_bin.deb
sudo update-alternatives --list java
sudo update-java-alternatives --set  /usr/lib/jvm/jdk-21-oracle-x64
sudo apt install maven

# install and run the server application (need to adapt the config file and mongodb config)
#sudo mkdir -p /etc/ssl/springboot
#sudo cp database_certificate.crt /etc/ssl/springboot/
#sudo cp database_key.key /etc/ssl/springboot/
#mvn -f ~/Desktop/a56-niklas-kevin-vuk/pom.xml clean install
#mvn -f ~/Desktop/a56-niklas-kevin-vuk/crypto-lib/pom.xml clean install
#mvn -f ~/Desktop/a56-niklas-kevin-vuk/server-apk/pom.xml clean install
javac MyApp.java
java MyApp

# Firewall settings
sudo apt install ufw
sudo chmod +x firewallsettings.sh
./firewallsettings.sh

# As promised in the README, shutting off Bridged Adapter
sudo ifconfig eth2 down
