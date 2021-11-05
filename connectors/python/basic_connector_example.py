from threading import Event
from time import sleep

from social_interaction_cloud.basic_connector import BasicSICConnector


class Example:
    """Example that shows how to use the BasicSICConnector. For more information go to
    https://socialrobotics.atlassian.net/wiki/spaces/CBSR/pages/616398873/Python+Examples#Basic-SIC-Connector"""

    def __init__(self, server_ip: str):
        self.sic = BasicSICConnector(server_ip)
        self.awake_lock = Event()

    def run(self) -> None:
        # active Social Interaction Cloud connection
        self.sic.start()

        # set language to English
        self.sic.set_language('en-US')

        # stand up and wait until this action is done (whenever the callback function self.awake is called)
        self.sic.wake_up(self.awake)
        self.awake_lock.wait()  # see https://docs.python.org/3/library/threading.html#event-objects

        self.sic.say_animated('You can tickle me by touching my head.')
        # Execute that_tickles call each time the middle tactile is touched
        self.sic.subscribe_touch_listener('MiddleTactilTouched', self.that_tickles)

        # You have 10 seconds to tickle the robot
        sleep(10)

        # Unsubscribe the listener if you don't need it anymore.
        self.sic.unsubscribe_touch_listener('MiddleTactilTouched')

        # Go to rest mode
        self.sic.rest()

        # close the Social Interaction Cloud connection
        self.sic.stop()

    def awake(self) -> None:
        """Callback function for wake_up action. Called only once.
        It lifts the lock, making the program continue from self.awake_lock.wait()"""
        self.awake_lock.set()

    def that_tickles(self) -> None:
        """Callback function for touch listener. Every time the MiddleTactilTouched event is generated, this
         callback function is called, making the robot say 'That tickles!'"""
        self.sic.say_animated('That tickles!')


example = Example('127.0.0.1')
example.run()
