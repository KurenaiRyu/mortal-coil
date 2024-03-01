#include "solver.hpp"
#include <iostream>
#include <future>
#include "ctpl.h"
#include <queue>

#define MAX_SIZE 1000

std::string p = "";

ctpl::thread_pool pool(1);

std::map<char, std::pair<int, int>> directions = {
    {'U', {-1, 0}},
    {'D', {1, 0}},
    {'L', {0, -1}},
    {'R', {0, 1}}
};

const char* solve(int level, int height, int width, const char* mapstr) {
    // initialize map
    std::vector<std::vector<int>> map(height, std::vector<int>(width, 0));
    int remaining = 0;
    #pragma omp parallel for reduction(+:remaining)
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            if (mapstr[i * width + j] == 'X') {
                map[i][j] = -1;
            } else {
                map[i][j] = 0;
                remaining++;
            }
        }
    }
    // draw(map);

    // solve
    std::vector<std::future<std::string>> results;
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            if (map[i][j] == 0) {
                results.push_back(std::async(std::launch::async, [=]() {
                    std::pair<int, int> start = {i, j};
                    std::string path = singleSolve(height, width, map, remaining, start, start, "");
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
    //         if (map[i][j] == 0) {
    //             std::pair<int, int> start = {i, j};
    //             std::string path = singleSolve(height, width, map, remaining, start, start, "");
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
    return p.c_str();
}

std::string singleSolve(int height, int width, std::vector<std::vector<int>> map, int remaining, std::pair<int, int> start, std::pair<int, int> cur, std::string path) {
    if (start == cur) {
        map[cur.first][cur.second] = 1;
        remaining--;
    }
    std::pair<int, int> c=cur;
    std::vector<std::vector<int>> m=map;
    int r = remaining;
    for (auto& [dir, d] : directions) {
        std::pair<int, int> next = {c.first + d.first, c.second + d.second};
        if (0 <= next.first && next.first < height && 0 <= next.second && next.second < width && map[next.first][next.second] == 0) {
            path.push_back(dir);
            while (0 <= next.first && next.first < height && 0 <= next.second && next.second < width && map[next.first][next.second] == 0) {
                map[next.first][next.second] = 1;
                remaining--;
                next.first += d.first;
                next.second += d.second;
            }
            cur = {next.first - d.first, next.second - d.second};
            // std::cout << path << " " << cur.first << " " << cur.second << " " << remaining << " " << check(map, remaining) << std::endl;
            // draw(map);
            if (remaining == 0) {
                return path;
            }
            if (check(map, remaining) == true) {
                std::string result = singleSolve(height, width, map, remaining, start, cur, path);
                if (result != "") {
                    return result;
                }
            }
            map = m;
            path.pop_back();
            remaining = r;
            // std::cout << "r " << path << " " << cur.first << " " << cur.second << " " << remaining << std::endl;
            // draw(map);
        }
    }

    return "";
}

void draw(std::vector<std::vector<int>> &map) {
    for (int i = 0; i < map.size(); i++) {
        for (int j = 0; j < map[i].size(); j++) {
            if (map[i][j] == -1) {
                std::cout << "X";
            } else if (map[i][j] == 0) {
                std::cout << ".";
            } else {
                std::cout << "*";
            }
        }
        std::cout << std::endl;
    }
    std::cout << std::endl;
}

bool check(std::vector<std::vector<int>> &map, int &remaining) {
    int n = 1;
    std::pair<int, int> start;
    std::queue<std::pair<int, int>> q;
    for (int i = 0; i < map.size(); i++) {
        for (int j = 0; j < map[i].size(); j++) {
            if (map[i][j] == 0) {
                start = {i, j};
                break;
            }
        }
        if (map[start.first][start.second] == 0) {
            break;
        }
    }
    q.push(start);

    std::vector<std::vector<int>> m=map;
    m[start.first][start.second] = 1;
    while (!q.empty()) {
        std::pair<int, int> cur = q.front();
        q.pop();
        for (auto& [_, d] : directions) {
            std::pair<int, int> next = {cur.first + d.first, cur.second + d.second};
            if (0 <= next.first && next.first < m.size() && 0 <= next.second && next.second < m[0].size() && m[next.first][next.second] == 0) {
                m[next.first][next.second] = 1;
                q.push(next);
                n++;
            }
        }
    }

    return n == remaining;
}

int degree(std::vector<std::vector<int>> &map, std::pair<int, int> &cur) {
    int n = 0;
    for (auto& [_, d] : directions) {
        std::pair<int, int> next = {cur.first + d.first, cur.second + d.second};
        if (valid(map, next)) {
            n++;
        }
    }
    return n;
}

bool through(std::vector<std::vector<int>> &map, std::pair<int, int> &cur) {
    std::pair<int, int> up = {cur.first - 1, cur.second};
    std::pair<int, int> down = {cur.first + 1, cur.second};
    std::pair<int, int> left = {cur.first, cur.second - 1};
    std::pair<int, int> right = {cur.first, cur.second + 1};
    if ((!valid(map, up)) && (!valid(map, down))) {
        return true;
    }
    if ((!valid(map, left)) && (!valid(map, right))) {
        return true;
    }
    return false;
}

bool valid(std::vector<std::vector<int>> &map, std::pair<int, int> &cur) {
    return 0 <= cur.first && cur.first < map.size() && 0 <= cur.second && cur.second < map[0].size() && map[cur.first][cur.second] == 0;
}
