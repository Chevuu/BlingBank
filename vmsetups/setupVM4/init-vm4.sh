#!/bin/bash

# Configure network adapters
sudo cat /etc/network/interfaces > /etc/network/interfaces.backup
mv interfaces /etc/network/interfaces

# Static IP after first launch
sudo ifconfig eth0 192.168.2.254
sudo systemctl restart NetworkManager

# Firewall settings
sudo apt install ufw
sudo chmod +x firewallsettings.sh
#./firewallsettings.sh

# As promised in the README, shutting off Bridged Adapter
sudo ifconfig eth1 down

# Move mongodb config file to /etc
sudo mv mongod.conf /etc/mongodb.conf