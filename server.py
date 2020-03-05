#Imports modules
import socket
import struct
import cv2 as cv

#Sets up listener
listensocket = socket.socket()
listenPort = 1234
numberOfConnections=999
thisIp = socket.gethostname()
print("HostName: "+str(thisIp))
listensocket.bind(('', listenPort))

#Starts Server
listensocket.listen(numberOfConnections)
print("Started Listening, press q to stop listening")



while (True):
    try:
        #Accepts Connection
        (clientsocket, address) = listensocket.accept()
        #print("Connected")
        buf = bytearray()

        while len(buf)<4:
            buf += clientsocket.recv(4-len(buf))
        size = struct.unpack('!i', buf)
        print("receiving %s bytes" % size)

        with open('result.png', 'wb') as img:
            while True:
                data = clientsocket.recv(1024)
                if not data:
                    break
                img.write(data)
                #print(data)
        print('received image, yay!')

        #Closes image
        img.close()


        image = cv.imread('result.png', cv.IMREAD_COLOR)
        cv.imshow( "Display window", image )
        #cv.waitKey(0) 
        if cv.waitKey(1) & 0xFF == ord('q'):
            break

    except listensocket.error:
        #listensocket.sendall('q^'.encode())
        listensocket.close()

#Closes socket
#listensocket.sendall('q^'.encode())
listensocket.close()