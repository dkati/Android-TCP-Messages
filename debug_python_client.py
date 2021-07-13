# CLIENT
import socket
import time

HOST = '192.168.0.104'  # The server's hostname or IP address
PORT = 8000  # The port used by the server

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((HOST, PORT))
while True:
    s.send(b'pame ligo\n')
    # print thank you message
    s.send(b'as einai\n')
    s.send(b'-------\n')
    time.sleep(1)
