from social_interaction_cloud.action import ActionRunner
from social_interaction_cloud.basic_connector import BasicSICConnector, RobotPosture


class Example:

    def __init__(self, server_ip: str):
        self.sic = BasicSICConnector(server_ip)

    def run(self) -> None:
        self.sic.start()
        action_runner = ActionRunner(self.sic)

        action_runner.run_waiting_action('wake_up')
        action_runner.run_waiting_action('set_language', 'en-US')

        action_runner.load_waiting_action('say', 'I\'m going to crouch now.')
        action_runner.load_waiting_action('go_to_posture', RobotPosture.CROUCH,
                                          additional_callback=self.posture_callback)
        action_runner.run_loaded_actions()

        action_runner.load_waiting_action('say', 'I\'m going to stand now.')
        action_runner.load_waiting_action('go_to_posture', RobotPosture.STAND,
                                          additional_callback=self.posture_callback)
        action_runner.run_loaded_actions()

        action_runner.run_waiting_action('rest')

        self.sic.stop()

    @staticmethod
    def posture_callback(success: bool) -> None:
        print('It worked!' if success else 'It failed...')


example = Example('127.0.0.1')
example.run()
