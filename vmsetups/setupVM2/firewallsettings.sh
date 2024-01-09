#!/bin/bash

sudo ufw reset

# Deny incoming and outgoing traffic by default
sudo ufw default deny outgoing
sudo ufw default deny incoming

# Allow specific incoming traffic
sudo ufw allow from 192.168.0.100 to any port 443 proto tcp
sudo ufw allow from 192.168.1.1 to any port 443 proto tcp

# Allow specific outgoing traffic
sudo ufw allow out on eth0 to any port 443 proto tcp
sudo ufw allow out on eth1 to any port 443 proto tcp

sudo ufw enable




