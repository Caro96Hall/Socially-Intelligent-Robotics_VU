from argparse import ArgumentParser
from os import getenv
from os.path import abspath, dirname
from time import time

from redis import Redis

if __name__ == '__main__':
    parser = ArgumentParser()
    parser.add_argument('--username', type=str, help='Username', default='default')
    args = parser.parse_args()

    host = getenv('DB_IP')
    password = getenv('DB_PASS')
    self_signed = getenv('DB_SSL_SELFSIGNED')
    if self_signed == '1':
        cert_file = dirname(abspath(__file__)) + '/cert.pem'
        redis = Redis(host=host, ssl=True, ssl_ca_certs=cert_file, password=password)
    else:
        redis = Redis(host=host, ssl=True, password=password)

    devices = redis.zrevrangebyscore(name='user:' + args.username, min=(time() - 60), max='+inf')
    devices.sort()
    for device in devices:
        print(device.decode())

    redis.close()
