sudo ufw reset

# By default deny outgoing and incoming traffic
sudo ufw default deny outgoing
sudo ufw default deny incoming

# exception for outgoing traffic
sudo ufw allow out on eth0 to any port 443 proto tcp

# exception for incoming traffic
sudo ufw allow from 192.168.2.1 to any port 443 proto tcp

sudo ufw enable
