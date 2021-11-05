import asyncio
from os import getenv
from os.path import abspath, dirname

import websockets
from redis import Redis

TOPICS = ['session_start', 'session_log', 'session_end']


async def producer(websocket, path):
    loggerId = await websocket.recv()
    print(loggerId)

    host = getenv('DB_IP')
    password = getenv('DB_PASS')
    self_signed = getenv('DB_SSL_SELFSIGNED')
    if self_signed == '1':
        cert_file = dirname(abspath(__file__)) + '/cert.pem'
        redis = Redis(host=host, ssl=True, ssl_ca_certs=cert_file, password=password)
    else:
        redis = Redis(host=host, ssl=True, password=password)

    pubsub = redis.pubsub(ignore_subscribe_messages=True)
    mapping = dict.fromkeys(((loggerId + '_' + t) for t in TOPICS), lambda msg: websocket.send(msg))
    pubsub.subscribe(**mapping)
    pubsub.run_in_thread(sleep_time=0.001)


async def main():
    async with websockets.serve(producer, port=8080):
        await asyncio.Future()  # run forever


asyncio.run(main())
