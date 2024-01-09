#!/bin/bash

# Configure Network adapters
sudo cat /etc/network/interfaces > /etc/network/interfaces.backup
sudo mv interfaces /etc/network/interfaces

# Set IPs for the first launch
sudo ifconfig eth0 192.168.0.10/24 up
sudo ifconfig eth1 192.168.1.254/24 up
sudo systemctl restart NetworkManager 

# Setup Nginx
sudo apt install nginx
sudo systemctl start nginx
sudo systemctl enable nginx
sudo mkdir -p /etc/nginx/private
sudo mkdir -p /etc/nginx/certs
sudo cp cert-key.key /etc/nginx/private
sudo cp fullchaingatewaycert.crt /etc/nginx/certs
sudo cat /etc/nginx/sites-available/default > /etc/nginx/sites-available/default.backup
sudo mv default /etc/nginx/sites-available/default
sudo service nginx reload

# Firewall Settings
sudo apt install ufw
sudo chmod +x firewallsettings.sh
./firewallsettings.sh

# As promised in the README, shutting off Bridged Adapter
sudo ifconfig eth2 down
