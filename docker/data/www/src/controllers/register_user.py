from argparse import ArgumentParser
from os import getenv
from os.path import abspath, dirname

from redis import Redis

if __name__ == '__main__':
    parser = ArgumentParser()
    parser.add_argument('--username', type=str, help='Username')
    parser.add_argument('--password', type=str, help='Password')
    args = parser.parse_args()

    host = getenv('DB_IP')
    password = getenv('DB_PASS')
    self_signed = getenv('DB_SSL_SELFSIGNED')
    if self_signed == '1':
        cert_file = dirname(abspath(__file__)) + '/cert.pem'
        redis = Redis(host=host, ssl=True, ssl_ca_certs=cert_file, password=password)
    else:
        redis = Redis(host=host, ssl=True, password=password)

    pipe = redis.pipeline()
    pipe.acl_setuser(enabled=True, username=args.username, passwords=['+' + args.password],
                     categories=['+@all', '-@dangerous'], keys=['user:' + args.username, args.username + '-*',
                                                                'emotion_detection', 'intent_detection',
                                                                'people_detection', 'face_recognition',
                                                                'robot_memory'])
    pipe.acl_save()
    result = pipe.execute()
    redis.close()

    if result == [True, True]:
        print('Registration completed!')
    else:
        print('Registration failed...')
