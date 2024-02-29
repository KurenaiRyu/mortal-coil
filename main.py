import requests
from lxml import etree
from enum import Enum
from copy import deepcopy

s = requests.Session()
url = 'https://www.hacker.org/coil/'
cookie = {
    'PHPSESSID': '50cb93d11f730a4fc7bbb1935546c345',
    'phpbb3_gebwk_k': '3nwac5ltva4rd4x0',
    'phpbb3_gebwk_u': '50406',
    'phpbb3_gebwk_sid': 'b076b1199369056ed789f1a47268e206'
}

class State(Enum):
    WALL = -1
    EMPTY = 0
    PASS = 1
    UP = 2
    DOWN = 3
    LEFT = 4
    RIGHT = 5

Opt = {
    State.UP: (-1, 0),
    State.DOWN: (1, 0),
    State.LEFT: (0, -1),
    State.RIGHT: (0, 1)
}

OptStr = {
    State.UP: 'U',
    State.DOWN: 'D',
    State.LEFT: 'L',
    State.RIGHT: 'R'
}

class Coil:
    curLevel : int = 0
    width : int = 0
    height : int = 0
    map2D : list[list[State]] = []
    start : tuple[int, int] = (-1, -1)
    end : tuple[int, int] = (-1, -1)
    steps : list[State] = []
    remain : int = -1
    cur : tuple[int, int] = (-1, -1)

    @classmethod
    def parse(cls, script : str) -> 'Coil':
        coil = Coil()
        str = script.split(';')
        coil.curLevel = int(str[0].strip().split(' ')[3])
        coil.width = int(str[1].strip().split(' ')[3])
        coil.height = int(str[2].strip().split(' ')[3])
        mapstr = str[3].strip().split(' ')[3].strip('"')
        coil.map2D = [[State.EMPTY if mapstr[j*coil.width+i] == '.' else State.WALL for i in range(coil.width)] for j in range(coil.height)]
        coil.remain = mapstr.count('.')
        return coil
    
    def getSolution(self) -> dict[str, str]:
        # return f'x={self.start[1]}&y={self.start[0]}&path={"".join([OptStr[step] for step in self.steps])}'
        return {'x': str(self.start[1]), 'y': str(self.start[0]), 'path': "".join([OptStr[step] for step in self.steps])}
    
    def draw(self):
        for i in range(self.height):
            for j in range(self.width):
                if self.map2D[i][j] == State.WALL:
                    print('X', end='')
                elif self.map2D[i][j] == State.EMPTY:
                    print('.', end='')
                else:
                    print('*', end='')
            print()

# def solve(coil : Coil) -> 'Coil':
#     for i in range(coil.height):
#         for j in range(coil.width):
#             if coil.map2D[i][j] == State.EMPTY:
#                 coil.start = (i, j)
#                 coil.cur = (i, j)
#                 coil.steps.clear()
#                 result = singleSolve(deepcopy(coil))
#                 if result.remain == 1:
#                     break
#         if result.remain == 1:
#             break
#     return result

from multiprocessing import Pool
import copy

def solve(coil : Coil) -> 'Coil':
    with Pool() as pool:
        results = []
        for i in range(coil.height):
            for j in range(coil.width):
                if coil.map2D[i][j] == State.EMPTY:
                    coil.start = (i, j)
                    coil.cur = (i, j)
                    coil.steps.clear()
                    result = pool.apply_async(singleSolve, (copy.deepcopy(coil),))
                    results.append(result)

        for result in results:
            res = result.get()
            if res.remain == 1:
                break

    return res

def singleSolve(oldcoil : Coil) -> 'Coil':
    for opt in Opt:
        next = (oldcoil.cur[0]+Opt[opt][0], oldcoil.cur[1]+Opt[opt][1])
        if 0 <= next[0] < oldcoil.height and 0 <= next[1] < oldcoil.width and oldcoil.map2D[next[0]][next[1]] == State.EMPTY:
            coil = deepcopy(oldcoil)
            coil.steps.append(opt)
            coil.map2D[coil.cur[0]][coil.cur[1]] = opt
            while 0 <= next[0] < coil.height and 0 <= next[1] < coil.width and coil.map2D[next[0]][next[1]] == State.EMPTY:
                coil.cur = next
                coil.map2D[next[0]][next[1]] = State.PASS
                coil.remain -= 1
                next = (coil.cur[0]+Opt[opt][0], coil.cur[1]+Opt[opt][1])
            if coil.remain == 1:
                return coil
            else:
                # coil.draw()
                newcoil = singleSolve(coil)
                if newcoil.remain == 1:
                    return newcoil
            coil.steps.pop()
    return oldcoil

def main():
    r = s.get(url, cookies=cookie)
    while True:
        tree = etree.HTML(r.text, parser=etree.HTMLParser())
        script = tree.xpath('//td[@id="pgfirst"]/script/text()')[0]
        # script = 'var curLevel = 4; var width = 7; var height = 6; var boardStr = ".................XX..X..XX.....X......X...";'
        coil = Coil.parse(script)
        coil.curLevel = int(tree.xpath('//body/text()')[4].split(' ')[1])
        print(coil.curLevel)
        coil.draw()
        result = solve(coil)
        print(coil.start, coil.steps)
        r = s.post(url, data=result.getSolution())
        print(r.url, r.request.body, sep='?')

if __name__ == '__main__':
    main()
