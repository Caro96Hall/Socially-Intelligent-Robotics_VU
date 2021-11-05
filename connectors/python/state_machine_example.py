from transitions import Machine

from social_interaction_cloud.action import ActionRunner
from social_interaction_cloud.basic_connector import BasicSICConnector
from social_interaction_cloud.detection_result_pb2 import DetectionResult


class ExampleRobot(object):
    """Example that shows how to implement a State Machine with pyTransitions. For more information go to
    https://socialrobotics.atlassian.net/wiki/spaces/CBSR/pages/616398873/Python+Examples#State-Machines-with-PyTransitions"""

    states = ['asleep', 'awake', 'introduced', 'asked_name', 'got_acquainted']

    def __init__(self, sic: BasicSICConnector):
        self.sic = sic
        self.action_runner = ActionRunner(self.sic)
        self.machine = Machine(model=self, states=ExampleRobot.states, initial='asleep')

        self.user_model = {}
        self.recognition_manager = {'attempt_success': False, 'attempt_number': 0}

        # Define transitions
        self.machine.add_transition(trigger='start', source='asleep', dest='awake',
                                    before='wake_up', after='introduce')
        self.machine.add_transition(trigger='introduce', source='awake', dest='introduced',
                                    before='introduction', after='get_name')
        self.machine.add_transition(trigger='get_name', source='introduced', dest='asked_name',
                                    before='ask_name', after='get_acquainted')
        self.machine.add_transition(trigger='get_acquainted', source='asked_name', dest='got_acquainted',
                                    conditions='has_name',
                                    before='get_acquainted_with', after='rest')
        self.machine.add_transition(trigger='get_acquainted', source='asked_name', dest='got_acquainted',
                                    unless='has_name',
                                    before='get_acquainted_without', after='rest')
        self.machine.add_transition(trigger='rest', source='*', dest='asleep',
                                    before='saying_goodbye')

    def wake_up(self) -> None:
        self.action_runner.load_waiting_action('set_language', 'en-US')
        self.action_runner.load_waiting_action('wake_up')
        self.action_runner.run_loaded_actions()

    def introduction(self) -> None:
        self.action_runner.run_waiting_action('say_animated', 'Hi I am Nao and I am a social robot.')

    def ask_name(self) -> None:
        while not self.recognition_manager['attempt_success'] and self.recognition_manager['attempt_number'] < 2:
            self.action_runner.run_waiting_action('say', 'What is your name?')
            self.action_runner.run_waiting_action('speech_recognition', 'answer_name', 3,
                                                  additional_callback=self.on_intent)
        self.reset_recognition_management()

    def on_intent(self, detection_result: DetectionResult) -> None:
        if detection_result and detection_result.intent == 'answer_name' and len(detection_result.parameters) > 0:
            self.user_model['name'] = detection_result.parameters['name'].struct_value['name']
            self.recognition_manager['attempt_success'] = True
        else:
            self.recognition_manager['attempt_number'] += 1

    def reset_recognition_management(self) -> None:
        self.recognition_manager.update({'attempt_success': False, 'attempt_number': 0})

    def has_name(self) -> bool:
        return 'name' in self.user_model

    def get_acquainted_with(self) -> None:
        self.action_runner.run_waiting_action('say_animated', 'Nice to meet you ' + self.user_model['name'])

    def get_acquainted_without(self) -> None:
        self.action_runner.run_waiting_action('say_animated', 'Nice to meet you')

    def saying_goodbye(self) -> None:
        self.action_runner.run_waiting_action('say_animated', 'Well this was fun.')
        self.action_runner.run_waiting_action('say_animated', 'I will see you around.')
        self.action_runner.run_waiting_action('rest')


class StateMachineExample(object):

    def __init__(self, server_ip: str, dialogflow_key_file: str, dialogflow_agent_id: str):
        self.sic = BasicSICConnector(server_ip, dialogflow_key_file, dialogflow_agent_id)
        self.sic.start()
        self.robot = ExampleRobot(self.sic)

    def run(self) -> None:
        self.robot.start()
        self.sic.stop()


example = StateMachineExample('127.0.0.1',
                              '<dialogflow_key_file.json>',
                              '<dialogflow_agent_id>')
example.run()
