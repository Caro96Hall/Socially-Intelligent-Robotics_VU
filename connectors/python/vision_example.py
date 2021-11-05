from time import sleep

from social_interaction_cloud.action import ActionRunner
from social_interaction_cloud.basic_connector import BasicSICConnector


class Example:
    """For this example you will need to turn on the PeopleDetection and FaceRecognition services.
    When running this without a robot, you need the computer-camera, computer-robot and computer-speaker."""

    def __init__(self, server_ip: str):
        self.sic = BasicSICConnector(server_ip)

    def run(self) -> None:
        self.sic.start()
        action_runner = ActionRunner(self.sic)

        action_runner.run_waiting_action('set_language', 'en-US')
        action_runner.run_waiting_action('set_idle')
        action_runner.run_vision_listener('people', self.i_spy_with_my_little_eye, False)
        action_runner.run_waiting_action('say', 'Hello, I see you')

        action_runner.run_vision_listener('face', self.face_recognition, True)
        sleep(10)

        self.sic.stop()

    @staticmethod
    def i_spy_with_my_little_eye() -> None:
        print('I see someone!')

    @staticmethod
    def face_recognition(identifier: str) -> None:
        print('I recognize you as face #' + identifier)


example = Example('127.0.0.1')
example.run()
