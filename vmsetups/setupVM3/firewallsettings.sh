#!/bin/bash

sudo ufw reset

# Per default forbid incoming and outgoing traffic
sudo ufw default deny incoming
sudo ufw default deny outgoing

# Allow neccessary outgoing traffic
sudo ufw allow out on eth0 to any port 443 proto tcp
sudo ufw allow out on eth1 to any port 443 proto tcp

# Allow neccesary incoming traffic 
sudo ufw allow from 192.168.2.254 to any port 443 proto tcp
sudo ufw allow from 192.168.1.254 to any port 443 proto tcp

sudo ufw enable
