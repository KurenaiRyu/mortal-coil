#include "solver.hpp"
#include <iostream>
#include <future>
#include <queue>

std::string p = "";

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

    int *degreemap = new int[height * width];
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            if (map[i * width + j] == 0) {
                std::pair<int, int> start = {i, j};
                degreemap[i * width + j] = degree(height, width, map, start);
            }
            else {
                degreemap[i * width + j] = 0;
            }
        }
    }

    // solve
    int k = 0;
    for (int i = 0; i < height; i++) {
        std::vector<std::future<std::string>> results;
        for (int j = 0; j < width; j++) {
            if (map[i * width + j] == 0) {
                results.push_back(std::async(std::launch::async, [=]() {
                    std::pair<int, int> start = {i, j};
                    int *m = new int[height * width];
                    memcpy(m, map, height * width * sizeof(int));
                    int *d = new int[height * width];
                    memcpy(d, degreemap, height * width * sizeof(int));
                    std::string path = singleSolve(height, width, m, remaining, start, start, "", d);
                    delete[] m, d;
                    if (path != "") {
                        std::string result = std::to_string(j) + " " + std::to_string(i) + " " + path;
                        return result;
                    }
                    return std::string("");
                }));
            }
        }
        for (auto& result_future : results) {
            std::string result = result_future.get();
            printf("\r%d/%d", ++k, remaining);
            if (!result.empty()) {
                std::cout << std::endl;
                char* r = new char[result.size() + 1];
                memcpy(r, result.c_str(), result.size() + 1);
                return r;
            }
        }
    }

    // int n = 0;
    // for (int i = 0; i < height; i++) {
    //     for (int j = 0; j < width; j++) {
    //         if (map[i * width + j] == 0) {
    //             std::pair<int, int> start = {i, j};
    //             int *m = new int[height * width];
    //             memcpy(m, map, height * width * sizeof(int));
    //             int *d = new int[height * width];
    //             memcpy(d, degreemap, height * width * sizeof(int));
    //             std::string path = singleSolve(height, width, m, remaining, start, start, "", d);
    //             // delete[] m;
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

std::string singleSolve(const int &height, const int &width, int map[], int remaining, std::pair<int, int> start, std::pair<int, int> cur, std::string path, int degreemap[]) {
    if (start == cur) {
        map[cur.first*width + cur.second] = 1;
        degreeupdate(height, width, map, cur, degreemap);
        remaining--;
    }
    std::pair<int, int> c=cur;
    // int ** m=map;
    int r = remaining;
    for (auto& [dir, d] : directions) {
        std::pair<int, int> next = {c.first + d.first, c.second + d.second};
        if (validsize(height, width, next) && degreemap[next.first*width + next.second] > 0) {
            path.push_back(dir);
            while (validsize(height, width, next) && degreemap[next.first*width + next.second] > 0) {
                map[next.first*width + next.second] = 1;
                degreeupdate(height, width, map, next, degreemap);
                remaining--;
                next.first += d.first;
                next.second += d.second;
            }
            cur = {next.first - d.first, next.second - d.second};
            // std::cout << path << " " << cur.first << " " << cur.second << " " << remaining << " " << check(height, width, map, remaining, cur, degreemap) << std::endl;
            // draw(height, width, map);
            // draw(height, width, degreemap, false);
            if (remaining == 0) {
                return path;
            }
            if (check(height, width, map, remaining, cur, degreemap) == true) {
                std::string result = singleSolve(height, width, map, remaining, start, cur, path, degreemap);
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
                degreeupdate(height, width, map, next, degreemap);
                remaining++;
                next.first += d.first;
                next.second += d.second;
            }
            map[next.first*width + next.second] = 0;
            degreeupdate(height, width, map, next, degreemap);
            remaining++;
            // draw(height, width, map);

            path.pop_back();
            remaining = r;
            // std::cout << "r " << path << " " << cur.first << " " << cur.second << " " << remaining << std::endl;
            // draw(map);
        }
        else if (remaining == 1 && validsize(height, width, next) && map[next.first*width + next.second] == 0) {
            if (path[path.size() - 1] != dir) {
                path.push_back(dir);
            }
            return path;
        }
    }

    return "";
}

void draw(const int &height, const int &width, int map[], bool print) {
    if (print) {
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
    else {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                std::cout << map[i * width + j];
            }
            std::cout << std::endl;
        }
        std::cout << std::endl;
    }
}

bool check(const int &height, const int &width, int map[], int &remaining, std::pair<int, int> &cur, int degreemap[]) {
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

    if (degreemap[start.first*width + start.second] + near(start, cur) == 1) {
        s++;
    }

    while (!q.empty()) {
        std::pair<int, int> c = q.front();
        q.pop();
        // draw(height, width, m);
        for (auto& [_, d] : directions) {
            std::pair<int, int> next = {c.first + d.first, c.second + d.second};
            if (valid(height, width, m, next)) {
                m[next.first * width + next.second] = 1;
                q.push(next);
                n++;
                if (validsize(height, width, next) && degreemap[next.first*width + next.second] + near(next, cur) == 1) {
                    s++;
                    if (s > 1) {
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
        if (validsize(height, width, next) && map[next.first*width + next.second] == 0) {
            n++;
        }
    }
    return n;
}

int degreewithend(const int &height, const int &width, int map[], std::pair<int, int> &cur, std::pair<int, int> &end) {
    int n = 0;
    for (auto& [_, d] : directions) {
        std::pair<int, int> next = {cur.first + d.first, cur.second + d.second};
        if (valid(height, width, map, next)) {
            n++;
        }
    }
    if (distance(cur, end) == 1) {
        n++;
    }
    return n;
}

bool validsize(const int &height, const int &width, std::pair<int, int> &cur) {
    return 0 <= cur.first && cur.first < height && 0 <= cur.second && cur.second < width;
}

bool valid(const int &height, const int &width, int map[], std::pair<int, int> &cur) {
    return validsize(height, width, cur) && map[cur.first*width + cur.second] == 0;
}

int distance(std::pair<int, int> &a, std::pair<int, int> &b) {
    return abs(a.first - b.first) + abs(a.second - b.second);
}

bool near(std::pair<int, int> &a, std::pair<int, int> &b) {
    return distance(a, b) == 1;
}

void degreeupdate(const int &height, const int &width, int map[], std::pair<int, int> &cur, int degreemap[]) {
    for (auto& [_, d] : directions) {
        std::pair<int, int> next = {cur.first + d.first, cur.second + d.second};
        if (validsize(height, width, next) && map[next.first*width + next.second] == 0) {
            degreemap[next.first*width + next.second] = degree(height, width, map, next);
        }
    }
    if (map[cur.first*width + cur.second] == 0) {
        degreemap[cur.first*width + cur.second] = degree(height, width, map, cur);
    }
    else {
        degreemap[cur.first*width + cur.second] = 0;
    }
}
