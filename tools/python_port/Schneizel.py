import subprocess

class Schneizel:
    def __init__(self,path_:str):
        self.path = path_
        self.process = subprocess.Popen("java -jar Schneizel_cli.jar",
            universal_newlines=True,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT)
        self.valid_print_chars = ["+","-","|"]

    def _put(self,command):
        self.process.stdin.write(f"{command}\n")
        self.process.stdin.flush()

    def _readline(self):
        return self.process.stdout.readline()

    def get_fen(self):
        fen = ""
        while not "/" in fen:
            self._put("fen")
            fen = self._readline()
        return fen

    def get_best_move(self):
        best_move = ""
        while not "best move" in best_move:
            self._put("go")
            best_move = self._readline()
        best_move = best_move.split(" ")[3]
        return best_move.strip()

    def make_move(self,move):
        if len(move) >= 4:
            self._put(move)
            return self._readline() != ''
        return False

    def play_best_move(self):
        self.make_move(self.get_best_move().strip())

    def print_board(self):
        i = 0
        board = ""
        temp = ""
        while i != 17:
            self._put("d")
            temp = self._readline()
            for c in temp:
                if c in self.valid_print_chars:
                    board += temp
                    i = i+1
                    break
        print(board)
