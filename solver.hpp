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

extern "C" EXPORT const char* solve(int level, int height, int width, const char* mapstr);

std::string singleSolve(int height, int width, std::vector<std::vector<int>> map, int remaining, std::pair<int, int> start, std::pair<int, int> cur, std::string path);

void draw(std::vector<std::vector<int>> &map);

bool check(std::vector<std::vector<int>> &map, int &remaining);

int degree(std::vector<std::vector<int>> &map, std::pair<int, int> &pos);

bool through(std::vector<std::vector<int>> &map, std::pair<int, int> &cur);

bool valid(std::vector<std::vector<int>> &map, std::pair<int, int> &cur);
