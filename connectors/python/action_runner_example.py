from threading import Event

from social_interaction_cloud.action import ActionRunner, Action, ActionFactory
from social_interaction_cloud.basic_connector import BasicSICConnector


class Example:
    """Example that shows how to use the Action, ActionFactory, and ActionRunner classes. For more information go to
    https://socialrobotics.atlassian.net/wiki/spaces/CBSR/pages/616398873/Python+Examples#Action,-ActionFactory,-and-ActionRunner"""

    def __init__(self, server_ip: str):
        self.sic = BasicSICConnector(server_ip)
        self.hello_action_lock = Event()

    def run(self) -> None:
        self.sic.start()
        self.sic.set_language('en-US')

        #################
        # Action        #
        #################
        # Create an action that can be executed when needed and reused
        hello_action = Action(self.sic.say, 'Hello, Action!', callback=self.hello_action_callback,
                              lock=self.hello_action_lock)
        hello_action.perform().wait()  # perform() returns the lock, so you can immediately call wait() on it.
        hello_action.perform().wait()  # you can reuse an action.

        #################
        # Action Factory#
        #################
        # Use the ActionFactory to build actions. You can build actions with a lock build inside of them by
        # using build_waiting_action(). It saves you from keeping track of your locks.
        # build_action() and build_waiting_action() use the name of SIC method instead of an explicit reference to it
        # as input. For example build_action('say', ...) instead of build_action(self.sic.say, ...).
        action_factory = ActionFactory(self.sic)
        hello_action_factory = action_factory.build_waiting_action('say', 'Hello, Action Factory!',
                                                                   additional_callback=self.hello_action_factory_callback)
        hello_action_factory.perform().wait()

        #################
        # Action Runner #
        #################
        # With an ActionRunner you can run regular and waiting actions right away or load them to run them in parallel.
        # When running the loaded actions, it will wait automatically for all loaded waiting actions to finish.
        action_runner = ActionRunner(self.sic)
        action_runner.run_waiting_action('say', 'Hello, Action Runner!',
                                         additional_callback=self.hello_action_runner_callback)
        # run_waiting_action('say', ...) waits to finish talking before continuing

        # The following actions will run in parallel.
        action_runner.load_waiting_action('wake_up')
        action_runner.load_waiting_action('set_eye_color', 'green')
        action_runner.load_waiting_action('say', 'I am standing up now')
        action_runner.run_loaded_actions()  # If you want to keep an action sequence for reuse, add False as input.
        # run_loaded_actions() will wait automatically before all loaded waiting actions (not regular actions) to finish
        # before continuing

        action_runner.run_action('say', 'I will start sitting down as I say this. Because I am not a waiting action')
        action_runner.run_action('set_eye_color', 'white')
        action_runner.run_waiting_action('rest')

        self.sic.stop()

    def hello_action_callback(self) -> None:
        print('Hello Action Done')
        self.hello_action_lock.set()
        self.hello_action_lock.clear()  # necessary for reuse

    @staticmethod
    def hello_action_factory_callback() -> None:
        print('Hello Action Factory Done')

    @staticmethod
    def hello_action_runner_callback() -> None:
        print('Hello Action Runner Done')


example = Example('127.0.0.1')
example.run()
