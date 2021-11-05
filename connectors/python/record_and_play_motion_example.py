from time import sleep

from social_interaction_cloud.action import ActionRunner
from social_interaction_cloud.basic_connector import BasicSICConnector


class Example:

    def __init__(self, server_ip: str):
        self.sic = BasicSICConnector(server_ip)
        self.action_runner = ActionRunner(self.sic)
        self.motion = None

    def run(self) -> None:
        self.sic.start()

        joints = ['RArm']
        self.action_runner.load_waiting_action('set_language', 'en-US')
        self.action_runner.load_waiting_action('wake_up')
        self.action_runner.run_loaded_actions()

        self.action_runner.run_waiting_action('set_stiffness', joints, 0)
        self.action_runner.load_waiting_action('say', 'You can now move my right arm.')
        self.action_runner.load_waiting_action('start_record_motion', joints)
        self.action_runner.run_loaded_actions()
        sleep(5)
        self.action_runner.load_waiting_action('stop_record_motion', additional_callback=self.retrieve_recorded_motion)
        self.action_runner.load_waiting_action('say', 'Finished recording')
        self.action_runner.run_loaded_actions()

        self.action_runner.run_waiting_action('set_stiffness', joints, 100)
        if self.motion:
            self.action_runner.run_waiting_action('say', 'I will now replay your movement')
            self.action_runner.run_waiting_action('play_motion', self.motion)
        else:
            self.action_runner.run_waiting_action('say', 'Something went wrong.')

        self.action_runner.run_waiting_action('rest')
        self.sic.stop()

    def retrieve_recorded_motion(self, motion) -> None:
        self.motion = motion


example = Example('127.0.0.1')
example.run()
