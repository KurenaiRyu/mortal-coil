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

extern "C" EXPORT const char* solve(const int level, const int height, const int width, const char* mapstr);

std::string singleSolve(const int &height, const int &width, int map[], int remaining, std::pair<int, int> start, std::pair<int, int> cur, std::string path);

void draw(const int &height, const int &width, int map[]);

bool check(const int &height, const int &width, int map[], int &remaining);

int degree(const int &height, const int &width, int map[], std::pair<int, int> &pos);

bool through(const int &height, const int &width, int map[], std::pair<int, int> &cur);

bool valid(const int &height, const int &width, int map[], std::pair<int, int> &cur);
