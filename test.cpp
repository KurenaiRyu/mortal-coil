#include "solver.hpp"
#include <iostream>
#include"time.h"

int main() {
    int level = 87;
    int height = 32;
    int width = 32;
    std::string mapstr = ".....XXXXXX.....X..XXX......XXX....X.XXXXXX..XX....XX....X..XXX.XX.X..XXXXXX.....X....X..X....X.XX.XX.XXX...XXX....X..X....X....XX....X...X...X.X....XX...XX.X.X....X......XX.....X.....X..X...X.XX......X.....X....X......XX.......XX...XX.....XX....XXXX....X...X..X...X..X.X....X..XX...X....XXXX...X.X.X..X..X...X...X...X.X.......X.....X.........X.......X...XX.....X....XXXX.X.......XX........XXX.....XXXX..X...X....X....X........XX...XX........XX........XXXXXX.XXX..X..X...X..X...X....XXX.....X...XX....X...XX.X........X.......XXXX...XX..XX..X..X..XX......X.......XXXXX....XX...X.....XXX...XXX...XX........X.X...X...X.........X.XX..XXXXX...X..XX.............X...X...XXXX........XXXX...X......X.......X....XX.....X..X...X......XX.X..X....X..XX....X......X.XX....X.X...X...XXX...X..X....X..XX.......X.........X...XXXX.....XX...XX.....XX..........XX..XXXX.....XX...X.......X...X..X.XX.....X.....X.....XXX...X..X...XX.X.X...XXX....XXXX........X......X....XX...XX....X..X...XXX.XXX..XX.......X...XX.XX...XXXXX.............X...X....";
    // int level = 9;
    // int width = 5;
    // int height = 7;
    // std::string mapstr = "...................X....X..........";
    clock_t   start,   finish;
    start =clock(); 
    std::string result = solve(level, width, height, mapstr.c_str());
    finish = clock();
    std::cout << result << std::endl;
    printf("%f seconds\n",(double)(finish-start)/CLOCKS_PER_SEC);
    return 0;
}