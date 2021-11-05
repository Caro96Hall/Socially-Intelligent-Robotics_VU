from social_interaction_cloud.action import ActionRunner
from social_interaction_cloud.basic_connector import BasicSICConnector


class MyConnector(BasicSICConnector):
    def __init__(self, server_ip: str):
        super(MyConnector, self).__init__(server_ip)

    def on_browser_button(self, button: str) -> None:
        print(button + ' pressed, exiting...')
        self.stop()


class Example:
    """Example that shows how to use a browser connection (on a Pepper or the computer-browser).
    The webserver service needs to be running for this!"""

    def __init__(self, server_ip: str):
        self.sic = MyConnector(server_ip)

    def run(self) -> None:
        self.sic.start()
        self.sic.set_language('en-US')

        action_runner = ActionRunner(self.sic)
        text = self.get_text('Hello, world!')
        button = self.get_button('Done')
        html = self.get_header() + self.get_body(text + button) + self.get_footer()
        action_runner.run_action('browser_show', html)
        # And now we wait until someone presses the button (see on_browser_button above)

    @staticmethod
    def get_header() -> str:
        """
        A header (navbar) with a listening icon on the left and a VU logo on the right.
        """
        return '<nav class="navbar mb-5">' \
               '<div class="navbar-brand listening_icon"></div>' \
               '<div class="navbar-nav vu_logo"></div>' \
               '</nav>'

    @staticmethod
    def get_body(contents: str) -> str:
        """
        The given contents in a centered container.
        """
        return '<main class="container text-center">' + contents + '</main>'

    @staticmethod
    def get_footer() -> str:
        """
        A footer that shows the currently recognized spoken text (if any).
        """
        return '<footer class="fixed-bottom">' \
               '<p class="lead bg-light text-center speech_text"></p>' \
               '</footer>'

    @staticmethod
    def get_text(txt: str) -> str:
        """
        A simple text display.
        """
        return '<h1>' + txt + '</h1>'

    @staticmethod
    def get_button(label: str) -> str:
        """
        A clickable button.
        """
        return '<button class="btn btn-primary btn-lg mt-5 ml-3">' + label + '</button>'


example = Example('127.0.0.1')
example.run()
