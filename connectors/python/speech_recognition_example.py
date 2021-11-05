from social_interaction_cloud.action import ActionRunner
from social_interaction_cloud.basic_connector import BasicSICConnector


class Example:
    """Example that uses speech recognition. Prerequisites are the availability of a dialogflow_key_file,
    a dialogflow_agent_id, and a running Dialogflow service. For help meeting these Prerequisites see
    https://socialrobotics.atlassian.net/wiki/spaces/CBSR/pages/260276225/The+Social+Interaction+Cloud+Manual"""

    def __init__(self, server_ip: str, dialogflow_key_file: str, dialogflow_agent_id: str):
        self.sic = BasicSICConnector(server_ip, 'en-US', dialogflow_key_file, dialogflow_agent_id)
        self.action_runner = ActionRunner(self.sic)

        self.user_model = {}
        self.recognition_manager = {'attempt_success': False, 'attempt_number': 0}

    def run(self) -> None:
        self.sic.start()

        self.action_runner.load_waiting_action('set_language', 'en-US')
        self.action_runner.load_waiting_action('wake_up')
        self.action_runner.run_loaded_actions()

        while not self.recognition_manager['attempt_success'] and self.recognition_manager['attempt_number'] < 2:
            self.action_runner.run_waiting_action('say', 'Hi I am Nao. What is your name?')
            self.action_runner.run_waiting_action('speech_recognition', 'answer_name', 3,
                                                  additional_callback=self.on_intent)
        self.reset_recognition_management()

        if 'name' in self.user_model:
            self.action_runner.run_waiting_action('say', 'Nice to meet you ' + self.user_model['name'])
        else:
            self.action_runner.run_waiting_action('say', 'Nice to meet you')

        self.action_runner.run_waiting_action('rest')
        self.sic.stop()

    def on_intent(self, detection_result: dict) -> None:
        if detection_result and detection_result['intent'] == 'answer_name' and len(detection_result['parameters']) > 0:
            self.user_model['name'] = detection_result['parameters']['name']
            self.recognition_manager['attempt_success'] = True
        else:
            self.recognition_manager['attempt_number'] += 1

    def reset_recognition_management(self) -> None:
        self.recognition_manager.update({'attempt_success': False, 'attempt_number': 0})


example = Example('127.0.0.1',
                  '<dialogflow_key_file.json>',
                  '<dialogflow_agent_id>')
example.run()
