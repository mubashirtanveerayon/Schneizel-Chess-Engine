import os
import errno
import subprocess

def _get_file():
    jar_file = "Schneizel_cli.jar"
    path = os.path.join(os.path.dirname(__file__), jar_file)

    # you have to write code which will auto download the Schneizel_cli.jar file
    if not os.path.exists(path):
        raise FileNotFoundError(
            errno.ENOENT,
            os.strerror(errno.ENOENT),
            jar_file
            )

    return path

class Schneizel:
    def __init__(self, path=None):

        if path is None:
            self.path = _get_file()
        else:
            self.path = path

        self.process = subprocess.Popen(f"java -jar \"{self.path}\"",
            universal_newlines=True,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT)

        self.command_put = False
        #self.process.stdout.readline()
        self.VERSION = self.get_version()
        self.VERSION = float(self.get_version())

    def _put(self, command):
        self.command_put = self.process.poll() is None
        if self.command_put:
            self.process.stdin.write(f"{command}\n")
            self.process.stdin.flush()

    def _readline(self):
        return None if not self.command_put else self.process.stdout.readline()

    def get_fen(self):
        self._put("fen")
        fen = self._readline()
        return fen

    def get_best_move(self):
        self._put("go")
        return self._readline().split(" ")[3].strip()

    def make_move(self, move):
        if len(move) >= 4:
            self._put(move)
            return self._readline() != ''
        return False

    def play_best_move(self):
        self._put("play")
    
    def get_board_visual(self) -> str:
        self._put("d")
        return ''.join(list(filter((str("\n")).__ne__, [self._readline() for i in range(22)][1:])))

    def get_version(self):
        self._put("v")
        return self._readline().strip()

if __name__ == "__main__":
    engine = Schneizel()

    print(engine.VERSION)
    while True:
        print(engine.get_board_visual())
        print(engine.get_fen())
        p = input("Enter yout move: ")
        engine.make_move(p)
        engine.play_best_move()
        print("\n##################################################\n")

