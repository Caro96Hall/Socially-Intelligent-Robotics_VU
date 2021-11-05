from argparse import ArgumentParser
from os import getenv
from os.path import abspath, dirname

from redis import Redis

if __name__ == '__main__':
    parser = ArgumentParser()
    parser.add_argument('--identifier', type=str, help='The target device identifier')
    parser.add_argument('--command', type=str, help='The name of the command')
    parser.add_argument('--data', type=str, help='The accompanying data')
    args = parser.parse_args()

    host = getenv('DB_IP')
    password = getenv('DB_PASS')
    self_signed = getenv('DB_SSL_SELFSIGNED')
    if self_signed == '1':
        cert_file = dirname(abspath(__file__)) + '/cert.pem'
        redis = Redis(host=host, ssl=True, ssl_ca_certs=cert_file, password=password)
    else:
        redis = Redis(host=host, ssl=True, password=password)

    redis.publish(args.identifier + '_' + args.command, args.data)
    redis.close()
