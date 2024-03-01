#include "solver.hpp"
#include <iostream>
#include"time.h"

int main() {
    // int level = 33;
    // int height = 14;
    // int width = 14;
    // std::string mapstr = "...X......X....X...XX...X.X...X.......X...X......X..XX.....X...X...X...X....XX.X.....X.....X.XX..........X....X.X...XX..XX......X.XX..X..X.X....X...X.XX.X.X....X...X..X......XX..X...X.............";
    int level = 9;
    int width = 3;
    int height = 3;
    std::string mapstr = "X.......X";
    clock_t   start,   finish;
    start =clock(); 
    std::string result = solve(level, width, height, mapstr.c_str());
    finish = clock();
    std::cout << result << std::endl;
    printf("%f seconds\n",(double)(finish-start)/CLOCKS_PER_SEC);
    return 0;
}