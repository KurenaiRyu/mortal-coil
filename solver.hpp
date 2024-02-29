#pragma once
#include <Vector>
#include <String>
#include <map>

#ifdef _MSC_VER
    #define EXPORT __declspec(dllexport)
#else
    #define EXPORT __attribute__((visibility("default")))
#endif

#define MAX_SIZE 1000

enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT
};

std::map<Direction, std::pair<int, int>> directions = {
    {UP, {0, -1}},
    {DOWN, {0, 1}},
    {LEFT, {-1, 0}},
    {RIGHT, {1, 0}}
};

std::map<Direction, char> directionChars = {
    {UP, 'U'},
    {DOWN, 'D'},
    {LEFT, 'L'},
    {RIGHT, 'R'}
};

extern "C" EXPORT std::string solve(int level, int height, int width, std::string mapstr);

std::string singleSolve(int height, int width, int map[MAX_SIZE][MAX_SIZE], int remaining, std::pair<int, int> cur, std::string path);
