from time import sleep
from social_interaction_cloud.abstract_connector import AbstractSICConnector


class MyConnector(AbstractSICConnector):
    """This example shows you how to create your own SIC connector by inheriting AbstractSICConnector.
    For more information go to
    https://socialrobotics.atlassian.net/wiki/spaces/CBSR/pages/616398873/Python+Examples#Abstract-SIC-Connector"""

    def __init__(self, server_ip: str):
        super(MyConnector, self).__init__(server_ip)

    def run(self) -> None:
        self.start()
        self.set_language('en-US')
        sleep(1)  # wait for the language to change
        self.say('Hello, world!')
        sleep(3)  # wait for the robot to be done speaking (to see the relevant prints)
        self.stop()

    def on_robot_event(self, event: str) -> None:
        print(event)


# Run the application
my_connector = MyConnector('127.0.0.1')
my_connector.run()
