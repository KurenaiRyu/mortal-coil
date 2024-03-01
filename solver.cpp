#include "solver.hpp"
#include <iostream>
#include <future>
#include "ctpl.h"
#include <queue>

#define MAX_SIZE 1000

std::string p = "";

ctpl::thread_pool pool(4);

std::map<char, std::pair<int, int>> directions = {
    {'U', {-1, 0}},
    {'D', {1, 0}},
    {'L', {0, -1}},
    {'R', {0, 1}}
};

const char* solve(const int level, const int height, const int width, const char* mapstr) {
    // initialize map
    // std::vector<std::vector<int>> map(height, std::vector<int>(width, 0));
    int *map = new int[height * width];
    int remaining = 0;
    #pragma omp parallel for reduction(+:remaining)
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            if (mapstr[i * width + j] == 'X') {
                map[i * width + j] = -1;
            } else {
                map[i * width + j] = 0;
                remaining++;
            }
        }
    }
    // draw(map);

    // solve
    std::vector<std::future<std::string>> results;
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            if (map[i * width + j] == 0) {
                results.push_back(std::async(std::launch::async, [=]() {
                    std::pair<int, int> start = {i, j};
                    int *m = new int[height * width];
                    memcpy(m, map, height * width * sizeof(int));
                    std::string path = singleSolve(height, width, m, remaining, start, start, "");
                    delete[] m;
                    if (path != "") {
                        std::string result = std::to_string(j) + " " + std::to_string(i) + " " + path;
                        return result;
                    }
                    return std::string("");
                }));
            }
        }
    }

    int i = 0;
    for (auto& result_future : results) {
        std::string result = result_future.get();
        printf("\r%d/%d", ++i, remaining);
        if (!result.empty()) {
            std::cout << std::endl;
            char* r = new char[result.size() + 1];
            memcpy(r, result.c_str(), result.size() + 1);
            return r;
        }
    }

    // int n = 0;
    // for (int i = 0; i < height; i++) {
    //     for (int j = 0; j < width; j++) {
    //         if (map[i * width + j] == 0) {
    //             std::pair<int, int> start = {i, j};
    //             int *m = new int[height * width];
    //             memcpy(m, map, height * width * sizeof(int));
    //             std::string path = singleSolve(height, width, m, remaining, start, start, "");
    //             delete[] m;
    //             if (path != "") {
    //                 std::cout << std::endl;
    //                 std::string result = std::to_string(j) + " " + std::to_string(i) + " " + path;
    //                 char* r = new char[result.size() + 1];
    //                 memcpy(r, result.c_str(), result.size() + 1);
    //                 return r;
    //             }
    //             printf("\r%d/%d", ++n, remaining);
    //         }
    //     }
    // }

    std::cout << std::endl;
    delete[] map;
    return p.c_str();
}

std::string singleSolve(const int &height, const int &width, int map[], int remaining, std::pair<int, int> start, std::pair<int, int> cur, std::string path) {
    if (start == cur) {
        map[cur.first*width + cur.second] = 1;
        remaining--;
    }
    std::pair<int, int> c=cur;
    // int ** m=map;
    int r = remaining;
    for (auto& [dir, d] : directions) {
        std::pair<int, int> next = {c.first + d.first, c.second + d.second};
        if (valid(height, width, map, next)) {
            path.push_back(dir);
            while (valid(height, width, map, next)) {
                map[next.first*width + next.second] = 1;
                remaining--;
                next.first += d.first;
                next.second += d.second;
            }
            cur = {next.first - d.first, next.second - d.second};
            // std::cout << path << " " << cur.first << " " << cur.second << " " << remaining << " " << check(height, width, map, remaining) << std::endl;
            // draw(height, width, map);
            if (remaining == 0) {
                return path;
            }
            if (check(height, width, map, remaining, cur) == true) {
                std::string result = singleSolve(height, width, map, remaining, start, cur, path);
                if (result != "") {
                    return result;
                }
            }
            // map = m;

            // std::cout << "r " << path << " " << cur.first << " " << cur.second << " " << remaining << std::endl;
            // draw(height, width, map);
            next = {c.first + d.first, c.second + d.second};
            while (cur != next) {
                map[next.first*width + next.second] = 0;
                remaining++;
                next.first += d.first;
                next.second += d.second;
            }
            map[next.first*width + next.second] = 0;
            remaining++;
            // draw(height, width, map);

            path.pop_back();
            remaining = r;
            // std::cout << "r " << path << " " << cur.first << " " << cur.second << " " << remaining << std::endl;
            // draw(map);
        }
    }

    return "";
}

void draw(const int &height, const int &width, int map[]) {
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            if (map[i * width + j] == -1) {
                std::cout << "X";
            } else if (map[i * width + j] == 0) {
                std::cout << ".";
            } else {
                std::cout << "*";
            }
        }
        std::cout << std::endl;
    }
    std::cout << std::endl;
}

bool check(const int &height, const int &width, int map[], int &remaining, std::pair<int, int> &cur) {
    int s = 0;
    int n = 1;
    std::pair<int, int> start;
    std::queue<std::pair<int, int>> q;
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            if (map[i * width + j] == 0) {
                start = {i, j};
                break;
            }
        }
        if (map[start.first * width + start.second] == 0) {
            break;
        }
    }
    q.push(start);

    int *m = new int[height * width];
    memcpy(m, map, height * width * sizeof(int));
    m[start.first * width + start.second] = 1;

    if (degree(height, width, map, start) == 1) {
        s++;
    }

    while (!q.empty()) {
        std::pair<int, int> cur = q.front();
        q.pop();
        // draw(height, width, m);
        for (auto& [_, d] : directions) {
            std::pair<int, int> next = {cur.first + d.first, cur.second + d.second};
            if (valid(height, width, m, next)) {
                m[next.first * width + next.second] = 1;
                q.push(next);
                n++;
                if (degree(height, width, map, next) == 1) {
                    s++;
                    if (s > 2) {
                        delete[] m;
                        return false;
                    }
                }
            }
        }
    }
    delete[] m;

    return n == remaining;
}

int degree(const int &height, const int &width, int map[], std::pair<int, int> &cur) {
    int n = 0;
    for (auto& [_, d] : directions) {
        std::pair<int, int> next = {cur.first + d.first, cur.second + d.second};
        if (valid(height, width, map, next)) {
            n++;
        }
    }
    return n;
}

bool through(const int &height, const int &width, int map[], std::pair<int, int> &cur) {
    std::pair<int, int> up = {cur.first - 1, cur.second};
    std::pair<int, int> down = {cur.first + 1, cur.second};
    std::pair<int, int> left = {cur.first, cur.second - 1};
    std::pair<int, int> right = {cur.first, cur.second + 1};
    if ((!valid(height, width, map, up)) && (!valid(height, width, map, down))) {
        return true;
    }
    if ((!valid(height, width, map, left)) && (!valid(height, width, map, right))) {
        return true;
    }
    return false;
}

bool valid(const int &height, const int &width, int map[], std::pair<int, int> &cur) {
    return 0 <= cur.first && cur.first < height && 0 <= cur.second && cur.second < width && map[cur.first*width + cur.second] == 0;
}

int distance(std::pair<int, int> &a, std::pair<int, int> &b) {
    return abs(a.first - b.first) + abs(a.second - b.second);
}
