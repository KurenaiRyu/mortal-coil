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

std::string singleSolve(const int &height, const int &width, int map[], int remaining, std::pair<int, int> start, std::pair<int, int> cur, std::string path, int degreemap[]);

void draw(const int &height, const int &width, int map[], bool print = true);

bool check(const int &height, const int &width, int map[], int &remaining, std::pair<int, int> &cur, int degreemap[]);

int degree(const int &height, const int &width, int map[], std::pair<int, int> &pos);

int degreewithend(const int &height, const int &width, int map[], std::pair<int, int> &cur, std::pair<int, int> &end);

bool validsize(const int &height, const int &width, std::pair<int, int> &cur);
bool valid(const int &height, const int &width, int map[], std::pair<int, int> &cur);

int distance(std::pair<int, int> &a, std::pair<int, int> &b);
bool near(std::pair<int, int> &a, std::pair<int, int> &b);

void degreeupdate(const int &height, const int &width, int map[], std::pair<int, int> &cur, int degreemap[]);
