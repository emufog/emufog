def getIP(n):
    ip = exp.get_node(n).cmd("hostname -I").rstrip()
    return ip